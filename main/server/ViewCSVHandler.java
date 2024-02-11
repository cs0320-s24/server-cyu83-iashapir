package server;

import csv.CSVDataSource;
import spark.Request;
import spark.Response;
import spark.Route;

public class ViewCSVHandler implements Route {
	private final CSVDataSource dataSource;
	public ViewCSVHandler(CSVDataSource dataSource){
		this.dataSource = dataSource;
	}
	@Override
	public Object handle(Request request, Response response) {
		// TODO: send back the entire CSV file's contents as a Json 2-dimensional array
		return null;
	}
}