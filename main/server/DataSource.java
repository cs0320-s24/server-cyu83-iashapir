package server;

public interface DataSource {
  String getData(StateAndCounty stateAndCounty) throws DatasourceException;
}
