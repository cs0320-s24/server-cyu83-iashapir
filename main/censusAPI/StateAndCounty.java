package censusAPI;

/**
 * Record used to store user's inputted state and county and passed into DataSource's getData method
 * @param stateName
 * @param countyName
 */
public record StateAndCounty(String stateName, String countyName) {
}
