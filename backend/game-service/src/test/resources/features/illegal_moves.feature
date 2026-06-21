Feature: Illegal move rejection

  Background:
    Given two registered players exist:
      | alias | role  |
      | Alice | white |
      | Bob   | black |
    And an active game between Alice and Bob

  Scenario Outline: Reject invalid or out-of-turn moves
    When <player> attempts to play "<move>"
    Then the move is rejected with message "<message>"

    Examples:
      | player | move  | message              |
      | Alice  | g1g4  | Illegal move!        |
      | Bob    | e7e5  | It's not your turn!  |
      | Alice  | e2    | Invalid move format  |

  Scenario: Reject empty move payload via service validation
    When Alice attempts to play ""
    Then the move is rejected with message "Invalid move format"
