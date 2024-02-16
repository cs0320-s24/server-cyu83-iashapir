package censusAPI;

public interface DataSource {
  String getData(StateAndCounty stateAndCounty) throws DatasourceException, IllegalArgumentException;
}
