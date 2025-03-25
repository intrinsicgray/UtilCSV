package io.intrinsicgray.utilcsv;

import java.io.IOException;
import java.io.Writer;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import java.util.*;
import java.util.stream.Collectors;

public class CSVFormatter {

    private String newline = System.lineSeparator();
    private char delimiter = ',';
    private char quote     = '\"';

    private boolean useHeader;
    private boolean alwaysUseQuotes;


    // Getters and setters
    public String getNewline() {
        return newline;
    }

    public void setNewline(String newline) throws NullPointerException, IllegalArgumentException {
        if(newline == null)   throw new NullPointerException("Newline cannot be null");
        if(newline.isBlank()) throw new IllegalArgumentException("Newline cannot be empty");

        this.newline = newline;
    }

    public char getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(char delimiter) {
        this.delimiter = delimiter;
    }

    public char getQuote() {
        return quote;
    }

    public void setQuote(char quote) {
        this.quote = quote;
    }

    public boolean isUseHeader() {
        return useHeader;
    }

    public void setUseHeader(boolean useHeader) {
        this.useHeader = useHeader;
    }

    public boolean isAlwaysUseQuotes() {
        return alwaysUseQuotes;
    }

    public void setAlwaysUseQuotes(boolean alwaysUseQuotes) {
        this.alwaysUseQuotes = alwaysUseQuotes;
    }


    public CSVFormatter newline(String newline) throws NullPointerException, IllegalArgumentException {
        setNewline(newline);
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
        return cell.contains(String.valueOf(this.delimiter)) || alwaysUseQuotes
                ? this.quote + cell.replace(String.valueOf(this.quote), String.valueOf(this.quote+this.quote)) + this.quote
                : cell;
    }


    private void writeOnBuffer(List<Map<String, String>> rows, List<String> headerOrder, Writer writer) throws IllegalArgumentException, IOException {
        if(headerOrder.isEmpty()) {
            final Set<String> fields = new HashSet<>();
            rows.forEach(row -> fields.addAll(row.keySet()));

            headerOrder.addAll(fields);
            headerOrder.sort(Comparator.naturalOrder());
        } else {
            final int columnsCnt = rows.stream()
                    .max(Comparator.comparingInt(Map::size))
                    .map(Map::size)
                    .orElse(0);

            if(headerOrder.size() != columnsCnt) {
                throw new IllegalArgumentException("Wrong headers. Need to pass exactly "+ columnsCnt +" columns names, or empty");
            }
        }

        if(this.useHeader) {
            writer.write(
                    headerOrder
                            .stream()
                            .map(this::formatCell)
                            .collect(Collectors.joining(String.valueOf(this.delimiter)))
                            + this.newline
            );
        }

        for(Map<String, String> row : rows) {
            String strRow;
            for(int i=0; i<headerOrder.size()-1; i++) {
                strRow = formatCell(row.getOrDefault(headerOrder.get(i), "")) + (i == headerOrder.size()-1 ? this.newline : this.delimiter);
                writer.append(strRow);
            }
        }
    }


    // Public methods
    public void format(List<?> rows, Writer writer, List<String> headerOrder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, IOException {
        if(rows == null)   throw new IllegalArgumentException("rows cannot be null");
        if(rows.isEmpty()) return;

        final List<Map<String, String>> mappedRows = new ArrayList<>();

        for(Object obj : rows) {
            final Map<String, String> mappedObj = new HashMap<>();

            Object value;
            String methodName;
            String headerName;
            CSVProperty csvProperty;
            for(Field field : obj.getClass().getDeclaredFields()) {
                csvProperty = field.getAnnotation(CSVProperty.class);
                if(csvProperty != null) {
                    if(!csvProperty.value().isBlank()) {
                        headerName = csvProperty.value();
                    } else if(csvProperty.position() > 0) {
                        headerName = "Column " + csvProperty.position();
                    } else {
                        headerName = field.getName();
                    }

                    methodName = (field.getType().equals(boolean.class) ? "is":"get")
                            + field.getName().substring(0,1).toUpperCase()
                            + field.getName().substring(1);

                    value = obj.getClass().getMethod(methodName).invoke(obj);
                    mappedObj.put(headerName, value == null ? null : value.toString());
                }
            }

            mappedRows.add(mappedObj);
        }

        writeOnBuffer(mappedRows, headerOrder, writer);
    }

    public void format(List<?> rows, Writer writer, String... headerOrder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, IOException {
        format(rows, writer, new ArrayList<>(Arrays.asList(headerOrder)));
    }



}
