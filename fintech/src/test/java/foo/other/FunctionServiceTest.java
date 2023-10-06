package foo.other;

import foo.models.Weather;
import foo.other.FunctionService;
import org.assertj.core.util.DoubleComparator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FunctionServiceTest {

    List<Weather> weathers;

    @BeforeAll
    void createListWeathers() {
        weathers = new ArrayList<>();
    }

    @AfterEach()
    void clearListWeathers() {
        weathers.clear();
    }

    @Test
    void getAverageTemperatureOfAllRegionsShouldReturnZero() {
        List<Weather> weathers = new ArrayList<>();

        double expected = 0;
        double actual = FunctionService.getAverageTemperatureOfAllRegions(weathers);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getAverageTemperatureOfAllRegions() {
        weathers.add(new Weather(3L, "Tula", 5.031, LocalDateTime.now()));
        weathers.add(new Weather(1L, "Moscow", 0.1, LocalDateTime.now()));
        weathers.add(new Weather(0L, "Belgorod", -15.131, LocalDateTime.now()));

        double expected = -3.33333;
        double actual = FunctionService.getAverageTemperatureOfAllRegions(weathers);

        assertThat(actual).isEqualTo(expected, withPrecision(0.01d));
    }

    @Test
    void getAverageTemperatureOfAllWhenListEmpty() {
        double expected = 0;

        double actual = FunctionService.getAverageTemperatureOfAllRegions(weathers);

        assertThat(actual).isEqualTo(expected, withPrecision(0.01d));
    }

    @Test
    void getAverageTemperatureByRegion() {
        weathers.add(new Weather(3L, "Tula", 0.0, LocalDateTime.now()));
        weathers.add(new Weather(3L, "Tula", 5.0, LocalDateTime.now()));
        weathers.add(new Weather(1L, "Moscow", 2.0, LocalDateTime.now()));
        weathers.add(new Weather(1L, "Moscow", -12.0, LocalDateTime.now()));
        weathers.add(new Weather(0L, "Belgorod", 4.3, LocalDateTime.now()));
        weathers.add(new Weather(0L, "Belgorod", 1.4, LocalDateTime.now()));
        weathers.add(new Weather(2L, "Tambov", -10.5, LocalDateTime.now()));
        weathers.add(new Weather(2L, "Tambov", -5.2, LocalDateTime.now()));

        Map<Long, Double> expected = new HashMap<>();
        expected.put(3L, 2.5);
        expected.put(1L, -5.0);
        expected.put(0L, 2.85);
        expected.put(2L, -7.85);

        Map<Long, Double> actual = FunctionService.getAverageTemperatureByRegion(weathers);

        assertThat(actual).hasSameSizeAs(expected);
        for (Map.Entry<Long, Double> entry : expected.entrySet()) {
            assertThat(actual).containsKey(entry.getKey());
            assertThat(actual.get(entry.getKey())).isEqualTo(entry.getValue(), withPrecision(0.001d));
        }
    }

    @Test
    void getAverageTemperatureByRegionWhenListEmpty() {
        Map<Long, Double> actual = FunctionService.getAverageTemperatureByRegion(weathers);

        assertThat(actual).isEmpty();
    }

    @Test
    void getRegionsWhereTemperatureGreat() {
        weathers.add(new Weather(3L, "Tula", 7.0, LocalDateTime.now()));
        weathers.add(new Weather(3L, "Tula", 15.0, LocalDateTime.now()));
        weathers.add(new Weather(1L, "Moscow", 2.0, LocalDateTime.now()));
        weathers.add(new Weather(1L, "Moscow", -12.0, LocalDateTime.now()));
        weathers.add(new Weather(0L, "Belgorod", 4.3, LocalDateTime.now()));
        weathers.add(new Weather(0L, "Belgorod", 1.4, LocalDateTime.now()));
        weathers.add(new Weather(2L, "Tambov", -10.5, LocalDateTime.now()));
        weathers.add(new Weather(2L, "Tambov", -5.2, LocalDateTime.now()));

        double temperature = 7;

        String expected = "Tula";
        List<String> actual = FunctionService.getRegionsWhereTemperatureGreat(weathers, temperature);

        assertThat(actual).hasSize(1);
        assertThat(actual.get(0)).isEqualTo(expected);
    }

    @Test
    void getRegionsWhereTemperatureGreatWhenListEmpty() {
        int temperature = 7;
        List<String> actual = FunctionService.getRegionsWhereTemperatureGreat(weathers, temperature);

        assertThat(actual).isEmpty();

    }

    @Test
    void getRegionsWhereTemperatureGreatHasDuplicate() {
        weathers.add(new Weather(3L, "Tambov", 7.0, LocalDateTime.now()));
        weathers.add(new Weather(3L, "Tula", 15.0, LocalDateTime.now()));

        double temperature = 7;

        String expected = "Tula";
        List<String> actual = FunctionService.getRegionsWhereTemperatureGreat(weathers, temperature);

        assertThat(actual).hasSize(1);
        assertThat(actual.get(0)).isEqualTo(expected);
    }

    @Test
    void getMapByKeyEqualsLong() {
        weathers.add(new Weather(3L, "Tula", 5.031, LocalDateTime.now()));
        weathers.add(new Weather(1L, "Moscow", 1.1, LocalDateTime.now()));
        weathers.add(new Weather(0L, "Belgorod", -15.131, LocalDateTime.now()));
        weathers.add(new Weather(0L, "Belgorod", 10.131, LocalDateTime.now()));

        Map<Long, List<Double>> expected = new HashMap<>();
        expected.put(3L, List.of(weathers.get(0).getTemperature()));
        expected.put(1L, List.of(weathers.get(1).getTemperature()));
        expected.put(0L, List.of(weathers.get(2).getTemperature(), weathers.get(3).getTemperature()));

        Map<Long, List<Double>> actual = FunctionService.getMapByKeyEqualsId(weathers);

        assertThat(actual).hasSameSizeAs(expected);
        for (Map.Entry<Long, List<Double>> entry : expected.entrySet()) {
            assertThat(actual).containsKey(entry.getKey());
            assertThat(actual.get(entry.getKey()))
                    .usingComparatorForType(new DoubleComparator(0.001), Double.class)
                    .containsExactlyElementsOf(entry.getValue());
        }
    }

    @Test
    void getMapByKeyEqualsLongWhenListEmpty() {
        Map<Long, List<Double>> actual = FunctionService.getMapByKeyEqualsId(weathers);
        assertThat(actual).isEmpty();
    }

    @Test
    void getMapByKeyEqualsTemperature() {
        weathers.add(new Weather(3L, "Tula", 5.031, LocalDateTime.now()));
        weathers.add(new Weather(1L, "Moscow", 5.331, LocalDateTime.now()));
        weathers.add(new Weather(0L, "Belgorod", 15.631, LocalDateTime.now()));
        weathers.add(new Weather(2L, "Tambov", 10.131, LocalDateTime.now()));

        Map<Integer, Collection<Weather>> expected = new HashMap<>();
        expected.put(5, List.of(weathers.get(0), weathers.get(1)));
        expected.put(15, List.of(weathers.get(2)));
        expected.put(10, List.of(weathers.get(3)));

        Map<Integer, List<Weather>> actual = FunctionService.getMapByKeyEqualsTemperature(weathers);

        assertThat(actual).hasSameSizeAs(expected);
        for (Map.Entry<Integer, Collection<Weather>> entry : expected.entrySet()) {
            assertThat(actual).containsKey(entry.getKey());
            assertThat(actual.get(entry.getKey()))
                    .usingComparatorForType(new DoubleComparator(0.001), Double.class)
                    .containsExactlyElementsOf(entry.getValue());
        }
    }

    @Test
    void getMapByKeyEqualsTemperatureWhenListEmpty() {
        Map<Integer, List<Weather>> actual = FunctionService.getMapByKeyEqualsTemperature(weathers);

        assertThat(actual).isEmpty();
    }

}