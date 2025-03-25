package io.intrinsicgray.utilcsv;

import java.io.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import java.util.*;
import java.util.stream.Collectors;

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
