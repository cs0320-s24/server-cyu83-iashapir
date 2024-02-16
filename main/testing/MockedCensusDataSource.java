package testing;

import censusAPI.DataSource;
import censusAPI.DatasourceException;
import censusAPI.StateAndCounty;

public class MockedCensusDataSource implements DataSource {

  private final String constantData;

  public MockedCensusDataSource(String constantData) {
    this.constantData = constantData;
  }


  @Override
  public String getData(StateAndCounty sc) throws DatasourceException, IllegalArgumentException{
    return constantData;
  }
}
