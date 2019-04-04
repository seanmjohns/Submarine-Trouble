package com.pigcoder.subtrouble;


import org.junit.Test;

import java.awt.Dimension;

import static org.junit.Assert.assertEquals;

public class PlayerTest {

	@Test
	public void playerSpawnsInMiddleOfOcean() {
		Player player = new Player();
		assertEquals("Player spawns in the middle of the ocean",
				new Dimension((int)player.getX(), (int)player.getY()),
				new Dimension((int)(GameArea.SIZE.getWidth()/2 - Player.SIZE.getWidth()/2),
						(int)(GameFrame.OCEANLEVEL - Player.SIZE.getHeight()/4*3)));
	}

	//@Test
	public void playerShouldNotMoveOffTheScreen() {
		Player player = new Player();

		player.x = 0;
		player.xVel = -1;
		player.move();
		assertEquals("Player should not move off left side of screen", 0, player.x, 0.0);

		player.x = GameArea.SIZE.getWidth() - Player.SIZE.getWidth();
		player.xVel = 1;
		player.move();
		assertEquals("Player should not move off right side of screen", GameArea.SIZE.getWidth() - Player.SIZE.getWidth(), player.x,0.0);
	}

}
