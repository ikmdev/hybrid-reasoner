package dev.ikm.reasoner.hybrid.snomed;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Interval {

	private int lowerBound, upperBound;

	private boolean lowerOpen, upperOpen;

	public int getLowerBound() {
		return lowerBound;
	}

	public int getUpperBound() {
		return upperBound;
	}

	public boolean isLowerOpen() {
		return lowerOpen;
	}

	public boolean isUpperOpen() {
		return upperOpen;
	}

	public Interval(int lowerBound, boolean lowerOpen, int upperBound, boolean upperOpen) {
		super();
		this.lowerBound = lowerBound;
		this.lowerOpen = lowerOpen;
		this.upperBound = upperBound;
		this.upperOpen = upperOpen;
	}

	private Interval() {
	}

	public static Interval fromString(String str) {
		str = str.replace(" ", "");
		String regex = "^(\\[|\\()(\\-?\\d+),(\\-?\\d+)(\\]|\\))$";
		Pattern pat = Pattern.compile(regex);
		Matcher mat = pat.matcher(str);
		if (!mat.matches())
			throw new IllegalArgumentException(str);
		Interval ret = new Interval();
		ret.lowerOpen = mat.group(1).equals("(");
		ret.lowerBound = Integer.parseInt(mat.group(2));
		ret.upperBound = Integer.parseInt(mat.group(3));
		ret.upperOpen = mat.group(4).equals(")");
		return ret;
	}

	@Override
	public String toString() {
		return (lowerOpen ? "(" : "[") + lowerBound + "," + upperBound + (upperOpen ? ")" : "]");
	}

	private int getLowerContainsValue() {
		if (this.isLowerOpen())
			return this.getLowerBound() + 1;
		return this.getLowerBound();
	}

	private int getUpperContainsValue() {
		if (this.isUpperOpen())
			return this.getUpperBound() - 1;
		return this.getUpperBound();
	}

	public boolean contains(Interval that) {
		return this.getLowerContainsValue() <= that.getLowerContainsValue()
				&& this.getUpperContainsValue() >= that.getUpperContainsValue();
	}

	// This would work for float
//	public boolean contains(Interval that) {
//		return Integer.compare(this.getLowerBound(),
//				that.getLowerBound()) <= (this.isLowerOpen() && !that.isLowerOpen() ? -1 : 0)
//				&& Integer.compare(this.getUpperBound(),
//						that.getUpperBound()) >= (this.isUpperOpen() && !that.isUpperOpen() ? 1 : 0);
//	}

}
