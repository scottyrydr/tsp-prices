package org.sparkyware;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * @author scott
 *
 */
public class TspFundPrices {

    /**
     * Element holding the share price table details
     */
    private Element priceTable;

    public TspFundPrices(String urlStr) throws IOException {
	super();

	System.out.println("Connecting to TSP website for prices...");
	Document doc;
	doc = Jsoup.connect(urlStr).get();

	String title = doc.title();
	System.out.println("Successfully loaded TSP page: " + title);

	priceTable = doc.select("tbody").get(1);
    }

    /**
     * Iterate through the web page elements and construct direct representation of
     * the pricing table.
     * 
     * @return List of TableRow objects representing original web page pricing
     *         table.
     */
    public ArrayList<TableRow> genFullPriceTables() {
	ArrayList<TableRow> tableRows = new ArrayList<TableRow>();

	for (Iterator<Element> iterator = this.priceTable.getElementsByTag("tr").iterator(); iterator.hasNext();) {
	    Element anElement = (Element) iterator.next();
	    // System.out.println("anElement: " + anElement.text());
	    TableRow aTableRow = new TableRow(anElement);
	    tableRows.add(aTableRow);
	    System.out.println(aTableRow.toCSV());
	}
	return tableRows;
    }

    public ArrayList<TableRow> genSingleFundTable(ArrayList<TableRow> tableRows) {
	/*
	 * Now generate sequence of rows representing individual fund prices. Each row
	 * is a different day. For example:
	 * 
	 * Date,Close,Low,High,Volume 2017-12-01,37.1238,0,0,0 2017-11-30,37.1975,0,0,0
	 * 2017-11-29,36.8818,0,0,0
	 */

	ArrayList<TableRow> fundPriceRows = new ArrayList<TableRow>();

	TableRow firstRow = new TableRow("Date", "Close", "Low", "High", "Volume");
	fundPriceRows.add(firstRow);

	for (Iterator<TableRow> iterator = tableRows.iterator(); iterator.hasNext();) {

	    TableRow tableRow = (TableRow) iterator.next();
	    ArrayList<String> valueStrings = tableRow.getValueStrings();

	    // Skip the first row - starts with "Date"
	    if (valueStrings.get(0).contains("Date")) {
		continue;
	    }

	    TableRow secondRow = new TableRow(valueStrings.get(0), valueStrings.get(8), Integer.toString(0),
		    Integer.toString(0), Integer.toString(0));
	    fundPriceRows.add(secondRow);
	}
	return fundPriceRows;
    }

    public static void main(String[] args) throws IOException {

	TspFundPrices priceGrabber = new TspFundPrices(
		"https://www.tsp.gov/InvestmentFunds/FundPerformance/index.html");

	// Transform original web page table into array of TableRow objects
	ArrayList<TableRow> tableRows = priceGrabber.genFullPriceTables();

	// Generate table of prices for a single fund
	ArrayList<TableRow> fundPriceRows = priceGrabber.genSingleFundTable(tableRows);

	for (TableRow tableRow : fundPriceRows) {
	    System.out.println(tableRow.toCSV());
	}

    }

}
