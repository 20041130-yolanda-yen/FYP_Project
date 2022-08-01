package com.example.fypproject;

public class ScannedVehicles {

    private int id;
    private String car_num;
    private String location;
    private String date;
    private String time;
    private String type;
    private String results;

    public ScannedVehicles(int id, String car_num, String location, String date, String time, String type, String results) {
        this.id = id;
        this.car_num = car_num;
        this.location = location;
        this.date = date;
        this.time = time;
        this.type = type;
        this.results = results;
    }

    public ScannedVehicles() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCar_num() {
        return car_num;
    }

    public void setCar_num(String car_num) {
        this.car_num = car_num;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getResults() {
        return results;
    }

    public void setResults(String results) {
        this.results = results;
    }
}

