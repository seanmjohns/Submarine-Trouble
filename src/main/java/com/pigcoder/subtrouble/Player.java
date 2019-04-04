package com.pigcoder.subtrouble;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;

public class Player extends Rectangle2D.Double {

	public static final Dimension SIZE = new Dimension(70, 30);

	public static final File destroyerImageFile = new File("Destroyer.png");
	public static Image destroyerImage;

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

	public int direction = 0; //Left: 0, Right: 1

	public double health = 100;

	public void damaged(double amount) {
		health =- amount;
		if(health < 1) {
			GameFrame.gameOver = true;
		}
	}

	//Direction: 0 is left, 1 is right
	public void move() {

	}

	public Player() {
		y = GameFrame.OCEANLEVEL - SIZE.getHeight()/4*3;
		x = GameArea.SIZE.getWidth()/2 - SIZE.getWidth()/2;
		this.width = SIZE.getWidth();
		this.height = SIZE.getHeight();
	}

}
