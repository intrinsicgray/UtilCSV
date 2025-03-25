package io.intrinsicgray.utilcsv;

import io.intrinsicgray.utilcsv.exception.CellCannotBeParsedException;
import io.intrinsicgray.utilcsv.exception.ColumnNameNotPresentException;
import io.intrinsicgray.utilcsv.exception.InvalidColumnOrderException;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CSVParser extends CSVUtil {

    @Override
    public CSVParser lineSeparator(LineSeparator lineSeparator) {
        setLineSeparator(lineSeparator);
        return this;
    }

    @Override
    public CSVParser delimiter(char delimiter) {
        setDelimiter(delimiter);
        return this;
    }

    @Override
    public CSVParser quote(char quote) {
        setQuote(quote);
        return this;
    }

    @Override
    public CSVParser useHeader(boolean useHeader) {
        setUseHeader(useHeader);
        return this;
    }

    @Override
    public CSVParser alwaysUseQuotes(boolean alwaysUseQuotes) {
        setAlwaysUseQuotes(alwaysUseQuotes);
        return this;
    }


    // Private methods
    private Object convertValue(Class<?> type, String value) throws CellCannotBeParsedException {
        if (type == String.class) return value;
        if (type == int.class || type == Integer.class) return Integer.parseInt(value.trim());
        if (type == double.class || type == Double.class) return Double.parseDouble(value.trim());
        if (type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(value);
        if (type == LocalDate.class) return LocalDate.parse(value);
        if (type == LocalDateTime.class) return LocalDateTime.parse(value);
        if (type.isEnum()) return Enum.valueOf(type.asSubclass(Enum.class), value);

        throw new CellCannotBeParsedException("Class "+type.getName()+" cannot be parsed (value: "+value+")");
    }


    private <T> List<T> parse(List<String> rows, Class<T> destinationClass) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        final List<T> result = new ArrayList<>();

        List<List<String>> splittedRows = rows
                .stream()
                .filter(row -> !row.isBlank())
                .map(row -> {
                    boolean insideMarks = false;
                    for(int i=0; i<row.length(); i++) {
                        if(row.charAt(i) == this.delimiter.charAt(0) && insideMarks) {
                            row = row.substring(0,i) + "|||DEL|||" + row.substring(i+1);
                        } else if(row.charAt(i) == this.quote.charAt(0)) {
                            insideMarks = !insideMarks;
                        }
                    }

                    row = row.replace(this.quote+this.quote, "|||QTE|||");
                    row = row.replace(this.quote, "");
                    row = row.replace("|||QTE|||", this.quote);

                    return Arrays.stream(row.split(this.delimiter))
                            .map(cell -> cell.replace("|||DEL|||", this.delimiter))
                            .collect(Collectors.toList());
                })
                .collect(Collectors.toList());

        if(splittedRows.isEmpty() || (this.useHeader && splittedRows.size() == 1)) {
            return result;
        }

        final List<Column> columns = new ArrayList<>();
        if(this.useHeader) {
            int columnCnt = 0;
            for(String columnName : splittedRows.get(0)) {
                columns.add(new Column(columnName, columnCnt, null, null));
                columnCnt++;
            }

            for(Field field : destinationClass.getDeclaredFields()) {
                final CSVColumn csvColumn = field.getAnnotation(CSVColumn.class);

                if(csvColumn != null) {
                    if(csvColumn.name().isBlank()) {
                        throw new ColumnNameNotPresentException("The @CSVColumn annotation on "+destinationClass.getName()+"."+field.getName()+" does not have a valid name. Enter the column name if you set \"useHeader\" to true");
                    }

                    final String methodName = "set" + field.getName().substring(0,1).toUpperCase() + field.getName().substring(1);
                    columns
                            .stream()
                            .filter(column -> column.getName().equals(csvColumn.name()))
                            .findAny()
                            .ifPresent(column -> {
                                column.setType(field.getType());
                                column.setMethodName(methodName);
                            });
                }
            }

            splittedRows = splittedRows.subList(1, splittedRows.size());

        } else {
            for(Field field : destinationClass.getDeclaredFields()) {
                final CSVColumn csvColumn = field.getAnnotation(CSVColumn.class);

                if(csvColumn != null) {
                    final Optional<Column> conflictColumn = columns
                            .stream()
                            .filter(column -> column.getOrder() == csvColumn.order())
                            .findAny();

                    if(conflictColumn.isPresent()) {
                        throw new InvalidColumnOrderException(destinationClass.getName()+"."+field.getName()+" and "+destinationClass.getName()+"."+field.getName()+" have the same order value: " + conflictColumn.get().getOrder());
                    }

                    final String methodName = "set" + field.getName().substring(0,1).toUpperCase() + field.getName().substring(1);
                    columns.add(new Column(field.getName(), csvColumn.order(), field.getType(), methodName));
                }
            }

            for(int i = 0; i<columns.size(); i++) {
                columns.get(i).setOrder(i);
            }
        }


        final Constructor<T> constructor = destinationClass.getDeclaredConstructor();
        for(List<String> row : splittedRows) {
            final T obj = constructor.newInstance();

            String cell;
            for(Column column : columns) {
                cell = row.get(column.getOrder());

                final Method method = destinationClass.getDeclaredMethod(column.getMethodName(), column.getType());
                method.invoke(obj, convertValue(column.getType(), cell));
            }

            result.add(obj);
        }

        return result;
    }


    //Public methods
    public <T> List<T> parse(BufferedReader reader, Class<T> destinationClass) throws IllegalArgumentException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, IOException {
        final List<String> rows = new ArrayList<>();
        final StringBuilder row = new StringBuilder();

        int character;
        while((character = reader.read()) != -1) {
            if(
                    (character == '\n' && this.lineSeparator.equals(LineSeparator.LF)) ||
                    (character == '\r' && this.lineSeparator.equals(LineSeparator.CR)) ||
                    (character == '\n' && row.toString().endsWith("\r") && this.lineSeparator.equals(LineSeparator.CRLF))
            ) {
                rows.add(row.toString());
                row.setLength(0);
            } else {
                row.append((char) character);
            }
        }
        if(row.length() > 0) {
            rows.add(row.toString());
        }

        return parse(rows, destinationClass);
    }

    public <T> List<T> parse(File file, Class<T> destinationClass) throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        try(final FileReader fileReader = new FileReader(file)) {
            final BufferedReader bufferedReader = new BufferedReader(fileReader);

            final List<T> result = parse(bufferedReader, destinationClass);

            bufferedReader.close();
            return result;
        }
    }

    public <T> List<T> parse(String csvContent, Class<T> destinationClass) throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        if(csvContent == null) throw new NullPointerException("csvContent cannot be null");

        final StringReader stringReader     = new StringReader(csvContent);
        final BufferedReader bufferedReader = new BufferedReader(stringReader);

        final List<T> result = parse(bufferedReader, destinationClass);

        bufferedReader.close();
        return result;
    }
}
