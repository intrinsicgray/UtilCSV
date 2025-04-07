package io.intrinsicgray.utilcsv;

import io.intrinsicgray.utilcsv.exception.ListCannotBeFormattedException;

import java.io.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for converting a list of objects into a CSV file.
 * This class processes objects annotated with {@link CSVColumn} and generates
 * a properly formatted CSV output, handling headers, quotes, and line endings.
 *
 * <p>The formatter automatically extracts field values based on the {@link CSVColumn}
 * annotation and orders them accordingly. It also ensures proper escaping of values
 * when necessary (e.g., wrapping values in quotes if they contain commas or newlines).</p>
 *
 * <p>Usage example:</p>
 * <pre>
 * List&lt;Person&gt; people = getPeopleList();
 * CSVFormatter csvFormatter = new CSVFormatter();
 *
 * //Save as string
 * String csvContent = csvFormatter.format(people);
 *
 * //Save as file
 * File file = new File("output.csv");
 * file = csvFormatter.format(people, file);
 *
 * //Use a buffered writer
 * FileWriter writer = new FileWriter("output.csv");
 * BufferedWriter bw = new BufferedWriter(writer);
 * csvFormatter.format(people, bw);
 *
 * writer.close();
 * bw.close();
 * </pre>
 *
 * @author Intrinsic gray (mdegiovanni97@gmail.com)
 * @version 0.1.0
 * @since 0.1.0
 */
public class CSVFormatter extends CSVUtil {

    @Override
    public CSVFormatter lineSeparator(LineSeparator lineSeparator) throws IllegalArgumentException {
        setLineSeparator(lineSeparator);
        return this;
    }

    @Override
    public CSVFormatter delimiter(char delimiter) {
        setDelimiter(delimiter);
        return this;
    }

    @Override
    public CSVFormatter quote(char quote) {
        setQuote(quote);
        return this;
    }

    @Override
    public CSVFormatter useHeader(boolean useHeader) {
        setUseHeader(useHeader);
        return this;
    }

    @Override
    public CSVFormatter alwaysUseQuotes(boolean alwaysUseQuotes) {
        setAlwaysUseQuotes(alwaysUseQuotes);
        return this;
    }


    // Private methods
    private String formatCell(String cell) {
        return alwaysUseQuotes || cell.contains(this.delimiter)
                ? this.quote + escape(cell.replace(this.quote, this.quote + this.quote)) + this.quote
                : escape(cell);
    }

    private void writeRowOnBuffer(List<String> orderedCells, Writer writer) throws IOException {
        final String strRow = orderedCells
                .stream()
                .map(this::formatCell)
                .collect(Collectors.joining(this.delimiter)) + this.lineSeparator.value;

        writer.write(strRow);
    }


    // Public methods

    /**
     * Format a {@link List} of object into a CSV file using a user-defined {@link BufferedWriter}
     *
     * @param rows The list of object to format into a CSV
     * @param writer The BufferedWriter where the formatter will write the CSV file
     * @param <T> The class of the objects
     *
     * @throws NullPointerException If rows or writer is null
     * @throws ListCannotBeFormattedException If there's a problem during the CSV formatting (check cause)
     */
    public <T> void format(List<T> rows, BufferedWriter writer) throws NullPointerException, ListCannotBeFormattedException {
        if(rows == null)   throw new NullPointerException("rows cannot be null");
        if(writer == null) throw new NullPointerException("writer cannot be null");

        if(rows.isEmpty()) return;


        final Class<?> clazz = rows.get(0).getClass();
        final List<Column> columns = new ArrayList<>();

        for(Field field : clazz.getDeclaredFields()) {
            final CSVColumn csvColumn = field.getAnnotation(CSVColumn.class);

            if(csvColumn != null) {
                final String columnName = csvColumn.name().isBlank() ? field.getName() : csvColumn.name();

                final String methodName = (field.getType().equals(boolean.class) ? "is":"get")
                        + field.getName().substring(0,1).toUpperCase()
                        + field.getName().substring(1);

                columns.add(new Column(columnName, csvColumn.order(), field.getType(), methodName));
            }
        }
        columns.sort(Comparator.comparing(Column::getOrder));

        try {
            if(this.useHeader) {
                writeRowOnBuffer(
                        columns
                                .stream()
                                .map(Column::getName)
                                .collect(Collectors.toList()),
                        writer
                );
            }

            for(Object row : rows) {
                final List<String> orderedCells = new ArrayList<>();

                for(Column column : columns) {
                    final Object value = clazz.getMethod(column.getMethodName()).invoke(row);
                    orderedCells.add(value == null ? "" : value.toString());
                }

                writeRowOnBuffer(orderedCells, writer);
            }
        } catch (IOException | InvocationTargetException | IllegalArgumentException | NoSuchMethodException | IllegalAccessException e) {
            throw new ListCannotBeFormattedException("Error during CSV formatting, "+e.getMessage(), e);
        }
    }

    /**
     * Format a {@link List} of object into a CSV file, saving the result into a String
     *
     * @param rows The list of object to format into a CSV
     * @param <T> The class of the objects
     *
     * @return A string containing the CSV content
     *
     * @throws NullPointerException If rows or writer is null
     * @throws IOException If there's a problem during the CSV formatting (check cause)
     * @throws ListCannotBeFormattedException If there's a problem during the CSV formatting (check cause)
     */
    public <T> String format(List<T> rows) throws NullPointerException, IOException, ListCannotBeFormattedException {
        final StringWriter stringWriter     = new StringWriter();
        final BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);
        format(rows, bufferedWriter);

        bufferedWriter.close();
        return stringWriter.toString();
    }

    /**
     * Format a {@link List} of object into a CSV file, saving the result into a {@link File}
     *
     * @param rows The list of object to format into a CSV
     * @param <T> The class of the objects
     *
     * @return The saved file. Note that the file will not be automatically created.
     *
     * @throws NullPointerException If rows or writer is null
     * @throws IOException If there's a problem during the CSV formatting (check cause)
     * @throws ListCannotBeFormattedException If there's a problem during the CSV formatting (check cause)
     */
    public <T> File format(List<T> rows, File file) throws NullPointerException, IOException, ListCannotBeFormattedException {
        final FileWriter fileWriter         = new FileWriter(file, false);
        final BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        format(rows, bufferedWriter);

        fileWriter.close();
        bufferedWriter.close();
        return file;
    }

}
