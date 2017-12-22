package org.sparkyware;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

import org.jsoup.nodes.Element;

public class TableRow {

    private ArrayList<String> valueStrings = null;

    private Element rowElement;

    public TableRow(Element aRow) {
	this.rowElement = aRow;
	populate();
    }

    /**
     * Construct a TableRow based on a comma-separated string
     * 
     * @param csvString
     */
    public TableRow(String csvString) {
	valueStrings = new ArrayList<String>();

	for (String cellValue : csvString.split(",")) {
	    // Remove spaces at beginning of a cell value
	    cellValue = cellValue.replaceAll("^ ", "");
	    valueStrings.add(cellValue);
//	    System.out.println(cellValue);
	}

    }

    /**
     * Construct a TableRow with a specific set of string values.
     */
    public TableRow(String string, String string2, String string3, String string4, String string5) {
	valueStrings = new ArrayList<String>();
	valueStrings.add(string);
	valueStrings.add(string2);
	valueStrings.add(string3);
	valueStrings.add(string4);
	valueStrings.add(string5);
    }

    /**
     * Populate a TableRow object from the Document Element objects.
     */
    private void populate() {
	valueStrings = new ArrayList<String>();

	for (Element aRowCell : rowElement.getAllElements()) {
	    // System.out.println(aRowCell.text());
	    valueStrings.add(aRowCell.text());
	}
	// This is a hack, removing the outermost tbody element.
	// FIXME should resolve this in the original document parsing, not here
	valueStrings.remove(0);
    }

    /**
     * Return the values of each column of this row as an array of Strings.
     * 
     * @return cell value array
     */
    public ArrayList<String> getValueStrings() {
	return valueStrings;
    }

    /**
     * Return this TableRow represented as comma-separated values.
     * 
     * @return Comma-separated value representation
     */
    public String toCSV() {
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, uuuu");
	StringBuffer buffer = new StringBuffer();
	for (String value : valueStrings) {

	    // Change date string from "Mmm dd, YYYY" to "YYYY-mm-dd"
	    // FIXME not the right place to do this
	    try {
		LocalDate date = LocalDate.parse(value, formatter);
		value = date.format(DateTimeFormatter.ISO_DATE);
	    } catch (DateTimeParseException e) {
		// e.printStackTrace();
	    }

	    buffer.append(value).append(",");
	}

	// Remove final comma
	buffer.deleteCharAt(buffer.length() - 1);

	return buffer.toString();
    }

    public void dumpValues() {
	for (String cellValue : this.valueStrings) {
	    System.out.println(cellValue);
	}

    }

}
