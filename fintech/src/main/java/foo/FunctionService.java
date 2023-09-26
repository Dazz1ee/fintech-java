package foo;

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

    public static Map<UUID, Double> getAverageTemperatureByRegion(List<Weather> weathers) {
        return weathers.stream()
                .collect(Collectors.groupingBy(Weather::getRegionId, Collectors.averagingDouble(Weather::getTemperature)));
    }

    /** Задание 2 */
    public static List<String> getRegionsWhereTemperatureGreat(List<Weather> weathers, double temperature) {
        return distinct(
                weathers.parallelStream()
                .filter(weather -> weather.getTemperature() > temperature),
                Weather::getRegionId
                )
                .map(Weather::getRegionName)
                .toList();
    }

    private static <O, H> Stream<O> distinct(Stream<O> input, Function<O, H> byKeyExtractor) {
        Set<H> set = new HashSet<>();
        return input.filter(o -> set.add(byKeyExtractor.apply(o)));
    }

    /** Задание 3 */

    public static Map<UUID, List<Double>> getMapByKeyEqualsUUID(List<Weather> list) {
        return list.parallelStream()
                .collect(Collectors
                        .groupingBy(Weather::getRegionId,
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
                    new Weather(Region.BELGOROD.getId(), Region.BELGOROD.getName(), temperature, LocalDateTime.now()),
                    new Weather(Region.MOSCOW.getId(), Region.MOSCOW.getName(), temperature, LocalDateTime.now()),
                    new Weather(Region.TAMBOV.getId(), Region.TAMBOV.getName(), random.nextDouble(upperBound - lowerBound) + lowerBound, LocalDateTime.now()),
                    new Weather(Region.TULA.getId(), Region.TULA.getName(), random.nextDouble(upperBound - lowerBound) + lowerBound, LocalDateTime.now()),
                    new Weather(Region.VLADIMIR.getId(), Region.VLADIMIR.getName(), random.nextDouble(upperBound - lowerBound) + lowerBound, LocalDateTime.now())
            );
        })
        .limit(1000)
        .forEach(weathers::addAll);
    }
}
