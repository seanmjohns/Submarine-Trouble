package com.pigcoder.subtrouble;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.*;

public class Main {

	public static boolean testing = false;

	public static GameFrame window;

	//Pictures
	public static File ocean1ImageFile = new File("Ocean1.png");
	public static Image ocean1Image;
	public static int ocean1ImageOffset = -3;

	public static File ocean2ImageFile = new File("Ocean2.png");
	public static Image ocean2Image;
	public static int ocean2ImageOffset = -3;

	public static File backgroundImageFile = new File("Background.png");
	public static Image backgroundImage;
	public static int backgroundImageOffset = 0;

	public static File explosionImageFile = new File("Explosion.png");
	public static Image explosionImage;

	public static void main(String[] args) {
		loadResources();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				window = new GameFrame();
				window.createGui(false);
			}
		});
	}

	public static boolean loadResources() {
		try {
			Player.destroyerImage = ImageIO.read(Main.class.getResource("/" + Player.destroyerImageFile.getName()));
			Submarine.submarineImage = ImageIO.read(Main.class.getResource("/" + Submarine.submarineImageFile.getName()));
			Submarine.detectedPlayerImage = ImageIO.read(Main.class.getResource("/" + Submarine.detectedPlayerImageFile.getName()));
			Torpedo.torpedoImage = ImageIO.read(Main.class.getResource("/" + Torpedo.torpedoImageFile.getName()));
			DepthCharge.depthChargeImage = ImageIO.read(Main.class.getResource("/" + DepthCharge.depthChargeImageFile.getName()));
			DepthCharge.depthChargeIndicatorImage = ImageIO.read(Main.class.getResource("/" + DepthCharge.depthChargeIndicatorImageFile.getName()));
			Crate.speedCrateImage = ImageIO.read(Main.class.getResource("/" + Crate.speedCrateImageFile.getName()));
			Crate.armorCrateImage = ImageIO.read(Main.class.getResource("/" + Crate.armorCrateImageFile.getName()));
			Crate.healthCrateImage = ImageIO.read(Main.class.getResource("/" + Crate.healthCrateImageFile.getName()));
			ocean1Image = ImageIO.read(Main.class.getResource("/" + ocean1ImageFile.getName()));
			ocean2Image = ImageIO.read(Main.class.getResource("/" + ocean2ImageFile.getName()));
			backgroundImage = ImageIO.read(Main.class.getResource("/" + backgroundImageFile.getName()));
			explosionImage = ImageIO.read(Main.class.getResource("/" + explosionImageFile.getName()));
			return true;
		} catch(IOException e) {
			e.printStackTrace();
			return false;
		}
	}

}

class GameFrame extends JFrame {

	//Areas
	public static JPanel area;

	public static final Dimension SIZE = new Dimension(600, 400);

	public static BasicMenu mainMenu = new BasicMenu( "Main Menu", new String[] {"PLAY", "HELP", "TOGGLE SUBS", "QUIT"}, new Color(0,0, 200, 100), new Color(0,0,0, 200), new Color(0,0,200, 200), 0, 0);
	public static BasicMenu difficultyMenu = new BasicMenu( "Difficulty Menu", new String[] {"EASY", "NORMAL", "HARD", "BACK"}, new Color(0,0, 200, 100), new Color(0,0,0, 200), new Color(0,0,200, 200),0, 0);
	public static BasicMenu helpMenu = new HelpMenu("Help Menu", new String[] {"BACK"}, new Color(0,0, 200, 100), new Color(0,0,0, 200), new Color(0,0,200, 200), 0, (int)SIZE.getHeight()/2 - 35);

	public static final int OCEANLEVEL = (int)(GameArea.SIZE.getHeight()/4);
	public static final int OCEANFLOORLEVEL = (int)(GameArea.SIZE.getHeight()-40);
	public static final int MAXIMUMOCEANFLOORLEVEL = (int)(GameArea.SIZE.getHeight()-20);

	//Basic game vars
	public static boolean inGame = false;
	public static boolean gameOver = false;
	public static boolean paused = false;
	public static boolean inMainMenu = false;
	public static boolean inDifficultyMenu = false;
	public static boolean inHelpMenu = false;
	public static boolean backgroundDisabled = false;
	public static int score = 0;
	public static int difficulty = 2;
	public static Player player;
	public static ArrayList<Submarine> submarines = new ArrayList<>();
	public static ArrayList<Torpedo> torpedoes = new ArrayList<>();
	public static ArrayList<DepthCharge> depthCharges = new ArrayList<>();
	public static ArrayList<Explosion> explosions = new ArrayList<>();
	public static ArrayList<Crate> crates = new ArrayList<>();
	public static ArrayList<ScoreIncrease> scoreIncreases = new ArrayList<>();
	public static ArrayList<String> keysHeld = new ArrayList<>();
	public static long timeAtPause = 0;
	public static long timeSincePaused = 0;

	//Sub speeds
	public static double subEasySpeed = 2.25;
	public static double subNormalSpeed = 2.25;
	public static double subHardSpeed = 3;
	public static double subMenuSpeed = 2;

	//Tasks
	public static ScheduledThreadPoolExecutorThatCatchesErrors es = new ScheduledThreadPoolExecutorThatCatchesErrors(10);

	public static Runnable repainter;
	public static final long repaintDelay = 16;
	public static Runnable subMover;
	public static final long subMoverDelay = 50;
	public static Runnable subCreator;
	public static final long easySubCreatorDelay = 7500;
	public static final long normalSubCreatorDelay = 5000;
	public static final long hardSubCreatorDelay = 2500;
	public static final long menuSubCreatorDelay = 50;
	public static Runnable torpedoMover;
	public static final long torpedoMoverDelay = 50;
	public static Runnable depthChargeMover;
	public static final long depthChargeMoverDelay = 50;
	public static Runnable keybindManager;
	public static final long keybindManagerDelay = 15;
	public static Runnable crateMover;
	public static final long crateMoverDelay = 50;
	public static Runnable powerupManager;
	public static final long powerupManagerDelay = 16;

	public static ScheduledFuture repainterInAction;
	public static ScheduledFuture subMoverInAction;
	public static ScheduledFuture subCreatorInAction;
	public static ScheduledFuture torpedoMoverInAction;
	public static ScheduledFuture depthChargeMoverInAction;
	public static ScheduledFuture keybindManagerInAction;
	public static ScheduledFuture crateMoverInAction;
	public static ScheduledFuture powerupManagerInAction;

	public void createGui(boolean testing) {

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(false);
		this.setTitle("Pigcoder's Submarine Trouble");

		if(!testing) {goToMainMenu(true);}
		this.setVisible(!testing);

		createKeybinds();

		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if(player != null && !gameOver && !paused && inGame) {
					player.timeSinceLastDrop = System.currentTimeMillis() - player.timePausedSinceLastDrop - player.timeOfLastDrop;
					if (player.timeSinceLastDrop > player.currentDepthChargeDropDelay) {
						if (e.getY() - Main.window.getInsets().top - Main.window.getInsets().bottom > OCEANLEVEL) {
							player.timeSinceLastDrop = 0;
							player.timePausedSinceLastDrop = 0;
							player.timeOfLastDrop = System.currentTimeMillis();
							depthCharges.add(new DepthCharge(player.getCenterX() - DepthCharge.SIZE.getWidth() / 2, player.getCenterY() - DepthCharge.SIZE.getHeight() / 2, e.getY() - Main.window.getInsets().top - Main.window.getInsets().bottom));
						}
					}
				}
			}
		});
		if(!testing) {createExecutorTasks();}
		this.addWindowFocusListener(new WindowAdapter() {
			@Override
			public void windowLostFocus(WindowEvent e) {
				pause();
			}
		});
	}

	public void changeContentArea() {
		this.setContentPane(area);
		this.pack();
	}

	public void goToMainMenu(boolean resetSubs) {
		area = mainMenu;
		changeContentArea();
		((BasicMenu)area).selectedButton = 1;
		if(resetSubs) { submarines.clear(); }
		torpedoes.clear();
		depthCharges.clear();
		crates.clear();
		restartTask(subCreatorInAction, subCreator, menuSubCreatorDelay, true);
		restartTask(subMoverInAction, subMover, subMoverDelay, true);
		paused = false;
		inGame = false;
		gameOver = false;
		inMainMenu = true;
		inDifficultyMenu = false;
		inHelpMenu = false;
	}

	public void goToDifficultyMenu() {
		area = difficultyMenu;
		changeContentArea();
		((BasicMenu)area).selectedButton = 1;
		paused = false;
		inGame = false;
		gameOver = false;
		inMainMenu = false;
		inDifficultyMenu = true;
		inHelpMenu = false;
	}

	public void goToHelpMenu() {
		area = helpMenu;
		changeContentArea();
		((BasicMenu)area).selectedButton = 1;
		paused = false;
		inGame = false;
		gameOver = false;
		inMainMenu = false;
		inDifficultyMenu = false;
		inHelpMenu = true;
	}

	public void startGame(int difficulty) { //1: easy, 2: normal, 3: hard
		area = new GameArea();
		changeContentArea();
		player = new Player();
		GameFrame.difficulty = difficulty;
		submarines.clear();
		torpedoes.clear();
		depthCharges.clear();
		crates.clear();
		explosions.clear();
		keysHeld.clear();
		scoreIncreases.clear();
		restartTask(powerupManagerInAction, powerupManager, powerupManagerDelay, true);
		if(difficulty == 1) {
			restartTask(subCreatorInAction, subCreator, easySubCreatorDelay, true);
			restartTask(subMoverInAction, subMover, subMoverDelay, true);
			player.currentSpeedIncrease = Player.easySpeedIncrease;
			player.currentMaxSpeed = Player.easyMaxSpeed;
			player.currentDepthChargeDropDelay = Player.easyDepthChargeDropDelay;
		} else if(difficulty == 2) {
			restartTask(subCreatorInAction, subCreator, normalSubCreatorDelay, true);
			restartTask(subMoverInAction, subMover, subMoverDelay, true);
			player.currentSpeedIncrease = Player.normalSpeedIncrease;
			player.currentMaxSpeed = Player.normalMaxSpeed;
			player.currentDepthChargeDropDelay = Player.normalDepthChargeDropDelay;
		} else if(difficulty == 3) {
			restartTask(subCreatorInAction, subCreator, hardSubCreatorDelay, true);
			restartTask(subMoverInAction, subMover, subMoverDelay, true);
			player.currentSpeedIncrease = Player.hardSpeedIncrease;
			player.currentMaxSpeed = Player.hardMaxSpeed;
			player.currentDepthChargeDropDelay = Player.hardDepthChargeDropDelay;
		}
		submarines.clear();
		torpedoes.clear();
		depthCharges.clear();
		crates.clear();
		explosions.clear();
		keysHeld.clear();
		scoreIncreases.clear();
		score = 0;
		paused = false;
		inGame = true;
		gameOver = false;
		inMainMenu = false;
		inDifficultyMenu = false;
		submarines.clear();
		torpedoes.clear();
		depthCharges.clear();
		crates.clear();
		explosions.clear();
		keysHeld.clear();
		scoreIncreases.clear();
	}

	public void restartTask(ScheduledFuture task, Runnable runnable, long delay, boolean mayInterruptIfRunning) {
		if (task != null) {
			task.cancel(mayInterruptIfRunning);
		}
		if (runnable != null) {
			ScheduledFuture currentTask = es.scheduleAtFixedRate(runnable, 0, delay, TimeUnit.MILLISECONDS);
			if(task == repainterInAction) { repainterInAction = currentTask; }
			else if(task == subMoverInAction) { subMoverInAction = currentTask; }
			else if(task == subCreatorInAction) { subCreatorInAction = currentTask; }
			else if(task == torpedoMoverInAction) { torpedoMoverInAction = currentTask; }
			else if(task == depthChargeMoverInAction) { depthChargeMoverInAction = currentTask; }
			else if(task == keybindManagerInAction) { keybindManagerInAction = currentTask; }
			else if(task == crateMoverInAction) { crateMoverInAction = currentTask; }
			else if(task == powerupManagerInAction) { powerupManagerInAction = currentTask; }
		}
	}

	public void pause() {
		paused = true;
		timeAtPause = System.currentTimeMillis();
	}

	public void unpause() {
		timeSincePaused = System.currentTimeMillis() - timeAtPause;
		if(player.hasArmorBoost) {
			player.timePausedWhileHadArmorBoost += timeSincePaused;
		} if(player.hasSpeedBoost) {
			player.timePausedWhileHadSpeedBoost += timeSincePaused;
		}
		player.timePausedSinceLastDrop += timeSincePaused;
		for(Explosion e : new ArrayList<>(explosions)) {
			e.pauseTimeSinceCreated += timeSincePaused;
		}
		timeSincePaused = 0;
		paused = false;
	}

	public void createKeybinds() {

		//Add key listeners
		InputMap im = this.getRootPane().getInputMap(JComponent.WHEN_FOCUSED);
		ActionMap am = this.getRootPane().getRootPane().getActionMap();
		im.put(KeyStroke.getKeyStroke("W"), "up");
		im.put(KeyStroke.getKeyStroke("A"), "left");
		im.put(KeyStroke.getKeyStroke("S"), "down");
		im.put(KeyStroke.getKeyStroke("D"), "right");
		im.put(KeyStroke.getKeyStroke("UP"), "up");
		im.put(KeyStroke.getKeyStroke("LEFT"), "left");
		im.put(KeyStroke.getKeyStroke("DOWN"), "down");
		im.put(KeyStroke.getKeyStroke("RIGHT"), "right");
		im.put(KeyStroke.getKeyStroke("ENTER"), "select");
		im.put(KeyStroke.getKeyStroke("ESCAPE"), "quit");
		im.put(KeyStroke.getKeyStroke("R"), "restart");
		im.put(KeyStroke.getKeyStroke("E"), "pause");
		im.put(KeyStroke.getKeyStroke("F"), "disableBackground");

		am.put("up", new KeyBinder("up"));
		am.put("left", new KeyBinder("left"));
		am.put("down", new KeyBinder("down"));
		am.put("right", new KeyBinder("right"));
		am.put("select", new KeyBinder("select"));
		am.put("quit", new KeyBinder("quit"));
		am.put("restart", new KeyBinder("restart"));
		am.put("pause", new KeyBinder("pause"));
		am.put("disableBackground", new KeyBinder("disableBackground"));

		//Released keys
		im.put(KeyStroke.getKeyStroke("released W"), "releasedup");
		im.put(KeyStroke.getKeyStroke("released A"), "releasedleft");
		im.put(KeyStroke.getKeyStroke("released S"), "releaseddown");
		im.put(KeyStroke.getKeyStroke("released D"), "releasedright");
		im.put(KeyStroke.getKeyStroke("released UP"), "releasedup");
		im.put(KeyStroke.getKeyStroke("released LEFT"), "releasedleft");
		im.put(KeyStroke.getKeyStroke("released DOWN"), "releaseddown");
		im.put(KeyStroke.getKeyStroke("released RIGHT"), "releasedright");

		am.put("releasedup", new KeyBinder("releasedup"));
		am.put("releasedleft", new KeyBinder("releasedleft"));
		am.put("releaseddown", new KeyBinder("releaseddown"));
		am.put("releasedright", new KeyBinder("releasedright"));

	}

	public void createExecutorTasks() {

		repainter = new Runnable() {
			@Override
			public void run() {
				if(area != null) {
					if(player != null && !paused && inGame) {
						player.timeSinceLastDrop = System.currentTimeMillis() - player.timePausedSinceLastDrop - player.timeOfLastDrop;
					}
					if(!paused) {
						//Score increases
						ArrayList<ScoreIncrease> newScoreIncreases = new ArrayList<>();
						for(ScoreIncrease si : new ArrayList<>(scoreIncreases)) {
							si.move();
							if(!(si.y <= si.targetY)) {
								newScoreIncreases.add(si);
							}
						}
						scoreIncreases = newScoreIncreases;
						//Explosions
						ArrayList<Explosion> newExplosions = new ArrayList<>();
						for (Explosion e : new ArrayList<>(explosions)) {
							e.timeSinceCreated = System.currentTimeMillis() - e.timeCreated - e.pauseTimeSinceCreated;
							e.timeUntilDisappear = Explosion.DEFAULTTIMEUNTILDISSAPEAR - e.timeSinceCreated;
							if (!(e.timeUntilDisappear <= 0)) {
								newExplosions.add(e);
							}
						}
						explosions = newExplosions;
					}

					//Repaint
					area.repaint();
				}
			}
		};

		//Repaint the screen about 60 fps
		repainterInAction = ((ScheduledExecutorService) es).scheduleAtFixedRate(repainter, 0, repaintDelay, TimeUnit.MILLISECONDS);

		subMover = new Runnable() {
			@Override
			public void run() {
				if((!gameOver && !paused && inGame) || ((inDifficultyMenu || inMainMenu || inHelpMenu) && !backgroundDisabled)) {
					try {
						ArrayList<Submarine> newSubs = new ArrayList<>();
						for (Submarine sub : new ArrayList<>(submarines)) {
							sub.move();
							//If on the screen
							if (!(sub.x < 0 - Submarine.SIZE.getWidth() || sub.x > GameArea.SIZE.getWidth())) {
								newSubs.add(sub);
							} else {
								if (sub.targetPoint != null) {
									newSubs.add(sub);
								}
							}
						}
						submarines = newSubs;
					} catch(NullPointerException e) { } //This will happen on an extremely rare case when the player restarts (Which clears the subs)
				}
			}
		};

		subCreator = new Runnable() {
			@Override
			public void run() {
				if((!gameOver && !paused && inGame) || ((inDifficultyMenu || inMainMenu || inHelpMenu) && !backgroundDisabled)) {
					Submarine sub = new Submarine();
					if(!inGame) {
						sub.speed = subMenuSpeed;
					} else if(difficulty == 1) {
						sub.speed = subEasySpeed;
					} else if (difficulty == 2) {
						sub.speed = subNormalSpeed;
					} else if (difficulty == 3) {
						sub.speed = subHardSpeed;
					}
					submarines.add(sub);
				}
			}
		};

		depthChargeMover = new Runnable() {
			@Override
			public void run() {
				if(!gameOver && !paused) {
					for(DepthCharge dc : new ArrayList<>(depthCharges)) {
						dc.move();
					}
				}
			}
		};

		torpedoMover = new Runnable() {
			@Override
			public void run() {
				if(player != null && !gameOver && !paused) {
					ArrayList<Torpedo> newTorpedoes = new ArrayList<>();
					for (Torpedo t : new ArrayList<>(torpedoes)) {
						t.move();
						if(t.y >= GameFrame.OCEANFLOORLEVEL) {
							t.targetPoint = null;
							continue;
						} else if(t.y <= GameFrame.OCEANLEVEL) {
							t.targetPoint = null;
							continue;
						}

						//Damage the player if it collides
						if (t.intersects(player)) {
							player.damaged(Torpedo.damage);
							GameFrame.explosions.add(new Explosion((int)t.getCenterX(), (int)t.getCenterY()));
							continue;
						}

						/*/Damage a sub if one is hit by a torpedo
						ArrayList<Submarine> newSubs = new ArrayList<>();
						for (Submarine sub : new ArrayList<>(submarines)) {
							boolean subShouldBeKept = true;
							if (tShouldBeKept) {
								if (t.intersects(sub)) {
									if (t.parentSub != null && !(t.parentSub == sub)) {
										sub.damaged(t.damage);
										if (!(sub.health < 1)) {
											subShouldBeKept = true;
											tShouldBeKept = false;
										} else {
											subShouldBeKept = false;
											tShouldBeKept = false;
										}
									} else {
										subShouldBeKept = true;
										tShouldBeKept = true;
									}
								} else {
									subShouldBeKept = true;
								}
							}
							if (subShouldBeKept) {
								newSubs.add(sub);
							}
						}
						submarines = newSubs;
						*/
						newTorpedoes.add(t);
					}
					torpedoes = newTorpedoes;
				}
			}
		};

		keybindManager = new Runnable() {
			@Override
			public void run() {
				if(player != null && !gameOver && !paused) {
					if(!player.hasSpeedBoost) {
						if (keysHeld.contains("left")) {
							player.xVel = player.xVel - player.currentSpeedIncrease;
						}
						if (keysHeld.contains("right")) {
							player.xVel = player.xVel + player.currentSpeedIncrease;
						} else if (!(keysHeld.contains("left") && !keysHeld.contains("right"))) {
							player.xVel = player.xVel / 1.01;
						}
						if (player.xVel > player.currentMaxSpeed) {
							player.xVel = player.currentMaxSpeed;
						} else if (player.xVel < -player.currentMaxSpeed) {
							player.xVel = -player.currentMaxSpeed;
						}
					} else {
						if (keysHeld.contains("left")) {
							player.xVel = player.xVel - player.currentSpeedIncrease*2;
						}
						if (keysHeld.contains("right")) {
							player.xVel = player.xVel + player.currentSpeedIncrease*2;
						} else if (!(keysHeld.contains("left") && !keysHeld.contains("right"))) {
							player.xVel = player.xVel / 1.01;
						}
						if (player.xVel > player.currentMaxSpeed*2) {
							player.xVel = player.currentMaxSpeed*2;
						} else if (player.xVel < -player.currentMaxSpeed*2) {
							player.xVel = -player.currentMaxSpeed*2;
						}
					}

					if (player.x + player.xVel > GameArea.SIZE.getWidth() - Player.SIZE.getWidth()) {
						player.xVel = 0;
					} else if (player.x + player.xVel < 0) {
						player.xVel = 0;
					}

					//Change direction if necessary
					if (player.xVel < 0) {
						player.direction = 0;
					} else if (player.xVel > 0) {
						player.direction = 1;
					}

					player.x += player.xVel;
				} if(inMainMenu) {

				}
			}
		};

		crateMover = new Runnable() {
			@Override
			public void run() {
				if(!gameOver && !paused) {
					for (Crate c : crates) {
						c.move();
					}
				}
			}
		};

		powerupManager = new Runnable() {
			@Override
			public void run() {
				if(player != null && inGame && !paused) {
					//Deal with powerup times
					//Armor
					if (player.hasArmorBoost) {
						player.timeSinceCollectedArmorBoost = System.currentTimeMillis() - player.timeCollectedArmorBoost - player.timePausedWhileHadArmorBoost;
						player.timeLeftWithArmorBoost = Crate.SPEEDTIME - player.timeSinceCollectedArmorBoost;
						if (Crate.ARMORTIME - player.timeSinceCollectedArmorBoost <= 0) {
							player.hasArmorBoost = false;
							player.timeLeftWithArmorBoost = 0;
							player.timeCollectedArmorBoost = 0;
							player.timeSinceCollectedArmorBoost = 0;
							player.timePausedWhileHadArmorBoost = 0;
						}
					}
					//Speed
					if (player.hasSpeedBoost) {
						player.timeSinceCollectedSpeedBoost = System.currentTimeMillis() - player.timeCollectedSpeedBoost - player.timePausedWhileHadSpeedBoost;
						player.timeLeftWithSpeedBoost = Crate.SPEEDTIME - player.timeSinceCollectedSpeedBoost;
						if (Crate.SPEEDTIME - player.timeSinceCollectedSpeedBoost <= 0) {
							player.hasSpeedBoost = false;
							player.timeLeftWithSpeedBoost = 0;
							player.timeCollectedSpeedBoost = 0;
							player.timeSinceCollectedSpeedBoost = 0;
							player.timePausedWhileHadSpeedBoost = 0;
						}
					}
				}
			}
		};

		//The repainter is above if you were wondering

		//Move enemies
		subMoverInAction = ((ScheduledExecutorService) es).scheduleAtFixedRate(subMover,0, subMoverDelay, TimeUnit.MILLISECONDS);

		//Create enemies every 5 seconds
		subCreatorInAction = ((ScheduledExecutorService) es).scheduleAtFixedRate(subCreator, 0, menuSubCreatorDelay, TimeUnit.MILLISECONDS);

		//Move depth charges
		depthChargeMoverInAction = ((ScheduledExecutorService) es).scheduleAtFixedRate(depthChargeMover, 0, depthChargeMoverDelay, TimeUnit.MILLISECONDS);

		//Move torpedoes
		torpedoMoverInAction = ((ScheduledExecutorService) es).scheduleAtFixedRate(torpedoMover, 0, torpedoMoverDelay, TimeUnit.MILLISECONDS);

		//Keybinds
		keybindManagerInAction = ((ScheduledExecutorService) es).scheduleAtFixedRate(keybindManager, 0, keybindManagerDelay, TimeUnit.MILLISECONDS);

		//Move crates
		crateMoverInAction = ((ScheduledExecutorService) es).scheduleAtFixedRate(crateMover, 0, crateMoverDelay, TimeUnit.MILLISECONDS);

		//Manage the current powerups
		powerupManagerInAction = ((ScheduledExecutorService) es).scheduleAtFixedRate(powerupManager, 0, powerupManagerDelay, TimeUnit.MILLISECONDS);
	}

}

class GameArea extends JPanel {

	public static final Dimension SIZE = new Dimension(600, 400);

	public GameArea() {
		this.setPreferredSize(SIZE);
		generateOceanFloor();
		generateOceanSurface();
	}

	public static final int healthBarWidth = 100;
	public static final int healthBarHeight = 10;
	public static final int healthBarOffsetX = (int)(SIZE.getWidth()/2 - healthBarWidth/2);
	public static final int healthBarOffsetY = 10;

	public static final int scoreOffsetX = 10;
	public static final int scoreOffsetY = 20;

	public static final int reloadBarOffsetY = 10;
	public static final int reloadBarWidth = 10;
	public static final int reloadBarOffsetX = (int)(SIZE.getWidth() - 10 - reloadBarWidth);
	public static final int reloadBarHeight = 100 - reloadBarOffsetY*2;

	public static final int speedBoostBarOffsetY = 10;
	public static final int speedBoostBarWidth = 10;
	public static final int speedBoostBarOffsetX = (int)(SIZE.getWidth() - 10*2 - reloadBarWidth - speedBoostBarWidth);
	public static final int speedBoostBarHeight = 100 - reloadBarOffsetY*2;

	public static final int armorBoostBarOffsetY = 10;
	public static final int armorBoostBarWidth = 10;
	public static final int armorBoostBarOffsetX = (int)(SIZE.getWidth() - 10*3 - reloadBarWidth - speedBoostBarWidth - armorBoostBarWidth);
	public static final int armorBoostBarHeight = 100 - reloadBarOffsetY*2;


	public ArrayList<Rectangle2D.Double> oceanFloor = new ArrayList<>();
	public ArrayList<Image> oceanImages = new ArrayList<>();

	public void generateOceanFloor() {
		double lastHeight = ThreadLocalRandom.current().nextInt(GameFrame.OCEANFLOORLEVEL, GameFrame.MAXIMUMOCEANFLOORLEVEL);

		final int rectWidth = 8;

		for(int i=0; i<SIZE.getWidth()/rectWidth; i++) {
			if(lastHeight - rectWidth <= GameFrame.OCEANFLOORLEVEL) {
				lastHeight = lastHeight + ThreadLocalRandom.current().nextDouble(0, 4);
			} else if(lastHeight + rectWidth >= GameFrame.MAXIMUMOCEANFLOORLEVEL) {
				lastHeight = lastHeight + ThreadLocalRandom.current().nextDouble(-4, 0);
			} else {
				lastHeight = lastHeight + ThreadLocalRandom.current().nextDouble(-4, 4);
			}
			oceanFloor.add(new Rectangle2D.Double(i*rectWidth, lastHeight, rectWidth, SIZE.getHeight() - lastHeight));
		}
	}

	public void generateOceanSurface() {
		if(Main.ocean1Image != null && Main.ocean2Image != null) {
			for (int i = 0; i < Math.ceil(SIZE.getWidth() / Main.ocean1Image.getWidth(null)); i++) {
				int num = ThreadLocalRandom.current().nextInt(1, 3);
				if (num == 1) {
					oceanImages.add(Main.ocean1Image);
				} else if (num == 2) {
					oceanImages.add(Main.ocean2Image);
				}
			}
		}
	}

	@Override
	public void paintComponent(Graphics g) {

		Graphics2D g2 = (Graphics2D)(g);

		g2.setRenderingHints(new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON));

		g2.setFont(new Font("Lucida Grande", g2.getFont().getStyle(), g2.getFont().getSize()));

		//Background
		if(Main.backgroundImage != null) {
			g2.drawImage(Main.backgroundImage, 0,0 + Main.backgroundImageOffset, (int)Main.backgroundImage.getWidth(null), Main.backgroundImage.getHeight(null), null);
		} else {
			g2.setPaint(Color.WHITE);
			g2.fillRect(0, 0, (int) SIZE.getWidth(), (int) SIZE.getHeight());
		}
		//the ocean
		g2.setPaint(new Color(29,26,109));
		g2.fillRect(0, GameFrame.OCEANLEVEL, (int) SIZE.getWidth(), (int) SIZE.getHeight());
		for(int i=0; i<oceanImages.size() ; i++) {
			int offset = 0;
			if(oceanImages.get(i) == Main.ocean1Image) { offset = Main.ocean1ImageOffset; }
			else if(oceanImages.get(i) == Main.ocean2Image) { offset = Main.ocean2ImageOffset; }
			g2.drawImage(oceanImages.get(i), i * Main.ocean1Image.getWidth(null), GameFrame.OCEANLEVEL + offset, oceanImages.get(i).getWidth(null), oceanImages.get(i).getHeight(null), null);
		}


		//The ocean floor
		g2.setPaint(Color.BLACK);
		for(Rectangle2D.Double r : oceanFloor) {
			g2.draw(r);
			g2.fill(r);
		}

		//The torpedoes
		for(Torpedo t : new ArrayList<>(GameFrame.torpedoes)) {
			AffineTransform old = g2.getTransform();
			if(Torpedo.torpedoImage != null) {
				g2.translate(t.getCenterX(), t.getCenterY());
				g2.rotate(Math.sin(t.angle) + 90); //NOTE the sin here
				g2.drawImage(Torpedo.torpedoImage, (int)-Torpedo.SIZE.getWidth()/2, (int)-Torpedo.SIZE.getHeight()/2, (int)t.SIZE.getWidth(), (int)t.SIZE.getHeight(), null);
			} else {
				//g2.translate(t.getCenterX(), t.getCenterY());
				g2.rotate(t.angle); //Note the 90 here, this makes it point with the nose
				g2.setPaint(Color.LIGHT_GRAY);
				g2.fill(t);
				g2.setPaint(Color.BLACK);
				g2.draw(t);
			}
			//Reverse everything
			g2.setTransform(old);
		}

		if(GameFrame.player != null) {
			//the Player
			if (Player.destroyerImage != null) {
				if(GameFrame.player.direction == 0) {
					g2.drawImage(Player.destroyerImage, (int) GameFrame.player.x + (int)GameFrame.player.width, (int) GameFrame.player.y, (int) -Player.SIZE.getWidth(), (int) Player.SIZE.getHeight(), null);
				} else {
					g2.drawImage(Player.destroyerImage, (int) GameFrame.player.x, (int) GameFrame.player.y, (int) Player.SIZE.getWidth(), (int) Player.SIZE.getHeight(), null);
				}
			} else { //Just draw a rectangle in the image's place if unable to draw the image
				g2.setPaint(Color.LIGHT_GRAY);
				g2.fill(GameFrame.player);
				g2.setPaint(Color.BLACK);
				g2.draw(GameFrame.player);
			}
		}

		//Draw the crates
		for(Crate c : new ArrayList<>(GameFrame.crates)) {
			if(Crate.healthCrateImage != null && Crate.speedCrateImage != null && Crate.armorCrateImage != null) {
				if(c.type == 1) { //Health
					g2.drawImage(Crate.healthCrateImage, (int)c.x, (int)c.y, (int)c.SIZE.getWidth(), (int)c.SIZE.getHeight(), null);
				} else if(c.type == 2) { //Speed
					g2.drawImage(Crate.speedCrateImage, (int)c.x, (int)c.y, (int)c.SIZE.getWidth(), (int)c.SIZE.getHeight(), null);
				} else if(c.type == 3) { //Armor
					g2.drawImage(Crate.armorCrateImage, (int)c.x, (int)c.y, (int)c.SIZE.getWidth(), (int)c.SIZE.getHeight(), null);
				}

			} else {
				if(c.type == 1) { //Health
					g2.setPaint(Color.PINK);
				} else if(c.type == 2) { //Speed
					g2.setPaint(new Color(50,100,255)); //Light Blue
				} else if(c.type == 3) { //Armor
					g2.setPaint(Color.ORANGE);
				}
				g2.fill(c);
				g2.setPaint(Color.BLACK);
				g2.draw(c);
			}
			//Health bar
			g2.setPaint(Color.RED);
			g2.fillRect((int)c.x, (int)(c.y - Crate.healthBarOffsetY), Crate.healthBarWidth, Crate.healthBarHeight);
			g2.setPaint(Color.GREEN);
			g2.fillRect((int)c.x, (int)(c.y - Crate.healthBarOffsetY), (int)(Crate.healthBarWidth*(c.health/Crate.MAXHEALTH)), Crate.healthBarHeight);
			g2.setPaint(Color.BLACK);
			g2.drawRect((int)c.x, (int)(c.y - Crate.healthBarOffsetY), Crate.healthBarWidth, Crate.healthBarHeight);
		}

		//Draw the subs
		try {
			for (Submarine sub : new ArrayList<>(GameFrame.submarines)) {
				if(sub.detectedPlayer && Submarine.detectedPlayerImage != null) {
					g2.drawImage(Submarine.detectedPlayerImage, (int) sub.x + (int) Submarine.SIZE.getWidth()/2 - Submarine.detectedPlayerImage.getWidth(null)/2, (int) sub.y - Submarine.detectedPlayerImage.getHeight(null) - Submarine.healthBarHeight - Submarine.healthBarOffsetY, Submarine.detectedPlayerImage.getWidth(null), Submarine.detectedPlayerImage.getHeight(null), null);
				}
				if (Submarine.submarineImage != null) {
					if (sub.direction == 1) {
						g2.drawImage(Submarine.submarineImage, (int) sub.x + (int) Submarine.SIZE.getWidth(), (int) sub.y, (int) -sub.SIZE.getWidth(), (int) sub.SIZE.getHeight(), null);
					} else {
						g2.drawImage(Submarine.submarineImage, (int) sub.x, (int) sub.y, (int) sub.SIZE.getWidth(), (int) sub.SIZE.getHeight(), null);
					}

				} else {
					g2.setPaint(Color.LIGHT_GRAY);
					g2.fill(sub);
					g2.setPaint(Color.BLACK);
					g2.draw(sub);
				}
				//Health bar
				g2.setPaint(Color.RED);
				g2.fillRect((int) sub.x, (int) (sub.y - Submarine.healthBarOffsetY), Submarine.healthBarWidth, Submarine.healthBarHeight);
				g2.setPaint(Color.GREEN);
				g2.fillRect((int) sub.x, (int) (sub.y - Submarine.healthBarOffsetY), (int) (Submarine.healthBarWidth * (sub.health / Submarine.MAXHEALTH)), Submarine.healthBarHeight);
				g2.setPaint(Color.BLACK);
				g2.drawRect((int) sub.x, (int) (sub.y - Submarine.healthBarOffsetY), Submarine.healthBarWidth, Submarine.healthBarHeight);
			}
		} catch(NullPointerException e) { }

		//The depth charges
		for(DepthCharge dc : new ArrayList<>(GameFrame.depthCharges)) {
			if(DepthCharge.depthChargeImage != null) {
				g2.drawImage(DepthCharge.depthChargeImage, (int)dc.x, (int)dc.y, (int)dc.width, (int)dc.height, null);
			} else {
				g2.setPaint(Color.LIGHT_GRAY);
				g2.fill(dc);
				g2.setPaint(Color.BLACK);
				g2.draw(dc);
			}
		}

		//The explosions. The explosion is only visible for 1 frame
		if(Main.explosionImage != null) {
			for(Explosion e : new ArrayList<>(GameFrame.explosions)) {
				AffineTransform old = g2.getTransform();
				g2.translate((int)e.getX(), (int)e.getY());
				g2.rotate(Math.toRadians(ThreadLocalRandom.current().nextDouble(0, 360)));
				g2.drawImage(Main.explosionImage,  -Main.explosionImage.getWidth(null)/2, -Main.explosionImage.getHeight(null)/2, (int)Main.explosionImage.getWidth(null), (int)Main.explosionImage.getHeight(null), null); //Note that I need 0 here because of the translation
				g2.setTransform(old);
			}
		}

		//The indicator
		try {
			if (this.getMousePosition() != null && this.getMousePosition().getY() > GameFrame.OCEANLEVEL) {
				if (this.getMousePosition().getY() > GameFrame.OCEANLEVEL + 20) {
					g2.drawLine((int) GameFrame.player.getCenterX(), GameFrame.OCEANLEVEL + 10, (int) GameFrame.player.getCenterX(), (int) this.getMousePosition().getY() - DepthCharge.depthChargeIndicatorImage.getHeight(null));
				}
				g2.drawImage(DepthCharge.depthChargeIndicatorImage, (int) GameFrame.player.getCenterX() - DepthCharge.depthChargeIndicatorImage.getWidth(null) / 2, (int) this.getMousePosition().getY() - DepthCharge.depthChargeIndicatorImage.getHeight(null) / 2, DepthCharge.depthChargeIndicatorImage.getWidth(null), DepthCharge.depthChargeIndicatorImage.getHeight(null), null);
			}
		} catch (NullPointerException e) { } //Dont do anything because that means that the mouse just disappeared somehow

		//The ocean blue tint
		g2.setPaint(new Color(0,0,200,100));
		g2.fillRect(0,GameFrame.OCEANLEVEL, (int)SIZE.getWidth(), (int)SIZE.getHeight());

		//The HUD

		//Health bar
		g2.setPaint(Color.RED);
		g2.fillRect(healthBarOffsetX, healthBarOffsetY, healthBarWidth, healthBarHeight);
		g2.setPaint(Color.GREEN);
		g2.fillRect(healthBarOffsetX, healthBarOffsetY, (int)(healthBarWidth*(GameFrame.player.health/Player.MAXHEALTH)), healthBarHeight);
		g2.setPaint(Color.BLACK);
		g2.drawRect(healthBarOffsetX, healthBarOffsetY, healthBarWidth, healthBarHeight);
		Font oldFont = g2.getFont();
		g2.setFont(new Font(g2.getFont().getName(), Font.PLAIN, 7));
		g2.drawString(GameFrame.player.health + " / " + Submarine.MAXHEALTH, (int)(healthBarOffsetX + healthBarWidth/2 - g2.getFontMetrics().stringWidth(GameFrame.player.health + " / " + Player.MAXHEALTH)/2), (int)(healthBarOffsetY + healthBarHeight/7*8));
		g2.setFont(oldFont);

		//Reload Bar
		g2.setPaint(Color.DARK_GRAY);
		g2.fillRect(reloadBarOffsetX, reloadBarOffsetY, reloadBarWidth, reloadBarHeight);
		g2.setPaint(Color.LIGHT_GRAY);
		if(GameFrame.player.timeSinceLastDrop >= GameFrame.player.currentDepthChargeDropDelay) {
			g2.fillRect(reloadBarOffsetX, reloadBarOffsetY, reloadBarWidth, reloadBarHeight);
		} else {
			g2.fillRect(reloadBarOffsetX , reloadBarOffsetY + reloadBarHeight - (int)(reloadBarHeight * ((double)(GameFrame.player.timeSinceLastDrop) / (double)GameFrame.player.currentDepthChargeDropDelay)), reloadBarWidth, (int) (reloadBarHeight * ((double)(GameFrame.player.timeSinceLastDrop) / (double)GameFrame.player.currentDepthChargeDropDelay)));
		}

		g2.setPaint(Color.BLACK);
		g2.drawRect(reloadBarOffsetX, reloadBarOffsetY, reloadBarWidth, reloadBarHeight);

		AffineTransform old = g2.getTransform();
		g2.setFont(new Font(g2.getFont().getName(), Font.PLAIN, 9));
		g2.translate(reloadBarOffsetX + reloadBarWidth/2 - g2.getFontMetrics().getHeight()/3, reloadBarOffsetY + reloadBarHeight/2 - g2.getFontMetrics().stringWidth("RELOAD")/2);
		g2.rotate(Math.toRadians(90));
		g2.drawString("RELOAD", 0,0);
		g2.setTransform(old);

		//Speed boost timer
		g2.setPaint(Color.DARK_GRAY);
		g2.fillRect(speedBoostBarOffsetX, speedBoostBarOffsetY, speedBoostBarWidth, speedBoostBarHeight);
		g2.setPaint(new Color(50,100,255));
		if(GameFrame.player.timeLeftWithSpeedBoost >= 0) {
			g2.fillRect(speedBoostBarOffsetX , speedBoostBarOffsetY + speedBoostBarHeight - (int)(speedBoostBarHeight * ((double)(GameFrame.player.timeLeftWithSpeedBoost) / (double)Crate.SPEEDTIME)), speedBoostBarWidth, (int)(speedBoostBarHeight * ((double)(GameFrame.player.timeLeftWithSpeedBoost) / (double)Crate.SPEEDTIME)));
		}

		g2.setPaint(Color.BLACK);
		g2.drawRect(speedBoostBarOffsetX, speedBoostBarOffsetY, speedBoostBarWidth, speedBoostBarHeight);
		old = g2.getTransform();
		g2.setFont(new Font(g2.getFont().getName(), Font.PLAIN, 9));
		g2.translate(speedBoostBarOffsetX + speedBoostBarWidth/2 - g2.getFontMetrics().getHeight()/3, speedBoostBarOffsetY + speedBoostBarHeight/2 - g2.getFontMetrics().stringWidth("SPEED")/2);
		g2.rotate(Math.toRadians(90));
		g2.drawString("SPEED", 0,0);
		g2.setTransform(old);

		//Armor boost timer
		g2.setPaint(Color.DARK_GRAY);
		g2.fillRect(armorBoostBarOffsetX, armorBoostBarOffsetY, armorBoostBarWidth, armorBoostBarHeight);
		g2.setPaint(Color.ORANGE);
		if(GameFrame.player.timeLeftWithArmorBoost >= 0) {
			g2.fillRect(armorBoostBarOffsetX , armorBoostBarOffsetY + armorBoostBarHeight - (int)(armorBoostBarHeight * ((double)(GameFrame.player.timeLeftWithArmorBoost) / (double)Crate.SPEEDTIME)), armorBoostBarWidth, (int)(armorBoostBarHeight * ((double)(GameFrame.player.timeLeftWithArmorBoost) / (double)Crate.ARMORTIME)));
		}

		g2.setPaint(Color.BLACK);
		g2.drawRect(armorBoostBarOffsetX, armorBoostBarOffsetY, armorBoostBarWidth, armorBoostBarHeight);

		old = g2.getTransform();
		g2.setFont(new Font(g2.getFont().getName(), Font.PLAIN, 9));
		g2.translate(armorBoostBarOffsetX + armorBoostBarWidth/2 - g2.getFontMetrics().getHeight()/3, armorBoostBarOffsetY + armorBoostBarHeight/2 - g2.getFontMetrics().stringWidth("ARMOR")/2);
		g2.rotate(Math.toRadians(90));
		g2.drawString("ARMOR", 0,0);
		g2.setTransform(old);

		//Score
		g2.setPaint(Color.BLACK);
		g2.setFont(new Font(g2.getFont().getName(), Font.BOLD, 12));
		g2.drawString(Integer.toString(GameFrame.score), scoreOffsetX, scoreOffsetY);

		//Paused
		if(GameFrame.paused) {
			g2.setFont(new Font(g2.getFont().getName(), Font.BOLD, 25));
			g2.drawString("PAUSED", (int)SIZE.getWidth()/2 - g2.getFontMetrics().stringWidth("PAUSED")/2, (int)SIZE.getHeight()/2 - g2.getFontMetrics().getHeight()/2);
			g2.setFont(new Font(g2.getFont().getName(), Font.BOLD, 10));
			g2.drawString("Press E to Continue", (int)SIZE.getWidth()/2 - g2.getFontMetrics().stringWidth("Press E to Continue")/2, (int)SIZE.getHeight()/2 - g2.getFontMetrics().getHeight()/2 + 10);
		}

		if(GameFrame.gameOver) {
			g2.setFont(new Font(g2.getFont().getName(), Font.BOLD, 25));
			g2.setPaint(Color.BLACK);
			g2.drawString("Press R to restart", (int)GameArea.SIZE.getWidth()/2 - g2.getFontMetrics().stringWidth("Press R to restart")/2, (int)GameArea.SIZE.getHeight()/2 - g2.getFontMetrics().getHeight()/2);
		}

		g2.setPaint(Color.BLACK);
		g2.setFont(new Font(g2.getFont().getName(), Font.BOLD, 10));
		if(GameFrame.difficulty == 1) {
			g2.drawString("EASY", (int)GameArea.SIZE.getWidth()/2 - g2.getFontMetrics().stringWidth("EASY")/2, healthBarOffsetY*2 + healthBarHeight*2);
		} else if(GameFrame.difficulty == 2) {
			g2.drawString("NORMAL", (int)GameArea.SIZE.getWidth()/2 - g2.getFontMetrics().stringWidth("NORMAL")/2, healthBarOffsetY*2 + healthBarHeight*2);
		} else if(GameFrame.difficulty == 3) {
			g2.drawString("HARD", (int)GameArea.SIZE.getWidth()/2 - g2.getFontMetrics().stringWidth("HARD")/2, healthBarOffsetY*2 + healthBarHeight*2);
		}

		//Bottom controls
		g2.setPaint(Color.LIGHT_GRAY);
		g2.setFont(new Font(g2.getFont().getName(), Font.BOLD, 10));
		g2.drawString("PAUSE - E      RESTART - R      QUIT - ESCAPE", 10, (int)SIZE.getHeight() - 10);

		//Score Increases
		g2.setPaint(Color.GREEN);
		g2.setFont(new Font(g2.getFont().getName(), Font.BOLD, 10));
		for(ScoreIncrease si : new ArrayList<>(GameFrame.scoreIncreases)) {
			g2.drawString(Integer.toString(si.amount), (int)si.x - g2.getFontMetrics().stringWidth(Integer.toString(si.amount)), (int)si.y);
		}

	}
}

class HelpMenu extends BasicMenu {

	public static final Dimension SIZE = new Dimension(600, 400);

	public static final int mainXOffset = 20;
	public static final int mainYOffset = 20;
	public static final int mainYBottomOffset = 50;

	public HelpMenu(String name, String[] buttonTexts, Color buttonColor, Color backgroundColor, Color buttonSelectedColor, int xOffset, int yOffset) {
		super(name, buttonTexts, buttonColor, backgroundColor, buttonSelectedColor, xOffset, yOffset);
	}

	public void paintComponent(Graphics g) {

		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D)g;

		g2.setRenderingHints(new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON));

		g2.setFont(new Font("Lucida Grande", g2.getFont().getStyle(), g2.getFont().getSize()));

		//The main background
		g2.setPaint(backgroundColor);
		g2.fillRoundRect(mainXOffset, mainYOffset, (int)SIZE.getWidth() - mainXOffset*2, (int)SIZE.getHeight() - mainYOffset*2 - mainYBottomOffset,20, 20);
		g2.setPaint(Color.BLACK);
		g2.drawRoundRect(mainXOffset, mainYOffset, (int)SIZE.getWidth() - mainXOffset*2, (int)SIZE.getHeight() - mainYOffset*2 - mainYBottomOffset,20, 20);

		//Header
		g2.setPaint(Color.WHITE);
		int headerY = mainYOffset + 10;
		g2.setFont(new Font(g2.getFont().getName(), Font.BOLD, 20));
		g2.drawString("HELP", (int)SIZE.getWidth()/2 - g2.getFontMetrics().stringWidth("HELP")/2, headerY + g2.getFontMetrics().getHeight());
		int headerLineY = headerY + g2.getFontMetrics().getHeight() + 10;
		g2.drawLine((int)SIZE.getWidth()/5*2, headerLineY, (int)SIZE.getWidth()/5*3, headerLineY);

		//Basics
		g2.setFont(new Font(g2.getFont().getName(), Font.PLAIN, 12));
		int basicsY = headerLineY + 10;
		g2.drawString("THIS IS YOU", (int)SIZE.getWidth()/5*1 - g2.getFontMetrics().stringWidth("THIS IS YOU")/2, basicsY + g2.getFontMetrics().getHeight());
		g2.drawString("AVOID THIS", (int)SIZE.getWidth()/5*2 - g2.getFontMetrics().stringWidth("AVOID THIS")/2, basicsY + g2.getFontMetrics().getHeight());
		g2.drawString("SHOT BY THIS", (int)SIZE.getWidth()/5*3 - g2.getFontMetrics().stringWidth("SHOT BY THIS")/2, basicsY + g2.getFontMetrics().getHeight());
		g2.drawString("DESTROY WITH THIS", (int)SIZE.getWidth()/5*4 - g2.getFontMetrics().stringWidth("DESTROY WITH THIS")/2, basicsY + g2.getFontMetrics().getHeight());
		int basicsPicsY = basicsY + g2.getFontMetrics().getHeight() + 10;
		g2.drawImage(Player.destroyerImage, (int)SIZE.getWidth()/5*1 - Player.destroyerImage.getWidth(null)/2, basicsPicsY, Player.destroyerImage.getWidth(null), Player.destroyerImage.getHeight(null), null);
		g2.drawImage(Torpedo.torpedoImage, (int)SIZE.getWidth()/5*2 - Torpedo.torpedoImage.getWidth(null)/2*2, basicsPicsY, Torpedo.torpedoImage.getWidth(null)*2, Torpedo.torpedoImage.getHeight(null)*2, null);
		g2.drawImage(Submarine.submarineImage, (int)SIZE.getWidth()/5*3 - Submarine.submarineImage.getWidth(null)/2*2, basicsPicsY, Submarine.submarineImage.getWidth(null)*2, Submarine.submarineImage.getHeight(null)*2, null);
		g2.drawImage(DepthCharge.depthChargeImage, (int)SIZE.getWidth()/5*4 - DepthCharge.depthChargeImage.getWidth(null)/2*2, basicsPicsY, DepthCharge.depthChargeImage.getWidth(null)*2, DepthCharge.depthChargeImage.getHeight(null)*2, null);

		//Separator (Not actually there cuz it looks mega uggers)
		int separatorY = basicsPicsY + Player.destroyerImage.getHeight(null) + 20;
		//g2.drawLine((int)SIZE.getWidth()/7*1, separatorY, (int)SIZE.getWidth()/7*6, separatorY);

		//Powerups
		g2.setFont(new Font(g2.getFont().getName(), Font.BOLD, 15));
		int powerupHeaderY = separatorY + 20;
		g2.drawString("POWERUPS", (int)SIZE.getWidth()/7*2 - g2.getFontMetrics().stringWidth("POWERUPS")/2, powerupHeaderY);
		//Labels
		g2.setFont(new Font(g2.getFont().getName(), Font.PLAIN, 10));
		int labelY = powerupHeaderY + 15 + 10;
		g2.drawString("HEALTH", (int)SIZE.getWidth()/7*1 - g2.getFontMetrics().stringWidth("HEALTH")/2, labelY);
		g2.drawString("SPEED", (int)SIZE.getWidth()/7*2 - g2.getFontMetrics().stringWidth("SPEED")/2, labelY);
		g2.drawString("ARMOR", (int)SIZE.getWidth()/7*3 - g2.getFontMetrics().stringWidth("ARMOR")/2, labelY);
		int powerupPicsY = labelY + g2.getFontMetrics().getHeight();
		g2.drawImage(Crate.healthCrateImage, (int)SIZE.getWidth()/7*1 - Crate.healthCrateImage.getWidth(null)/2, powerupPicsY, Crate.healthCrateImage.getWidth(null), Crate.healthCrateImage.getHeight(null), null);
		g2.drawImage(Crate.speedCrateImage, (int)SIZE.getWidth()/7*2 - Crate.speedCrateImage.getWidth(null)/2, powerupPicsY, Crate.speedCrateImage.getWidth(null), Crate.speedCrateImage.getHeight(null), null);
		g2.drawImage(Crate.armorCrateImage, (int)SIZE.getWidth()/7*3 - Crate.armorCrateImage.getWidth(null)/2, powerupPicsY, Crate.armorCrateImage.getWidth(null), Crate.armorCrateImage.getHeight(null), null);
		int sublabelY = powerupPicsY + Crate.healthCrateImage.getHeight(null) + 20;
		g2.drawString("+50", (int)SIZE.getWidth()/7*1 - g2.getFontMetrics().stringWidth("+50")/2, sublabelY);
		g2.drawString("2X for 10 sec", (int)SIZE.getWidth()/7*2 - g2.getFontMetrics().stringWidth("2X for 10 sec")/2, sublabelY);
		g2.drawString("1/2 for 10 sec", (int)SIZE.getWidth()/7*3 - g2.getFontMetrics().stringWidth("1/2 for 10 sec")/2, sublabelY);
		//Obtaining
		int obtainingLabelY = sublabelY + 20;
		g2.drawString("Dropped by destroyed subs (10% chance)", (int)SIZE.getWidth()/7*2 - g2.getFontMetrics().stringWidth("Dropped by destroyed subs (10% chance)")/2, obtainingLabelY);
		int obtainingLabel2Y = obtainingLabelY + g2.getFontMetrics().getHeight() + 10;
		g2.drawString("Destroy the crates to collect", (int)SIZE.getWidth()/7*2 - g2.getFontMetrics().stringWidth("Destroy the crates to collect")/2, obtainingLabel2Y);

		//Controls
		g2.setFont(new Font(g2.getFont().getName(), Font.BOLD, 15));
		int controlsHeaderY = separatorY + 20;
		g2.drawString("CONTROLS", (int)SIZE.getWidth()/7*5 - g2.getFontMetrics().stringWidth("CONTROLS")/2, controlsHeaderY);
		//Depth charge label
		g2.setFont(new Font(g2.getFont().getName(), Font.PLAIN, 10));
		int depthChargeLabelY = controlsHeaderY + 15 + 10;
		g2.drawString("Click to drop depth charges", (int)SIZE.getWidth()/7*5 - g2.getFontMetrics().stringWidth("Click to drop depth charges")/2, depthChargeLabelY);

		//all the rest
		g2.setFont(new Font(g2.getFont().getName(), Font.PLAIN, 10));
		int firstRowControlsY = depthChargeLabelY + 30;
		int secondRowControlsY = firstRowControlsY + 30;
		int thirdRowControlsY = secondRowControlsY + 30;
		g2.drawString("MOVE - ARROWS/WASD", (int)(SIZE.getWidth()/7*5) - g2.getFontMetrics().stringWidth("MOVE - ARROWS/WASD")/2, firstRowControlsY);
		g2.drawString("QUIT - ESCAPE", (int)(SIZE.getWidth()/14*9.15) - g2.getFontMetrics().stringWidth("QUIT - ESCAPE")/2, secondRowControlsY);
		g2.drawString("PAUSE - E", (int)(SIZE.getWidth()/14*11.15) - g2.getFontMetrics().stringWidth("PAUSE - E")/2, secondRowControlsY);
		g2.drawString("TOGGLE SUBS - F", (int)(SIZE.getWidth()/14*9.15) - g2.getFontMetrics().stringWidth("TOGGLE SUBS - F")/2, thirdRowControlsY);
		g2.drawString("RESTART - R", (int)(SIZE.getWidth()/14*11.15) - g2.getFontMetrics().stringWidth("RESTART - R")/2, thirdRowControlsY);
	}

}

class BasicMenu extends JPanel {

	public static final Dimension SIZE = new Dimension(600, 400);

	public static final int buttonWidth = 100;
	public static final int buttonHeight = 25;
	public static final int buttonYMargin = 15;
	public static final int buttonXMargin = 10;

	public String[] buttonTexts;
	public Color buttonColor;
	public Color backgroundColor;
	public Color buttonSelectedColor;
	public ArrayList<MenuButton> buttons = new ArrayList<>();
	public int xOffset;
	public int yOffset;
	public int selectedButton = 1;
	public String name;

	public BasicMenu(String name, String[] buttonTexts, Color buttonColor, Color backgroundColor, Color buttonSelectedColor, int xOffset, int yOffset) {
		this.setPreferredSize(SIZE);
		this.name = name;
		this.buttonTexts = buttonTexts;
		this.buttonColor = buttonColor;
		this.backgroundColor = backgroundColor;
		this.buttonSelectedColor = buttonSelectedColor;
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		createButtons();

		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				for(MenuButton mb : buttons) {
					if(mb.disabled) { continue; }
					if(mb.contains(new Point2D.Double(e.getX(), e.getY()))) {
						//It gets here but not any farther
						if(((BasicMenu)GameFrame.area).name.equals("Main Menu")) {
							if (mb.buttonNumber == 1) {
								Main.window.goToDifficultyMenu();
							} else if (mb.buttonNumber == 2) {
								Main.window.goToHelpMenu();
							} else if (mb.buttonNumber == 3) {
								GameFrame.backgroundDisabled = !GameFrame.backgroundDisabled;
								GameFrame.submarines.clear();
							} else if (mb.buttonNumber == 4) {
								System.exit(0);
							}
						} else if(((BasicMenu)GameFrame.area).name.equals("Difficulty Menu")) {
							if (mb.buttonNumber == 1) {
								Main.window.startGame(1);
							} else if (mb.buttonNumber == 2) {
								Main.window.startGame(2);
							} else if (mb.buttonNumber == 3) {
								Main.window.startGame(3);
							} else if (mb.buttonNumber == 4) {
								Main.window.goToMainMenu(false);
							}
						} else if(((BasicMenu)GameFrame.area).name.equals("Help Menu")) {
							if (mb.buttonNumber == 1) {
								Main.window.goToMainMenu(false);
							}
						}
					}
				}
			}
		});

		this.addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				boolean onButton = false;
				for(MenuButton mb : buttons) {
					if(mb.contains(new Point2D.Double(e.getX(), e.getY()))) {
						selectedButton = mb.buttonNumber;
						onButton = true;
					}
				}
				if(!onButton) {
					selectedButton = 0;
				}
			}
		});

	}

	public void createButtons() {
		int basex = (int)SIZE.getWidth()/2 - buttonWidth/2 + xOffset;
		int basey = (int)SIZE.getHeight()/2 - (buttonTexts.length * buttonHeight + ((buttonTexts.length+1) * buttonYMargin))/2 + yOffset;
		for(int i=0; i<buttonTexts.length; i++) {
			int y = basey + i*buttonHeight + (i+1)*buttonYMargin;
			buttons.add(new MenuButton(buttonTexts[i], buttonColor, basex, y, buttonWidth, buttonHeight, i+1, this, buttonSelectedColor));
		}
	}

	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;

		g2.setRenderingHints(new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON));

		g2.setFont(new Font("Lucida Grande", g2.getFont().getStyle(), g2.getFont().getSize()));

		//The background
		g2.setPaint(new Color(50,150,255));
		g2.fillRect(0,0, (int)SIZE.getWidth(), (int)SIZE.getHeight());


		//Draw the subs
		try {
			for(Submarine sub : new ArrayList<>(GameFrame.submarines)) {
				if(Submarine.submarineImage != null) {
					if(sub.direction == 1) {
						g2.drawImage(Submarine.submarineImage, (int)sub.x + (int)Submarine.SIZE.getWidth(), (int)sub.y, (int)-sub.SIZE.getWidth(), (int)sub.SIZE.getHeight(), null);
					} else {
						g2.drawImage(Submarine.submarineImage, (int)sub.x, (int)sub.y, (int)sub.SIZE.getWidth(), (int)sub.SIZE.getHeight(), null);
					}
				} else {
					g2.setPaint(Color.LIGHT_GRAY);
					g2.fill(sub);
					g2.setPaint(Color.BLACK);
					g2.draw(sub);
				}
			}
		} catch(NullPointerException e) { }

		//The background
		g2.setPaint(backgroundColor);
		g2.fillRoundRect((int)SIZE.getWidth()/2 - buttonWidth/2 - buttonXMargin + xOffset, (int)SIZE.getHeight()/2 - buttonHeight*buttonTexts.length/2 - buttonYMargin*(buttonTexts.length+1)/2 + yOffset, buttonWidth + buttonXMargin*2, buttonHeight*buttonTexts.length + buttonYMargin*(buttonTexts.length+1), 15, 15);
		g2.setPaint(Color.BLACK);
		g2.drawRoundRect((int)SIZE.getWidth()/2 - buttonWidth/2 - buttonXMargin + xOffset, (int)SIZE.getHeight()/2 - buttonHeight*buttonTexts.length/2 - buttonYMargin*(buttonTexts.length+1)/2 + yOffset, buttonWidth + buttonXMargin*2, buttonHeight*buttonTexts.length + buttonYMargin*(buttonTexts.length+1),15, 15);

		//The buttons
		for(MenuButton mb : buttons) {
			mb.draw(g2);
		}
	}

	class MenuButton extends Rectangle2D.Double {

		private String text;
		private Color background;
		private boolean disabled;

		private int buttonNumber;

		private BasicMenu parentMenu;

		private Color selectedColor;

		public MenuButton(String text, Color background, int x, int y, int width, int height, int buttonNumber, BasicMenu parentMenu, Color selectedColor) {
			this.text = text;
			this.background = background;
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			this.buttonNumber = buttonNumber;
			this.parentMenu = parentMenu;
			this.selectedColor = selectedColor;
		}

		public void draw(Graphics2D g2) {
			g2.setColor(background);
			if(parentMenu.selectedButton == buttonNumber) {
				g2.setColor(selectedColor);
			}
			g2.fillRoundRect((int)x, (int)y, (int)width, (int)height, 15, 15);
			g2.setColor(Color.BLACK);
			g2.drawRoundRect((int)x, (int)y, (int)width, (int)height, 15, 15);
			g2.setPaint(Color.WHITE);
			g2.drawString(text, (int)(x + width/2 - g2.getFontMetrics().stringWidth(text)/2), (int)(y + height/2 + g2.getFontMetrics().getHeight()/4));
		}
	}

}

class KeyBinder extends AbstractAction {

	public String cmd;

	public KeyBinder(String cmd) {
		this.cmd = cmd;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(GameFrame.area instanceof GameArea) {
			if(cmd.contains("released")) {
				GameFrame.keysHeld.remove(cmd.substring(8));
			}
			else {
				if (cmd.equals("restart")) {
					Main.window.startGame(GameFrame.difficulty);
				} else if (cmd.equals("quit")) {
					Main.window.goToMainMenu(true);
				} else if (cmd.equals("pause")) {
					GameFrame.paused = !GameFrame.paused;
					if(GameFrame.paused) {
						Main.window.pause();
					} else {
						Main.window.unpause();
					}
				} else if(!GameFrame.keysHeld.contains(cmd)) {
					GameFrame.keysHeld.add(cmd);
				}
			}
		} else if(GameFrame.area == GameFrame.mainMenu) {
			if (cmd.equals("quit")) {
				System.exit(0);
			} if(cmd.equals("up")) {
				((BasicMenu)GameFrame.area).selectedButton--;
				if(((BasicMenu)GameFrame.area).selectedButton < 1) {
					((BasicMenu)GameFrame.area).selectedButton = 1;
				}
			} if(cmd.equals("down")) {
				((BasicMenu)GameFrame.area).selectedButton++;
				if(((BasicMenu)GameFrame.area).selectedButton > ((BasicMenu)GameFrame.area).buttonTexts.length) {
					((BasicMenu)GameFrame.area).selectedButton = ((BasicMenu)GameFrame.area).buttonTexts.length;
				}
			} else if(cmd.equals("select")) {
				if(((BasicMenu)GameFrame.area).selectedButton == 1) {
					Main.window.goToDifficultyMenu();
				} else if(((BasicMenu)GameFrame.area).selectedButton == 2) {
					Main.window.goToHelpMenu();
				} else if(((BasicMenu)GameFrame.area).selectedButton == 3) {
					GameFrame.backgroundDisabled = !GameFrame.backgroundDisabled;
					GameFrame.submarines.clear();
				} else if(((BasicMenu)GameFrame.area).selectedButton == 4) {
					System.exit(0);
				}
			} else if(cmd.equals("disableBackground")) {
				GameFrame.backgroundDisabled = !GameFrame.backgroundDisabled;
				GameFrame.submarines.clear();
			}
		} else if(GameFrame.area == GameFrame.difficultyMenu) {
			if (cmd.equals("quit")) {
				Main.window.goToMainMenu(false);
			}
			if (cmd.equals("up")) {
				((BasicMenu) GameFrame.area).selectedButton--;
				if (((BasicMenu) GameFrame.area).selectedButton < 1) {
					((BasicMenu) GameFrame.area).selectedButton = 1;
				}
			}
			if (cmd.equals("down")) {
				((BasicMenu) GameFrame.area).selectedButton++;
				if (((BasicMenu) GameFrame.area).selectedButton > ((BasicMenu) GameFrame.area).buttonTexts.length) {
					((BasicMenu) GameFrame.area).selectedButton = ((BasicMenu) GameFrame.area).buttonTexts.length;
				}
			} else if (cmd.equals("select")) {
				if (((BasicMenu) GameFrame.area).selectedButton == 1) {
					Main.window.startGame(1);
				} else if (((BasicMenu) GameFrame.area).selectedButton == 2) {
					Main.window.startGame(2);
				} else if (((BasicMenu) GameFrame.area).selectedButton == 3) {
					Main.window.startGame(3);
				} else if (((BasicMenu) GameFrame.area).selectedButton == 4) {
					Main.window.goToMainMenu(false);
				}
			} else if(cmd.equals("disableBackground")) {
				GameFrame.backgroundDisabled = !GameFrame.backgroundDisabled;
				GameFrame.submarines.clear();
			}
		} else if(GameFrame.area == GameFrame.helpMenu) {
			if (cmd.equals("select")) {
				if (((BasicMenu) GameFrame.area).selectedButton == 1) {
					Main.window.goToMainMenu(false);
				}
			}
			if (cmd.equals("quit")) {
				Main.window.goToMainMenu(false);
			}
			if (cmd.equals("up")) {
				((BasicMenu) GameFrame.area).selectedButton--;
				if (((BasicMenu) GameFrame.area).selectedButton < 1) {
					((BasicMenu) GameFrame.area).selectedButton = 1;
				}
			}
			if (cmd.equals("down")) {
				((BasicMenu) GameFrame.area).selectedButton++;
				if (((BasicMenu) GameFrame.area).selectedButton > ((BasicMenu) GameFrame.area).buttonTexts.length) {
					((BasicMenu) GameFrame.area).selectedButton = ((BasicMenu) GameFrame.area).buttonTexts.length;
				}
			} else if(cmd.equals("disableBackground")) {
				GameFrame.backgroundDisabled = !GameFrame.backgroundDisabled;
				GameFrame.submarines.clear();
			}
		}
	}
}

//This class is necessary to see exceptions of scheduled tasks http://code.nomad-labs.com/2011/12/09/mother-fk-the-scheduledexecutorservice/
class ScheduledThreadPoolExecutorThatCatchesErrors extends ScheduledThreadPoolExecutor {

	public ScheduledThreadPoolExecutorThatCatchesErrors(int corePoolSize) {
		super(corePoolSize);
	}

	@Override
	public ScheduledFuture scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
		return super.scheduleAtFixedRate(wrapRunnable(command), initialDelay, period, unit);
	}

	@Override
	public ScheduledFuture scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
		return super.scheduleWithFixedDelay(wrapRunnable(command), initialDelay, delay, unit);
	}

	private Runnable wrapRunnable(Runnable command) {
		return new LogOnExceptionRunnable(command);
	}

	private class LogOnExceptionRunnable implements Runnable {
		private Runnable theRunnable;

		public LogOnExceptionRunnable(Runnable theRunnable) {
			super();
			this.theRunnable = theRunnable;
		}

		@Override
		public void run() {
			try {
				theRunnable.run();
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}
}

class ScoreIncrease {

	public int amount;
	public double x;
	public double y;

	public int targetY;

	public ScoreIncrease(int amount, int x, int y) {
		this.amount = amount;
		this.x = x;
		this.y = y;
		targetY = y - 20;
	}

	public void move() {
		y -= 0.25;
	}

}