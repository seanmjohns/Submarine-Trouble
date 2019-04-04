package com.pigcoder.subtrouble;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.*;

public class Main {

	public static GameFrame window;

	//Pictures
	public static File ocean1ImageFile = new File("/Ocean1.png");
	public static Image ocean1Image;
	public static int ocean1ImageOffset = -3;

	public static File ocean2ImageFile = new File("/Ocean2.png");
	public static Image ocean2Image;
	public static int ocean2ImageOffset = -3;

	public static File backgroundImageFile = new File("Background.png");
	public static Image backgroundImage;
	public static int backgroundImageOffset = 0;

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
			Torpedo.torpedoImage = ImageIO.read(Main.class.getResource("/" + Torpedo.torpedoImageFile.getName()));
			ocean1Image = ImageIO.read(Main.class.getResource("/" + ocean1ImageFile.getName()));
			ocean2Image = ImageIO.read(Main.class.getResource("/" + ocean2ImageFile.getName()));
			backgroundImage = ImageIO.read(Main.class.getResource("/" + backgroundImageFile.getName()));
			return true;
		} catch(IOException e) {
			e.printStackTrace();
			return false;
		}
	}

}

class GameFrame extends JFrame {

	public static final int OCEANLEVEL = (int)(GameArea.SIZE.getHeight()/4);
	public static final int OCEANFLOORLEVEL = (int)(GameArea.SIZE.getHeight()-40);
	public static final int MAXIMUMOCEANFLOORLEVEL = (int)(GameArea.SIZE.getHeight()-20);

	public static GameArea area;

	//Basic game vars
	public static boolean inGame = true;
	public static boolean gameOver = false;
	public static Player player;
	public static ArrayList<Submarine> submarines = new ArrayList<Submarine>();
	public static ArrayList<Torpedo> torpedoes = new ArrayList<>();
	public static ArrayList<String> keysHeld = new ArrayList<>();

	//Timers
	public static ScheduledFuture repainter;

	public static ScheduledFuture subMover;

	public static ScheduledFuture subCreator;

	public static ScheduledFuture torpedoMover;

	public static ScheduledFuture keybindManager;

	public void createGui(boolean testing) {

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(false);

		area = new GameArea();
		this.setContentPane(area);
		this.pack();

		this.setVisible(!testing);

		createKeybinds();
		if(!testing) {

			ScheduledThreadPoolExecutorThatCatchesErrors es = new ScheduledThreadPoolExecutorThatCatchesErrors(10);
			//Repaint the screen about 60 fps
			repainter = ((ScheduledExecutorService) es).scheduleAtFixedRate(new Runnable() {
				@Override
				public void run() {
					if(area != null) {
						area.repaint();
					}
				}
			}, 0, 16, TimeUnit.MILLISECONDS);

			//Move enemies
			subMover = ((ScheduledExecutorService) es).scheduleAtFixedRate(new Runnable() {
				@Override
				public void run() {
					ArrayList<Submarine> newSubs = new ArrayList<>();
					for (Submarine sub : new ArrayList<>(submarines)) {
						sub.move();
						//If on the screen
						if(!(sub.x < 0 - Submarine.SIZE.getWidth() || sub.x > GameArea.SIZE.getWidth())) {
							newSubs.add(sub);
						} else {
							if(sub.targetPoint != null) {
								newSubs.add(sub);
							}
						}
					}
					submarines = newSubs;
				}
			}, 0, 50, TimeUnit.MILLISECONDS);

			//Create enemies every 5 seconds
			subCreator = ((ScheduledExecutorService) es).scheduleAtFixedRate(new Runnable() {
				@Override
				public void run() {
					Submarine sub = new Submarine();
					submarines.add(sub);
				}
			}, 0, 5000, TimeUnit.MILLISECONDS);

			//Move torpedoes
			torpedoMover = ((ScheduledExecutorService) es).scheduleAtFixedRate(new Runnable() {
				@Override
				public void run() {
					ArrayList<Torpedo> newTorpedoes = new ArrayList<>();
					for(Torpedo t : new ArrayList<>(torpedoes)) {
						boolean tShouldBeKept = true;
						t.move();

						//Damage the player if it collides
						if(t.intersects(player)) {
							player.damaged(t.damage);
							continue;
						}

						//Damage a sub if one is hit by a torpedo
						ArrayList<Submarine> newSubs = new ArrayList<>();
						for(Submarine sub : new ArrayList<>(submarines)) {
							boolean subShouldBeKept = true;
							if(tShouldBeKept) {
								if (t.intersects(sub)) {
									if (t.parentSub != null && !(t.parentSub == sub)) {
										sub.damaged(t.damage);
										if (!(sub.health < 1)) {
											subShouldBeKept = true;
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
						if(tShouldBeKept) {
							newTorpedoes.add(t);
						}
					}
				}
			}, 0, 50, TimeUnit.MILLISECONDS);

			//Keybinds
			keybindManager = ((ScheduledExecutorService) es).scheduleAtFixedRate(new Runnable() {
				@Override
				public void run() {
					if(player != null) {
						if (keysHeld.contains("left")) {
							player.xVel = player.xVel - 0.01;
						}
						if (keysHeld.contains("right")) {
							player.xVel = player.xVel + 0.01;
						} else if (!(keysHeld.contains("left") && !keysHeld.contains("right"))) {
							player.xVel = player.xVel / 1.01;
						}
						if(player.xVel > 1) {
							player.xVel = 1;
						} else if (player.xVel < -1) {
							player.xVel = -1;
						}

						if (player.x + player.xVel > GameArea.SIZE.getWidth() - Player.SIZE.getWidth()) {
							player.xVel = 0;
						} else if (player.x + player.xVel < 0) {
							player.xVel = 0;
						}

						if (player.xVel < 0) {
							player.direction = 0;
						} else if (player.xVel > 0) {
							player.direction = 1;
						}

						player.x += player.xVel;
					}
				}
			}, 0, 15, TimeUnit.MILLISECONDS);

		}

		startGame();

	}

	public void startGame() {
		player = new Player();
		inGame = true;
	}

	public void createKeybinds() {

		//Add key listeners
		InputMap im = this.getRootPane().getInputMap(JComponent.WHEN_FOCUSED);
		ActionMap am = this.getRootPane().getRootPane().getActionMap();
		im.put(KeyStroke.getKeyStroke("A"), "left");
		im.put(KeyStroke.getKeyStroke("D"), "right");
		im.put(KeyStroke.getKeyStroke("LEFT"), "left");
		im.put(KeyStroke.getKeyStroke("RIGHT"), "right");
		im.put(KeyStroke.getKeyStroke("ENTER"), "select");
		im.put(KeyStroke.getKeyStroke("ESCAPE"), "quit");

		am.put("left", new KeyBinder("left"));
		am.put("right", new KeyBinder("right"));
		am.put("select", new KeyBinder("select"));
		am.put("quit", new KeyBinder("quit"));

		//Released keys
		im.put(KeyStroke.getKeyStroke("released A"), "releasedleft");
		im.put(KeyStroke.getKeyStroke("released D"), "releasedright");
		im.put(KeyStroke.getKeyStroke("released LEFT"), "releasedleft");
		im.put(KeyStroke.getKeyStroke("released RIGHT"), "releasedright");

		am.put("releasedleft", new KeyBinder("releasedleft"));
		am.put("releasedright", new KeyBinder("releasedright"));

	}

}

class GameArea extends JPanel {

	public static final Dimension SIZE = new Dimension(600, 400);

	public GameArea() {
		this.setPreferredSize(SIZE);
		generateOceanFloor();
		generateOceanSurface();
	}

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
				g2.drawImage(Torpedo.torpedoImage, (int)-Torpedo.SIZE.getWidth()/2, (int)-Torpedo.SIZE.getHeight()/2, (int)t.SIZE.getWidth(), (int)t.SIZE.getHeight(), null); //Note that I need 0 here because of the translation
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

		//Draw the subs
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

		//The ocean blue tint
		g2.setPaint(new Color(0,0,200,100));
		g2.fillRect(0,GameFrame.OCEANLEVEL, (int)SIZE.getWidth(), (int)SIZE.getHeight());

	}
}

class KeyBinder extends AbstractAction {

	public String cmd;

	public KeyBinder(String cmd) {
		this.cmd = cmd;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(GameFrame.inGame) {
			if(cmd.contains("released")) {
				GameFrame.keysHeld.remove(cmd.substring(8));
			}
			else {
				if(!GameFrame.keysHeld.contains(cmd)) {
					GameFrame.keysHeld.add(cmd);
				}
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