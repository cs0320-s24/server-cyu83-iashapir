package server;

import csv.CSVDataSource;
import csv.CSVFailureResponse;
import csv.CSVSuccessResponse;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.HashMap;
import java.util.Map;

public class ViewCSVHandler implements Route {
	private final CSVDataSource dataSource;
	public ViewCSVHandler(CSVDataSource dataSource){
		this.dataSource = dataSource;
	}
	@Override
	public Object handle(Request request, Response response) {
		Map<String, Object> responseMap = new HashMap<>();

		if (!this.dataSource.isLoaded()) {
			responseMap.put("result","error_no_file_loaded");
			responseMap.put("Message","Please load a CSV before searching.");
			return new CSVFailureResponse(responseMap).serialize();
		}

		response.type("application/json");
		responseMap.put("Data",dataSource.getDataset());
		return new CSVSuccessResponse(responseMap).serialize();

	}
}