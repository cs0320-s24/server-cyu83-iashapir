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
		// TODO: send back the entire CSV file's contents as a Json 2-dimensional array
		Map<String, Object> responseMap = new HashMap<>();

		if (!this.dataSource.isLoaded()) {
			responseMap.put("Message","A CSV file is not loaded, please load one before trying to view.");
			return new CSVFailureResponse(responseMap).serialize();
		}

		responseMap.put("Data",dataSource.getDataset());
		return new CSVSuccessResponse(responseMap).serialize();

	}
}