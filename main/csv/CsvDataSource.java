package csv;

import java.util.List;

public class CsvDataSource {

  private List<List<String>> dataset;
  private String filepath;
  private boolean hasHeader;

  public CsvDataSource(){

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

}
