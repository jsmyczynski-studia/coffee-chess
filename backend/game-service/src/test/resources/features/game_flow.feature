Feature: Starting and playing a human game

  Background:
    Given two registered players exist:
      | alias | role  |
      | Alice | white |
      | Bob   | black |

  Scenario: Create a game and play an opening sequence
    When Alice starts a standard game against Bob
    Then the game status should be IN_PROGRESS
    And it should be Alice's turn
    When Alice plays the following moves:
      | move  |
      | e2e4  |
    And Bob plays the following moves:
      | move  |
      | e7e5  |
    Then the move list should contain:
      | move  |
      | e2e4  |
      | e7e5  |
    But the game should not have ended
