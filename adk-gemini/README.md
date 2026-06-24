# adk-gemini

An **AG-UI agent backed by a [Google ADK](https://github.com/google/adk-java)
`LlmAgent`** (a Gemini model), exposed over Spring Boot with
[`ag-ui-adk`](https://github.com/ag-ui-4j/ag-ui-adk) +
[`ag-ui-spring`](https://github.com/ag-ui-4j/ag-ui-spring).

The app is one `@SpringBootApplication` with a single `Agent` bean: the
`ag-ui-spring-boot-starter` serves `POST /agent` as Server-Sent Events, and
[`AdkAgent`](https://github.com/ag-ui-4j/ag-ui-adk) adapts the ADK agent to the
AG-UI event stream.

## Prerequisites

- **Java 17+** and **Maven**
- A **Gemini API key** — the Google GenAI SDK reads `GOOGLE_API_KEY`:

  ```bash
  export GOOGLE_API_KEY=your-gemini-api-key
  ```

  (Or configure Vertex AI per the GenAI SDK.)

- The `ag-ui`, `ag-ui-spring` and `ag-ui-adk` artifacts in your local Maven
  repository (until they are on Maven Central):

  ```bash
  mvn -f /path/to/ag-ui/pom.xml        install -DskipTests
  mvn -f /path/to/ag-ui-spring/pom.xml install -DskipTests
  mvn -f /path/to/ag-ui-adk/pom.xml    install -DskipTests
  ```

## Run

```bash
mvn spring-boot:run
```

The agent is at `http://localhost:8081/agent` (port `8081` so it can run alongside
the `spring-ai-ollama` example on `8080`).

## Try it

```bash
curl -N -X POST http://localhost:8081/agent \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream" \
  -d '{
        "threadId": "t1",
        "runId": "r1",
        "messages": [{"id": "1", "role": "user", "content": "Tell me a short joke."}],
        "tools": []
      }'
```

You'll see `RUN_STARTED` → `TEXT_MESSAGE_*` → `RUN_FINISHED`. If the ADK agent
calls a tool, it runs server-side and you'll also see
`TOOL_CALL_START/ARGS/END/RESULT`.

## How it works

```
Google ADK LlmAgent (Gemini)   → the agent (model + any ADK tools)
ag-ui-adk (AdkAgent)           → adapts ADK's event stream to AG-UI events
ag-ui-spring-boot-starter      → serves it at the /agent SSE endpoint
```

## Configure

- `adk.model` — the Gemini model id (default `gemini-2.0-flash`).
- `ag-ui.server.path` — the endpoint path (defaults to `/agent`).
- `GOOGLE_API_KEY` — your Gemini API key (read by the GenAI SDK).
