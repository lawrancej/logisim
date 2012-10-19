package com.cburch.logisim.util.test;

import static org.junit.Assert.*;

import org.junit.Test;

import com.cburch.logisim.util.Nulls;

/**
 * Nulls provides null safe equality tests. Java 7 has this in the Objects (not Object) class
 */
public class NullsTest {

	@Test
	public void testEqual() {
		Object a = new Object();
		Object b = new Object();
		assertTrue(Nulls.equal(null, null));
		assertFalse(Nulls.equal(null, a));
		assertFalse(Nulls.equal(a, null));
		assertTrue(Nulls.equal(a, a));
		assertFalse(Nulls.equal(a, b));
	}

	@Test
	public void testUnequal() {
		Object a = new Object();
		Object b = new Object();
		assertFalse(Nulls.unequal(null, null));
		assertTrue(Nulls.unequal(null, a));
		assertTrue(Nulls.unequal(a, null));
		assertFalse(Nulls.unequal(a, a));
		assertTrue(Nulls.unequal(a, b));
	}

}
