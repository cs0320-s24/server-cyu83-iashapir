package censusAPI;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;

import java.util.concurrent.TimeUnit;

/**
 * CachingCensusDataSource
 * - wraps censusDataSource with caching functionality
 * - User inputs duration and cache entry max
 */
public class CachingCensusDataSource implements DataSource {
  private DataSource censusDataSource;
  private LoadingCache<StateAndCounty, String> cache;

  /**
   * Setup cache
   * @param censusDataSource --supplies census searching functionality
   * @param durationMinutes --entered by developer
   * @param cacheEntries --entered by developer
   */
  public CachingCensusDataSource(DataSource censusDataSource, int durationMinutes, int cacheEntries) {
    this.censusDataSource = censusDataSource;

    this.cache = CacheBuilder.newBuilder()
        // How many entries maximum in the cache?
        .maximumSize(cacheEntries)
        // How long should entries remain in the cache?
        .expireAfterWrite(durationMinutes, TimeUnit.MINUTES)
        // Keep statistical info around for profiling purposes
        .recordStats()
        .build(
            // Strategy pattern: how should the cache behave when
            // it's asked for something it doesn't have?
            new CacheLoader<>() {
              @Override //load() computes or retrieves the value corresponding to key.
              public String load(StateAndCounty stateAndCounty) throws DatasourceException {
                System.out.println("called load for: "+ stateAndCounty.stateName() + " "  + stateAndCounty.countyName());
                // If this isn't yet present in the cache, load it:
                  return censusDataSource.getData(stateAndCounty);
              }
            });
  }

  /**
   * Checks cache to see if sc is there, if not calls censusDataSource's getData method
   * @param sc -- state and county to query
   * @return -- either
   * @throws DatasourceException
   * @throws IllegalArgumentException
   */
  @Override
  public String getData(StateAndCounty sc) throws DatasourceException, IllegalArgumentException{
    // "get" is designed for concurrent situations; for today, use getUnchecked:
      try {
        String result = cache.get(sc);
        System.out.println(cache.stats());
        return result;
      }
      /**sadly this is the only way we found to make sure exceptions throws in datasource's
       * getData method actually make their way to the handler class, otherwise our server would
       * crash*/
      catch(Exception e){
        if(e.getClass()==IllegalArgumentException.class){
          throw new IllegalArgumentException(e.getMessage());
        }
        throw new DatasourceException(e.getMessage());
      }
  }


  /**ONLY USE FOR TESTING**/
  public CacheStats getStats(){
    return this.cache.stats();
  }


}
