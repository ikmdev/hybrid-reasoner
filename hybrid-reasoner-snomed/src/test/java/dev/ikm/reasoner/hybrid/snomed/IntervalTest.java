package dev.ikm.reasoner.hybrid.snomed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntervalTest {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(IntervalTest.class);

	@Test
	public void open() {
		Interval i = Interval.fromString("(10, 20) 30");
		i = Interval.fromString(i.toString());
		assertTrue(i.isLowerOpen());
		assertEquals(10, i.getLowerBound());
		assertEquals(20, i.getUpperBound());
		assertTrue(i.isUpperOpen());
		assertEquals(30, i.getUnitOfMeasure());
	}

	@Test
	public void closed() {
		Interval i = Interval.fromString("[10, 20] 30");
		i = Interval.fromString(i.toString());
		assertFalse(i.isLowerOpen());
		assertEquals(10, i.getLowerBound());
		assertEquals(20, i.getUpperBound());
		assertFalse(i.isUpperOpen());
		assertEquals(30, i.getUnitOfMeasure());
	}

	@Test
	public void lowerOpen() {
		Interval i = Interval.fromString("(10, 20] 30");
		i = Interval.fromString(i.toString());
		assertTrue(i.isLowerOpen());
		assertEquals(10, i.getLowerBound());
		assertEquals(20, i.getUpperBound());
		assertFalse(i.isUpperOpen());
		assertEquals(30, i.getUnitOfMeasure());
	}

	@Test
	public void upperOpen() {
		Interval i = Interval.fromString("[10, 20) 30");
		i = Interval.fromString(i.toString());
		assertFalse(i.isLowerOpen());
		assertEquals(10, i.getLowerBound());
		assertEquals(20, i.getUpperBound());
		assertTrue(i.isUpperOpen());
		assertEquals(30, i.getUnitOfMeasure());
	}

	private boolean contains(String i1, String i2) {
		return Interval.fromString(i1 + 1).contains(Interval.fromString(i2 + 1));
	}

	@Test
	public void contains() {
		assertTrue(contains("[0,10]", "[1,9]"));
		assertTrue(contains("[0,10]", "[0,10]"));
		assertTrue(contains("[0,10]", "(0,10)"));
		assertTrue(contains("[0,10]", "(-1,11)"));
		assertTrue(contains("[0,10]", "[0,11)"));
		assertTrue(contains("[0,10]", "(-1,10]"));
		assertTrue(contains("(0,10)", "(1,9)"));
		assertTrue(contains("(0,10)", "(0,10)"));
		assertTrue(contains("(0,10)", "[1,9]"));
		assertTrue(contains("(0,10]", "[1,10]"));
		assertTrue(contains("[0,10)", "[0,9]"));
		assertFalse(contains("[0,10]", "[-1,10]"));
		assertFalse(contains("[0,10]", "[0,11]"));
		assertFalse(contains("[0,10]", "[-1,11]"));
		assertFalse(contains("[0,10]", "(-2,10]"));
		assertFalse(contains("[0,10]", "[0,12)"));
		assertFalse(contains("(0,10)", "(-1,11)"));
	}

	@Test
	public void negative() {
		Interval i = Interval.fromString("[-10, -20) -30");
		i = Interval.fromString(i.toString());
		assertFalse(i.isLowerOpen());
		assertEquals(-10, i.getLowerBound());
		assertEquals(-20, i.getUpperBound());
		assertTrue(i.isUpperOpen());
		assertEquals(-30, i.getUnitOfMeasure());
	}

	@Test
	public void longMaxUnit() {
		Interval i = Interval.fromString("[-10, -20) " + Long.MAX_VALUE);
		i = Interval.fromString(i.toString());
		assertFalse(i.isLowerOpen());
		assertEquals(-10, i.getLowerBound());
		assertEquals(-20, i.getUpperBound());
		assertTrue(i.isUpperOpen());
		assertEquals(Long.MAX_VALUE, i.getUnitOfMeasure());
	}

	@Test
	public void longMinUnit() {
		Interval i = Interval.fromString("[-10, -20) " + Long.MIN_VALUE);
		i = Interval.fromString(i.toString());
		assertFalse(i.isLowerOpen());
		assertEquals(-10, i.getLowerBound());
		assertEquals(-20, i.getUpperBound());
		assertTrue(i.isUpperOpen());
		assertEquals(Long.MIN_VALUE, i.getUnitOfMeasure());
	}

}
