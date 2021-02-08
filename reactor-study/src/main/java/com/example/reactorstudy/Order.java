package com.example.reactorstudy;

import java.time.LocalDate;

public class Order {

    private Status status;
    private LocalDate startDate;

    public Order(Status status, LocalDate startDate) {
        this.status = status;
        this.startDate = startDate;
    }

    public Status getStatus() {
        return status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    @Override
    public String toString() {
        return "Order{" +
            "status=" + status +
            ", startDate=" + startDate +
            '}';
    }
}
