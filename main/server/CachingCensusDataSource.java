//package server;
//import com.google.common.cache.CacheBuilder;
//import com.google.common.cache.CacheLoader;
//import com.google.common.cache.LoadingCache;
//
//import java.util.Collection;
//import java.util.concurrent.TimeUnit;
//import java.util.Collection;
//
//public class CachingCensusDataSource implements DataSource{
//  private CensusDataSource censusDataSource;
//  private LoadingCache<StateAndCounty, String> cache;
//
//  public CachingCensusDataSource(CensusDataSource censusDataSource, int durationMinutes, int cacheEntries) {
//    this.censusDataSource = censusDataSource;
//
//    this.cache = CacheBuilder.newBuilder()
//        // How many entries maximum in the cache?
//        .maximumSize(cacheEntries)
//        // How long should entries remain in the cache?
//        .expireAfterWrite(durationMinutes, TimeUnit.MINUTES)
//        // Keep statistical info around for profiling purposes
//        .recordStats()
//        .build(
//            // Strategy pattern: how should the cache behave when
//            // it's asked for something it doesn't have?
//            new CacheLoader<>() {
//              @Override //load() computes or retrieves the value corresponding to key.
//              public String load(StateAndCounty stateAndCounty) throws DatasourceException{ //TODO: examine whether we want to be doing this here
//                System.out.println("called load for: "+ stateAndCounty.stateName() + " "  + stateAndCounty.countyName());
//                // If this isn't yet present in the cache, load it:
//                return censusDataSource.getData(stateAndCounty.stateName(),
//                    stateAndCounty.countyName());
//              }
//            });
//  }
//
//  @Override
//  public String getData(StateAndCounty sc) throws DatasourceException {
//    return null;
//  }
//
//}
