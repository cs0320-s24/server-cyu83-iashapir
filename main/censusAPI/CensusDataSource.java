package censusAPI;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import okio.Buffer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 * https://api.census.gov/data/2021/acs/acs1/subject/variables?get=NAME,S2802_C03_022E&
 *
 * https://api.census.gov/data/2010/dec/sf1?get=NAME&for=state:*
 */
public class CensusDataSource implements DataSource {
    private Map<String, String> stateCodes;
  private List<List<String>> listStateCodes;
  private List<List<String>> countyCodes;

    /**
     * constructor - fills in the stateCodes HashMap
     */
  public CensusDataSource() {
      List<List<String>> stateCodesList = List.of(List.of("Alabama","01"),
              List.of("Alaska","02"),
              List.of("Arizona","04"),
              List.of("Arkansas","05"),
              List.of("California","06"),
              List.of("Louisiana","22"),
              List.of("Kentucky","21"),
              List.of("Colorado","08"),
              List.of("Connecticut","09"),
              List.of("Delaware","10"),
              List.of("District of Columbia","11"),
              List.of("Florida","12"),
              List.of("Georgia","13"),
              List.of("Hawaii","15"),
              List.of("Idaho","16"),
              List.of("Illinois","17"),
              List.of("Indiana","18"),
              List.of("Iowa","19"),
              List.of("Kansas","20"),
              List.of("Maine","23"),
              List.of("Maryland","24"),
              List.of("Massachusetts","25"),
              List.of("Michigan","26"),
              List.of("Minnesota","27"),
              List.of("Mississippi","28"),
              List.of("Missouri","29"),
              List.of("Montana","30"),
              List.of("Nebraska","31"),
              List.of("Nevada","32"),
              List.of("New Hampshire","33"),
              List.of("New Jersey","34"),
              List.of("New Mexico","35"),
              List.of("New York","36"),
              List.of("North Carolina","37"),
              List.of("North Dakota","38"),
              List.of("Ohio","39"),
              List.of("Oklahoma","40"),
              List.of("Oregon","41"),
              List.of("Pennsylvania","42"),
              List.of("Rhode Island","44"),
              List.of("South Carolina","45"),
              List.of("South Dakota","46"),
              List.of("Tennessee","47"),
              List.of("Texas","48"),
              List.of("Utah","49"),
              List.of("Vermont","50"),
              List.of("Virginia","51"),
              List.of("Washington","53"),
              List.of("West Virginia","54"),
              List.of("Wisconsin","55"),
              List.of("Wyoming","56"),
              List.of("Puerto Rico","72"));
       this.stateCodes = new HashMap<>();
        for (List<String> state: stateCodesList) {
            this.stateCodes.put(state.get(0), state.get(1));
        }
  }

 // @Override
  //public String getData(StateAndCounty sc) throws DatasourceException {
 public String getData(StateAndCounty sc) throws DatasourceException, IllegalArgumentException{

      if(!(this.stateCodes.containsKey(sc.stateName()))){
        throw new IllegalArgumentException("State: '" + sc.stateName() + "' not valid.");
      }
      //get state code from our internally stored hashmap
      String stateCode = this.stateCodes.get(sc.stateName());
      String countyCode = this.findCountyCode(stateCode, sc.countyName());

      try {
          // build request to get broadband data from census api for given state and county
          URL requestURL = new URL("https","api.census.gov",
                  "/data/2021/acs/acs1/subject/variables?get=NAME,S2802_C03_022E&for=county:"+countyCode+"&in=state:"+stateCode);
          HttpURLConnection clientConnection = connect(requestURL);
          Moshi moshi = new Moshi.Builder().build();
          Type listListString = Types.newParameterizedType(List.class, List.class, String.class);
          JsonAdapter<List<List<String>>> adapter = moshi.adapter(listListString);

          List<List<String>> body = adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

          clientConnection.disconnect();

          // validity checks for response
          if (body == null || body.size() != 2 || body.get(0).size() != 4) {
              throw new DatasourceException("Malformed response from Census API");
          }

          return body.get(1).get(1); // get broadband data
      } catch (IOException e) {
          throw new DatasourceException(e.getMessage(), e);
      }
  }


    /**
     * Method to find the countyCode given a county name
     * @return
     */
    public String findCountyCode(String stateCode, String countyName) throws IllegalArgumentException, DatasourceException{
        if(this.countyCodes==null || !this.countyCodes.get(1).get(2).equals(stateCode)) { // countyCodes saved but not the right state
            try {
                this.requestCountyCodes(stateCode);
            } catch (IOException e) {
                System.out.println("caught IO exception from request");
                throw new DatasourceException(e.getMessage());
            }
        }

        System.out.println("trying to get stateName from hashmap");
        for (int i = 1; i < countyCodes.size(); i++) {
          String countyAndState = countyCodes.get(i).get(0);
          if (countyAndState.substring(0, countyAndState.indexOf(",")).equals(countyName)) { // parse string "county, state" to just county
            return countyCodes.get(i).get(2);
          }
        }

        //got through counties and didn't find the one that was inputted
        throw new IllegalArgumentException("County: '" + countyName + "' not valid.");
    }

    /**
     * requests countyCodes from census
     * calls deserialize to turn them into hashmap
     * stores that hashmap as instance variable
     * @return
     * @throws URISyntaxException
     * @throws IOException
     * @throws InterruptedException
     */
    private void requestCountyCodes(String stateCode) throws IOException, DatasourceException {
        //builds request to census for state code data
        URL requestURL =
                new URL("https", "api.census.gov", "/data/2010/dec/sf1?get=NAME&for=county:*&in=state:"+stateCode);
        HttpURLConnection clientConnection = connect(requestURL);
        Moshi moshi = new Moshi.Builder().build();
        Type listListString = Types.newParameterizedType(List.class, List.class, String.class);
        JsonAdapter<List<List<String>>> adapter = moshi.adapter(listListString);
        try {
            List<List<String>> body =
                    adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
            clientConnection.disconnect();
            if (body == null) {
                throw new DatasourceException("Malformed response from Census API");
            }
            //this.stateCodes = body;
            System.out.println(body);
            this.countyCodes = body;
        } catch (Exception e) { //changed to Exception for debugging
            System.out.println(e.getMessage());
        }
    }

  /**
   * Private helper method; throws IOException so different callers
   * can handle differently if needed.
   */
  private static HttpURLConnection connect(URL requestURL) throws DatasourceException, IOException {
    URLConnection urlConnection = requestURL.openConnection();
    if(! (urlConnection instanceof HttpURLConnection))
      throw new DatasourceException("unexpected: result of connection wasn't HTTP");
    HttpURLConnection clientConnection = (HttpURLConnection) urlConnection;
    clientConnection.connect(); // GET
    if(clientConnection.getResponseCode() != 200)
      throw new DatasourceException("unexpected: API connection not success status "+clientConnection.getResponseMessage());
    return clientConnection;
  }


}
