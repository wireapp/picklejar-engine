Feature: Feature2

  @tag1
  Scenario: Scenario with [ in name
    Given Step without parameters

  @tag1
  Scenario Outline: Scenario with ] in name
    Given Step with <Parameter> as string parameter

    Examples:
      | Parameter |
      | Hello     |