package io.intrinsicgray.utilcsv;

import java.io.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import java.util.*;
import java.util.stream.Collectors;

public class CSVFormatter {

    private LineSeparator lineSeparator = LineSeparator.getFromValue(System.lineSeparator());
    private String delimiter            = ",";
    private String quote                = "\"";

    private boolean useHeader;
    private boolean alwaysUseQuotes;


    // Getters and setters
    public LineSeparator getLineSeparator() {
        return lineSeparator;
    }

    public void setLineSeparator(LineSeparator lineSeparator) throws IllegalArgumentException {
        if(lineSeparator == null)   throw new NullPointerException("Newline cannot be null");
        this.lineSeparator = lineSeparator;
    }

    public char getDelimiter() { return delimiter.charAt(0); }
    public void setDelimiter(char delimiter) { this.delimiter = String.valueOf(delimiter); }

    public char getQuote() { return quote.charAt(0); }
    public void setQuote(char quote) { this.quote = String.valueOf(quote); }

    public boolean isUseHeader() { return useHeader; }
    public void setUseHeader(boolean useHeader) { this.useHeader = useHeader; }

    public boolean isAlwaysUseQuotes() { return alwaysUseQuotes; }
    public void setAlwaysUseQuotes(boolean alwaysUseQuotes) { this.alwaysUseQuotes = alwaysUseQuotes; }


    public CSVFormatter lineSeparator(LineSeparator lineSeparator) throws IllegalArgumentException {
        setLineSeparator(lineSeparator);
        return this;
    }

    public CSVFormatter delimiter(char delimiter) {
        setDelimiter(delimiter);
        return this;
    }

    public CSVFormatter quote(char quote) {
        setQuote(quote);
        return this;
    }

    public CSVFormatter useHeader(boolean useHeader) {
        setUseHeader(useHeader);
        return this;
    }

    public CSVFormatter alwaysUseQuotes(boolean alwaysUseQuotes) {
        setAlwaysUseQuotes(alwaysUseQuotes);
        return this;
    }


    // Private methods
    private String formatCell(String cell) {
        return alwaysUseQuotes || cell.contains(this.delimiter)
                ? this.quote + cell.replace(this.quote, this.quote + this.quote) + this.quote
                : cell;
    }

    private void writeRowOnBuffer(List<String> orderedCells, Writer writer) throws IOException {
        final String strRow = orderedCells
                .stream()
                .map(this::formatCell)
                .collect(Collectors.joining(this.delimiter)) + this.lineSeparator.value;

        writer.write(strRow);
    }


    // Public methods
    public <T> void format(List<T> rows, Writer writer) throws NullPointerException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, IOException {
        if(rows == null)   throw new NullPointerException("rows cannot be null");
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

                columns.add(new Column(columnName, csvColumn.order(), methodName));
            }
        }
        columns.sort(Comparator.comparing(Column::getOrder));


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
    }


    public <T> String format(List<T> rows) throws IOException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        final StringWriter stringWriter = new StringWriter();
        format(rows, stringWriter);

        return stringWriter.toString();
    }

    public <T> File format(List<T> rows, File file) throws IOException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        final FileWriter fileWriter = new FileWriter(file, false);
        final BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        format(rows, bufferedWriter);

        bufferedWriter.close();
        return file;
    }

}
