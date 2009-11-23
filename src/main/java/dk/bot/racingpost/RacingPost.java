package dk.bot.racingpost;

import java.util.Date;
import java.util.List;

import dk.bot.racingpost.model.RacingPostMarket;

/**Racing Post website adapter.
 * 
 * @author daniel
 *
 */
public interface RacingPost {

	/** Get HR markets for a given day
	 * 
	 * @param day
	 * @return
	 */
	public List<RacingPostMarket> getMarkets(Date day);
}
