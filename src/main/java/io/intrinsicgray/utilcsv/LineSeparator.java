package io.intrinsicgray.utilcsv;

/**
 * Enum representing common line separators used in text files, including CSV files.
 * This enum provides a standardized way to handle different newline characters
 * across various operating systems.
 *
 * <p>Supported values:</p>
 * <ul>
 *     <li>{@link #CR} - Carriage Return ("\r"), used in classic macOS.</li>
 *     <li>{@link #LF} - Line Feed ("\n"), used in Unix/Linux and modern macOS.</li>
 *     <li>{@link #CRLF} - Carriage Return + Line Feed ("\r\n"), used in Windows.</li>
 * </ul>
 *
 * <p>Usage example:</p>
 * <pre>
 * LineSeparator separator = LineSeparator.CRLF;
 * String newline = separator.value;
 * </pre>
 *
 * @author Intrinsic gray (mdegiovanni97@gmail.com)
 * @version 0.1.0
 * @since 0.1.0
 */
public enum LineSeparator {

    LF("\n"),
    CR("\r"),
    CRLF("\r\n");


    public final String value;

    LineSeparator(String value) {
        this.value = value;
    }


    public static LineSeparator getFromValue(String value) throws NullPointerException, IllegalArgumentException {
        if(value == null) throw new NullPointerException("value cannot be null");

        switch (value) {
            case "\n":
                return LineSeparator.LF;
            case "\r":
                return LineSeparator.CR;
            case "\r\n":
                return LineSeparator.CRLF;
            default:
                throw new IllegalArgumentException("Invalid line separator value ("+value+")");
        }
    }
}
