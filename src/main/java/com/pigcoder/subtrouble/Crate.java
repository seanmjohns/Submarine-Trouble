package com.pigcoder.subtrouble;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.concurrent.ThreadLocalRandom;

public class Crate extends Rectangle2D.Double {

	public static final Dimension SIZE = new Dimension(20,20);

	public static final double MAXHEALTH = 50;

	public static final int healthBarOffsetY = 10;
	public static final int healthBarWidth = (int)SIZE.getWidth();
	public static final int healthBarHeight = 5;

	public static final File crateImageFile = new File("Crate.png");
	public static Image crateImage;

	//Types
	public static final int HEALTH = 1;
	public static final int HEALTHAMOUNT = 50;
	public static final int SPEED = 2;
	public static final int SPEEDTIME = 10000;
	public static final int ARMOR = 3;
	public static final int ARMORTIME = 10000;
	public static final int ARMORPERCENTBLOCKED = 50;

	public int type;

	public double health = MAXHEALTH;

	public double speed = 1;

	public void damaged(double amount) {
		health -= amount;
	}

	public void move() {
		if(y == GameFrame.OCEANFLOORLEVEL) { return; }

		if(y + speed >= GameFrame.OCEANFLOORLEVEL) {
			y = GameFrame.OCEANFLOORLEVEL;
		} else {
			y += speed;
		}
	}

	public void destroyed() {
		if(type == 1) {
			GameFrame.player.healed(HEALTHAMOUNT);
		} else if(type == 2) {
			GameFrame.player.timeLeftWithSpeedBoost = SPEEDTIME;
			GameFrame.player.timeCollectedSpeedBoost = System.currentTimeMillis();
			GameFrame.player.hasSpeedBoost = true;
		} else if(type == 3) {
			GameFrame.player.timeLeftWithArmorBoost = ARMORTIME;
			GameFrame.player.timeCollectedArmorBoost = System.currentTimeMillis();
			GameFrame.player.hasArmorBoost = true;
		}
	}

	public Crate(double x, double y) {
		this.x = x;
		this.y = y;
		this.speed = ThreadLocalRandom.current().nextDouble(1,2);
		this.width = SIZE.getWidth();
		this.height = SIZE.getHeight();
		this.type = ThreadLocalRandom.current().nextInt(1,4);
	}
}
