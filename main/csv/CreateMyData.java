package csv;

import java.util.List;

/**
 * Class used when parsing csvs into List objects for searching Throws no exceptions Does nothing
 * interesting
 */
public class CreateMyData implements CreatorFromRow<List<String>> {
  public CreateMyData() {}

  /**
   * @param row
   * @return row, unchanged
   * @throws FactoryFailureException
   */
  @Override
  public List<String> create(List<String> row) throws FactoryFailureException {
    return row;
  }
}
