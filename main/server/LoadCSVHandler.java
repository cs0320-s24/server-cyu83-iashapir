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

		if (this.checkYesOrNo(hasHeader) == 2) { // user should input "yes" or "no" for hasHeader
			responseMap.put("result","error_bad_request");
			return new CSVFailureResponse(responseMap).serialize();

		} else if (this.checkYesOrNo(hasHeader) == 1) {
			this.dataSource.setHasHeader(true);
		} else {
			this.dataSource.setHasHeader(false);
		}

		//check filepath
		if (filepath.contains("..")) {
			responseMap.put("result", "error_no_file_access_permission");
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


