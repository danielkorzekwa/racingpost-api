package dk.bot.racingpost;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.bot.racingpost.model.RacingPostMarket;
import dk.bot.racingpost.model.RacingPostRunner;

/**
 * Racing Post website adapter.
 * 
 * @author daniel
 * 
 */
public class RacingPostImpl implements RacingPost {

	private final Log log = LogFactory.getLog(RacingPostImpl.class.getSimpleName());

	private HttpClient client;

	private boolean loginStatus;

	public RacingPostImpl() {
		client = new HttpClient();
		client.getParams().setSoTimeout(10000);
		client.getParams().setCookiePolicy(CookiePolicy.RFC_2109);

	}

	public List<RacingPostMarket> getMarkets(Date day) {
		if (!loginStatus) {
			loginStatus = login("daniel.korzekwa@gmail.com", "xxx");
			if (!loginStatus) {
				throw new RacingPostException("RacingPost login error.");
			}
		}

		SimpleDateFormat requestDayFormat = new SimpleDateFormat("yyyy-MM-dd");

		try {

			List<String> raceUrls = getRaceUrls("http://www.racingpost.com/horses2/cards/home.sd?r_date="
					+ requestDayFormat.format(day));

			List<RacingPostMarket> markets = new ArrayList<RacingPostMarket>();
			for (String raceUrl : raceUrls) {
				markets.addAll(parseMeetingMarkets(raceUrl));
			}

			return markets;
		} catch (Exception e) {
			throw new RacingPostException(e);
		}
	}

	private List<RacingPostMarket> parseMeetingMarkets(String raceUrl) throws ParseException, IOException {
		SimpleDateFormat marketTimeFormat = new SimpleDateFormat("dd MMM yy hh:mm a");

		String responseBody = getResponseBody("http://www.racingpost.com" + raceUrl);
		Source source = new Source(new StringReader(responseBody));
		source.setLogger(null);

		/** Parse markets */
		List<RacingPostMarket> racingPostMarkets = new ArrayList<RacingPostMarket>();

		/** Parse meeting info */
		Element meetingContent = source.getFirstElement("class", "meetingCardsContent", true);
		if (meetingContent != null) {

			String meetingInfo = meetingContent.getFirstElement("div").getFirstElement("div").getFirstElement("h1")
					.getTextExtractor().toString();
			String[] meetingInfoArray = meetingInfo.split(" - ");
			String meetingName = meetingInfoArray[0].trim();
			String meetingDay = meetingInfoArray[1].trim();

			List<Element> marketElements = meetingContent.getAllElements("class", "cardBlock", true);
			for (Element marketElement : marketElements) {
				Element marketTimeTextElement = marketElement.getFirstElement("class", "timeLink", true);
				if (marketTimeTextElement != null) {
					String marketTimeText = marketTimeTextElement.getTextExtractor().toString();
					Date marketTime = marketTimeFormat.parse(meetingDay + " " + marketTimeText + " pm");
					List<Element> pElements = marketElement.getFirstElement("class", "information", true)
							.getChildElements();
					for (Element pElement : pElements) {
						String pElementText = pElement.getFirstElement().getTextExtractor().toString();

						/** Parse market runners */
						if (pElementText.startsWith("BETTING FORECAST")) {
							String runnersString = pElementText.split("BETTING FORECAST:")[1];
							runnersString = runnersString.substring(0, runnersString.length() - 1);
							String[] runnersStringArray = runnersString.split(",");

							List<RacingPostRunner> racingPostRunners = new ArrayList<RacingPostRunner>();
							for (String runnerString : runnersStringArray) {
								runnerString = runnerString.trim();
								int spaceIndex = runnerString.indexOf(" ");
								String forecastPrice = runnerString.substring(0, spaceIndex);
								String selectionName = runnerString.substring(spaceIndex + 1);

								racingPostRunners.add(new RacingPostRunner(selectionName, forecastPrice));
							}

							/** Add market to list */
							racingPostMarkets.add(new RacingPostMarket(meetingName, marketTime, racingPostRunners));
							break;
						}
					}
				} else {
					log.error("Can't find marketElement.getFirstElement(\"class\", \"timeLink\", true) element.");
				}

			}
		}

		return racingPostMarkets;
	}

	private List<String> getRaceUrls(String url) throws IOException {
		String responseBody = getResponseBody(url);
		Source source = new Source(new StringReader(responseBody));
		source.setLogger(null);

		Element racesResult = source.getAllElements("body").get(0).getFirstElement("id", "races_result", true);

		List<String> raceUrls = new ArrayList<String>();
		for (Element raceContainer : racesResult.getChildElements()) {
			if (raceContainer.getName().equals("div") && raceContainer.getAttributeValue("class") != null
					&& raceContainer.getAttributeValue("class").equals("cardHome")) {
				for (Element race : raceContainer.getAllElements("a")) {
					if (race.getAttributeValue("class") != null && race.getAttributeValue("class").equals("DataLink")) {
						String raceUrl = race.getAttributeValue("href");
						if (raceUrl.startsWith("/horses2/cards/meeting_of_cards.sd")) {
							raceUrls.add(raceUrl);
						}
					}
				}
			}

		}

		return raceUrls;
	}

	/**
	 * 
	 * @param userName
	 * @param password
	 * @return true if successfull
	 * @throws IOException
	 * @throws HttpException
	 */
	private boolean login(String userName, String password) {

		PostMethod authpost = new PostMethod("https://reg.racingpost.com/modal_dialog/login.sd?protoSecure=0");

		try {
			// Prepare login parameters
			NameValuePair[] data = { new NameValuePair("in_un", userName), new NameValuePair("in_pw", password),
					new NameValuePair("process", "IN"), new NameValuePair("logInType", "lightbox"),
					new NameValuePair("PARGS", ""), };
			authpost.setRequestBody(data);

			int statusCode = client.executeMethod(authpost);
			if (statusCode != HttpStatus.SC_OK) {
				throw new IOException("GET error: " + statusCode);
			}

			String authResponse = authpost.getResponseBodyAsString(Integer.MAX_VALUE);

			boolean loggedin = false;
			Cookie[] logoncookies = client.getState().getCookies();
			for (int i = 0; i < logoncookies.length; i++) {
				if (logoncookies[i].getName().equals("PermRpLogin")) {
					loggedin = true;
					break;
				}
			}

			return loggedin;
		} catch (Exception e) {
			throw new RacingPostException(e);
		} finally {
			authpost.releaseConnection();
		}
	}

	private String getResponseBody(String url) throws IOException {

		// Create a method instance.
		GetMethod method = new GetMethod(url);

		try {
			// Execute the method.
			int statusCode = client.executeMethod(client.getHostConfiguration(), method, client.getState());

			if (statusCode != HttpStatus.SC_OK) {
				throw new IOException("GET error: " + statusCode);
			}

			// Read the response body.
			String responseBody = method.getResponseBodyAsString(Integer.MAX_VALUE);
			return responseBody;
		} finally {
			method.releaseConnection();
		}
	}
}
