Feature: Scenario With Outline (Examples)

  Scenario Outline: Scenario With Outline (Examples)
    When Step which uses placeholder <Placeholder1> from Examples
    Then Step which uses placeholder <Placeholder2> from Examples

  Examples:
    | Placeholder1 | Placeholder2 |
    | Value1       | Value2       |