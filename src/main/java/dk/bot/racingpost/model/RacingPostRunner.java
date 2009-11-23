package dk.bot.racingpost.model;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.bot.racingpost.RacingPostException;

/**
 * Racing Post runner bean.
 * 
 * @author daniel
 * 
 */
public class RacingPostRunner implements Serializable{

	private final Log log = LogFactory.getLog(RacingPostRunner.class.getSimpleName());

	
	private String selectionName;

	/** Forcast of decimal price made by RacingPost. */
	private Double forcastPrice;

	/** Forcast of fraction price made by RacingPost. */
	private String forcastFractionPrice;

	public RacingPostRunner(String selectionName, String forcastFractionPrice) {
		this.selectionName = selectionName;
		this.forcastFractionPrice = forcastFractionPrice;

		try {
			if (!forcastFractionPrice.equals("Evs")) {
				String[] forcastFractionArray = forcastFractionPrice.split("/");
				int fractionA = Integer.parseInt(forcastFractionArray[0]);
				int fractionB = Integer.parseInt(forcastFractionArray[1]);

				forcastPrice = (double) (fractionA + fractionB) / (double) fractionB;
			}
			else {
				log.error("Fraction price= " + forcastFractionPrice + ". Check what does it mean.");
			}
		} catch (NumberFormatException e) {
			throw new RacingPostException(e);
		}
	}

	public String getSelectionName() {
		return selectionName;
	}

	public void setSelectionName(String selectionName) {
		this.selectionName = selectionName;
	}

	public Double getForcastPrice() {
		return forcastPrice;
	}

	public String getForcastFractionPrice() {
		return forcastFractionPrice;
	}

	public void setForcastFractionPrice(String forcastFractionPrice) {
		this.forcastFractionPrice = forcastFractionPrice;
	}

}
