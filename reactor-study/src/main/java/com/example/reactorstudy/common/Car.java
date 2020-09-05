package com.example.reactorstudy.common;

public class Car {
    private CarMaker carMaker;
    private CarType carType;
    private String carName;
    private int carPrice;
    private boolean isNew;

    public Car(String carName){
        this.carName = carName;
    }

    public Car(String carName, CarType carType){
        this.carName = carName;
        this.carType = carType;
    }

    public Car(CarMaker carMaker, String carName, CarType carType, int carPrice){
        this.carMaker = carMaker;
        this.carName = carName;
        this.carType = carType;
        this.carPrice = carPrice;
    }

    public Car(CarMaker carMaker, CarType carType, String carName, int carPrice, boolean isNew) {
        this.carMaker = carMaker;
        this.carType = carType;
        this.carName = carName;
        this.carPrice = carPrice;
        this.isNew = isNew;
    }

    public CarMaker getCarMaker() {
        return carMaker;
    }

    public void setCarMaker(CarMaker carMaker) {
        this.carMaker = carMaker;
    }

    public CarType getCarType() {
        return carType;
    }

    public void setCarType(CarType carType) {
        this.carType = carType;
    }

    public String getCarName() {
        return carName;
    }

    public void setCarName(String carName) {
        this.carName = carName;
    }

    public int getCarPrice() {
        return carPrice;
    }

    public void setCarPrice(int carPrice) {
        this.carPrice = carPrice;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }
}
