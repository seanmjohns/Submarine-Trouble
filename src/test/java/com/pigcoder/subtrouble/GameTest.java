package com.pigcoder.subtrouble;

import org.junit.Test;

import java.awt.geom.Rectangle2D;

import static org.junit.Assert.assertTrue;

public class GameTest {

	@Test
	public void oceanFloorDoesNotExceedOceanFloorLevelOrGoBelowScreen() {
		GameArea area = new GameArea();
		for(int i=0; i<50; i++) {
			area.generateOceanFloor();

			for (Rectangle2D.Double r : area.oceanFloor) {
				assertTrue("Ocean floor line should not exceed ocean floor level", area.SIZE.getHeight() - r.getHeight() >= GameFrame.OCEANFLOORLEVEL);
			}
		}
	}

}
