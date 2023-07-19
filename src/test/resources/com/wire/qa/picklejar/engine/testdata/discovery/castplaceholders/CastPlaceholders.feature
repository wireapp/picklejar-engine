Feature: Cast placeholders

  Scenario: Cast placeholders
    When Step with text as string parameter
    And Step with 123 as int parameter
    And Step with 1 as single int parameter
    And Step with 67000000000 as long parameter
    And Step with 1.123 as float parameter
    And Step with 18.12330001 as double parameter
    And Step with true as boolean parameter
    And Step with False as boolean parameter