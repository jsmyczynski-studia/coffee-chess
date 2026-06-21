Feature: Bot replies and clock flag fall

  Background:
    Given a registered human player Alice

  Scenario: Bot replies after the human move
    Given a vs-bot game where Alice plays white
    And the engine will reply with "e7e5"
    When Alice plays e2e4
    Then the bot should reply with a legal move
    And the move list should contain:
      | move  |
      | e2e4  |
      | e7e5  |

  Scenario: Flag fall ends the game on time
    Given an active game between Alice and Bob
    And Alice has almost no time left on the clock
    When Alice attempts to play "e2e4"
    Then the game status should be BLACK_WINS
    And the end reason should be TIME_OUT

  Scenario: Scheduled flag fall checker ends overdue games
    Given an active game between Alice and Bob
    And Alice's clock has already expired
    When the flag fall checker runs
    Then the game status should be BLACK_WINS
    And the end reason should be TIME_OUT
