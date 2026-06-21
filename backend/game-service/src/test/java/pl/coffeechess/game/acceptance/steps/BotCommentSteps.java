package pl.coffeechess.game.acceptance.steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import pl.coffeechess.game.acceptance.GameScenarioContext;
import pl.coffeechess.game.client.LlmClient;
import pl.coffeechess.game.service.TrashTalkService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class BotCommentSteps {

    @Autowired
    private GameScenarioContext context;

    @Autowired
    private TrashTalkService trashTalkService;

    @Autowired
    private LlmClient llmClient;

    @Given("the LLM returns a messy remark")
    public void theLlmReturnsMessyRemark() {
        when(llmClient.complete(anyString(), anyString()))
                .thenReturn("WOW!!! You're totally CRUSHED 😎😎 — enjoy that loss...");
    }

    @When("the remark is sanitized for chat")
    public void remarkIsSanitizedForChat() {
        context.setSanitizedComment(trashTalkService.generateRemark("bot just captured a piece"));
    }

    @Then("the comment should be all lowercase")
    public void commentShouldBeLowercase() {
        assertThat(context.getSanitizedComment()).isNotNull();
        assertThat(context.getSanitizedComment()).isEqualTo(context.getSanitizedComment().toLowerCase());
    }

    @And("the comment should contain no punctuation")
    public void commentShouldContainNoPunctuation() {
        assertThat(context.getSanitizedComment()).matches("[a-z0-9\\s]+");
    }

    @And("the comment should have at most {int} words")
    public void commentShouldHaveAtMostWords(int maxWords) {
        assertThat(context.getSanitizedComment().split("\\s+").length).isLessThanOrEqualTo(maxWords);
    }
}
