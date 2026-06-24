# spring-ai-ollama

A minimal **AG-UI agent backed by a local [Ollama](https://ollama.com) model**,
wired with Spring Boot + Spring AI and the
[`ag-ui-spring-ai-spring-boot-starter`](https://github.com/ag-ui-4j/ag-ui-spring).

The whole app is one empty `@SpringBootApplication` class — the starter
auto-registers a `SpringAiAgent` from the Ollama `ChatClient.Builder` and serves
it at `POST /agent` as Server-Sent Events.

## Prerequisites

- **Java 17+** and **Maven**
- **[Ollama](https://ollama.com/download)** running locally, with a model pulled:

  ```bash
  ollama serve            # start the server (default http://localhost:11434)
  ollama pull llama3.2    # or any model you prefer
  ```

- The `ag-ui` and `ag-ui-spring` artifacts in your local Maven repository. Until
  they are on Maven Central, build them first:

  ```bash
  mvn -f /path/to/ag-ui/pom.xml install -DskipTests
  mvn -f /path/to/ag-ui-spring/pom.xml install -DskipTests
  ```

## Run

```bash
mvn spring-boot:run
```

The agent is now at `http://localhost:8080/agent`.

## Try it

Send a `RunAgentInput` and watch the AG-UI event stream:

```bash
curl -N -X POST http://localhost:8080/agent \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream" \
  -d '{
        "threadId": "t1",
        "runId": "r1",
        "messages": [{"id": "1", "role": "user", "content": "Tell me a short joke."}],
        "tools": []
      }'
```

You will see a stream of events:

```
data: {"type":"RUN_STARTED","threadId":"t1","runId":"r1"}
data: {"type":"TEXT_MESSAGE_START","messageId":"...","role":"assistant"}
data: {"type":"TEXT_MESSAGE_CONTENT","messageId":"...","delta":"Why "}
data: {"type":"TEXT_MESSAGE_CONTENT","messageId":"...","delta":"did "}
...
data: {"type":"TEXT_MESSAGE_END","messageId":"..."}
data: {"type":"RUN_FINISHED","threadId":"t1","runId":"r1"}
```

Or point the [`HttpAgent`](https://github.com/ag-ui-4j/ag-ui/tree/main/client)
client at `http://localhost:8080/agent`.

## Server-side tool: weather

[`WeatherTools`](src/main/java/io/github/agui4j/examples/ollama/WeatherTools.java) is a
Spring AI `@Tool` (a Java port of the Mastra `weatherTool`) that uses the free
[Open-Meteo](https://open-meteo.com) APIs (**no API key required**): it geocodes
the city name to coordinates, then fetches the current conditions.

It's registered as a **backend** tool with `SpringAiAgent.builder(...).tools(...)`
(see [`OllamaAgentApplication`](src/main/java/io/github/agui4j/examples/ollama/OllamaAgentApplication.java)).
The agent runs it itself and surfaces the whole call to the client as
`TOOL_CALL_START` → `TOOL_CALL_ARGS` → `TOOL_CALL_END` → **`TOOL_CALL_RESULT`**,
then re-prompts the model with the result so it can answer in natural language.

Just ask, e.g. *"What's the weather in Paris?"* — you'll see the tool call and its
result in the AG-UI event stream, followed by the model's text answer.

> Backend tools, AG-UI **client-side** tools and shared state can all be used
> together: the agent owns the tool loop, so enabling `share-state` no longer
> disables server-side tool execution.

## Configure

See [`application.yml`](src/main/resources/application.yml):

- `spring.ai.ollama.chat.options.model` — the Ollama model to use. Use a strong
  tool-calling model such as `qwen2.5` or `llama3.1`; small models struggle to
  produce correct tool calls.
- `ag-ui.spring-ai.share-state` — `true` enables AG-UI shared state (the
  `update_state` tool and `STATE_SNAPSHOT`/`STATE_DELTA` events); see the note above.
- `ag-ui.spring-ai.state-updates` — `SNAPSHOT` (default) or `DELTA`.
- `ag-ui.server.path` — the endpoint path (defaults to `/agent`).

## How it works

```
spring-ai-starter-model-ollama        → auto-configures an Ollama ChatModel + ChatClient.Builder
ag-ui-spring-ai-spring-boot-starter   → exposes the agent at the /agent SSE endpoint
Agent @Bean (this example)            → SpringAiAgent + the backend weatherTool
```

Glue code is one `@Tool` class plus a small `Agent` bean that registers it.
