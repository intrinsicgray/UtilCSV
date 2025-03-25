package io.intrinsicgray.utilcsv.test;

import io.intrinsicgray.utilcsv.CSVColumn;

public class Addressbook {

    @CSVColumn(value = 1, name = "Name")
    private String firstName;

    @CSVColumn(value = 0, name = "Surname")
    private String lastName;

    @CSVColumn(value = 2, name = "Age")
    private int age;


    public Addressbook(String firstName, String lastName, int age) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
    }


    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
