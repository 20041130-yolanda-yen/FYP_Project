package com.example.fypproject;

public class FirebaseData {
    private int id;
    private String car_numFb;
    private String driver_id;
    private String driver_name;
    private String color;
    private String brand;
    private String approved_parking;
    private String carpark_type;

    public FirebaseData(int id, String car_num, String driver_id, String driver_name, String color, String brand, String approved_parking, String carpark_type){
        this.id = id;
        this.car_numFb = car_num;
        this.driver_id = driver_id;
        this.driver_name = driver_name;
        this.color = color;
        this.brand = brand;
        this.approved_parking = approved_parking;
        this.carpark_type = carpark_type;
    }

    public FirebaseData() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCar_numFb() {
        return car_numFb;
    }

    public void setCar_numFb(String car_numFb) {
        this.car_numFb = car_numFb;
    }

    public String getDriver_id() {
        return driver_id;
    }

    public void setDriver_id(String driver_id) {
        this.driver_id = driver_id;
    }

    public String getDriver_name() {
        return driver_name;
    }

    public void setDriver_name(String driver_name) {
        this.driver_name = driver_name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getApproved_parking() {
        return approved_parking;
    }

    public void setApproved_parking(String approved_parking) { this.approved_parking = approved_parking;
    }

    public String getCarpark_type() {
        return carpark_type;
    }

    public void setCarpark_type(String carpark_type) {

        this.carpark_type = carpark_type;
    }
}