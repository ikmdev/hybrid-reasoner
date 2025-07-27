package dev.ikm.reasoner.hybrid.snomed;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Interval {

	private int lowerBound, upperBound;

	private boolean lowerOpen, upperOpen;

	private long unitOfMeasure;

	public int getLowerBound() {
		return lowerBound;
	}

	public void setLowerBound(int lowerBound) {
		this.lowerBound = lowerBound;
	}

	public int getUpperBound() {
		return upperBound;
	}

	public void setUpperBound(int upperBound) {
		this.upperBound = upperBound;
	}

	public boolean isLowerOpen() {
		return lowerOpen;
	}

	public void setLowerOpen(boolean lowerOpen) {
		this.lowerOpen = lowerOpen;
	}

	public boolean isUpperOpen() {
		return upperOpen;
	}

	public void setUpperOpen(boolean upperOpen) {
		this.upperOpen = upperOpen;
	}

	public long getUnitOfMeasure() {
		return unitOfMeasure;
	}

	public void setUnitOfMeasure(long unitOfMeasure) {
		this.unitOfMeasure = unitOfMeasure;
	}

	public Interval(int lowerBound, boolean lowerOpen, int upperBound, boolean upperOpen, long unitOfMeasure) {
		super();
		this.lowerBound = lowerBound;
		this.lowerOpen = lowerOpen;
		this.upperBound = upperBound;
		this.upperOpen = upperOpen;
		this.unitOfMeasure = unitOfMeasure;
	}

	private Interval() {
	}

	public static Interval fromString(String str) {
		str = str.replace(" ", "");
		String regex = "^(\\[|\\()(\\-?\\d+),(\\-?\\d+)(\\]|\\))(\\-?\\d+)$";
		Pattern pat = Pattern.compile(regex);
		Matcher mat = pat.matcher(str);
		if (!mat.matches())
			throw new IllegalArgumentException(str);
		Interval ret = new Interval();
		ret.lowerOpen = mat.group(1).equals("(");
		ret.lowerBound = Integer.parseInt(mat.group(2));
		ret.upperBound = Integer.parseInt(mat.group(3));
		ret.upperOpen = mat.group(4).equals(")");
		ret.unitOfMeasure = Long.parseLong(mat.group(5));
		return ret;
	}

	@Override
	public String toString() {
		return toString(true);
	}

	public String toString(boolean includeUnitOfMeasure) {
		return (lowerOpen ? "(" : "[") + lowerBound + "," + upperBound + (upperOpen ? ")" : "]")
				+ (includeUnitOfMeasure ? unitOfMeasure : "");
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
				&& this.getUpperContainsValue() >= that.getUpperContainsValue()
				&& this.unitOfMeasure == that.unitOfMeasure;
	}

}
