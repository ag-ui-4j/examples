# ag-ui-4j examples

Runnable examples for the [AG-UI protocol](https://docs.ag-ui.com) Java libraries
([`ag-ui`](https://github.com/ag-ui-4j/ag-ui) and
[`ag-ui-spring`](https://github.com/ag-ui-4j/ag-ui-spring)).

Each example is a self-contained project — build and run it on its own.

## Examples

| Example | Description |
|---------|-------------|
| [`spring-ai-ollama`](spring-ai-ollama) | A Spring Boot + Spring AI agent backed by a local **Ollama** model (with a server-side weather tool), exposing the AG-UI `/agent` SSE endpoint. |
| [`adk-gemini`](adk-gemini) | A Spring Boot agent backed by a **Google ADK** `LlmAgent` (Gemini) via `ag-ui-adk`, exposing the AG-UI `/agent` SSE endpoint. |

## Note on dependencies

The examples depend on the `ag-ui` and `ag-ui-spring` artifacts. Until those are
published to Maven Central, build them into your local Maven repository first:

```bash
mvn -f /path/to/ag-ui/pom.xml install -DskipTests
mvn -f /path/to/ag-ui-spring/pom.xml install -DskipTests
```

## License

Licensed under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).
