package io.intrinsicgray.utilcsv;

public class CSVParser {

    private String newline;
    private char delimiter;

    private boolean useHeader;
    private boolean alwaysUseMarks;


    public CSVParser(String newline, char delimiter, boolean useHeader, boolean alwaysUseMarks) {
        this.newline        = newline;
        this.delimiter      = delimiter;
        this.useHeader      = useHeader;
        this.alwaysUseMarks = alwaysUseMarks;
    }

    public CSVParser(String newline, char delimiter, boolean useHeader) {
        this(newline, delimiter, useHeader, true);
    }

    public CSVParser(char delimiter, boolean useHeader) {
        this(System.lineSeparator(), delimiter, useHeader);
    }

    public CSVParser(boolean useHeader) {
        this(',', useHeader);
    }

    public CSVParser() {
        this(true);
    }


    private String formatCell(String cell) {
        if(cell == null) {
            cell = "";
        }

        return (cell.contains(String.valueOf(this.delimiter)) || this.alwaysUseMarks)
                ? "\"%s\"".formatted(cell)
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
                throw new IllegalArgumentException("Wrong headers. Need to pass exactly %d columns names, or empty".formatted(columnsCnt));
            }
        }

        final StringBuilder stringBuilder = new StringBuilder();

        if(this.useHeader) {
            stringBuilder
                    .append(String.join(String.valueOf(this.delimiter), headerOrder.stream().map(this::formatCell).toList()))
                    .append(this.newline);
        }

        rows.forEach(row -> {
            String cell;

            for(int i=0; i<headerOrder.size()-1; i++) {
                cell = row.getOrDefault(headerOrder.get(i), "");

                stringBuilder
                        .append(formatCell(cell))
                        .append(this.delimiter);
            }

            cell = row.getOrDefault(headerOrder.getLast(), "");
            stringBuilder
                    .append(formatCell(cell))
                    .append(this.newline);
        });

        return stringBuilder.toString();
    }

    private Object convertValue(Class<?> type, String value) throws IllegalArgumentException {
        if (type == String.class) return value;
        if (type == int.class || type == Integer.class) return Integer.parseInt(value);
        if (type == double.class || type == Double.class) return Double.parseDouble(value);
        if (type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(value);
        if (type == LocalDate.class) return LocalDate.parse(value);
        if (type == LocalDateTime.class) return LocalDateTime.parse(value);
        if (type.isEnum()) return Enum.valueOf(type.asSubclass(Enum.class), value);

        throw new IllegalArgumentException("Class %s not supported on CSV parser (value \"%s\")".formatted(type.getName(), value));
    }


    public String format(SequencedCollection<?> rows, List<String> headerOrder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
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

    public String format(SequencedCollection<?> rows, String... headerOrder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return format(rows, new ArrayList<>(Arrays.asList(headerOrder)));
    }


    public <T> List<T> parse(String csvContent, Class<T> destinationClass) throws IllegalArgumentException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if(csvContent == null || csvContent.isBlank()) {
            throw new IllegalArgumentException("CSV content cannot be null or empty");
        }

        final List<T> result = new ArrayList<>();

        final String delimiterStr = String.valueOf(this.delimiter);
        List<List<String>> rows = Arrays.stream(csvContent.split(this.newline))
                .filter(row -> !row.isBlank())
                .map(row -> {
                    boolean insideMarks = false;
                    for(int i=0; i<row.length(); i++) {
                        if(row.charAt(i) == this.delimiter && insideMarks) {
                            row = row.substring(0,i) + "|||DEL|||" + row.substring(i+1);
                        } else if(row.charAt(i) == '\"') {
                            insideMarks = !insideMarks;
                        }
                    }

                    row = row.replace("\"", "");
                    return Arrays.stream(row.split(delimiterStr))
                            .map(cell -> cell.replace("|||DEL|||", delimiterStr))
                            .toList();
                })
                .toList();

        if(rows.size() < 2) {
            return result;
        }

        final Constructor<T> constructor = destinationClass.getDeclaredConstructor();

        final List<String> header;
        if(this.useHeader) {
            header = rows.getFirst();
            rows = rows.subList(1, rows.size());
        } else {
            header = null;
        }

        String value;
        String methodName;
        CSVProperty csvProperty;
        for(List<String> row : rows) {
            final T object = constructor.newInstance();

            for(Field field : destinationClass.getDeclaredFields()) {
                csvProperty = field.getAnnotation(CSVProperty.class);
                if(csvProperty != null) {
                    if(!csvProperty.value().isBlank()) {
                        if(header == null) {
                            throw new IllegalArgumentException("Header must be present in order to use \"value\" CSV property");
                        }

                        value = row.get(header.indexOf(csvProperty.value()));
                    } else if(csvProperty.position() > 0) {
                        value = row.get(csvProperty.position() - 1);
                    } else {
                        throw new IllegalArgumentException("Invalid @CSVProperty annotation on field \"%s\" on class %s. When parsing, the annotation must contains \"value\" or \"position\"".formatted(field.getName(), destinationClass.getName()));
                    }

                    methodName = "set" + field.getName().substring(0,1).toUpperCase() + field.getName().substring(1);
                    for(Method method : destinationClass.getDeclaredMethods()) {
                        if(Modifier.isPublic(method.getModifiers()) && method.getName().equals(methodName)) {
                            method.invoke(object, convertValue(method.getParameterTypes()[0], value));
                            break;
                        }
                    }
                }
            }

            result.add(object);
        }

        return result;
    }

}
