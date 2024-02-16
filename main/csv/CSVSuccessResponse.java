package csv;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.util.Map;

/**
 * response object to send when no errors occurred
 * @param response_type the type of response, in this case a success
 * @param responseMap the response map for any other data to show the user
 */
public record CSVSuccessResponse(String response_type, Map<String, Object> responseMap) {

  /**
   * constructor for a CSVSuccessResponse
   * @param responseMap the responseMap to put in the failure response
   */
  public CSVSuccessResponse(Map<String, Object> responseMap) {
    this("success", responseMap);
  }

  /**
   * helper to serialize java into json
   * @return this response, serialized as Json
   */
  public String serialize() {
    try {
      // Initialize Moshi which takes in this class and returns it as JSON!
      Moshi moshi = new Moshi.Builder().build();
      JsonAdapter<CSVSuccessResponse> adapter = moshi.adapter(CSVSuccessResponse.class);
      return adapter.toJson(this);
    } catch (Exception e) {
      // For debugging purposes, show in the console _why_ this fails
      // Otherwise we'll just get an error 500 from the API in integration
      // testing.
      e.printStackTrace();
      throw e;
    }
  }
}
