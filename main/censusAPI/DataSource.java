package censusAPI;

/**
 * DataSource interface implemented by our 3 broadband data sources so that we can use them
 * in our BroadbandHandler interchangeably
 */
public interface DataSource {
  String getData(StateAndCounty stateAndCounty) throws DatasourceException, IllegalArgumentException;
}
