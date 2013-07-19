package org.github.teamq.gradle.gwt.demo.client;

import org.github.teamq.gradle.gwt.demo.shared.FieldVerifier;

import com.google.gwt.junit.client.GWTTestCase;

public class FieldVerifierTest extends GWTTestCase {

	@Override
	public String getModuleName() {
		return "org.github.teamq.gradle.gwt.demo.Demo";
	}

	public void testIsValidName() {
		assertFalse(FieldVerifier.isValidName(null));
		assertFalse(FieldVerifier.isValidName("AB"));
		assertFalse(FieldVerifier.isValidName("ABC"));
		assertTrue(FieldVerifier.isValidName("ABCD"));
	}
	
}
