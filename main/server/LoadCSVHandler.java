package server;

import csv.CsvDataSource;
import spark.Request;
import spark.Response;
import spark.Route;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


public class LoadCSVHandler implements Route {
	private final CsvDataSource dataSource;
	private boolean hasHeader;
	public LoadCSVHandler(CsvDataSource dataSource){
		this.dataSource = dataSource;
	}

	/**
	 * Query params:
	 * filepath
	 * contains headers = 'hasHeader'
	 * @param request
	 * @param response
	 * @return
	 */
	@Override
	public Object handle(Request request, Response response) {

		// get query params for CSV filepath to load
		String filepath = request.queryParams("filepath");
		String hasHeader = request.queryParams("hasHeader");

		return null;
	}

	private void preParseTasks(String hasHeader){
		// check whether header isString provided correctly
		if (this.checkYesOrNo(hasHeader) == 2) {
			System.out.println(
					"make sure you enter 'yes' or 'no' for whether your data has a header. please reenter "
							+ "your arguments and try again!");
			// load value of inputted 3rd argument into this.hasHeader
		} else if (this.checkYesOrNo(hasHeader) == 1) {
			this.dataSource.setHasHeader(true);
		} else {
			this.dataSource.setHasHeader(false);
		}
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


	private void runREPL() {
		BufferedReader replReader = new BufferedReader(new InputStreamReader(System.in));

		System.out.println("");
		this.printPrompt();

		try {
			String input = replReader.readLine();

			while (input != null) {

				String[] result = regexSplitCSVRow.split(input);

				// ARGUMENT VALIDITY CHECKS:

				// check argument number for validity:
				boolean providedCol = false;
				boolean providedStringHeader;

				if (result.length == 5) {
					providedCol = true; // mark if header col was provided
				} else if (result.length != 3) {
					System.out.println(
							"sorry, you entered an invalid number of search terms. please reenter your arguments and make sure"
									+ " to follow the directions above.");
					this.printPrompt(); // rerun repl
					input = replReader.readLine();
					continue;
				}

				// check whether header value correctly formatted/present
				if (this.checkYesOrNo(result[2]) == 2) {
					System.out.println(
							"make sure you enter 'yes' or 'no' for whether your data has a header. please reenter "
									+ "your arguments and try again!");
					this.printPrompt();
					input = replReader.readLine();
					continue;
					// load value of inputted 3rd argument into this.hasHeader
				} else if (this.checkYesOrNo(result[2]) == 1) {
					this.hasHeader = true;
				} else {
					this.hasHeader = false;
				}

				// check whether header isString provided correctly
				if (this.checkYesOrNo(result[4]) == 2) {
					System.out.println(
							"make sure you enter 'yes' or 'no' for whether your data has a header. please reenter "
									+ "your arguments and try again!");
					this.printPrompt();
					input = replReader.readLine();
					continue;
					// load value of inputted 3rd argument into this.hasHeader
				} else if (this.checkYesOrNo(result[4]) == 1) {
					providedStringHeader = true;
				} else {
					providedStringHeader = false;
				}
				// PARSING AND SEARCHING
				try {
					FileReader reader = new FileReader("data/" + result[0]);

					CSVParser parser = new CSVParser<>(reader, this.dataCreator, this.hasHeader);
					String searchTerm = result[1];
					List<List<String>> dataset = parser.parse();
					Searcher searcher = new Searcher(dataset, parser, this.hasHeader);

					// call search() method based on whether fourth argument was provided
					if (providedCol) {
						searcher.search(searchTerm, result[3], providedStringHeader);
					} else {
						searcher.search(searchTerm);
					}

				} catch (FileNotFoundException f) {
					System.out.println(
							"sorry, your file could not be located. "
									+ "please double check your filepath and reenter your search arguments.");
					this.printPrompt();
					input = replReader.readLine();
					continue;
				}

				this.printPrompt();
				input = replReader.readLine();
			}

			// HANDLE ERRORS FROM PARSE
		} catch (IOException e) {
			System.err.println("whoops! encountered IO exception :(. try rerunning the program!");
			System.exit(1);

		} catch (FactoryFailureException f) {
			System.err.print(
					"whoops! encountered this error: "
							+ f.getMessage()
							+ " :(. try rerunning"
							+ "the program!");
		}
	}
}