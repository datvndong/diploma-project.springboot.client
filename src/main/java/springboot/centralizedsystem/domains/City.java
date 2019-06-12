package springboot.centralizedsystem.domains;

public class City {

    private String weekday;
    private String date;
    private String name;
    private String country;
    private double temperature;
    private String description;

    public String getWeekday() {
        return weekday;
    }

    public void setWeekday(String weekday) {
        this.weekday = weekday;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public City(String weekday, String date, String name, String country, double temperature, String description) {
        super();
        this.weekday = weekday;
        this.date = date;
        this.name = name;
        this.country = country;
        this.temperature = temperature;
        this.description = description;
    }

    public City() {
        super();
    }
}
