package csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * FUNCTIONALITY - "DEVELOPERS" want to use this - we don't know how they'll want to use it or what
 * their data will look like - returning list: maybe return arraylist?
 *
 * <p>- CSV parser itself should be printing to how to handle errors?
 *
 * <p>- this class SHOULD deal with headers because you don't want to end up with a Person class
 * that stores rows as Name: Name, Value: Value
 */
public class CSVParser<T> implements Iterable<T>{

  static final Pattern regexSplitCSVRow =
      Pattern.compile(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*(?![^\\\"]*\\\"))");
  private Reader reader;
  private CreatorFromRow<T> creator;
  private Boolean hasHeader;
  private String[] headerCol;

  /**
   * @param reader passed in from Searcher class i guess
   * @param creator CreatorFromRow object that we call create on
   * @param hasHeader indicates whether the data has a header column
   */
  public CSVParser(Reader reader, CreatorFromRow<T> creator, Boolean hasHeader) {
    this.reader = reader;
    this.creator = creator;
    this.hasHeader = hasHeader;
  }

  @Override
  public Iterator<T> iterator(){
    try{
      return this.parse().iterator();
    }
    catch(FactoryFailureException f){

    }
    catch(IOException e){

    }
    return null;
  }

  /**
   * catch everything within the while loop so the code can keep running after error! - basically we
   * want a method to print/skip the row/then keep parsing
   *
   * @return parser can just throw FactoryFailure too
   */
  public List<T> parse() throws IOException, FactoryFailureException {

    String line;
    BufferedReader bReader = new BufferedReader(reader);
    List<T> dataset = new ArrayList<T>();

    // if there is a header, parse first row into separate data structure
    if (this.hasHeader) {
      line = bReader.readLine();
      this.headerCol = this.regexSplitCSVRow.split(line);
    }

    // this line will be first or second row depending on whether header was indicated
    line = bReader.readLine();
    while (line != null) {
      String[] result = this.regexSplitCSVRow.split(line);
      // this adds each T to our internal dataset -> might throw FF Exception
      T row = this.creator.create(Arrays.asList(result));
      dataset.add(row);
      line = bReader.readLine();
    }

    bReader.close();

    return dataset;
  }

  /**
   * maybe throw an error here if headerCol is null? (i.e. there's no header?)
   *
   * @return
   */
  public String[] getHeaderCol() {
    return this.headerCol;
  }
}
