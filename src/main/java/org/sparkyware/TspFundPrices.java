package org.sparkyware;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.cli.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * @author scott
 */
public class TspFundPrices {

    private final static Logger LOGGER = Logger.getLogger(TspFundPrices.class.getName());

    /**
     * Element holding the share price table header row
     */
    private ArrayList<WebElement> headerRowElements;

    /**
     * Share price body row elements
     */
    private ArrayList<WebElement> bodyRowElements;
    /**
     * Share price row values, internal representation
     */
    private ArrayList<TableRow> tableRows;
    private WebDriver driver;

    public TspFundPrices() {
        LOGGER.setLevel(Level.INFO);
        tableRows = new ArrayList<>();
    }

    public TspFundPrices(URL url) throws IOException {
        LOGGER.setLevel(Level.INFO);
        tableRows = new ArrayList<>();

        LOGGER.log(Level.INFO, "Connecting to TSP website for prices...");
        WebDriverManager.chromedriver().setup();

        driver = new ChromeDriver();
        driver.get(url.toString());

        String title = driver.getTitle();
        LOGGER.log(Level.INFO, "Successfully loaded TSP page: " + title);

        // Pull cells from thead/tr elements - these are the column headings
        headerRowElements = (ArrayList<WebElement>) driver.findElements(By.xpath("//thead/tr"));
        LOGGER.log(Level.INFO, "Found " + headerRowElements.size() + " 'tr' elements in thead");

        // Extract elements from tbody - these are the prices
        bodyRowElements = (ArrayList<WebElement>) driver.findElements(By.xpath("//tbody/tr"));
        LOGGER.log(Level.INFO, "Found " + bodyRowElements.size() + " 'tr' elements in tbody");

        // Transform original web page table into array of TableRow objects
        tableRows = genFullPriceTables();
    }

    private void shutdownDriver() {
        if (driver != null) {
            driver.quit();
        }
    }

    /**
     * Load a CSV file of fund prices and populate a collection of TableRow objects.
     *
     * @param csvFileName CSV File name
     * @return
     */
    public void loadCsvPrices(String csvFileName) {

        LOGGER.log(Level.INFO, "Loading CSV File: {0}", csvFileName);
        try {
            Stream<String> stream = Files.lines(Paths.get(csvFileName));
            for (Iterator<String> iterator = stream.iterator(); iterator.hasNext(); ) {
                String strLine = iterator.next();
                // Remove spaces at end of line
                strLine = strLine.replaceAll(" $", "");
                LOGGER.log(Level.INFO, strLine);
                // Create corresponding TableRow and add to collection
                tableRows.add(new TableRow(strLine));
            }
            stream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            LOGGER.log(Level.WARNING, "Unable to find the file: {0}", csvFileName);
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.log(Level.WARNING, "Unable to read the file: {0}", csvFileName);
        }

    }

    /**
     * Iterate through the WebElement elements and construct direct representation of
     * the pricing table.
     *
     * @return List of TableRow objects representing original web page pricing
     * table.
     */
    private ArrayList<TableRow> genFullPriceTables() {

        for (WebElement anElement : this.headerRowElements) {
            LOGGER.log(Level.FINE, "Raw row from table: {0}", anElement.getText());
            TableRow aTableRow = new TableRow(anElement, "th");
            tableRows.add(aTableRow);
        }

        for (WebElement anElement : this.bodyRowElements) {
            LOGGER.log(Level.FINE, "Raw row from table: {0}", anElement.getText());
            TableRow aTableRow = new TableRow(anElement, "th|td");
            tableRows.add(aTableRow);
        }
        return tableRows;
    }

    /**
     * Return list of all fund names in the fund price table
     *
     * @return All fund names found in the fund price table
     */
    public List<String> getFundNames() {

        List<String> fundNames = new ArrayList<>();

        // Find row that has "[Dd]ate" as first value
        TableRow firstRow = tableRows.get(1);
        for (TableRow tableRow : tableRows) {
            if (tableRow.getValueStrings().get(0).matches("[Dd]ate")) {
                firstRow = tableRow;
                break;
            }
        }

        // Iterate through values in this row, skipping "[Dd]ate". Values are the fund
        // names
        for (String aFundName : firstRow.getValueStrings()) {
            if (aFundName.matches("[Dd]ate")) {
                continue;
            }
            fundNames.add(aFundName.trim());
        }

        return fundNames;
    }

    private ArrayList<TableRow> getSingleFundTable(String aFund) {

        ArrayList<TableRow> fundTableRows;

        // Find aFund in the first row to get its index
        TableRow firstRow = tableRows.get(0);

        int fundIndex;
        for (fundIndex = 0; fundIndex < firstRow.getValueStrings().size(); fundIndex++) {
            String fundName = firstRow.getValueStrings().get(fundIndex).trim();
            if (fundName.equals(aFund)) {
                break;
            }
        }

        fundTableRows = getSingleFundTable(fundIndex);

        return fundTableRows;
    }

    /**
     * Generate table of share prices for a single fund. The specific fund is
     * identified by the colNum parameter, identifying the column number in the full
     * price table.
     * <p>
     * Each single fund row has values: date, fund price, 0, 0, 0
     *
     * @param colNum Chooses the fund for which to generate to the table
     * @return Table of share prices for a single fund over time
     */
    public ArrayList<TableRow> getSingleFundTable(int colNum) {

        ArrayList<TableRow> fundPriceRows = new ArrayList<>();

        TableRow firstRow = new TableRow("Date", "Close", "Low", "High", "Volume");
        fundPriceRows.add(firstRow);

        // From each multi-fund price row, pull the price from colNum and populate a new
        // output row
        for (TableRow tableRow : tableRows) {

            ArrayList<String> valueStrings = tableRow.getValueStrings();

            // Skip rows starting with "Date"
            if (valueStrings.get(0).matches("[Dd]ate") || valueStrings.get(0).length() == 0) {
                continue;
            }

            // Single fund/single day output row is: date value, share price, 0, 0, 0
            // WebElement prices include "$" pre-pended, need to remove that
            TableRow aRow = new TableRow(valueStrings.get(0), valueStrings.get(colNum).replaceAll("^[$]", ""),
                    Integer.toString(0), Integer.toString(0), Integer.toString(0));
            fundPriceRows.add(aRow);
        }
        return fundPriceRows;
    }

    public static void main(String[] args) throws IOException, ParseException {

        // Set up command line options and parsing
        Options options = new Options();
        options.addOption("f", true, "Input CSV file");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        TspFundPrices priceGrabber = new TspFundPrices();

        /*
         https://secure.tsp.gov/components/CORS/getSharePrices.html?Lfunds=0&InvFunds=1&format=CSV&download=1
         Choose source type (file vs URL) and use TspFundPrices methods to load/parse and create
         internal representations of the share prices
        */
        if (cmd.hasOption("f")) {
            LOGGER.log(Level.INFO, "Attempting to load fund prices from file: " + cmd.getOptionValue("f"));
            priceGrabber.loadCsvPrices(cmd.getOptionValue("f"));
        }
        else {
            URL siteUrl = new URL("https", "tsp.gov", "/share-price-history/");
            LOGGER.log(Level.INFO, "Loading fund prices from website: " + siteUrl);
            priceGrabber = new TspFundPrices(siteUrl);
        }

        // Get list of all fund names in the prices retrieved from the site
        List<String> fundNames = priceGrabber.getFundNames();

        // For each fund, generate a string of prices with one line per daily price
        for (String aFund : fundNames) {
            ArrayList<TableRow> fundPriceRows;
            System.out.println(aFund);
            fundPriceRows = priceGrabber.getSingleFundTable(aFund);

            // Iterate through prices for a single fund, generate output string
            StringBuilder sb = new StringBuilder();
            for (TableRow tableRow : fundPriceRows) {
                sb.append(tableRow.toCSV()).append("\n");
                // System.out.println(tableRow.toCSV());
            }

            // Write the fund's prices to CSV file
            Writer writer = new FileWriter(aFund + ".csv");
            writer.append(sb);
            writer.close();
        }

        priceGrabber.shutdownDriver();
    }

}
