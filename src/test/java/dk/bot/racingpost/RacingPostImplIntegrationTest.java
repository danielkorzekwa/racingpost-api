package dk.bot.racingpost;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Test;

import dk.bot.racingpost.model.RacingPostMarket;

public class RacingPostImplIntegrationTest {

	private static RacingPost racingPost = new RacingPostImpl();

	@Test
	public void testGetMarkets() throws ParseException {
		DateTime today = new DateTime(System.currentTimeMillis());
		List<RacingPostMarket> markets = racingPost.getMarkets(today.toDate());
		assertEquals(true, markets.size() > 0);
	}
	
	@Test
	public void testGetMarketsIn1Day() throws ParseException {
		DateTime inDays = new DateTime(System.currentTimeMillis()).plusDays(1);
		List<RacingPostMarket> markets = racingPost.getMarkets(inDays.toDate());
		if(inDays.getHourOfDay()>19) {
		assertEquals(true, markets.size() > 0);
		}
		else {
			assertEquals(0, markets.size());
		}
	}
	
	@Test
	public void testGetMarketsIn2Days() throws ParseException {
		DateTime inDays = new DateTime(System.currentTimeMillis()).plusDays(2);
		List<RacingPostMarket> markets = racingPost.getMarkets(inDays.toDate());
		assertEquals(0, markets.size());
	}

}
