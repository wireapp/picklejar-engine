@featuretag
Feature: Feature2

  @scenariotag1 @scenariotag2 @scenariotag3 @scenariotag4
  Scenario: Scenario 1
    When Step without parameters

  @scenariotag1 @scenariotag2 @scenariotag3 @scenariotag4
  Scenario: Scenario 2
    When Step with Hello as string parameter
    And Step with 1000 as int parameter
    And Step with 1 as single int parameter
    And Step with 67000000000 as long parameter
    And Step with 1.123 as float parameter
    And Step with 18.12330001 as double parameter
    And Step with true as boolean parameter
    And Step with False as boolean parameter

  @scenariotag1 @scenariotag2 @scenariotag3 @scenariotag4
  Scenario: Scenario 3
    When Step with true and 566756.8979 and 11000432402340324 and This is a string with ][]|]|“¢]{|}]} different parameters