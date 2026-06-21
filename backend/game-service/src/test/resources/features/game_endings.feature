Feature: Game endings

  Background:
    Given two registered players exist:
      | alias | role  |
      | Alice | white |
      | Bob   | black |

  Scenario: Resignation ends the game for the opponent
    Given an active game between Alice and Bob
    When Alice resigns the game
    Then the game status should be BLACK_WINS
    And the end reason should be RESIGNATION

  Scenario: Draw by agreement
    Given an active game between Alice and Bob
    And Alice has played e2e4
    When Alice offers a draw
    And Bob accepts the draw offer
    Then the game status should be DRAW
    And the end reason should be AGREEMENT
    But the game should not be a checkmate

  Scenario: Checkmate ends the game
    Given an active game between Alice and Bob with FEN "rnbqkbnr/ppppp2p/8/5pp1/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 2"
    When Alice plays d1h5
    Then the game status should be WHITE_WINS
    And the end reason should be CHECKMATE
