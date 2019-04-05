package com.pigcoder.subtrouble;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.concurrent.ThreadLocalRandom;

public class Submarine extends Rectangle2D.Double {

	public static final Dimension SIZE = new Dimension(30, 16);

	public static final File submarineImageFile = new File("Submarine.png");
	public static Image submarineImage;

	public static final int healthBarOffsetY = 10;
	public static final int healthBarWidth = (int)SIZE.getWidth();
	public static final int healthBarHeight = 5;

	public static final int MAXHEALTH = 100;

	public static final int DEPTHCHARGEDETECTIONRANGE = 200;


	public static final int MAXATTACKDISTANCE = 100;

	public static final int MAXDODGEDISTANCE = 200;

	int direction; //Left: 0, Right: 1

	double x;
	double y;

	double speed = 2.25;

	public double health = MAXHEALTH;

	public void damaged(double amount) {
		health = health - amount;
		//Note that the sub is not removed here because that is handled by the scheduled executor
	}

	public double getX() { return x; }
	public void setX(double x) { this.x = x; }
	public double getY() { return y; }
	public void setY(double y) { this.y = y; }

	public int currentAction = WANDER;
	public static final int WANDER = 1;
	public static final int DODGE = 2;
	public static final int ATTACK = 3;
	public static final int SCOUT = 4;
	public static final int SINK = 5;

	public boolean detectedPlayer;
	public boolean atSurface;


	Point2D.Double targetPoint;

	public void move() {
		//If there is no target, then just move across the screen
		if(((currentAction == WANDER && targetPoint == null) || !GameFrame.inGame) && !Main.testing) {
			if(direction == 0) {
				x = x - speed;
			} else if (direction == 1) {
				x = x + speed;
			}
			if(!GameFrame.inGame) {
				return;
			}
		} else {
			if(targetPoint != null) {
				double angle = Math.atan2((targetPoint.getX() - x), (targetPoint.getY() - y));
				x += Math.sin(angle) * speed;
				y += Math.cos(angle) * speed;
			}
		}
		//Make the sub turn around if necessary
		if(targetPoint != null) {
			if (x < targetPoint.getX()) {
				direction = 1;
			} else if (x > targetPoint.getX()) {
				direction = 0;
			}
			//Make the sub stop if target point reached
			if ((Math.abs(x - targetPoint.getX()) + Math.abs(y - targetPoint.getY()) / 2 < speed)) {
				targetPoint = null;
				return;
			}
		}
		if(Math.abs(y - GameFrame.OCEANLEVEL) < 5) {
			atSurface = true;
			detectedPlayer = true;
		} else {
			atSurface = false;
		}
		if(targetPoint == null) {
			determineNewAction();
		}
		//System.out.println(x + " "+ y);
	}

	public void setTargetPoint(double x, double y) {
		if(y < GameFrame.OCEANLEVEL) {
			y = GameFrame.OCEANLEVEL;
		} else if (y > GameFrame.OCEANFLOORLEVEL - 20) {
			y = GameFrame.OCEANFLOORLEVEL - 20;
		}
		targetPoint = new Point2D.Double(x, y);
		//System.out.println(targetPoint);
	}

	public void determineNewAction() {
		if(GameFrame.player == null) { return; }
		if(!detectedPlayer) {
			int decision = ThreadLocalRandom.current().nextInt(100);
			if(decision == 1) {
				setTargetPoint(x, GameFrame.OCEANLEVEL);
				currentAction = SCOUT;
			} else if(decision > 1) {
				currentAction = WANDER;
			}
		} else {
			if(currentAction == ATTACK) {
				if((Math.abs(GameFrame.player.getCenterX() - getCenterX()) + Math.abs(GameFrame.player.getCenterY() - getCenterY()))/2 <= MAXATTACKDISTANCE) {
					if(direction == 0 && GameFrame.player.x - x < 0) {
						direction = 1;
					} else if(direction == 1 && GameFrame.player.x - x > 0) {
						direction = 0;
					}
					shoot();
				}
			}
			int decision;
			//This makes sure that the sub wont shoot 10 at a time
			if(currentAction == ATTACK) {
				decision = ThreadLocalRandom.current().nextInt(1, 3);
			} else {
				decision = ThreadLocalRandom.current().nextInt( 3);
			}
			if(decision == 1) { //Attack within 50 pixels of the player
				Point2D topLeft = new Point2D.Double(GameFrame.player.getCenterX() - MAXATTACKDISTANCE/2, GameFrame.OCEANLEVEL);
				//Point2D bottomright = new Point2D.Double(GameFrame.player.getCenterX() + MAXATTACKDISTANCE/2, GameFrame.OCEANLEVEL + MAXATTACKDISTANCE/2);
				int xInRect = ThreadLocalRandom.current().nextInt(MAXATTACKDISTANCE);
				int yInRect = ThreadLocalRandom.current().nextInt(MAXATTACKDISTANCE/2);
				setTargetPoint(topLeft.getX() + xInRect, topLeft.getY() + yInRect);
				currentAction = ATTACK;
			} else if(decision == 2) { //Dodge within 100 pixels of the player
				Point2D topLeft = new Point2D.Double(GameFrame.player.getCenterX() - MAXDODGEDISTANCE/2, GameFrame.OCEANLEVEL);
				//Point2D bottomright = new Point2D.Double(GameFrame.player.getCenterX() + MAXDODGEDISTANCE/2, GameFrame.OCEANLEVEL +  MAXDODGEDISTANCE/2);
				int xInRect = ThreadLocalRandom.current().nextInt( MAXDODGEDISTANCE);
				int yInRect = ThreadLocalRandom.current().nextInt( MAXDODGEDISTANCE/2);
				setTargetPoint(topLeft.getX() + xInRect, topLeft.getY() + yInRect);
				currentAction = DODGE;
			} else if(decision == 3) { //Sink to the same x but a random y below the sub
				setTargetPoint(x, ThreadLocalRandom.current().nextInt((int)(y + SIZE.getHeight()), (int)(GameFrame.OCEANFLOORLEVEL - SIZE.getHeight())));
				currentAction = SINK;
			}
		}
	}

	public void shoot() {
		//Create torpedo and put it at the front of the sub
		double tx = x;
		if(direction == 0) {
			tx = x;
		} else if(direction == 1) {
			tx = x + SIZE.getWidth();
		}
		Torpedo t = new Torpedo(tx, y + SIZE.getHeight()/2, this);
		GameFrame.torpedoes.add(t);
	}

	public void destroyed() {
		//Drop a powerup crate when destroyed
		if(true /*ThreadLocalRandom.current().nextInt(0, 10) == 1*/) {
			GameFrame.crates.add(new Crate(getCenterX() - Crate.SIZE.getWidth(), getCenterY() - Crate.SIZE.getHeight()));
		}
	}


	public Submarine() {
		y = 0;
		if(GameFrame.inGame || Main.testing) {
			y = ThreadLocalRandom.current().nextDouble(GameFrame.OCEANLEVEL + SIZE.getHeight(), GameFrame.OCEANFLOORLEVEL - SIZE.getHeight());
		} else {
			y = ThreadLocalRandom.current().nextDouble(0, GameFrame.SIZE.getHeight() - SIZE.getHeight());
		}
		int side = ThreadLocalRandom.current().nextInt(0, 2);
		if(side == 1) {
			x = -(SIZE.getWidth());
			direction = 1;
		} else if (side == 2) {
			x = GameArea.SIZE.getWidth();
			direction = 0;
		} else { //I dont know why this should happen
			x = GameArea.SIZE.getWidth();
			direction = 0;
		}
		width = SIZE.getWidth();
		height = SIZE.getHeight();
	}

}
