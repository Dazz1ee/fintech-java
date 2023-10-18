package foo.models;


import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@Data
public class WeatherType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Max(255)
    private String type;

    @OneToMany(mappedBy = "weather", fetch = FetchType.LAZY)
    @Transient
    @ToString.Exclude
    private List<Weather> weatherList;

    public WeatherType(Long id, String type) {
        this.id = id;
        this.type = type;

        if (weatherList == null) {
            weatherList = new ArrayList<>();
        }
    }

    public WeatherType(String type) {
        this.type = type;
        weatherList = new ArrayList<>();
    }

    public WeatherType() {
        weatherList = new ArrayList<>();
    }
}
