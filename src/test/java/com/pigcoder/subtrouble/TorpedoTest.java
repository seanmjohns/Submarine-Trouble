package com.pigcoder.subtrouble;

import org.junit.Test;

import java.awt.geom.Point2D;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.*;

public class TorpedoTest {

	@Test
	public void tReachesTargetPoint() {
		for (int i = 0; i < 100; i++) {
			GameFrame.player = new Player();
			Torpedo t = new Torpedo(300, 300, null);

			for (int p = 0; p < 1000; p++) { //Going to 1000 insures that the t makes it there
				t.move();
				if (t.targetPoint == null) {
					break;
				}
			}
			assertNull("t should reach target point", t.targetPoint);
		}
	}

	@Test
	public void tStaysInTheOcean() {
		for(int i=0; i<100; i++) {GameFrame.player = new Player();
			Torpedo t = new Torpedo(300, 300, null);

			//Make the target point somewhere above the ocean
			t.targetPoint = new Point2D.Double(
					ThreadLocalRandom.current().nextInt(0, (int)GameArea.SIZE.getWidth()),
					ThreadLocalRandom.current().nextInt(0, GameFrame.OCEANLEVEL));

			for (int p = 0; p < 1000; p++) { //Going to 1000 insures that the t makes it there
				t.move();
				if (t.targetPoint == null) {
					break;
				}
			}
			assertNull("t should stay below sea level", t.targetPoint);

			//Make the target point somewhere above the ocean
			t.targetPoint = new Point2D.Double(
					ThreadLocalRandom.current().nextInt(0, (int)GameArea.SIZE.getWidth()),
					ThreadLocalRandom.current().nextInt(GameFrame.OCEANFLOORLEVEL, (int)GameArea.SIZE.getHeight()));

			for (int p = 0; p < 1000; p++) { //Going to 1000 insures that the t makes it there
				t.move();
				if (t.targetPoint == null) {
					break;
				}
			}
			assertNull("t should stay above ocean floor level", t.targetPoint);
		}
	}

}