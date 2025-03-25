package io.intrinsicgray.utilcsv;

class Column {

    private String  name;
    private Integer order;
    private String  methodName;


    protected Column(String name, Integer order, String methodName) {
        this.name       = name;
        this.order      = order;
        this.methodName = methodName;
    }


    protected String getName() {
        return name;
    }

    protected Integer getOrder() {
        return order;
    }

    protected String getMethodName() {
        return methodName;
    }
}
