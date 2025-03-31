package io.intrinsicgray.utilcsv;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to mark fields in POJOs that represent columns in CSV files.
 * This annotation allows specifying the column name and its order in the CSV file.
 * A lower order value places the column earlier (i.e., towards the left).
 *
 * <p>Usage example:</p>
 * <pre>
 * public class Person {
 *     {@literal @}CSVColumn(name = "First Name", order = 1)
 *     private String firstName;
 *
 *     {@literal @}CSVColumn(name = "Age", order = 2)
 *     private int age;
 * }
 * </pre>
 *
 * @author Intrinsic gray (mdegiovanni97@gmail.com)
 * @version 0.1.0
 * @since 0.1.0
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CSVColumn {

    int    order() default 0;
    String name()  default "";

}
