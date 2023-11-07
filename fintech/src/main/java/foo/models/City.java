package foo.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "cities")
@AllArgsConstructor
@Data
public class City {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "weather", fetch = FetchType.LAZY)
    @Transient
    @ToString.Exclude
    private List<Weather> weatherList;

    public City(String cityName) {
        name = cityName;
        weatherList = new ArrayList<>();
    }

    public City(Long cityId, String cityName) {
        name = cityName;
        id = cityId;
        weatherList = new ArrayList<>();
    }

    public City() {
        weatherList = new ArrayList<>();
    }

}
