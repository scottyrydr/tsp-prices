package org.sparkyware;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) throws IOException {
	System.out.println("Hello World!");

	Document doc;
	doc = Jsoup.connect("https://www.tsp.gov/InvestmentFunds/FundPerformance/index.html").get();
	// Document doc;
	// InputStream inputStream =
	// Thread.currentThread().getContextClassLoader().getResourceAsStream("ec.html");
	// String docAsText = new BufferedReader(new
	// InputStreamReader(inputStream)).lines().parallel()
	// .collect(Collectors.joining("\n"));
	// doc = Jsoup.parse(docAsText);

	String title = doc.title();
	System.out.println("Successfully loaded TSP page: " + title);

	Element priceTable = doc.select("tbody").get(1);

	ArrayList<TableRow> tableRows = new ArrayList<TableRow>();
	
	for (Iterator<Element> iterator = priceTable.getElementsByTag("tr").iterator(); iterator.hasNext();) {
	    Element anElement = (Element) iterator.next();
	    System.out.println("anElement: " + anElement.text());
	    TableRow aTableRow = new TableRow(anElement);
	    tableRows.add(aTableRow);
	    aTableRow.dumpValues();
	    System.out.println(aTableRow.toCSV());
	}

    }
}
