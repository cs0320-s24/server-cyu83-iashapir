import spark.Request;
import spark.Response;
import spark.Route;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LoadCSVHandler implements Route {
	@Override
	public Object handle(Request request, Response response) {

		// get query params for CSV filepath to load
		String filepath = request.queryParams("participants");


	}
}