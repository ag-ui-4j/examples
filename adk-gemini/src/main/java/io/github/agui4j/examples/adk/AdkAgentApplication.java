package io.github.agui4j.examples.adk;

import com.google.adk.agents.LlmAgent;
import io.github.agui4j.adk.AdkAgent;
import io.github.agui4j.core.agent.Agent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * An AG-UI agent backed by a Google ADK {@link LlmAgent} (a Gemini model), exposed
 * over Spring Boot.
 *
 * <p>The {@code ag-ui-spring-boot-starter} provides the {@code POST /agent} SSE
 * endpoint and wires it to the {@link Agent} bean below — which adapts the ADK
 * agent via {@link AdkAgent}. ADK runs the model (and any ADK tools) and the
 * adapter translates its event stream to AG-UI events.
 *
 * <p>Requires a Gemini API key: set {@code GOOGLE_API_KEY} in the environment (the
 * Google GenAI SDK reads it automatically).
 */
@SpringBootApplication
public class AdkAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdkAgentApplication.class, args);
    }

    @Bean
    Agent agent(@Value("${adk.model:gemini-2.0-flash}") String model) {
        LlmAgent llmAgent = LlmAgent.builder()
                .name("assistant")
                .description("A helpful assistant")
                .model(model)
                .instruction("You are a helpful assistant. Answer concisely.")
                .build();
        return new AdkAgent(llmAgent);
    }
}
