package futures;

import static util.Errors.*;
import static util.Objects.*;

import java.util.*;

public class StrikeSequence {

	private final Double strikeStep;
	private final Integer numStrikes;

	public StrikeSequence(Double strikeStep, Integer numStrikes) {
		this.strikeStep = strikeStep;
		this.numStrikes = numStrikes;
	}

	public double[] strikes(double start) {
		double mid = Math.round(start / strikeStep) * strikeStep;
		bombUnless(numStrikes %2 == 1, "numStrikes must be odd.  Is " + numStrikes);
		int numSteps = ( numStrikes - 1 ) / 2;
		double[] result = new double[numStrikes];
		for(int i = -numSteps; i <= numSteps; i++)
			result[i + numSteps] = strikeStep * i + mid;
		return result;
	}

	public List<OptionTicker> tickers(double start, FuturesTicker prefix) {
		List<OptionTicker> result = empty();
		for(double strike : strikes(start))
			result.add(prefix.optionTicker(strike));
		return result;
	}

}
