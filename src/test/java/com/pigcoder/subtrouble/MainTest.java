package com.pigcoder.subtrouble;

import org.junit.Test;

import java.awt.Dimension;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MainTest {

	@Test
	public void windowShouldNotBeVisibleDuringTesting() {
		Main.testing = true;
		Main.testing = true;
		GameFrame testFrame = new GameFrame();
		testFrame.createGui(true);
		assertTrue("The window should not be visible during testing", !testFrame.isVisible());
	}

	@Test
	public void resourcesCanBeSuccessfullyLoaded() {
		Main.testing = true;
		assertTrue("Resources should load", Main.loadResources());
	}

}