package server;

import csv.CsvDataSource;
import spark.Request;
import spark.Response;
import spark.Route;

public class SearchCSVHandler implements Route {
	private final CsvDataSource dataSource;
	public SearchCSVHandler(CsvDataSource dataSource){
		this.dataSource = dataSource;
	}
	@Override
	public Object handle(Request request, Response response) {

		String searchVal = request.queryParams("searchVal");
		// TODO: add all other params needed to perform search
		return null;
	}
}