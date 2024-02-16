package testing;
import censusAPI.CachingCensusDataSource;
import censusAPI.DatasourceException;
import censusAPI.StateAndCounty;
import org.junit.jupiter.api.Test;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testng.AssertJUnit.assertTrue;


/**
 * Class for Caching Unit tests
 */
public class TestCaching {

  /**
   * This method creates a CachingCensusDataSource (wrapped around mock source) to
   * check that our caching functionality works as expected
   * @throws IOException
   */
  @Test
  public void testCachingSource() throws IOException{
    //setup stuff (so we have datasource in here)
    MockedCensusDataSource mockedSource = new MockedCensusDataSource("fakeResult");
    CachingCensusDataSource cachingSource = new CachingCensusDataSource(mockedSource, 10, 4);

    StateAndCounty first = new StateAndCounty("California", "LA");
    StateAndCounty second = new StateAndCounty("Montana", "LA");
    StateAndCounty third = new StateAndCounty("Colorado", "LA");
    StateAndCounty fourth = new StateAndCounty("Washington", "LA");
    StateAndCounty fifth = new StateAndCounty("Arizona", "LA");

    try{
      //fill one cache slot
      cachingSource.getData(first);
      assertTrue(cachingSource.getStats().hitCount()==0);

      //fill second slot
      cachingSource.getData(second);
      assertTrue(cachingSource.getStats().hitCount()==0);

      cachingSource.getData(first);
      //check that cache was hit
      assertEquals(cachingSource.getStats().hitCount(), 1);
      cachingSource.getData(second);
      //check cache hit
      assertEquals(cachingSource.getStats().hitCount(), 2);
      cachingSource.getData(third);
      cachingSource.getData(fourth);
      assertEquals(cachingSource.getStats().hitCount(), 2);

      //check all things now in cache:
      cachingSource.getData(first);
      cachingSource.getData(second);
      cachingSource.getData(third);
      cachingSource.getData(fourth);
      assertEquals(cachingSource.getStats().hitCount(), 6);

      //add another thing to cache
      cachingSource.getData(fifth);
      //check first thing no longer in cache
      cachingSource.getData(first);
      assertEquals(cachingSource.getStats().hitCount(), 6);

    }
    catch(DatasourceException e){
      System.out.println("datasource exception thrown");
    }

  }

}
