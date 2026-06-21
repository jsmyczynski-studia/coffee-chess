Feature: Bot trash-talk style

  Scenario: Sanitized bot comments follow LLM style rules
    Given the LLM returns a messy remark
    When the remark is sanitized for chat
    Then the comment should be all lowercase
    And the comment should contain no punctuation
    And the comment should have at most 12 words
