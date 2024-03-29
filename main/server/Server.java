package server;
import static spark.Spark.after;

import censusAPI.CachingCensusDataSource;
import censusAPI.CensusDataSource;
import csv.CSVDataSource;
import spark.Spark;

/**
 * Server
 * Starts listening from port and prepares to run "get" requests via our API
 * Sets up all 4 of our endpoints with the proper handlers
 */
public class Server {

	public static void main(String[] args) {
		int port = 3232;
		Spark.port(port);
		CSVDataSource csvDatasource = new CSVDataSource();

		// set up - allow clients to make requests to server
		after(
				(request, response) -> {
					response.header("Access-Control-Allow-Origin", "*");
					response.header("Access-Control-Allow-Methods", "*");
				});

		// set up handlers for endpoints
		Spark.get("loadcsv", new LoadCSVHandler(csvDatasource));
		Spark.get("viewcsv", new ViewCSVHandler(csvDatasource));
		Spark.get("searchcsv", new SearchCSVHandler(csvDatasource));
		CensusDataSource censusData = new CensusDataSource();
		Spark.get("broadband", new BroadbandHandler(new CachingCensusDataSource(censusData,
				10, 10)));
		Spark.init();
		Spark.awaitInitialization();

		//Message to print when server starts runnning
		System.out.println("Server started at http://localhost:" + port);
		System.out.println("Endpoints: loadcsv, viewcsv, searchcsv, broadband");
		System.out.println("Do not use viewcsv or searchcsv without first loading a csv file via the loadcsv endpoint.");
		System.out.println("Reference README for instructions on parameters for queries.");
	}

}