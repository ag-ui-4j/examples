package io.github.agui4j.examples.ollama;

import io.github.agui4j.core.agent.Agent;
import io.github.agui4j.spring.ai.SpringAiAgent;
import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * A minimal AG-UI agent backed by a local Ollama model, with a server-side
 * weather tool.
 *
 * <p>The {@code ag-ui-spring-ai-spring-boot-starter} would auto-configure a bare
 * {@code SpringAiAgent}; here we define our own {@code Agent} bean to register the
 * {@link WeatherTools} server-side tool. The agent runs the tool itself and emits
 * {@code TOOL_CALL_START/ARGS/END/RESULT} for it, then re-prompts the model with
 * the result. It is exposed at {@code POST /agent} by the server starter.
 */
@SpringBootApplication
public class OllamaAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(OllamaAgentApplication.class, args);
    }

    /**
     * Builds the AG-UI agent over the auto-configured Ollama {@code ChatClient},
     * registering {@link WeatherTools} as a backend (server-executed) tool.
     */
    @Bean
    Agent agent(ChatClient.Builder chatClientBuilder, WeatherTools weatherTools,
                @Value("${ag-ui.spring-ai.share-state:false}") boolean shareState) {
        List<ToolCallback> backendTools = List.of(MethodToolCallbackProvider.builder()
                .toolObjects(weatherTools)
                .build()
                .getToolCallbacks());
        return SpringAiAgent.builder(chatClientBuilder.build())
                .tools(backendTools)
                .shareState(shareState)
                .build();
    }
}
