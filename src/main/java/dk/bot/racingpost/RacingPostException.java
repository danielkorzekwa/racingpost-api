package dk.bot.racingpost;

/**
 * 
 * @author daniel
 *
 */
public class RacingPostException extends RuntimeException{

	public RacingPostException(String message) {
		super(message);
	}
	
	public RacingPostException(String message,Throwable t) {
		super(message,t);
	}
	
	public RacingPostException(Throwable t) {
		super(t);
	}
}
