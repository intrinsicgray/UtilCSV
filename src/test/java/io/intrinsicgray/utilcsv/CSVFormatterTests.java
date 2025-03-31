package io.intrinsicgray.utilcsv;

import io.intrinsicgray.utilcsv.example.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CSVFormatterTests {

    private CSVFormatter formatter;


    @BeforeEach
    void resetFormatter() {
        this.formatter = new CSVFormatter();
    }


    @Test
    void testNullList() {
        assertThrows(NullPointerException.class, () ->formatter.format(null));
    }

    @Test
    void formatterEmptyList() {
        final List<PersonNameAndOrder> people = List.of();

        //String
        try {
            String csvContent = formatter.format(people);

            assertNotNull(csvContent);
            assertEquals("", csvContent);
        } catch (Exception e) {
            fail(e);
        }
    }


    private void testExpectedCsvContent(List<Person> people, String expectedCsv) {
        try {
            String csvContent = formatter.format(people);

            assertNotNull(csvContent);
            assertEquals(expectedCsv, csvContent);
        } catch (Exception e) {
            fail(e);
        }
    }


    @Test
    void formatterNameAndOrder() {
        final List<Person> people = List.of(
                new PersonNameAndOrder("Joseph Francis \"Joey\" Tribbiani Jr.", LocalDate.of(1968, Month.JANUARY, 9), 175, false),
                new PersonNameAndOrder("Sheldon Lee Cooper", LocalDate.of(1980, Month.FEBRUARY, 26), 186, true)
        );

        //Standard
        String expectedCsv =
                "Joseph Francis \"Joey\" Tribbiani Jr.,1968-01-09,175,false" + System.lineSeparator() +
                "Sheldon Lee Cooper,1980-02-26,186,true" + System.lineSeparator();

        testExpectedCsvContent(people, expectedCsv);


        //Use header
        formatter.useHeader(true);
        expectedCsv =
                "Full name,Birthdate,Height,Won a Nobel" + System.lineSeparator() +
                "Joseph Francis \"Joey\" Tribbiani Jr.,1968-01-09,175,false" + System.lineSeparator() +
                "Sheldon Lee Cooper,1980-02-26,186,true" + System.lineSeparator();

        testExpectedCsvContent(people, expectedCsv);


        //Comma on cell
        formatter.useHeader(false);
        people.get(0).setName("Joseph, Francis \"Joey\" Tribbiani Jr.");
        expectedCsv =
                "\"Joseph, Francis \"\"Joey\"\" Tribbiani Jr.\",1968-01-09,175,false" + System.lineSeparator() +
                "Sheldon Lee Cooper,1980-02-26,186,true" + System.lineSeparator();

        testExpectedCsvContent(people, expectedCsv);


        //Always use quotes
        people.get(0).setName("Joseph Francis \"Joey\" Tribbiani Jr.");
        formatter.alwaysUseQuotes(true);

        expectedCsv =
                "\"Joseph Francis \"\"Joey\"\" Tribbiani Jr.\",\"1968-01-09\",\"175\",\"false\"" + System.lineSeparator() +
                "\"Sheldon Lee Cooper\",\"1980-02-26\",\"186\",\"true\"" + System.lineSeparator();

        testExpectedCsvContent(people, expectedCsv);
    }

}
