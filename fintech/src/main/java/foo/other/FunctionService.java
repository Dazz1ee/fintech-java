package foo.other;

import foo.models.City;
import foo.models.Weather;
import foo.models.WeatherType;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class FunctionService {
    /** Задание 1 */
    public static Double getAverageTemperatureOfAllRegions(List<Weather> weathers) {
        return weathers.stream().collect(Collectors.averagingDouble(Weather::getTemperature));
    }

    public static Map<Long, Double> getAverageTemperatureByRegion(List<Weather> weathers) {
        return weathers.stream()
                .collect(Collectors.groupingBy(weather -> weather.getCity().getId(), Collectors.averagingDouble(Weather::getTemperature)));
    }

    /** Задание 2 */
    public static List<String> getRegionsWhereTemperatureGreat(List<Weather> weathers, double temperature) {
        return distinct(
                weathers.parallelStream()
                .filter(weather -> weather.getTemperature() > temperature),
                weather -> weather.getCity().getId()
                )
                .map(weather -> weather.getCity().getName())
                .toList();
    }

    private static <O, H> Stream<O> distinct(Stream<O> input, Function<O, H> byKeyExtractor) {
        Set<H> set = new HashSet<>();
        return input.filter(o -> set.add(byKeyExtractor.apply(o)));
    }

    /** Задание 3 */

    public static Map<Long, List<Double>> getMapByKeyEqualsId(List<Weather> list) {
        return list.parallelStream()
                .collect(Collectors
                        .groupingBy(weather -> weather.getCity().getId(),
                                Collectors.mapping(Weather::getTemperature, Collectors.toList())));
    }

    /**
     * Задание 4
     */
    public static Map<Integer, List<Weather>> getMapByKeyEqualsTemperature(List<Weather> weathers) {
        return weathers.parallelStream()
                .collect(Collectors.groupingBy(weather -> weather.getTemperature().intValue(), Collectors.toList()));
    }
    public static void fillList(List<Weather> weathers) {
        double upperBound = 41;
        double lowerBound = -41;
        Random random = new Random();
        Stream.generate(() -> {
            double temperature = random.nextDouble(upperBound - lowerBound) + lowerBound;
            return List.of(
                    new Weather(0L, new City(0L, "Belgorod"), new WeatherType(0L, "rainy"), temperature, LocalDateTime.now()),
                    new Weather(1L, new City(1L, "Moscow"), new WeatherType(0L, "rainy"), temperature, LocalDateTime.now()),
                    new Weather(2L, new City(2L, "Tambov"), new WeatherType(0L, "rainy"), random.nextDouble(upperBound - lowerBound) + lowerBound, LocalDateTime.now()),
                    new Weather(3L, new City(3L, "Tula"), new WeatherType(0L, "rainy"), random.nextDouble(upperBound - lowerBound) + lowerBound, LocalDateTime.now()),
                    new Weather(4L, new City(4L, "Vladimir"), new WeatherType(0L, "rainy"), random.nextDouble(upperBound - lowerBound) + lowerBound, LocalDateTime.now())
            );
        })
        .limit(1000)
        .forEach(weathers::addAll);
    }
}
