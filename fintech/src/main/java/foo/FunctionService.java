package foo;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
        return weathers.parallelStream()
                .filter(weather -> weather.getTemperature() > temperature)
                .filter(distinctByKey(Weather::getRegionId))
                .map(Weather::getRegionName)
                .collect(Collectors.toList());
    }

    private static Predicate<Weather> distinctByKey(Function<Weather, UUID> keyExtractor) {
        Set<UUID> set = ConcurrentHashMap.newKeySet();
        return t -> set.add(keyExtractor.apply(t));
    }

    /** Задание 3 */

    public static Map<UUID, List<Double>> getMapByKeyEqualsUUID(List<Weather> list) {
        return list.parallelStream()
                .collect(Collectors
                        .groupingBy(Weather::getRegionId,
                                Collectors.mapping(Weather::getTemperature, Collectors.toList())));
    }

    /** Задание 4 */
    public static Map<Integer, Collection<Weather>> getMapByKeyEqualsTemperature(List<Weather> weathers) {
        return weathers.parallelStream()
                .collect(Collectors.groupingBy(weather -> weather.getTemperature().intValue(), Collectors.toCollection(ArrayList::new)));
    }
    public static void fillList(List<Weather> weathers) {
        double upperBound = 41;
        double lowerBound = -41;
        Random random = new Random();

        for (int i = 0; i < 1000; i++) {
            double temperature = random.nextDouble(upperBound - lowerBound) + lowerBound;
            weathers.add(new Weather(Region.BELGOROD.getId(), Region.BELGOROD.getName(), temperature, LocalDateTime.now()));
            weathers.add(new Weather(Region.MOSCOW.getId(), Region.MOSCOW.getName(), temperature, LocalDateTime.now()));
            weathers.add(new Weather(Region.TAMBOV.getId(), Region.TAMBOV.getName(), random.nextDouble(upperBound - lowerBound) + lowerBound, LocalDateTime.now()));
            weathers.add(new Weather(Region.TULA.getId(), Region.TULA.getName(), random.nextDouble(upperBound - lowerBound) + lowerBound, LocalDateTime.now()));
            weathers.add(new Weather(Region.VLADIMIR.getId(), Region.VLADIMIR.getName(), random.nextDouble(upperBound - lowerBound) + lowerBound, LocalDateTime.now()));
        }
    }
}
