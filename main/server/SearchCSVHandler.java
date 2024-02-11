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
			responseMap.put("Message","A CSV file is not loaded, please load one before trying to search.");
			return new CSVFailureResponse(responseMap).serialize();
		}
		System.out.println("entered handle, checked datasource loaded");

		String searchTerm = request.queryParams("searchTerm");
		String column = request.queryParams("column");
		String colIsString = request.queryParams("colIsString");
		responseMap.put("searchTerm",searchTerm);
		responseMap.put("column", column);
		responseMap.put("colIsString",colIsString);
		System.out.println("put query params in responseMap");
		System.out.println(responseMap);

		if(searchTerm==null){
			responseMap.put("Message", "searchTerm not specified.");
			return new CSVFailureResponse(responseMap).serialize();
		}
		System.out.println("search term not null");

		//one of two second parameters inputted but not the others
		if((column==null && colIsString!=null) ||
				(colIsString==null && column!=null)){
			responseMap.put("Message", "Must specify both column name/index and whether column is a String");
			return new CSVFailureResponse(responseMap).serialize();
		}

		System.out.println("got through param checks");

		boolean colName = false;
		if(colIsString!=null){
			if (this.checkYesOrNo(colIsString) == 2) {
				responseMap.put("Message","make sure you enter 'yes' if your column is a name and 'no' if your column is an index");
				return new CSVFailureResponse(responseMap).serialize();

			} else if (this.checkYesOrNo(colIsString) == 1) {
				colName = true;
			}
		}

		System.out.println("right before searching");

		Searcher searcher = new Searcher(this.dataSource);
		System.out.println("created searcher");
		List<List<String>> results = new ArrayList<List<String>>();

		if(column==null && colIsString==null){
			System.out.println("entered search if statement");
			try {
				results = searcher.search(searchTerm);
				System.out.println("got results");
				System.out.println(results);
			}
			catch(IllegalArgumentException e){
				System.out.println(e);
			}

		}
		else{
			try{
				results = searcher.search(searchTerm, column, colName);
			}
			catch(IllegalArgumentException e){
				System.out.println(e);
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