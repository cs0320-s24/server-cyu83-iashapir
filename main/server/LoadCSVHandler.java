package server;

import csv.CSVFailureResponse;
import csv.CSVParser;
import csv.CSVSuccessResponse;
import csv.CSVDataSource;
import spark.Request;
import spark.Response;
import spark.Route;


import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * handler class for loading csv endpoint
 */
public class LoadCSVHandler implements Route {
	private final CSVDataSource dataSource;

	/**
	 * constructor for a loadcsvhandler
	 * @param dataSource - the datasource, which holds information about current
	 *                      state of the data loaded
	 */
	public LoadCSVHandler(CSVDataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * checks to make sure valid parameters were inputted. if so, parses the given
	 * csv file and stores the information in datasource.
	 * @param request the request to handle
	 * @param response use to modify properties of the response
	 * @return response content
	 */
	@Override
	public Object handle(Request request, Response response){
		// get query params for CSV filepath to load
		String filepath = "data/" + request.queryParams("filepath");
		String hasHeader = request.queryParams("hasHeader");
		Map<String, Object> responseMap = new HashMap<>();

		if (filepath.equals("data/") || hasHeader == null) {
			responseMap.put("result","error_bad_request");
			responseMap.put("Message","Please input filepath and hasHeader params.");
			return new CSVFailureResponse(responseMap).serialize();
		}

		if (this.checkYesOrNo(hasHeader) == 2) {
			responseMap.put("result","error_bad_request");
			responseMap.put("Message","Expected 'yes' or 'no' for hasHeader parameter.");
			return new CSVFailureResponse(responseMap).serialize();

		} else if (this.checkYesOrNo(hasHeader) == 1) {
			this.dataSource.setHasHeader(true);
		} else {
			this.dataSource.setHasHeader(false);
		}

		//check filepath
		if (filepath.contains("..")) {
			responseMap.put("result", "error_no_file_permissions");
			responseMap.put("Message","You don't have access to this file!");
			return new CSVFailureResponse(responseMap).serialize();
		}

		try {
			FileReader reader = new FileReader(filepath);
			CSVParser parser = new CSVParser(reader, this.dataSource.hasHeader());
			this.dataSource.setDataset(parser.parse());
			if(this.dataSource.hasHeader()) {
				this.dataSource.setHeaderCol(parser.getHeaderCol());
			}
		}
		catch(IOException e){
			responseMap.put("result", "error_reading_file");
			return new CSVFailureResponse(responseMap).serialize();
		}
		responseMap.put("Message", "Successfully loaded file: "+filepath);
		return new CSVSuccessResponse(responseMap).serialize();

	}

	/**
	 * helper to check if a given input is either "yes" or "no"
	 * @param yesOrNo
	 * @return an int - 1 if the input was "yes", 0 if the input was "no", and 2
	 * if neither
	 */
	private int checkYesOrNo(String yesOrNo) {
		String lowercase = yesOrNo.toLowerCase(Locale.ROOT);
		switch (lowercase) {
			case "y", "yes":
				return 1;
			case "n", "no":
				return 0;
			default:
				return 2;
		}
	}
}


