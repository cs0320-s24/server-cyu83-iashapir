package server;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import spark.Request;
import spark.Response;
import spark.Route;

import javax.swing.plaf.nimbus.State;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * API Key: 21456f9080396380fefd692adedb749df756600c
 * https://api.census.gov/data/2021/acs/acs1/subject/variables?get=NAME,S2802_C03_022E&
 * for=county:COUNTY_CODE&in=state:STATE_CODE (* is all)
 *
 * the job of this class is just to get the data, not actually run queries
 */
public class BroadbandHandler implements Route {

	private final DataSource state;

	public BroadbandHandler(DataSource state){
		this.state = state;
	}
	@Override
	public Object handle(Request request, Response response) {
		// Step 1: Prepare to send a reply of some sort
		Moshi moshi = new Moshi.Builder().build();
		// Replies will be Maps from String to Object. This isn't ideal; see reflection...
		Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
		JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);
		Map<String, Object> responseMap = new HashMap<>();

		//get query params for state and county requests
		String stateName = request.queryParams("state");
		String countyName = request.queryParams("county");

		try{
//			String stateCode = state.findStateCode(stateName);
//			String countyCode = state.findCountyCode(stateCode, countyName);
			//String data = state.getData(new StateAndCounty(stateName, countyName));
			String data = state.getData(new StateAndCounty(stateName, countyName));
			responseMap.put("result", "success");
//			responseMap.put("stateCode", stateCode);
//			responseMap.put("countyCode", countyCode);
			responseMap.put("state name", stateName);
			responseMap.put("county name", countyName);
			responseMap.put("broadband data", data);
			return adapter.toJson(responseMap);
		}
		catch(DatasourceException e){
			responseMap.put("type", "error");
			responseMap.put("result", e.getMessage());
			return adapter.toJson(responseMap);
		}
//		catch (IllegalArgumentException e) {
//
//		}
	}
}