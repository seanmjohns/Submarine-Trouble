package com.pigcoder.subtrouble;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;

public class DepthCharge extends Rectangle2D.Double {

	public static final int EXPLOSIONRADIUS = 50; //Damage = explosion radius * damageMultiplier
	public static final int damageMultiplier = 2;

	public static final Dimension SIZE = new Dimension(15, 7);

	public static final File depthChargeImageFile = new File("DepthCharge.png");
	public static Image depthChargeImage;

	public static final File depthChargeIndicatorImageFile = new File("DepthChargeIndicator.png");
	public static Image depthChargeIndicatorImage;

	public double speed = 1;

	public double targetY;

	public void move() {
		if(y <= targetY) {
			y += speed;
		}

		if(y >= targetY || y >= GameFrame.OCEANFLOORLEVEL) {
			explode();
		}
	}

	public void explode() {
		ArrayList newCrates = new ArrayList<>(); //Note that I need to do the crates first so that they do not get destroyed immediately
		for(Crate c : new ArrayList<>(GameFrame.crates)) {
			double distance = (Math.abs(getCenterY() - c.getCenterY()) + Math.abs(getCenterX() - c.getCenterX()))/2;
			if(distance <= EXPLOSIONRADIUS) {
				double damage = Math.abs(distance - EXPLOSIONRADIUS) * damageMultiplier;
				c.damaged(damage);
				if(c.health > 0) {
					newCrates.add(c);
				} else {
					c.destroyed();
				}
			} else {
				newCrates.add(c);
			}
		}
		GameFrame.crates = newCrates;
		ArrayList newSubs = new ArrayList<>();
		for(Submarine sub : new ArrayList<>(GameFrame.submarines)) {
			double distance = (Math.abs(getCenterY() - sub.getCenterY()) + Math.abs(getCenterX() - sub.getCenterX()))/2;
			if(distance <= Submarine.DEPTHCHARGEDETECTIONRANGE) { sub.detectedPlayer = true;}
			if(distance <= EXPLOSIONRADIUS) {
				double damage = Math.abs(distance - EXPLOSIONRADIUS) * damageMultiplier;
				sub.damaged(damage);
				GameFrame.score += damage; //Increase the player's score
				GameFrame.scoreIncreases.add(new ScoreIncrease((int)damage, (int)sub.x + (int)Submarine.SIZE.getWidth()/2 , (int)sub.y - 15));
				if(sub.health > 0) {
					newSubs.add(sub);
				} else {
					sub.destroyed();
				}
			} else {
				newSubs.add(sub);
			}
		}
		GameFrame.submarines = newSubs;
		GameFrame.depthCharges.remove(this);
		GameFrame.explosions.add(new Explosion((int)getCenterX(), (int)getCenterY()));
	}

	public DepthCharge(double x, double y, double targetY) {
		this.x = x;
		this.y = y;

		this.targetY = targetY;

		width = SIZE.getWidth();
		height = SIZE.getHeight();
	}

}
