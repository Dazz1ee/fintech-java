package foo;

import org.assertj.core.util.DoubleComparator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import java.util.*;

import static org.assertj.core.api.Assertions.*;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FunctionServiceTest {
    private List<Weather> weathers;

    @BeforeAll
    void fillWeathers() {
        weathers = new ArrayList<>();
        FunctionService.fillList(weathers);
    }

    @Test
    void getAverageTemperatureOfAllRegions() {
        double expected  = 0;
        for (Weather weather : weathers) {
            expected += weather.getTemperature();
        }
        expected /= weathers.size();

        double actual = FunctionService.getAverageTemperatureOfAllRegions(weathers);

        assertThat(actual).isEqualTo(expected, withPrecision(0.01d));
    }

    @Test
    void getAverageTemperatureByRegion() {
        Map<UUID, Double> expected = new HashMap<>();
        Map<UUID, Integer> countWeatherForRegion = new HashMap<>();
        for (Weather weather : weathers) {
            expected.put(weather.getRegionId(), expected.getOrDefault(weather.getRegionId(), 0d) +  weather.getTemperature());
            countWeatherForRegion.put(weather.getRegionId(), countWeatherForRegion.getOrDefault(weather.getRegionId(), 0) + 1);
        }
        for (Map.Entry<UUID, Integer> entry : countWeatherForRegion.entrySet()) {
            expected.put(entry.getKey(), expected.get(entry.getKey()) / entry.getValue());
        }

        Map<UUID, Double> actual = FunctionService.getAverageTemperatureByRegion(weathers);

        assertThat(actual).hasSameSizeAs(expected);
        for (Map.Entry<UUID, Double> entry : expected.entrySet()) {
            assertThat(actual).containsKey(entry.getKey());
            assertThat(actual.get(entry.getKey())).isEqualTo(entry.getValue(), withPrecision(0.001d));
        }
    }

    @Test
    void getRegionsWhereTemperatureGreat() {
        double temperature = 7;
        Map<UUID, String> expected = new HashMap<>();
        for (Weather weather : weathers) {
            if (weather.getTemperature() > temperature) {
                expected.put(weather.getRegionId(), weather.getRegionName());
            }
        }

        List<String> actual = FunctionService.getRegionsWhereTemperatureGreat(weathers, temperature);

        assertThat(actual).hasSameElementsAs(expected.values());

    }

    @Test
    void getMapByKeyEqualsUUID() {
        Map<UUID, List<Double>> expected = new HashMap<>();
        for (Weather weather : weathers) {
            expected.putIfAbsent(weather.getRegionId(), new ArrayList<>());
            expected.get(weather.getRegionId()).add(weather.getTemperature());
        }

        Map<UUID, List<Double>> actual = FunctionService.getMapByKeyEqualsUUID(weathers);

        assertThat(actual).hasSameSizeAs(expected);

        for (Map.Entry<UUID, List<Double>> entry : expected.entrySet()) {
            assertThat(actual).containsKey(entry.getKey());
            assertThat(actual.get(entry.getKey()))
                    .usingComparatorForType(new DoubleComparator(0.001), Double.class)
                    .containsExactlyElementsOf(entry.getValue());
        }
    }

    @Test
    void getMapByKeyEqualsTemperature() {
        Map<Integer, Collection<Weather>> expected = new HashMap<>();
        for (Weather weather : weathers) {
            expected.putIfAbsent(weather.getTemperature().intValue(), new ArrayList<>());
            expected.get(weather.getTemperature().intValue()).add(weather);
        }

        Map<Integer, Collection<Weather>> actual = FunctionService.getMapByKeyEqualsTemperature(weathers);

        assertThat(actual).hasSameSizeAs(expected);

        for (Map.Entry<Integer, Collection<Weather>> entry : expected.entrySet()) {
            assertThat(actual).containsKey(entry.getKey());
            assertThat(actual.get(entry.getKey()))
                    .usingComparatorForType(new DoubleComparator(0.001), Double.class)
                    .containsExactlyElementsOf(entry.getValue());
        }
    }


}