# UtilCSV
### v0.1.2

![GitHub License](https://img.shields.io/github/license/intrinsicgray/UtilCSV)
![Maven Central Last Update](https://img.shields.io/maven-central/last-update/io.github.intrinsicgray/utilcsv)
![GitHub Release](https://img.shields.io/github/v/release/intrinsicgray/UtilCSV)

## Overview
**UtilCSV** is a simple yet powerful Java library for handling CSV files.  
It allows converting annotated Java objects to CSV and vice versa, with support for headers, quotes, and customizable line separators.

## Features
- Parse CSV files into listed Java objects
- Format CSV data with customizable delimiters and quote characters
- Handle headers dynamically 
- Support for different line endings __(\n, \r, \r\n)__
- Escape special characters properly 
- Lightweight and dependency-free

## Installation
Using Maven:
```xml
<dependency>
    <groupId>io.github.intrinsicgray</groupId>
    <artifactId>csvutil</artifactId>
    <version>0.1.2</version>
</dependency>
```

Using Gradle:
```groovy
implementation 'io.github.intrinsicgray:csvutil:0.1.2'
```

---

## Getting started
To easily parse CSV data into objects, annotate class fields with **@CsvColumn**.
The library will automatically map each column to the correct field based on the header name or column index.

```java
public class Person {

    @CSVColumn(name = "Name")  // Maps to the "Name" column
    private String name;

    @CSVColumn(name = "Age")  // Maps to the "Age" column
    private int age;

    @CSVColumn(name = "City") // Maps to the "City" column
    private String city;

    // Default constructor is required
    public Person() {}

    // Getters and setters omitted...
}
```
or, If your CSV file does not have headers, you can specify column positions instead of names:
```java
public class Person {

    @CSVColumn(order = 1)  // Maps to the first column
    private String name;

    @CSVColumn(order = 2)  // Maps to the second column
    private int age;

    @CSVColumn(order = 3) // Maps to the third column
    private String city;

    // Default constructor is required
    public Person() {}

    // Getters and setters omitted...
}
```

### Parsing a CSV File into Java Objects
Given a CSV file people.csv like this:
```csv
Name,Age,City
Alice,30,New York
Bob,25,London
Charlie,28,San Francisco
```

You can parse it into a list of Person objects like this:
```java
File file = new File("input.csv");

CSVParser parser = new CSVParser().useHeader(true);
List<Person> people = parser.parse(file, Person.class);
```

### Writing Java Objects to a CSV File
To export a list of Java objects into a CSV file, use the **CSVFormatter** class.

```java
List<Person> people = Arrays.asList(
        new Person("Alice", 30, "New York"),
        new Person("Bob", 25, "London"),
        new Person("Charlie", 28, "San Francisco")
);

CSVFormatter formatter = new CSVFormatter();

// Write objects to a CSV file...
formatter.format(people, new File("output.csv"));

// ...or if you want, just put the CSV content into a String
String csvContent = formatter.format(people);
```
output:
```csv
Alice,30,New York
Bob,25,London
Charlie,28,San Francisco
```

### Customization
Both **CSVFormatter** and **CSVParser** have the following methods in order to properly set the CSV formating/parsing:

- ``setLineSeparator()``: Set the desired line separator between __\n__,__\r__ and __\r\n__. The system line separator is used by default
- ``setDelimiter()``: Set the desired delimiter character. Comma character (__,__) is used by default
- ``setQuotes()``: Set the desired charater to use as quote. Double quotes (__"__) are used by default
- ``useHeader()``: If set as true, then the assumption is that the first row of the CSV contains the columns names
- ``alwaysUseQuotes``: If set as true, all CSV cells contains quotes. Otherwise only the cells that contains the delimiter character will use the quotes

### Contributing
We'd love your contribution and bug reports are always welcome! You can open a bug report on GitHub.

## License
This project is licensed under the [Apache 2.0 License](https://github.com/intrinsicgray/UtilCSV/blob/master/LICENSE).