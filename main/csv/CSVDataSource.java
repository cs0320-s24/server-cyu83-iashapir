package csv;

import java.util.Collections;
import java.util.List;

/**
 * a CSVDataSource can be used to store the data parsed from a csv file
 */
public class CSVDataSource {

  private List<List<String>> dataset;
  private List<List<String>> public_dataset = null; // proxy with unmodifiable view
  private String filepath;
  private boolean hasHeader;
  private boolean loaded;
  private String[] headerCol;

  /**
   * constructor for a CSVDataSource object
   * always initialize the object as not loaded, as no file has been parsed
   * into the dataset yet
   */
  public CSVDataSource(){
    this.loaded = false;
  }

  /**
   * getter for the dataset
   * @return an unmodifiable copy of the dataset
   */
  public List<List<String>> getDataset(){
    if (public_dataset == null) {
      public_dataset = Collections.unmodifiableList(dataset);
    }
    return this.public_dataset;
  }

  /**
   * setter for the hasHeader field
   * @param hasHeader - a boolean to set hasHeader to
   */
  public void setHasHeader(Boolean hasHeader){
    this.hasHeader = hasHeader;
  }

  /**
   * setter for dataset field. this also sets loaded to true
   * @param data - a list of list of strings of the data to set dataset to
   */
  public void setDataset(List<List<String>> data){
    this.dataset = data;
    this.loaded = true;
  }

  /**
   * getter for the hasHeader field
   * @return a boolean representing if the dataset has a header
   */
  public boolean hasHeader(){
    return this.hasHeader;
  }

  /**
   * getter for loaded field
   * @return boolean representing if the dataset is loaded
   */
  public boolean isLoaded() {
    return this.loaded;
  }

  /**
   * setter for the headerCol field
   * @param header a list of strings representing headers for the dataset
   */
  public void setHeaderCol(String[] header){
    this.headerCol = header;
  }

  /**
   * getter for the headerCol field
   * @return a list of strings representing the headers
   */
  public String[] getHeaderCol(){
    return this.headerCol;
  }

}
