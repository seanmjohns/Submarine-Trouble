package com.pigcoder.subtrouble;

import org.junit.Test;

import java.awt.geom.Point2D;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.*;

public class DepthChargeTest {

	@Test
	public void depthChargeReachesTargetPoint() {
		Main.testing = true;
		for (int i = 0; i < 100; i++) {
			DepthCharge dc = new DepthCharge(300, 125, 250);

			for (int p = 0; p < 1000; p++) { //Going to 1000 insures that the depth charge makes it there
				dc.move();
				if(dc.y >= dc.targetY) {
					break;
				}
			}
			assertTrue("t should reach target point", dc.y >= dc.targetY);
		}
	}

	@Test
	public void depthChargeStaysInTheOcean() {
		Main.testing = true;
		for(int i=0; i<100; i++) {
			DepthCharge dc = new DepthCharge(300, 125, 350);

			for (int p = 0; p < 1000; p++) { //Going to 1000 insures that the t makes it there
				dc.move();
			}
			assertTrue("t should reach target point", dc.y <= GameFrame.OCEANFLOORLEVEL);
		}
	}

}