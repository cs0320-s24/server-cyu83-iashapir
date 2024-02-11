package csv;

import java.util.List;

public class CSVDataSource {

  private List<List<String>> dataset;
  private String filepath;
  private boolean hasHeader;
  private boolean loaded;

  public CSVDataSource(){
    this.loaded = false;
  }

  /**
   * TODO: maybe make this return unmodifyable copy?
   * @return
   */
  public List<List<String>> getDataset(){
    return this.dataset;
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

  public boolean getHasHeader(){
    return this.hasHeader;
  }
  public boolean isLoaded() {
    return this.loaded;
  }

}
