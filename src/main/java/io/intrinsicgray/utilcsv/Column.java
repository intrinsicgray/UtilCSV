package io.intrinsicgray.utilcsv;

class Column {

    private String  name;
    private Integer order;
    private Class<?> type;

    private String  methodName;


    protected Column(String name, Integer order, Class<?> type, String methodName) {
        this.name       = name;
        this.order      = order;
        this.type       = type;
        this.methodName = methodName;
    }


    protected String getName() { return name; }
    protected void setName(String name) { this.name = name; }

    protected Integer getOrder() { return order; }
    protected void setOrder(Integer order) { this.order = order; }

    public Class<?> getType() { return type; }
    public void setType(Class<?> type) { this.type = type; }

    protected String getMethodName() { return methodName; }
    protected void setMethodName(String methodName) { this.methodName = methodName; }
}
