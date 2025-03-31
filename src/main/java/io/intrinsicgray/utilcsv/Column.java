package io.intrinsicgray.utilcsv;

/**
 * Class used in order to manage the single column on a CSV file. The class contains info about:
 * <ul>
 *   <li>The column name</li>
 *   <li>The order of the column (from left to right)</li>
 *   <li>The class used to represent the information on the POJO class</li>
 *   <li>The name of the setter (during parse) or getter (during format) method invoked</li>
 * </ul>
 *
 * @author Intrinsic gray (mdegiovanni97@gmail.com)
 * @version 0.1.0
 * @since 0.1.0
 */
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

    protected Class<?> getType() { return type; }
    protected void setType(Class<?> type) { this.type = type; }

    protected String getMethodName() { return methodName; }
    protected void setMethodName(String methodName) { this.methodName = methodName; }
}
