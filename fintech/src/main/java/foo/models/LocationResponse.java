package foo.models;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public record LocationResponse (String name, String region,
                                String country, Double lat,
                                Double lon, @JsonProperty("tz_id") String tzId,
                                @JsonProperty("localtime_epoch") Long localTimeEpoch, @JsonFormat(pattern = "yyyy-MM-dd H:mm") LocalDateTime localtime) {
}
