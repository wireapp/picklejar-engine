Feature: Feature1

  @tag1
  Scenario Outline: Scenario with : in name
    Given Step with <Parameter> as string parameter

    Examples:
      | Parameter |
      | Hello     |

  @tag2
  Scenario: Scenario with / in name
    Given Step without parameters