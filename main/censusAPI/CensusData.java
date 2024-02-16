package censusAPI;

/**
 * Record to store
 * @param stateCode
 * @param stateName
 * @param countyCode
 * @param countyName
 * @param broadbandData
 */
public record CensusData(String stateCode, String stateName, String countyCode,
						 String countyName, String broadbandData) {

}
