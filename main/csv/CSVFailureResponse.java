package csv;

import com.squareup.moshi.Moshi;

import java.util.Map;

/**
 * response object to send if an error occurred somewhere
 * @param response_type the type of response, in this case an error
 * @param responseMap the response map for any other data to show the user
 */
public record CSVFailureResponse(String response_type, Map<String, Object> responseMap) {

  /**
   * constructor for a CSVFailureResponse
   * @param responseMap the responseMap to put in the failure response
   */
  public CSVFailureResponse(Map<String, Object> responseMap) {
    this("error", responseMap);
  }

  /**
   * helper to serialize java into json
   * @return this response, serialized as Json
   */
  public String serialize() {
    Moshi moshi = new Moshi.Builder().build();
    return moshi.adapter(CSVFailureResponse.class).toJson(this);
  }
}

