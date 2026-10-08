package org.sparkyware;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.cli.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
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
        tableRows = new ArrayList<>();
    }

    public TspFundPrices(URL url) {
        tableRows = new ArrayList<>();

        LOGGER.log(Level.INFO, "Connecting to TSP website for prices...");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");

        WebDriverManager.chromedriver().setup();

        driver = new ChromeDriver(options);
        driver.get(url.toString());

        String title = driver.getTitle();
        LOGGER.log(Level.INFO, "Successfully loaded TSP page: " + title);

        // Wait for browser to load dynamic content
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement sharePricesChartContent = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("share-prices-chart-lifecycle-content")));

        //.until(
        //ExpectedConditions.presenceOfElementLocated(By.id("share-prices-chart-lifecycle-content")));
        LOGGER.log(Level.INFO, "Dynamic table is present");

        /*
         * Big changes here. Need to find the tsp-graph-tabs__button element with id "tab-all-prices" and click it to
         * get the table to load. Then wait for the table to be present.
         */
        WebElement tabAllPricesClickable = wait.until(ExpectedConditions.elementToBeClickable(By.id("tab-all-prices")));
        tabAllPricesClickable.click();
        WebElement panelAllPricesElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[@id=\"share-prices-data-table-all\"]/table/tbody")));

        // Pull cells from thead/tr elements - these are the column headings
        headerRowElements = (ArrayList<WebElement>) driver.findElements(
                By.xpath("//*[@id=\"share-prices-data-table-all\"]/table/thead/tr"));
        LOGGER.log(Level.INFO, "Found " + headerRowElements.size() + " 'tr' elements in thead");

        // Extract elements from tbody - these are the prices
        bodyRowElements = (ArrayList<WebElement>) driver.findElements(
                By.xpath("//*[@id=\"share-prices-data-table-all\"]/table/tbody/tr"));
        LOGGER.log(Level.INFO, "Found " + bodyRowElements.size() + " 'tr' elements in tbody");

        // Transform original web page table into array of TableRow objects
        tableRows = genTableRowList();
    }

    public static void main(String[] args) throws IOException, ParseException, URISyntaxException {

        HashMap<String, String> fundSymbols = new HashMap<>();
        fundSymbols.put("C Fund", "*CFXX");
        fundSymbols.put("G Fund", "*GFXX");
        fundSymbols.put("F Fund", "*FFXX");
        fundSymbols.put("I Fund", "*IFXX");
        fundSymbols.put("S Fund", "*SFXX");

        // Set up command line options and parsing
        Options options = new Options();
        options.addOption("f", true, "Input CSV file");
        options.addOption("b", false, "Bulk mode - create consolidated CSV file of all funds");
        options.addOption("h", false, "Help");
        options.addOption("v", false, "Verbose logging");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        if (cmd.hasOption("v")) {
            System.out.println("setting Logger level to FINE");
            LOGGER.setLevel(Level.FINE);
        }
        else {
            LOGGER.setLevel(Level.INFO);
        }

        if (cmd.hasOption("h")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("TspFundPrices", options);
            System.exit(0);
        }

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
            URL siteUrl = new URI("https", "www.tsp.gov/share-price-history", null).toURL();
            LOGGER.log(Level.INFO, "Loading fund prices from website: " + siteUrl);
            priceGrabber = new TspFundPrices(siteUrl);
        }

        // Get list of all fund names in the prices retrieved from the site
        List<String> fundNames = priceGrabber.getFundNames();

        // Prepare to generate bulk output
        StringBuilder bulkSb = new StringBuilder();
        bulkSb.append("Symbol,Date,Close,Low,High,Volume\n");

        // For each fund, generate a string of prices with one line per daily price
        for (String aFund : fundNames) {
            ArrayList<TableRow> fundPriceRows;
            System.out.println(aFund);
            fundPriceRows = priceGrabber.getSingleFundTable(aFund);

            // Iterate through prices for a single fund, generate output string
            StringBuilder sb = new StringBuilder();
            // System.out.println(tableRow.toCSV());
            fundPriceRows.forEach(tableRow -> {
                sb.append(tableRow.toCSV(null)).append("\n");
                if (fundSymbols.containsKey(aFund)) {
                    bulkSb.append(fundSymbols.get(aFund)).append(",").append(tableRow.toCSV(aFund)).append("\n");
                }
            });


            // Write the fund's prices to CSV file
            Writer writer = new FileWriter(aFund + ".csv");
            writer.append("Date,Close,Low,High,Volume\n");
            writer.append(sb);
            writer.close();
        }

        priceGrabber.shutdownDriver();

        Writer bulkWriter = new FileWriter("TSP-All-Funds.csv");
        bulkWriter.append(bulkSb);
        bulkWriter.close();
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
    private ArrayList<TableRow> genTableRowList() {

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

    /**
     * Generate list of share price TableRow Objects for a single fund. The specific fund is
     * identified by the aFund parameter, which is the fund name as found in the
     * first row of the full price table.
     * <p>
     * Each single fund row has values: date, fund price, 0, 0, 0
     *
     * @param aFund Name of the fund for which to generate to the table
     * @return List of TableRow objects representing share prices for a single fund
     */
    private ArrayList<TableRow> getSingleFundTable(String aFund) {

        ArrayList<TableRow> fundTableRows;

        // Find aFund in the first row to get its index
        TableRow firstRow = tableRows.get(0);

        int fundColumnIndex;   // Column Index of the fund in the full price table
        for (fundColumnIndex = 0; fundColumnIndex < firstRow.getValueStrings().size(); fundColumnIndex++) {
            String fundName = firstRow.getValueStrings().get(fundColumnIndex).trim();
            if (fundName.equals(aFund)) {
                break;
            }
        }

        fundTableRows = getSingleFundTable(fundColumnIndex);

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

        // From each multi-fund price row, pull the price from colNum and populate a new
        // output row
        for (TableRow tableRow : tableRows) {

            ArrayList<String> valueStrings = tableRow.getValueStrings();

            // Skip rows starting with "Date"
            if (valueStrings.get(0).matches("[Dd]ate") || valueStrings.get(0).isEmpty()) {
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

}
