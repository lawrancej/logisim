package com.cburch.logisim.util;

import static org.junit.Assert.*;

import org.junit.Test;

import com.cburch.logisim.util.StringUtil;

public class StringUtilTest {

	@Test
	public void testStringUtilFormat() {
		assertEquals("Hello, Joe! How are you today?", StringUtil.format("%s, %s! %s?", "Hello", "Joe", "How are you today"));
		assertEquals("Hello, null", StringUtil.format("%s, %s", "Hello", null));
	}

}
