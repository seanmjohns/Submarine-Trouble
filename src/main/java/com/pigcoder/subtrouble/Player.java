package com.pigcoder.subtrouble;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.File;

public class Player extends Rectangle2D.Double {

	public static final Dimension SIZE = new Dimension(70, 30);

	public static final File destroyerImageFile = new File("Destroyer.png");
	public static Image destroyerImage;

	public static final int MAXHEALTH = 100;

	public static final long easyDepthChargeDropDelay = 1000; //milliseconds
	public static final long normalDepthChargeDropDelay = 2000; //milliseconds
	public static final long hardDepthChargeDropDelay = 3000; //milliseconds

	public static final double easySpeedIncrease = 0.02;
	public static final double easyMaxSpeed = 2;
	public static final double normalSpeedIncrease = 0.01;
	public static final double normalMaxSpeed = 1;
	public static final double hardSpeedIncrease = 0.005;
	public static final double hardMaxSpeed = 0.75;

	public static int xs[] = { 0, 70, 47, 23, 0};
	public static int ys[] = { 16, 16, 30, 30, 16};

	public static Polygon collisionPolygon = new Polygon(xs, ys, xs.length);

	public double xVel = 0;
	public double yVel = 0;

	double x;
	double y;

	public double getX() { return x; }
	public void setX(double x) { this.x = x; }
	public double getY() { return y; }
	public void setY(double y) { this.y = y; }

	public double currentSpeedIncrease = normalSpeedIncrease;
	public double currentMaxSpeed = normalMaxSpeed;

	public long currentDepthChargeDropDelay = normalDepthChargeDropDelay;

	public int direction = 0; //Left: 0, Right: 1

	public double health = 100;

	public long timeOfLastDrop = 0;
	public long timeSinceLastDrop = 0;
	public long timePausedSinceLastDrop = 0;

	public boolean hasSpeedBoost = false;
	public long timeLeftWithSpeedBoost = 0;
	public long timeCollectedSpeedBoost = 0;
	public long timeSinceCollectedSpeedBoost = 0;
	public long timePausedWhileHadSpeedBoost = 0;

	public boolean hasArmorBoost = false;
	public long timeLeftWithArmorBoost = 0;
	public long timeCollectedArmorBoost = 0;
	public long timeSinceCollectedArmorBoost = 0;
	public long timePausedWhileHadArmorBoost = 0;

	public void healed(double amount) {
		health += amount;
		if(health < 1) {
			GameFrame.gameOver = true;
		} else if(health > MAXHEALTH) {
			health = MAXHEALTH;
		}
	}

	public void damaged(double amount) {
		if(hasArmorBoost) {
			health -= amount*(Crate.ARMORPERCENTBLOCKED/100.0);
		} else {
			health -= amount;
		}
		if(health < 1) {
			GameFrame.gameOver = true;
		} else if(health > MAXHEALTH) {
			health = MAXHEALTH;
		}
	}

	public Player() {
		y = GameFrame.OCEANLEVEL - SIZE.getHeight()/4*3;
		x = GameArea.SIZE.getWidth()/2 - SIZE.getWidth()/2;
		this.width = SIZE.getWidth();
		this.height = SIZE.getHeight();
	}

}
