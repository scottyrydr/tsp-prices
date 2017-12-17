package org.sparkyware;

import java.util.ArrayList;

import org.jsoup.nodes.Element;

public class TableRow {

    private ArrayList<String> valueStrings = null;

    private Element rowElement;

    public TableRow(Element aRow) {
	this.rowElement = aRow;
	populate();
    }

    private void populate() {
	valueStrings = new ArrayList<String>();

	for (Element aRowCell : rowElement.getAllElements()) {
	    // System.out.println(aRowCell.text());
	    valueStrings.add(aRowCell.text());
	}
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
	StringBuffer buffer = new StringBuffer();
	for (String value : valueStrings) {
	    buffer.append(value).append(",");
	}
	buffer.deleteCharAt(buffer.length() - 1);
	return buffer.toString();
    }

    public void dumpValues() {
	for (String cellValue : this.valueStrings) {
	    System.out.println(cellValue);
	}

    }

}
