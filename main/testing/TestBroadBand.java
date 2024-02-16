package testing;

import censusAPI.CensusDataSource;
import org.junit.jupiter.api.Test;
import censusAPI.DatasourceException;
import censusAPI.StateAndCounty;
import org.testng.Assert;

/**
 * Unit Tests for our CensusDataSource
 * Tests to ensure that we get expected results from the census
 */
public class TestBroadBand {

  /**
   * Tests that our CensusDataSource returns correct information queried from the Census API
   * when given correctly formatted States and Counties
   */
  @Test
  public void testCensusQueries(){
    CensusDataSource source = new CensusDataSource();

    StateAndCounty laCA = new StateAndCounty("California", "Los Angeles County"); //89.9
    StateAndCounty pvdRI = new StateAndCounty("Rhode Island", "Providence County"); //85.4
    StateAndCounty kcWA = new StateAndCounty("Washington", "King County"); //93.3

    try {
      Assert.assertEquals(source.getData(laCA), "89.9");
      Assert.assertEquals(source.getData(pvdRI), "85.4");
      Assert.assertEquals(source.getData(kcWA), "93.3");
    }
    catch(DatasourceException e){
      System.out.println("datasource exception");
    }


  }

}
