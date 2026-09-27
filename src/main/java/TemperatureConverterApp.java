public class TemperatureConverterApp {

    public static void main(String[] args) {
        TemperatureConverter converter = new TemperatureConverter();

        System.out.println("Temperature Converter");
        System.out.println("---------------------");

        System.out.println("32°F = " +
                converter.fahrenheitToCelsius(32) + "°C");

        System.out.println("100°C = " +
                converter.celsiusToFahrenheit(100) + "°F");

        System.out.println("300K = " +
                converter.kelvinToCelsius(300) + "°C");

        System.out.println("Is 51°C extreme? " +
                converter.isExtremeTemperature(51));
    }
}
