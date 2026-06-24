package io.github.agui4j.examples.ollama;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * A <strong>backend</strong> Spring AI tool that returns the current weather for a
 * city using the free <a href="https://open-meteo.com">Open-Meteo</a> APIs (no API
 * key required). It first geocodes the city name to coordinates, then fetches the
 * current conditions. The agent runs this tool and feeds the result back to the
 * model.
 *
 * <p>Java port of the Mastra {@code weatherTool}.
 */
@Component
class WeatherTools {

    /** The tool result returned to the model (mirrors the Mastra output schema). */
    record Weather(double temperature, double feelsLike, int humidity, double windSpeed,
                   double windGust, String conditions, String location) {
    }

    private final RestClient restClient = RestClient.create();

    @Tool(name = "weatherTool", description = "Get current weather for a location")
    Weather getWeather(@ToolParam(description = "City name") String location) {
        GeocodingResponse geocoding = restClient.get()
                .uri("https://geocoding-api.open-meteo.com/v1/search?name={name}&count=1", location)
                .retrieve()
                .body(GeocodingResponse.class);
        if (geocoding == null || geocoding.results() == null || geocoding.results().isEmpty()) {
            throw new IllegalArgumentException("Location '" + location + "' not found");
        }
        GeocodingResult place = geocoding.results().get(0);

        WeatherResponse weather = restClient.get()
                .uri("https://api.open-meteo.com/v1/forecast?latitude={lat}&longitude={lon}"
                                + "&current=temperature_2m,apparent_temperature,relative_humidity_2m,"
                                + "wind_speed_10m,wind_gusts_10m,weather_code",
                        place.latitude(), place.longitude())
                .retrieve()
                .body(WeatherResponse.class);
        Current current = weather.current();

        return new Weather(
                current.temperature(),
                current.apparentTemperature(),
                current.humidity(),
                current.windSpeed(),
                current.windGust(),
                weatherCondition(current.weatherCode()),
                place.name());
    }

    private static String weatherCondition(int code) {
        return CONDITIONS.getOrDefault(code, "Unknown");
    }

    // --- Open-Meteo responses (only the fields we use) ---

    @JsonIgnoreProperties(ignoreUnknown = true)
    record GeocodingResponse(List<GeocodingResult> results) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record GeocodingResult(double latitude, double longitude, String name) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record WeatherResponse(Current current) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Current(
            @JsonProperty("temperature_2m") double temperature,
            @JsonProperty("apparent_temperature") double apparentTemperature,
            @JsonProperty("relative_humidity_2m") int humidity,
            @JsonProperty("wind_speed_10m") double windSpeed,
            @JsonProperty("wind_gusts_10m") double windGust,
            @JsonProperty("weather_code") int weatherCode) {
    }

    private static final Map<Integer, String> CONDITIONS = Map.ofEntries(
            Map.entry(0, "Clear sky"),
            Map.entry(1, "Mainly clear"),
            Map.entry(2, "Partly cloudy"),
            Map.entry(3, "Overcast"),
            Map.entry(45, "Foggy"),
            Map.entry(48, "Depositing rime fog"),
            Map.entry(51, "Light drizzle"),
            Map.entry(53, "Moderate drizzle"),
            Map.entry(55, "Dense drizzle"),
            Map.entry(56, "Light freezing drizzle"),
            Map.entry(57, "Dense freezing drizzle"),
            Map.entry(61, "Slight rain"),
            Map.entry(63, "Moderate rain"),
            Map.entry(65, "Heavy rain"),
            Map.entry(66, "Light freezing rain"),
            Map.entry(67, "Heavy freezing rain"),
            Map.entry(71, "Slight snow fall"),
            Map.entry(73, "Moderate snow fall"),
            Map.entry(75, "Heavy snow fall"),
            Map.entry(77, "Snow grains"),
            Map.entry(80, "Slight rain showers"),
            Map.entry(81, "Moderate rain showers"),
            Map.entry(82, "Violent rain showers"),
            Map.entry(85, "Slight snow showers"),
            Map.entry(86, "Heavy snow showers"),
            Map.entry(95, "Thunderstorm"),
            Map.entry(96, "Thunderstorm with slight hail"),
            Map.entry(99, "Thunderstorm with heavy hail"));
}
