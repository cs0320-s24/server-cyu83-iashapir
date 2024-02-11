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


public class LoadCSVHandler implements Route {
	private final CSVDataSource dataSource;
	private boolean hasHeader;

	public LoadCSVHandler(CSVDataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Query params:
	 * filepath
	 * contains headers = 'hasHeader'
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	@Override
	public Object handle(Request request, Response response){
		// get query params for CSV filepath to load
		String filepath = "data/" + request.queryParams("filepath");
		String hasHeader = request.queryParams("hasHeader");
		Map<String, Object> responseMap = new HashMap<>();

		if (this.checkYesOrNo(hasHeader) == 2) {
			responseMap.put("Message","make sure you enter 'yes' or 'no' for whether your data has a header. please reenter "
					+ "your arguments and try again!");
			return new CSVFailureResponse(responseMap).serialize();

		} else if (this.checkYesOrNo(hasHeader) == 1) {
			this.dataSource.setHasHeader(true);
		} else {
			this.dataSource.setHasHeader(false);
		}

		//check filepath
		if (filepath.contains("..")) {
			responseMap.put("Message", "You don't have permission to access that file");
			return new CSVFailureResponse(responseMap).serialize();
		}

		try {
			FileReader reader = new FileReader(filepath);
			CSVParser parser = new CSVParser(reader, this.dataSource.getHasHeader());
			this.dataSource.setDataset(parser.parse());
		}
		catch(IOException e){
			responseMap.put("Message", "Something went wrong while reading your file.");
			return new CSVFailureResponse(responseMap).serialize();
		}

		responseMap.put("Message", "Successfully loaded file: "+filepath);
		return new CSVSuccessResponse(responseMap).serialize();

	}


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


