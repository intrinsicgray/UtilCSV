package io.intrinsicgray.utilcsv;

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
