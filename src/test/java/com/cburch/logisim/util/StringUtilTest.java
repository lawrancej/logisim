package com.cburch.logisim.util;

import static org.junit.Assert.*;

import org.junit.Test;

import com.cburch.logisim.util.StringUtil;

public class StringUtilTest {

	@Test
	public void testStringUtilFormat() {
		assertEquals("Hello, Joe! How are you today?", String.format("%s, %s! %s?", "Hello", "Joe", "How are you today"));
		assertEquals("Hello, null", String.format("%s, %s", "Hello", null));
		assertEquals("Hi (1 of 3)", String.format("%1$s (%2$d of %3$d)", "Hi", 1,3));
	}

}
