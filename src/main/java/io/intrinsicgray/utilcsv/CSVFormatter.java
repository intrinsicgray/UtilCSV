package io.intrinsicgray.utilcsv;

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
        if(cell == null) return "";

        return cell.contains(String.valueOf(this.delimiter)) || alwaysUseQuotes
                ? this.quote + cell.replace(String.valueOf(this.quote), String.valueOf(this.quote+this.quote)) + this.quote
                : cell;
    }

    private String formatMap(List<Map<String, String>> rows, List<String> headerOrder) throws IllegalArgumentException {
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

        final StringBuilder stringBuilder = new StringBuilder();

        if(this.useHeader) {
            stringBuilder
                    .append(
                            headerOrder.stream()
                                    .map(this::formatCell)
                                    .collect(Collectors.joining(String.valueOf(this.delimiter)))
                    )
                    .append(this.newline);
        }

        rows.forEach(row -> {
            String cell;
            for(int i=0; i<headerOrder.size()-1; i++) {
                cell = row.getOrDefault(headerOrder.get(i), "");

                stringBuilder
                        .append(formatCell(cell))
                        .append(i == headerOrder.size()-1 ? this.newline : this.delimiter);
            }
        });

        return stringBuilder.toString();
    }


    // Public methods

    public String format(List<?> rows, List<String> headerOrder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        if(rows == null) {
            throw new IllegalArgumentException("rows cannot be null");
        }
        if(rows.isEmpty()) {
            return "";
        }

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

        return formatMap(mappedRows, headerOrder);
    }

    public String format(List<?> rows, String... headerOrder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return format(rows, new ArrayList<>(Arrays.asList(headerOrder)));
    }

}
