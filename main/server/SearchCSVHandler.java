package server;

import csv.CSVDataSource;
import csv.CSVFailureResponse;
import csv.CSVSuccessResponse;
import csv.Searcher;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SearchCSVHandler implements Route {
	private final CSVDataSource dataSource;
	public SearchCSVHandler(CSVDataSource dataSource){
		this.dataSource = dataSource;
	}

	/**
	 * searchTerm,column,columnType (yes if string)
	 * @param request
	 * @param response
	 * @return
	 */
	@Override
	public Object handle(Request request, Response response) throws IllegalArgumentException{

		Map<String, Object> responseMap = new HashMap<>();

		if (!this.dataSource.isLoaded()) {
			responseMap.put("result","error_no_file_loaded");
			return new CSVFailureResponse(responseMap).serialize();
		}

		String searchTerm = request.queryParams("searchTerm");
		String column = request.queryParams("column");
		String colIsString = request.queryParams("colIsString");
		responseMap.put("searchTerm",searchTerm);
		responseMap.put("column", column);
		responseMap.put("colIsString",colIsString);

		if(searchTerm==null){ // search term not specified
			responseMap.put("result", "error_bad_request");
			return new CSVFailureResponse(responseMap).serialize();
		}

		// one of two second parameters inputted but not the others
		// both column name/index and whether column is a String should be specified
		if((column==null && colIsString!=null) ||
				(colIsString==null && column!=null)){
			responseMap.put("result", "error_bad_request");
			return new CSVFailureResponse(responseMap).serialize();
		}

		boolean colName = false;
		if(colIsString!=null){
			if (this.checkYesOrNo(colIsString) == 2) { // should be "yes" or "no"
				responseMap.put("result", "error_bad_request");
				return new CSVFailureResponse(responseMap).serialize();

			} else if (this.checkYesOrNo(colIsString) == 1) {
				colName = true;
			}
		}

		Searcher searcher = new Searcher(this.dataSource);
		List<List<String>> results = new ArrayList<List<String>>();

		if(column==null && colIsString==null){
			try {
				results = searcher.search(searchTerm);
			}
			catch(IllegalArgumentException e){
				responseMap.put("result", "error_bad_request");
				return new CSVFailureResponse(responseMap).serialize();
			}

		}
		else{
			try{
				results = searcher.search(searchTerm, column, colName);
			}
			catch(IllegalArgumentException e){
				responseMap.put("result", "error_bad_request");
				return new CSVFailureResponse(responseMap).serialize();
			}
		}
		responseMap.put("Search Results",results);
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