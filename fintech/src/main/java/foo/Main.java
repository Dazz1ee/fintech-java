package foo;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Weather> weathers = new ArrayList<>();
        FunctionService.fillList(weathers);
        int temperature = 0;

        System.out.println(FunctionService.getAverageTemperatureByRegion(weathers));
        System.out.println(FunctionService.getAverageTemperatureOfAllRegions(weathers));
        System.out.println(FunctionService.getRegionsWhereTemperatureGreat(weathers, temperature));
        System.out.println(FunctionService.getMapByKeyEqualsUUID(weathers));
        System.out.println(FunctionService.getMapByKeyEqualsTemperature(weathers));
    }


}