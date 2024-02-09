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

		String CSVfilepath; // TODO - provide the filepath

		// set up handlers for endpoints
		Spark.get("loadcsv", new _Handler(CSVfilepath));
		Spark.get("viewcsv", new _Handler());
		Spark.get("searchcsv", new _Handler());
		Spark.get("broadband", new _Handler());
		Spark.init();
		Spark.awaitInitialization();

		System.out.println("Server started at http://localhost:" + port);
	}

}