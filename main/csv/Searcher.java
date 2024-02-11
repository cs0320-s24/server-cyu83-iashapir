package csv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Searcher Class: Searches inputted dataset for inputted value based on whether header column was
 * included in parsed data Searches by column when column name/index is provided
 */
public class Searcher {

  private HashMap<String, Integer> colsToIndex;
  private CSVParser parser;
  private List<List<String>> dataset;
  private Boolean hasHeader;
  private HashSet<Integer> rowMatchInts;

  public Searcher(List<List<String>> dataset, CSVParser parser, Boolean hasHeader) {
    this.parser = parser;
    this.dataset = dataset;
    this.hasHeader = hasHeader;
    // instantiate regardless of header presence in case user puts in bad input (col name when not
    // there)
    this.colsToIndex = new HashMap<>();
    if (hasHeader) {
      this.populateColToIndex();
    }
  }

  /**
   * Searches when header (either name or index) is provided
   *
   * @param searchTerm what you want to search for
   * @param header String column to search
   * @param isString true if header is a column name (not index)
   */
  public void search(String searchTerm, String header, Boolean isString) {

    int col_index = 0;

    // in this inputted header is a number/index
    if (!isString) {
      col_index = Integer.parseInt(header);
      if (col_index > this.findMaxRowLength() - 1) {
        System.err.println(
            "sorry, your inputted column index is too large. your dataset has "
                + this.findMaxRowLength()
                + " columns max.");
        return;
      }
    }
    // in this case it is a word/column header name
    else {
      // remove any malformed rows from the dataset that could affect search results (if header
      // exists)
      if (this.hasHeader) {
        this.removeMalformedRows();
      }
      if (this.colsToIndex.containsKey(header)) {
        col_index = this.colsToIndex.get(header);
      } else {
        System.err.println("sorry, inputted header: " + header + " not found in data");
        return;
      }
    }

    // use col_index to find rows that have searchTerm
    this.rowMatchInts = new HashSet<>();
    for (int i = 0; i < this.dataset.size(); i++) {
      // data is not "clean" so could have some rows that are too long/too short
      try {
        if (this.dataset.get(i).get(col_index).equals(searchTerm)) {
          this.rowMatchInts.add(i);
        }
      } catch (IndexOutOfBoundsException e) {
        // do nothing if we have one...just move to next row
      }
    }
    this.printRows(searchTerm);
  }

  /**
   * Performs a search when the column name/index is not provided
   *
   * @param searchTerm
   */
  public void search(String searchTerm) {
    this.rowMatchInts = new HashSet<>();
    // will not have an index out of bounds exception here since each loop is specific
    // loop through each row of dataset
    for (int i = 0; i < this.dataset.size(); i++) {

      for (int j = 0; j < this.dataset.get(i).size(); j++) {
        if (this.dataset.get(i).get(j).equals(searchTerm)) {
          this.rowMatchInts.add(i);
          break;
        }
      }
    }
    this.printRows(searchTerm);
  }

  /** Uses hashset of row matched row indices to print all rows with searchTerm */
  private void printRows(String searchTerm) {
    System.out.println("----------------------------------");
    System.out.println("results:");
    if (this.rowMatchInts.isEmpty()) {
      System.out.println("Sorry, the value " + searchTerm + " could not be located.");
    } else {
      for (int row : this.rowMatchInts) {
        System.out.print("Row " + row + ": ");
        for (int col = 0; col < this.dataset.get(row).size(); col++) {
          System.out.print(this.dataset.get(row).get(col) + " ");
        }
        System.out.println("");
      }
    }
  }

  /**
   * FOR TESTING PURPOSES ONLY returns a List<List<String>> corresponding to the searchTerm matches
   */
  public List<List<String>> getRowMatches() {
    List<List<String>> rowMatches = new ArrayList<List<String>>();
    if (this.rowMatchInts != null) {
      for (int row : this.rowMatchInts) {
        rowMatches.add(this.dataset.get(row));
      }
    }
    return rowMatches;
  }

  /**
   * Uses the header column parsed in CSVParser to populate our Col Name: Col Index hashmap This
   * function is only called if the CSV has a Header row
   */
  private void populateColToIndex() {
    String[] headerCol = this.parser.getHeaderCol();

    for (int i = 0; i < headerCol.length; i++) {
      // may need to check if it's already in there
      if (this.colsToIndex.containsKey(headerCol[i])) {
        System.out.println(
            "WARNING: duplicate column called "
                + headerCol[i]
                + " detected. For optimal search"
                + "results, we recommend using column index instead of name when searching this column.");
      }
      this.colsToIndex.put(headerCol[i], i);
    }
  }

  /**
   * removes malformed rows (rows with lengths different than that of the header) from the dataset
   * and tells the user which row number (and row) was removed
   */
  private void removeMalformedRows() {
    for (int i = this.dataset.size() - 1; i >= 0; i--) {
      if (this.dataset.get(i).size() != this.parser.getHeaderCol().length) {
        System.out.println(
            "WARNING: row "
                + i
                + " has been removed from dataset prior to search "
                + "because it does not have the correct number of entries");
        System.out.println("Removed row: " + this.dataset.get(i));
        this.dataset.remove(this.dataset.get(i));
      }
    }
  }

  /**
   * Returns length of longest row in dataset
   *
   * @return
   */
  private int findMaxRowLength() {
    int maxRowLength = 0;
    for (int i = 0; i < this.dataset.size(); i++) {
      if (this.dataset.get(i).size() >= maxRowLength) {
        maxRowLength = this.dataset.get(i).size();
      }
    }
    return maxRowLength;
  }
}
