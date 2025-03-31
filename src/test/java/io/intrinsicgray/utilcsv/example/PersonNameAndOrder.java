package io.intrinsicgray.utilcsv.example;

import io.intrinsicgray.utilcsv.CSVColumn;

import java.time.LocalDate;

public class PersonNameAndOrder implements Person {

    @CSVColumn(name = "Full name", order = 1)
    private String name;

    @CSVColumn(name = "Birthdate", order = 2)
    private LocalDate birthDate;

    @CSVColumn(name = "Height", order = 3)
    private int height;

    @CSVColumn(name = "Won a Nobel", order = 4)
    private boolean nobel;


    public PersonNameAndOrder() { }

    public PersonNameAndOrder(String name, LocalDate birthDate, int height, boolean nobel) {
        this.name      = name;
        this.birthDate = birthDate;
        this.height    = height;
        this.nobel     = nobel;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isNobel() {
        return nobel;
    }

    public void setNobel(boolean nobel) {
        this.nobel = nobel;
    }


    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", birthDate=" + birthDate +
                ", height=" + height +
                ", nobel=" + nobel +
                '}';
    }
}
