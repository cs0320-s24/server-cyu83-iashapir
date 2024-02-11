package csv;

import com.squareup.moshi.Moshi;

import java.util.Map;

public record CSVFailureResponse(String response_type, Map<String, Object> responseMap) {

  public CSVFailureResponse(Map<String, Object> responseMap) {
    this("error", responseMap);
  }

  /**
   * @return this response, serialized as Json
   */
  public String serialize() {
    Moshi moshi = new Moshi.Builder().build();
    return moshi.adapter(CSVFailureResponse.class).toJson(this);
  }
}

