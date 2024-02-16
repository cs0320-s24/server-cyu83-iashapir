package testing;

import server.DataSource;
import server.DatasourceException;
import server.StateAndCounty;

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
