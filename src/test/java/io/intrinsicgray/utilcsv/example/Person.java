package io.intrinsicgray.utilcsv.example;

import java.time.LocalDate;

public interface Person {



     String getName();
     void setName(String name);

     LocalDate getBirthDate();
     void setBirthDate(LocalDate birthDate);

     int getHeight();
     void setHeight(int height);

     boolean isNobel();
     void setNobel(boolean nobel);

}
