package com.pigcoder.subtrouble;


import org.junit.Test;

import java.awt.Dimension;

import static org.junit.Assert.assertEquals;

public class PlayerTest {

	@Test
	public void playerSpawnsInMiddleOfOcean() {
		Main.testing = true;
		Player player = new Player();
		assertEquals("Player spawns in the middle of the ocean",
				new Dimension((int)player.getX(), (int)player.getY()),
				new Dimension((int)(GameArea.SIZE.getWidth()/2 - Player.SIZE.getWidth()/2),
						(int)(GameFrame.OCEANLEVEL - Player.SIZE.getHeight()/4*3)));
	}


}
