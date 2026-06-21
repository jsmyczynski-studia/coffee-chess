package pl.coffeechess.analysis.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChessApiClientTest {

    private MockWebServer mockWebServer;
    private ChessApiClient client;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/v1").toString())
                .build();

        client = new ChessApiClient(webClient, new ObjectMapper(), 5, 16);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void getCandidateMoves_sendsFenVariantsAndDepth() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        [
                          {
                            "san": "e4",
                            "move": "e2e4",
                            "eval": 0.27,
                            "mate": null,
                            "winChance": 52.1,
                            "continuationArr": ["e7e5", "g1f3"]
                          },
                          {
                            "san": "d4",
                            "move": "d2d4",
                            "eval": 0.22,
                            "mate": null,
                            "winChance": 51.4,
                            "continuationArr": ["d7d5", "c2c4"]
                          }
                        ]
                        """));

        StepVerifier.create(client.getCandidateMoves(
                        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
                        2))
                .assertNext(candidates -> {
                    assertThat(candidates).hasSize(2);
                    assertThat(candidates.get(0).san()).isEqualTo("e4");
                    assertThat(candidates.get(0).uci()).isEqualTo("e2e4");
                    assertThat(candidates.get(0).eval()).isEqualTo(0.27);
                    assertThat(candidates.get(0).mate()).isNull();
                    assertThat(candidates.get(0).winChance()).isEqualTo(52.1);
                    assertThat(candidates.get(0).continuation()).containsExactly("e7e5", "g1f3");
                    assertThat(candidates.get(1).san()).isEqualTo("d4");
                })
                .verifyComplete();

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getBody().readUtf8()).isEqualTo(
                "{\"fen\":\"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1\",\"variants\":2,\"depth\":16}");
    }

    @Test
    void getCandidateMoves_parsesSingleObjectResponse() {
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "san": "Nf3",
                          "lan": "g1f3",
                          "eval": 0.35,
                          "mate": null,
                          "winChance": 53.2,
                          "continuationArr": []
                        }
                        """));

        StepVerifier.create(client.getCandidateMoves(
                        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
                        1))
                .assertNext(candidates -> {
                    assertThat(candidates).hasSize(1);
                    assertThat(candidates.get(0).san()).isEqualTo("Nf3");
                    assertThat(candidates.get(0).uci()).isEqualTo("g1f3");
                })
                .verifyComplete();
    }

    @Test
    void getCandidateMoves_clampsVariantsAndDepth() throws InterruptedException {
        ChessApiClient cappedClient = new ChessApiClient(
                WebClient.builder().baseUrl(mockWebServer.url("/v1").toString()).build(),
                new ObjectMapper(),
                5,
                20
        );

        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("[]"));

        StepVerifier.create(cappedClient.getCandidateMoves(
                        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
                        9))
                .assertNext(List::isEmpty)
                .verifyComplete();

        RecordedRequest request = mockWebServer.takeRequest();
        String body = request.getBody().readUtf8();
        assertThat(body).contains("\"variants\":5");
        assertThat(body).contains("\"depth\":16");
    }

    @Test
    void clampHelpers_boundValues() {
        assertThat(ChessApiClient.clampVariants(0)).isEqualTo(1);
        assertThat(ChessApiClient.clampVariants(3)).isEqualTo(3);
        assertThat(ChessApiClient.clampVariants(9)).isEqualTo(5);
        assertThat(ChessApiClient.clampDepth(0)).isEqualTo(1);
        assertThat(ChessApiClient.clampDepth(20)).isEqualTo(16);
    }
}
