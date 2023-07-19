Feature: Unique Id

  Scenario: Simple Scenario
    When Step without parameters

  Scenario Outline: Scenario With Two Examples
    When Step with <Placeholder> as string parameter

    Examples:
      | Placeholder |
      | Value1      |
      | Value2      |