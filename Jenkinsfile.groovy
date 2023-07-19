pipeline {
    agent {
        label 'WebApp_Linux'
    }

    options {
        parallelsAlwaysFailFast()
        disableConcurrentBuilds()
    }

    parameters {
        string(name: 'version', defaultValue: '0.0.1')
        booleanParam(name: 'deploy', defaultValue: false)
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/wireapp/picklejar-engine.git'
            }
        }

        stage('Build') {
            steps {
                withMaven(jdk: 'AdoptOpenJDK8', maven: 'M3', options: [junitPublisher(disabled: true)]) {
                    realtimeJUnit(keepLongStdio: true, testResults: 'target/surefire-reports/TEST*.xml') {
                        sh 'mvn clean package'
                    }
                }
                stash includes: 'target/*.jar', name: 'artifacts'
                archiveArtifacts artifacts: 'target/*.jar', followSymlinks: false
            }
        }

        stage('Deploy') {
            when {
                expression { return params.deploy }
            }
            steps {
                withCredentials([ usernamePassword( credentialsId: 'sonatype-nexus', usernameVariable: 'SONATYPE_USERNAME', passwordVariable: 'SONATYPE_PASSWORD' ),
                    file(credentialsId: 'DC640D79AF40EEFF.asc', variable: 'PGP_PRIVATE_KEY_FILE'),
                    string(credentialsId: 'PGP_PASSPHRASE', variable: 'PGP_PASSPHRASE') ]) {
                    withMaven(maven: 'M3') {
                        unstash 'artifacts'
                        sh(
                            script: """
                                touch local.properties
                                version=$version ./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository
                            """
                        )
                    }
                }
            }
        }
    }
}
