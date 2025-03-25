package io.intrinsicgray.utilcsv;

abstract class CSVUtil {

    protected LineSeparator lineSeparator = LineSeparator.getFromValue(System.lineSeparator());
    protected String delimiter            = ",";
    protected String quote                = "\"";

    protected boolean useHeader;
    protected boolean alwaysUseQuotes;


    public LineSeparator getLineSeparator() {
        return lineSeparator;
    }

    public void setLineSeparator(LineSeparator lineSeparator) throws IllegalArgumentException {
        if(lineSeparator == null)   throw new NullPointerException("Newline cannot be null");
        this.lineSeparator = lineSeparator;
    }

    public char getDelimiter() { return delimiter.charAt(0); }
    public void setDelimiter(char delimiter) { this.delimiter = String.valueOf(delimiter); }

    public char getQuote() { return quote.charAt(0); }
    public void setQuote(char quote) { this.quote = String.valueOf(quote); }

    public boolean isUseHeader() { return useHeader; }
    public void setUseHeader(boolean useHeader) { this.useHeader = useHeader; }

    public boolean isAlwaysUseQuotes() { return alwaysUseQuotes; }
    public void setAlwaysUseQuotes(boolean alwaysUseQuotes) { this.alwaysUseQuotes = alwaysUseQuotes; }


    public abstract CSVUtil lineSeparator(LineSeparator lineSeparator);
    public abstract CSVUtil delimiter(char delimiter);
    public abstract CSVUtil quote(char quote);
    public abstract CSVUtil useHeader(boolean useHeader);
    public abstract CSVUtil alwaysUseQuotes(boolean alwaysUseQuotes);

}
