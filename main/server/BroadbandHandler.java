import spark.Request;
import spark.Response;
import spark.Route;

public class BroadbandHandler implements Route {
	@Override
	public Object handle(Request request, Response response) {

		String state = request.queryParams("state");
		String county = request.queryParams("county");


	}
}