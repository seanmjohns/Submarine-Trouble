package com.pigcoder.subtrouble;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;

public class Torpedo extends Rectangle2D.Double {

	public static final Dimension SIZE = new Dimension(10, 5);

	public static final File torpedoImageFile = new File("Torpedo.png");
	public static Image torpedoImage;

	public static final double damage = 10.0;

	public double speed = 3;

	public double angle;

	public Point2D.Double targetPoint;

	public Submarine parentSub;

	public void move() {
		if(y >= GameFrame.OCEANFLOORLEVEL) {
			targetPoint = null;
			GameFrame.torpedoes.remove(this);
		} else if(y <= GameFrame.OCEANLEVEL) {
			targetPoint = null;
			GameFrame.torpedoes.remove(this);
		}
		if(targetPoint != null) {
			//angle = Math.atan2((targetPoint.getX() - x), (targetPoint.getY() - y));
			x += Math.sin(angle) * speed;
			y += Math.cos(angle) * speed;
			if(GameFrame.player.intersects(this)) {
				GameFrame.player.damaged(damage);
				GameFrame.torpedoes.remove(this);
			}
		} else {
			GameFrame.torpedoes.remove(this);
		}
		//System.out.println(x + " "+ y);
	}

	public Torpedo(double x, double y, Submarine parentSub) {
		this.x = x;
		this.y = y;

		this.parentSub = parentSub;

		targetPoint = new Point2D.Double(GameFrame.player.getCenterX(),GameFrame.OCEANLEVEL);
		//System.out.println(targetPoint);

		//Aim the torpedo
		angle = Math.atan2((targetPoint.getX() - getCenterX()), (targetPoint.getY() - getCenterY()));

		width = SIZE.getWidth();
		height = SIZE.getHeight();
	}

}
