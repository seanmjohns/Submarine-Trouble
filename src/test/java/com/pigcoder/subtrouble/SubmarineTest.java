package com.pigcoder.subtrouble;

import org.junit.Test;

import java.awt.geom.Point2D;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.*;

public class SubmarineTest {

	@Test
	public void spawnsBelowOceanLevelAndAboveOceanFloor() {
		for(int i=0; i<10000; i++) {
			Submarine sub = new Submarine();
			assertTrue("Sub is above sea level", sub.getY() >= GameFrame.OCEANLEVEL + Submarine.SIZE.getHeight());
			assertTrue("Sub is below ocean floor level", sub.getY() <= GameFrame.OCEANFLOORLEVEL - Submarine.SIZE.getHeight());
		}
	}

	@Test
	public void subTurnsAroundIfTargetIsBehind() {
		Submarine sub = new Submarine();

		//behind
		sub.x = 300;
		sub.y = 300;
		sub.direction = 1;
		sub.setTargetPoint(200, 150);
		sub.move();
		assertEquals("The sub should turn around", 0, sub.direction);

		//Directly upwards
		sub.x = 200;
		sub.y = 200;
		sub.direction = 1;
		sub.setTargetPoint(200, 300);
		sub.move();
		assertEquals("The sub should not turn around", 1, sub.direction);

		//In front
		sub.x = 200;
		sub.y = 200;
		sub.direction = 1;
		sub.setTargetPoint(300, 300);
		sub.move();
		assertEquals("The sub should not turn around", 1, sub.direction);
	}

	@Test
	public void subReachesTargetPoint() {
		for (int i = 0; i < 100; i++) {
			Submarine sub = new Submarine();

			//Make the target point anywhere in the ocean to truely test it
			sub.setTargetPoint(
					ThreadLocalRandom.current().nextInt(0, (int)GameArea.SIZE.getWidth()),
					ThreadLocalRandom.current().nextInt(GameFrame.OCEANLEVEL, GameFrame.OCEANFLOORLEVEL - (int)Submarine.SIZE.getHeight()));

			for (int p = 0; p < 1000; p++) { //Going to 1000 insures that the sub makes it there
				sub.move();
				if (sub.targetPoint == null) {
					break;
				}
			}
			assertNull("Sub should reach target point", sub.targetPoint);
		}
	}

	@Test
	public void subStaysInTheOcean() {
		for(int i=0; i<100; i++) {
			Submarine sub = new Submarine();

			//Make the target point somewhere above the ocean
			sub.setTargetPoint(
					ThreadLocalRandom.current().nextInt(0, (int)GameArea.SIZE.getWidth()),
					ThreadLocalRandom.current().nextInt(0, GameFrame.OCEANLEVEL));

			for (int p = 0; p < 1000; p++) { //Going to 1000 insures that the sub makes it there
				sub.move();
				if (sub.targetPoint == null) {
					break;
				}
			}
			assertNull("Sub should stay below sea level", sub.targetPoint);

			//Make the target point somewhere above the ocean
			sub.setTargetPoint(
					ThreadLocalRandom.current().nextInt(0, (int)GameArea.SIZE.getWidth()),
					ThreadLocalRandom.current().nextInt(GameFrame.OCEANFLOORLEVEL, (int)GameArea.SIZE.getHeight()));

			for (int p = 0; p < 1000; p++) { //Going to 1000 insures that the sub makes it there
				sub.move();
				if (sub.targetPoint == null) {
					break;
				}
			}
			assertNull("Sub should stay above ocean floor level", sub.targetPoint);
		}
	}

}