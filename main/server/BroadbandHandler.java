package server;

import censusAPI.DataSource;
import censusAPI.DatasourceException;
import censusAPI.StateAndCounty;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import spark.Request;
import spark.Response;
import spark.Route;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Uses handle method to run queries using the passed in DataSource's getData method
 * Checks parameters for validity before calling getData
 */
public class BroadbandHandler implements Route {

	private final DataSource state; //utilize polymorphism here so that our Cache and Mocked Sources can be used by same class

	public BroadbandHandler(DataSource state){
		this.state = state;
	}

	/**
	 * Handle
	 * this method creates Moshi object and calls our state's method to getData (from whatever source it has)
	 * returns serialized JSon responses for our API to display
	 * @param request
	 * @param response
	 * @return
	 */
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

		//Check bad parameter input and display helpful error messages
		if(stateName==null || countyName==null){
			responseMap.put("state name", stateName);
			responseMap.put("county name", countyName);
			responseMap.put("result", "error");
			responseMap.put("error_type", "missing_parameter");
			responseMap.put("error_arg", stateName == null ? "state" : "county");
			return adapter.toJson(responseMap);
		}

		//get date/time queried
		Date currentDate = new Date();

		try{
			//success! put all parameters and broadband information in response map
			String data = state.getData(new StateAndCounty(stateName, countyName));
			responseMap.put("result", "success");
			responseMap.put("state name", stateName);
			responseMap.put("county name", countyName);
			responseMap.put("broadband data", data);
			responseMap.put("census queried at", currentDate.toString());
			return adapter.toJson(responseMap);
		}
		//Failed due to datasource exception
		catch(DatasourceException e){
			responseMap.put("result", "error");
			responseMap.put("type", e.getMessage());
			responseMap.put("state name", stateName);
			responseMap.put("county name", countyName);
			responseMap.put("census queried at", currentDate.toString());
			return adapter.toJson(responseMap);
		}
		//Failed due to type/misspelling/misformatting/etc of state or county name
		catch(IllegalArgumentException e){
			responseMap.put("result", "error");
			responseMap.put("type", e.getMessage());
			responseMap.put("state name", stateName);
			responseMap.put("county name", countyName);
			responseMap.put("census queried at", currentDate.toString());
			return adapter.toJson(responseMap);
		}
	}
}