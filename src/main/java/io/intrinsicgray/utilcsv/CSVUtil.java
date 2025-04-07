package io.intrinsicgray.utilcsv;

abstract class CSVUtil {

    protected LineSeparator lineSeparator = LineSeparator.getFromValue(System.lineSeparator());
    protected String delimiter            = ",";
    protected String quote                = "\"";

    protected boolean useHeader;
    protected boolean alwaysUseQuotes;


    protected String escape(String str) {
        return str
                .replace("\t", "\\t")
                .replace("\b", "\\b")
                .replace("\t", "\\t")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\f", "\\f")
                .replace("\\", "\\\\");
    }


    /**
     * @return The line separator used for CSV parsing/formatting. The system line separator is set as default
     */
    public LineSeparator getLineSeparator() {
        return lineSeparator;
    }

    /**
     * Set the line separator to use in CSV parsing/formatting
     *
     * @param lineSeparator The {@link LineSeparator} to use on CSV file
     * @throws NullPointerException if lineSeparator is null
     */
    public void setLineSeparator(LineSeparator lineSeparator) throws NullPointerException {
        if(lineSeparator == null)   throw new NullPointerException("Newline cannot be null");
        this.lineSeparator = lineSeparator;
    }

    /**
     * @return The char used as delimiter character. Comma ( <i>,</i> ) is set as default
     */
    public char getDelimiter() { return delimiter.charAt(0); }

    /**
     * Set the delimiter to use in CSV parsing/formatting
     * @param delimiter The character to use as delimiter
     */
    public void setDelimiter(char delimiter) { this.delimiter = String.valueOf(delimiter); }

    /**
     * @return The char used as quote. Double quote ( <i>"</i> ) is set as default
     */
    public char getQuote() { return quote.charAt(0); }

    /**
     * Se the quotes to use in CSV parsing/formatting
     * @param quote The character to use as quotes
     */
    public void setQuote(char quote) { this.quote = String.valueOf(quote); }

    /**
     * If true:
     * <ul>
     *     <li><b>On parsing:</b> The CSV file will be read with the assumption that the first row contains the columns names</li>
     *     <li><b>On formatting:</b> The generated CSV will have the columns names on the first row</li>
     * </ul>
     *
     * @return True if the assumption is that the first row of the CSV file contains ths columns names
     */
    public boolean isUseHeader() { return useHeader; }

    /**
     * Set if the assumption is that the first row of the CSV file contains the columns names
     *
     * @param useHeader
     */
    public void setUseHeader(boolean useHeader) { this.useHeader = useHeader; }

    /**
     * If true:
     * <ul>
     *   <li><b>On parsing:</b> The CSV file will be read with the assumption that every cell contains quotes, even if not necessary</li>
     *   <li><b>On formatting:</b> The generated CSV will use quotes on every cell, even if not necessary</li>
     * </ul>
     *
     * @return True if the assumption is that the CSV file contains quotes on every cell
     */
    public boolean isAlwaysUseQuotes() { return alwaysUseQuotes; }

    /**
     * Set if the assumption is that the CSV file contains quotes on every cells
     *
     * @param alwaysUseQuotes
     */
    public void setAlwaysUseQuotes(boolean alwaysUseQuotes) { this.alwaysUseQuotes = alwaysUseQuotes; }


    public abstract CSVUtil lineSeparator(LineSeparator lineSeparator);
    public abstract CSVUtil delimiter(char delimiter);
    public abstract CSVUtil quote(char quote);
    public abstract CSVUtil useHeader(boolean useHeader);
    public abstract CSVUtil alwaysUseQuotes(boolean alwaysUseQuotes);

}
