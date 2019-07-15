package com.pigcoder.subtrouble;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class Explosion extends Rectangle2D.Double {

	public static final Dimension SIZE = new Dimension(30, 30);

	public static final long DEFAULTTIMEUNTILDISSAPEAR = 100;

	public long timeUntilDisappear = DEFAULTTIMEUNTILDISSAPEAR;
	public long pauseTimeSinceCreated = 0;
	public long timeCreated = 0;
	public long timeSinceCreated = 0;

	public Explosion(int x, int y) {
		this.x = x;
		this.y = y;
		this.width = SIZE.getWidth();
		this.height = SIZE.getHeight();
		timeCreated = System.currentTimeMillis();
	}
}
