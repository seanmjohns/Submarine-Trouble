package com.pigcoder.subtrouble;

import org.junit.Test;

import java.awt.Dimension;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MainTest {

	@Test
	public void windowShouldNotBeVisibleDuringTesting() {
		GameFrame testFrame = new GameFrame();
		testFrame.createGui(true);
		assertTrue("The window should not be visible during testing", !testFrame.isVisible());
	}

	@Test
	public void gameAreaShouldBe600By400() {
		GameFrame testFrame = new GameFrame();
		testFrame.createGui(true);
		assertEquals( "gameArea should be 400 x 600", new Dimension(600, 400), testFrame.area.getSize());
	}

	@Test
	public void resourcesCanBeSuccessfullyLoaded() {
		assertTrue("Resources should load", Main.loadResources());
	}

}