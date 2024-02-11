package main.server;
import static spark.Spark.after;
import spark.Spark;


public class Server {
	public static void main(String[] args) {
		int port = 3232;
		Spark.port(port);

		// set up - allow clients to make requests to server
		after(
				(request, response) -> {
					response.header("Access-Control-Allow-Origin", "*");
					response.header("Access-Control-Allow-Methods", "*");
				});

		// set up handlers for endpoints
		Spark.get("loadcsv", new LoadCSVHandler());
		Spark.get("viewcsv", new ViewCSVHandler());
		Spark.get("searchcsv", new SearchCSVHandler());
		Spark.get("broadband", new BroadbandHandler());
		Spark.init();
		Spark.awaitInitialization();

		System.out.println("Server started at http://localhost:" + port);
	}

}