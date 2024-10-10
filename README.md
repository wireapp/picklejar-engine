# Picklejar-Engine

A JUnit 5 Test engine for running cucumber test in Java. Suitable for very big test suite projects.

## Features

* Parallelism
* Deflake-Support (via [flaky-test-handler-plugin](https://github.com/jenkinsci/flaky-test-handler-plugin))
* Automatic rerun of failed tests
* Scenario tag support (include, exclude, cucumber-report)
* Feature tag support
* Attachments in cucumber-report
* Legacy JUnit XML report output (supports Jenkins [junit plugin](https://plugins.jenkins.io/junit/) and [junit-realtime-test-reporter plugin](https://plugins.jenkins.io/junit-realtime-test-reporter/))
* Resource lock support
* Multiple step packages possible
* Feature files in subdirectories possible
* Test execution by tags via maven
* Reports exceptions in BeforeScenario and AfterScenario methods in cucumber-report
* Reports failures with feature files (Missing step method, illegal character usage) with line number

## Known bugs

* JDK 11 is mandatory! The reason is a [JUnit 5 bug](https://github.com/junit-team/junit5/issues/1858) that makes the 
use of `newFixedThreadPool` break parallel tests. As workaround we implemented a `CustomStrategy` as described
[here](https://github.com/SeleniumHQ/selenium/issues/9359#issuecomment-826785222) but unfortunately this workaround only
works on JDK 11 and fails on JDK 8 and 17. Another workaround is to use `stream().parallel().forEach()` in the tests but
this is not always feasible.

## TODO

* Move log entries from Lifecycles into picklejar-engine starting with `[Execution]`
* Test Summary
  * Cleanup and fix flaky summary of launcher
* Better check for parameter correctness for BeforeEachScenario, BeforeEachStep, AfterEachStep, AfterEachScenario
  * Fail test completely on wrong parameters or even on discovery
  * Do not fail tests on Exceptions inside of the methods
* Missing tests:
  * Launcher tests for reruns
  * Negative tests for a step methods with too many/not enough parameters
  * Negative tests for wrong test context parameters on BeforeEachScenario, BeforeEachStep, AfterEachStep, AfterEachScenario
  * Multiple step packages with multiple BeforeEachScenario, BeforeEachStep, AfterEachStep, AfterEachScenario
* See [Gherkin Reference](https://cucumber.io/docs/gherkin/reference/#feature) for missing features:
  * Data Tables
  * Doc Strings
* Better error message when Scenario name is empty String

## How to setup

Add new test dependency to your project's pom.xml:
```xml
<dependency>
    <groupId>com.wire.qa</groupId>
    <artifactId>picklejar-engine</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```
Add exec-maven-plugin configuration and surefire configuration to your project's pom.xml:
```xml
<build>
  <resources>
    <resource>
      <directory>src/main/resources</directory>
      <filtering>true</filtering>
    </resource>
  </resources>

  <sourceDirectory>src/main/java</sourceDirectory>

  <plugins>
    <plugin>
      <groupId>org.codehaus.mojo</groupId>
      <artifactId>exec-maven-plugin</artifactId>
      <version>3.0.0</version>
      <executions>
        <execution>
          <phase>integration-test</phase>
          <goals>
            <goal>exec</goal>
          </goals>
        </execution>
      </executions>
      <configuration>
        <executable>java</executable>
        <includeProjectDependencies>true</includeProjectDependencies>
        <includePluginDependencies>false</includePluginDependencies>
        <skip>${skipTests}</skip>
        <classpathScope>test</classpathScope>
        <arguments>
          <argument>-classpath</argument>
          <classpath/>
          <argument>-Dpicklejar.tags=${picklejar.tags}</argument>
          <argument>-Djunit.jupiter.execution.parallel.enabled=true</argument>
          <argument>-Djunit.jupiter.execution.parallel.mode.default=concurrent</argument>
          <argument>-Djunit.jupiter.execution.parallel.mode.classes.default=concurrent</argument>
          <argument>-Djunit.jupiter.execution.parallel.config.strategy=fixed</argument>
          <argument>-Djunit.jupiter.execution.parallel.config.strategy=custom</argument>
          <argument>-Djunit.jupiter.execution.parallel.config.custom.class=com.wire.qa.picklejar.engine.CustomStrategy</argument>
          <argument>-Dpicklejar.parallelism=${picklejar.parallelism}</argument>
          <!-- Add additional arguments with -D here if you need them as properties in your tests -->
          <mainClass>com.wire.qa.picklejar.launcher.PicklejarLauncher</mainClass>
          <argument>${project.build.directory}</argument>
        </arguments>
      </configuration>
    </plugin>
    <!-- This is needed because maven magically always includes surefire by default and makes it impossible to use test property otherwise -->
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-surefire-plugin</artifactId>
      <version>2.12.4</version>
      <configuration>
          <skip>true</skip>
      </configuration>
    </plugin>
  </plugins>
</build>
```

## Configuration

Create a file `junit-platform.properties` in the **src/test/resources** directory (create directory if it does not
exist). Add at least **mandatory** properties from below:

Property |  | Explanation
------------ | ------------- | ----------
`com.wire.qa.picklejar.features.package` | Mandatory | Package name containing the feature files in your test resources directory
`com.wire.qa.picklejar.steps.packages` | Mandatory | Package name containing the step files (can be a comma separated list of multiple package names)
`com.wire.qa.picklejar.xml-reports.directory` | Optional | Directory name for JUnit xml reports under target/ directory (Default: xml-reports)
`com.wire.qa.picklejar.cucumber-report.filename` | Optional | File name for cucumber report json file under target/ directory (Default: cucumber-report.json)
`com.wire.qa.picklejar.engine.multiple-steps-matching-warning` | Optional | Warns if a step can be matched by more than one method annotation regex. Can be disabled to make the execution faster (Default: true)

## How to use

Set up a new class for your lifecycle. We usually call it `Lifecycle.java`. You can
use the following annotations to run code before/after a scenario or a step.

```java
public class LifeCycle {

  @BeforeEachScenario
  public TestContext setUp(Scenario scenario) {
    // ...
    return new CustomTestContext();
  }

  @BeforeEachStep
  public void beforeEachStep(CustomTestContext context, Scenario scenario, Step step) {
    // ...
  }

  @AfterEachStep
  public void afterEachStep(CustomTestContext context, Scenario scenario, Step step) {
    // ...
  }

  @AfterEachScenario
  public void tearDown(CustomTestContext context, Scenario scenario) {
      // ...
  }
}
```

To transfer context between one step to another create a class that inherits from `TestContext` and
fill it with getter and setter methods. The test context is usually created in the method that is
annotated with `@BeforeEachScenario` and is the return value of this method.

A test context can be a webdriver or other objects that should live throughout a scenario but should be detroyed at the
end of each scenario.

```java
import com.wire.qa.picklejar.engine.TestContext;

public class CustomTestContext extends TestContext {
    // ...
}
```

Steps are written in step classes. It is required that the step class name ends in `Steps.java`.

The constructor of the class gets the `TestContext` object that was created in the `@BeforeEachScenario` method.

```java
import io.cucumber.java.en.When;

public class LoginSteps {

  private final CustomTestContext context;

  public LoginSteps(CustomTestContext context) {
    this.context = context;
  }

  @When("^I click login button$")
  public void iClickLoginButton() {
      // ...
  }
}
```

### Resource lock support

To lock a resource for a scenario add the following tag to the scenario:

```cucumber
@resource=HardcodedTestAccount1
```

Test with the same resource name are executed sequentially instead of parallel.

## How to run

`mvn -Dpicklejar.tags=<tags> -Dpicklejar.parallelism=1 clean integration-test`

## Development

In IntelliJ Idea add picklejar-engine as imported module to project structure (select pom.xml) to enable syntax
highlighting and more for development.

### Architecture

#### Launcher

The `PicklejarLauncher` is replacing what was usually done by the maven-surefire-plugin. It runs the tests in parallel,
attaches different listeners (the listener that creates the cucumber reports, the one that creates Junit 4 xml reports,
etc.) and implements automatic and manual re-run of failing tests. It is doing this by creating a `DiscoveryRequest` and
executing it through the `PicklejarEngine`.

#### Engine

The `PicklejarEngine` is a custom test engine. It registers itself through the META-INF services file. A test engine is
started by giving it a `DiscoveryRequest`. A `DiscoveryRequest` can contain selectors and filters and this way controls
which specific tests are actually executed.

The discovery of test cases is done by registering different type of SelectorResolvers (for example
`FeatureSelectorResolver` and `ScenarioSelectorResolver`) on the `DiscoverySelectorResolver`. The discovery then
executes the resolve methods in the SelectorResolvers depending on the used selector.

The `SelectorResolvers` go through the test files and return a hierarchic structure of `TestDescriptors`. These
`TestDescriptors` contain also the implementation on how they are executed.

The returned structure of `TestDescriptors` is filtered by tags etc and the tests are executed.

A test engine can be configured differently by the tests. This is done through a properties file called
`junit-platform.properties` in the resource directory of the tests. These configuration values can be read through the
class `PicklejarConfiguration` which is available on several places of the engine.

#### Logging

JUnit 5 uses JUL (java.util.logging) for logging. The picklejar engine is disabling the output of JUnit platform
components in `PicklejarLogger` to focus on picklejar engine. The log level can be configured by creating a file
`logging.properties` with following content:
```
handlers= java.util.logging.ConsoleHandler

.level= FINE
java.util.logging.ConsoleHandler.level = FINE
java.util.logging.ConsoleHandler.formatter = java.util.logging.SimpleFormatter

com.wire.qa.picklejar.engine.logging.level=FINE
```

**Caution:** It is recommended to use the level FINE because using ALL might break your IDE because it outputs too much.

After that you need to set a new argument in the *exec-maven-plugin* configuration of the test's `pom.xml` file:
```xml
  <argument>-Djava.util.logging.config.file=/path/to/logging.properties</argument>
```

The tests for the test engine itself are setting `java.util.logging.config.file` in the surefire configuration.

#### Tests

Unit tests for the engine can be executed by:
```
./gradlew clean test
```

Run individual test:
```
./gradlew clean test --tests="*.generateRelativeFeatureFolder"
```
