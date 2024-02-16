package testing;

import censusAPI.DataSource;
import censusAPI.DatasourceException;
import censusAPI.StateAndCounty;

/**
 * Class to represent a fake CensusDataSource that will always return the same result
 * regardless of input
 * Used in tests to limit queries to real Census API Server
 */
public class MockedCensusDataSource implements DataSource {

  private final String constantData;

  public MockedCensusDataSource(String constantData) {
    this.constantData = constantData;
  }

  /**
   * Don't do anything special, just return our constant value
   * @param sc
   * @return
   * @throws DatasourceException
   * @throws IllegalArgumentException
   */
  @Override
  public String getData(StateAndCounty sc) throws DatasourceException, IllegalArgumentException{
    return constantData;
  }
}
