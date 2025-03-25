package io.intrinsicgray.utilcsv.test;

import io.intrinsicgray.utilcsv.CSVFormatter;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class Test {

    public static void main(String[] args) {

        List<Addressbook> addressbookList = new ArrayList<>();
        addressbookList.add(new Addressbook("Marco, \"Salvatore\"", "De Giovanni", 27));
        addressbookList.add(new Addressbook("Luca", "De Giovanni", 22));
        addressbookList.add(new Addressbook("Costanza", "Corsini", 27));

        CSVFormatter formatter = new CSVFormatter().useHeader(true).delimiter(';');

        try {
            System.out.println(formatter.format(addressbookList));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
