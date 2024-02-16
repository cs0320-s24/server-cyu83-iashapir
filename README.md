> **GETTING STARTED:** You must start from some combination of the CSV Sprint code that you and your partner ended up with. Please move your code directly into this repository so that the `pom.xml`, `/src` folder, etc, are all at this base directory.

> **IMPORTANT NOTE**: In order to run the server, run `mvn package` in your terminal then `./run` (using Git Bash for Windows users). This will be the same as the first Sprint. Take notice when transferring this run sprint to your Sprint 2 implementation that the path of your Server class matches the path specified in the run script. Currently, it is set to execute Server at `edu/brown/cs/student/main/server/Server`. Running through terminal will save a lot of computer resources (IntelliJ is pretty intensive!) in future sprints.



# How To
    Loadcsv:
Endpoint: 'loadcsv'
Query Parameters:
- "filepath": this should be the path to your csv file starting from the inside of the data folder (exclude '/data')
- "hasHeader": should be either "yes" or "no" depending on whether your csv has a header row


    Viewcsv:
Endpoint: 'viewcsv'
Important note: this can only be used *after* loading a csv via the loadcsv endpoint
No query parameters


    Searchcsv:
Endpoint: 'searchcsv'
Important note: this can only be used *after* loading a csv via the loadcsv endpoint
Query Parameters: 
- "searchTerm": enter what you would like to search for in your loaded csv
- "column": (OPTIONAL) enter either a column name or index you want to search in
- "colIsString": (required if a column was entered) should be "yes" if column entered is a header string, "no" if you entered a column index


    Broadband:
Endpoint: 'broadband'
Query Parameters:
- "state": enter a state from this list with the same formatting: https://api.census.gov/data/2010/dec/sf1?get=NAME&for=state:* 
- "county": enter a county (must be located in the state you entered)

# Project Details
    Project Name: Gwenyth
    Team members: Catherine Yu (cyu83) and Isabelle Shapiro (iashapir)
    Total time estimated: 12 hours
    PROJECT REPO: https://github.com/cs0320-s24/server-cyu83-iashapir

# Design Choices
    CSV:
In order to ensure that our LoadCSVHandler, ViewCSVHandler, and SearchCSVHandler could all
access the same dataset, we created the CSVDataSource class to store important information about
a CSV file, including its parsed output and header column. This state also kept track of 
whether a CSV file had been loaded so that calls to viewcsv and searchcsv only work when
they are supposed to. 

    Broadband:
In order to implement querying for census broadband data, we created a DataSource interface
and three classes that implement that DataSource interface. All three can be passed
into BroadbandHandler, which calls their getData method. The getData method takes in a StateAndCounty
parameter, which is a record that stores strings for the user's queried state and county. This 
makes caching possible, as each query has two keys.

The CensusDataSource class is the only class that actually queries the Census API
for broadband data. We made the choice to store state codes in an instance variable hashmap 
instead of querying the census for each state since those are constants. Additionally, we 
store the county codes that we query for each state in case the user queries counties in the same
state multiple times in a row (a likely scenario). These are replaced each time a new state is queried.

The CachingCensusDataSource class wraps caching functionality around our CensusDataSource class using
google's guava library. We allow the developer to input parameters that specify the size of the cache
and duration items stay in the cache before being deleted. It caches a String, which represents the 
broadband percentage corresponding to the inputted StateAndCounty object.

The MockedCensusDataSource class exists for testing only, and returns a constant value regardless
of what values are queried (if they meet parameter requirements for the CensusDataSource).


# Tests

    CSV:
Our TestCSVHandler file 

    Broadband:
Our TestBroadbandHandler file contains tests with the following descriptions:
- We use our MockedCensusDataSource to test that we get success responses from our API
if proper query parameters are entered
  - We also use the Mocked source to test that inputting too few parameters results in
an error response from our API
- We use our CensusDataSource and very few calls to the real census API to 
ensure that our error handling is working properly
  - We expect error responses when inputting invalid states and counties
- We use our CachingCensusDataSource wrapped around our MockCensusDataSource
test that our cache works properly by tracking hits and misses in the cache
and ensuring success for all valid queries


# Errors/Bugs


