package csv;

import java.util.Collections;
import java.util.List;

public class CSVDataSource {

  private List<List<String>> dataset;
  private List<List<String>> public_dataset = null; // proxy with unmodifiable view
  private String filepath;
  private boolean hasHeader;
  private boolean loaded;
  private String[] headerCol;

  public CSVDataSource(){
    this.loaded = false;
  }

  /**
   * TODO: maybe make this return unmodifyable copy?
   * @return
   */
  public List<List<String>> getDataset(){
    if (public_dataset == null) {
      public_dataset = Collections.unmodifiableList(dataset);
    }
    return this.public_dataset;
  }

  public void loadFilepath(){

  }

  public void setHasHeader(Boolean hasHeader){
    this.hasHeader = hasHeader;
  }

  public void setDataset(List<List<String>> data){
    this.dataset = data;
    this.loaded = true;
  }

  public boolean hasHeader(){
    return this.hasHeader;
  }
  public boolean isLoaded() {
    return this.loaded;
  }
  public void setHeaderCol(String[] header){
    this.headerCol = header;
  }

  public String[] getHeaderCol(){
    return this.headerCol;
  }

}
