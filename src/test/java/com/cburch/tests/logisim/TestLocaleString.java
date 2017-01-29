package com.cburch.tests.logisim;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.cburch.logisim.util.LocaleString;

public class TestLocaleString {
	
	private static void resetLocaleStringLang() {
		// Using reflection to change singleton state
		try {
			Field instanceField = LocaleString.class.getDeclaredField("self");
			instanceField.setAccessible(true);
			instanceField.set(LocaleString.class, null);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
		}

	}

	@Before
    public void setUp() {
    	Locale.setDefault(Locale.forLanguageTag("en"));
    }
	
	@After
    public void tearDown() {
    	Locale.setDefault(Locale.forLanguageTag("en"));
    	resetLocaleStringLang();
    }
	
	@Test
	public void testLocaleOptions() {
		Locale[] expectedLocales = {
				Locale.forLanguageTag("de"),
				Locale.forLanguageTag("el"),
				Locale.forLanguageTag("en"),
				Locale.forLanguageTag("es"),
				Locale.forLanguageTag("fr"),
				Locale.forLanguageTag("pt"),
				Locale.forLanguageTag("ru"),
		};
		
		assertArrayEquals(expectedLocales, LocaleString.getFromLocaleOptions());
	}
	
	@Test
	public void testGetFromLocaleDefault() {
		assertEquals("Logisim: Hex Editor", LocaleString.getFromLocale("hexFrameTitle"));
		assertEquals("Negate 0", LocaleString.getFromLocale("gateNegateAttr", "0"));
	}

	@Test
	public void testGetFromLocaleChangeLanguage() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		resetLocaleStringLang();
		
		Locale.setDefault(Locale.forLanguageTag("es"));
		assertEquals("Logisim: Editor Hex", LocaleString.getFromLocale("hexFrameTitle"));
		assertEquals("Negar 0", LocaleString.getFromLocale("gateNegateAttr", "0"));
	}
	
	@Test
	public void testGetFromLocaleUndefinedString() {
		assertEquals("undefinedString", LocaleString.getFromLocale("undefinedString"));
	}
	
	@Test
	public void testcreateLocaleSelector() {
		assertEquals(Locale.forLanguageTag("en"), LocaleString.createLocaleSelector().getLocale());
	}

}
