package dk.bot.racingpost.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**Racing Post market bean
 * 
 * @author daniel
 *
 */
public class RacingPostMarket implements Serializable{

	/** e.g. Kempton or Warwick*/
	private String meetingName;
	
	/** e.g. 03.11.2008 13:50*/
	private Date marketTime;

	List<RacingPostRunner> marketRunners;
	
	public RacingPostMarket(String meetingName, Date marketTime, List<RacingPostRunner> racingPostRunners) {
		this.meetingName=meetingName;
		this.marketTime=marketTime;
		this.marketRunners=racingPostRunners;
	}

	public String getMeetingName() {
		return meetingName;
	}

	public void setMeetingName(String meetingName) {
		this.meetingName = meetingName;
	}

	public Date getMarketTime() {
		return marketTime;
	}

	public void setMarketTime(Date marketTime) {
		this.marketTime = marketTime;
	}

	public List<RacingPostRunner> getMarketRunners() {
		return marketRunners;
	}

	public void setMarketRunners(List<RacingPostRunner> marketRunners) {
		this.marketRunners = marketRunners;
	}
}
