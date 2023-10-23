package foo.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record CurrentResponse (
    @JsonProperty("last_updated_epoch") Long lastUpdatedEpoch,
    @JsonFormat(pattern = "yyyy-MM-dd H:mm")
    @JsonProperty("last_updated") LocalDateTime lastUpdated,
    @JsonProperty("temp_c") Double tempC,
    @JsonProperty("temp_f") Double tempF,
    @JsonProperty("is_day") Integer isDay,
    Condition condition,
    @JsonProperty("wind_mph") Double windMph,
    @JsonProperty("wind_kph") Double windKph,
    @JsonProperty("wind_degree") Integer windDegree,
    @JsonProperty("wind_dir") String windDir,
    @JsonProperty("pressure_mb") Double pressureMb,
    @JsonProperty("pressure_in") Double pressureIn,
    @JsonProperty("precip_mm") Double precipMm,
    @JsonProperty("precip_in") Double precipIn,
    Integer humidity,
    Integer cloud,

    @JsonProperty("feelslike_c") Double feelsLikeC,
    @JsonProperty("feelslike_f") Double feelsLikeF,
    @JsonProperty("vis_km") Double visKm,
    @JsonProperty("vis_miles") Double visMiles,
    Double uv,
    @JsonProperty("gust_mph") Double gustMph,
    @JsonProperty("gust_kph") Double gustKph,
    @JsonProperty("air_quality") AirQuality airQuality)
{
    public record Condition(String text, String icon, Integer code){}
    public record AirQuality(Double co,
                              Double no2,
                              Double o3,
                              Double so2,
                              @JsonProperty("pm2_5") Double pm25,
                              Double pm10,
                              @JsonProperty("us_epa_index") Integer usEpaIndex,
                              @JsonProperty("gb_defra_index") Integer gbDefraIndex){}
}
