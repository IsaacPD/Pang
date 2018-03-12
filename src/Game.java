import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Game extends JFrame implements ActionListener {
	private Timer t = new Timer(10, this);
	private Timer tpu;

	static Audio sound = new Audio();

	final static int WIDTH = 640, HEIGHT = 480;
	private Pong p = new Pong();
	private boolean com = false;


	private Game() {
		int[] levels = {400, 350, 300, 225};

		tpu = new Timer(350, this);
		tpu.setActionCommand("CPU");
		t.setActionCommand("BALL");

		getContentPane().setBackground(java.awt.Color.BLACK);
		setTitle("Pong");
		setResizable(false);
		try {
			setIconImage(ImageIO.read(new File("pang.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
		setLocationRelativeTo(null);
		setSize(WIDTH, HEIGHT);
		addKeyListener(new KAdapter());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		add(p);

		int choice = JOptionPane.showConfirmDialog(null, "Play Vs. Cpu?", "Choose", JOptionPane.YES_NO_OPTION);

		com = choice == 0;

		t.start();
		if (com) {
			String difficulties[] = {"Easy", "Normal", "Hard", "Impossible"};

			int difficulty = JOptionPane.showOptionDialog(null, "Choose a difficulty", "Pong",
					JOptionPane.OK_OPTION, JOptionPane.PLAIN_MESSAGE, new ImageIcon("pang.png"), difficulties, difficulties[1]);
			tpu.setDelay(levels[difficulty]);
			tpu.start();
		}

		setVisible(true);
	}

	public static void main(String... args) {
		Game g = new Game();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (p.score1 < 15 && p.score2 < 15) {
			p.moveBall();
			if (com && e.getActionCommand().equals("CPU")) p.moveCPU();
		}
		repaint();
	}

	public static class Pong extends JComponent implements ActionListener {
		Timer timer = new Timer(1000, this);
		Timer rainbow = new Timer(1000, this);
		static int seconds = 0, rain = 0;

		final int LENGTH = 100, WIDENESS = 10, SPEED = 30;
		int l1 = 100, l2 = 100;
		double angle = 0;
		int x1 = 15, y1 = 160, x2 = Game.WIDTH - 30, y2 = 160;
		int score1 = 0, score2 = 0, multiplier = 1;

		Rectangle2D.Double p1 = new Rectangle2D.Double(x1, y1, WIDENESS, LENGTH);
		Rectangle2D.Double p2 = new Rectangle2D.Double(x2, y2, WIDENESS, LENGTH);

		Color powers[] = {Color.MAGENTA, Color.CYAN, Color.GREEN, Color.ORANGE};

		ArrayList<Ball> balls = new ArrayList<>(3);

		Net n = new Net();
		PowerUp p = new PowerUp(powers[(int) (Math.random() * 4)]);

		Pong() {
			timer.setActionCommand("LONG");
			rainbow.setActionCommand("RAINBOW");
			balls.add(new Ball());
		}

		@Override
		public void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setFont(new Font("VERDANA", Font.BOLD, 30));
			g2.setColor(Color.WHITE);

			if (score1 >= 15 || score2 >= 15) {
				String message = (score1 > score2) ? "Player One Wins" : "Player Two Wins";
				g2.drawString(message, 100, 100);

				g2.translate(Game.WIDTH / 2 - 27 - message.length(), Game.HEIGHT / 2 + 25);
				g2.rotate(angle);
				angle += Math.PI / 16;
				g2.setColor(new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255)));
				g2.drawString(message, 0, 0);
				g2.translate(-(Game.WIDTH / 2 - 27 - message.length()), -(Game.HEIGHT / 2 + 25));

				return;
			}


			g2.fill(p1);
			g2.fill(p2);
			n.draw(g2);

			g2.drawString("" + score1, Net.X - 30, Net.Y + 30);
			g2.drawString("" + score2, Net.X + 20, Net.Y + 30);

			if (rainbow.isRunning())
				g2.setColor(new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255)));
			for (Ball b : balls)
				b.draw(g2);

			p.draw(g2);
		}

		void moveP1(int dir) {
			if (!(y1 + dir * SPEED < 10) && !(y1 + dir * SPEED > Game.HEIGHT - 135)) {
				y1 += dir * SPEED;
				p1 = new Rectangle2D.Double(x1, y1, WIDENESS, l1);
			}
		}

		void moveP2(int dir) {
			if (!(y2 + dir * SPEED < 10) && !(y2 + dir * SPEED > Game.HEIGHT - 135)) {
				y2 += dir * SPEED;
				p2 = new Rectangle2D.Double(x2, y2, WIDENESS, l2);
			}
		}

		void moveCPU() {
			Ball closest = balls.get(0);

			for (int i = 1; i < balls.size(); i++) {
				if (closest.getX() < balls.get(i).getX())
					closest = balls.get(i);
			}

			double ballY = closest.getY();
			double center = p2.getY() + 50;

			if ((ballY - center) < 0 && !(y2 - SPEED < 10))
				y2 += -1 * SPEED;
			else if (!(y2 + SPEED > Game.HEIGHT - 135))
				y2 += SPEED;

			p2 = new Rectangle2D.Double(x2, y2, WIDENESS, l2);
		}

		void moveBall() {
			for (Ball b : balls)
				b.moveBall();
			collision();
			score();
		}

		void collision() {
			for (int i = 0; i < balls.size(); i++) {
				Ball b = balls.get(i);
				if (n.intersectsBorder(b)) {
					sound.playDoot();
					b.changeYDir();
					return;
				}
				boolean inter = p1.intersects(b) || p2.intersects(b);
				if (inter) {
					if (p1.intersects(b)) sound.playPang();
					if (p2.intersects(b)) sound.playPing();
					b.changeDir();
					b.changeSpeed();
				}
				if (b.intersects(p)) {
					Color c = p.getColor();
					if (c.equals(Color.MAGENTA)) {
						multiplier *= 2;
						rainbow.start();
					}
					if (c.equals(Color.CYAN)) {
						if (b.dir == 1) {
							l1 += 50 * multiplier;
							p1.setRect(x1, y2, WIDENESS, l1);
						} else {
							l2 += 50 * multiplier;
							p2.setRect(x2, y2, WIDENESS, l2);
						}
						timer.start();
					}
					if (c.equals(Color.ORANGE)) {
						if (b.dir == 1) score1 += multiplier;
						else score2 += multiplier;
					}
					if (c.equals(Color.GREEN)) {
						for (int j = 0; j < multiplier; j++) {
							balls.add(new Ball());
						}
					}

					p = new PowerUp(powers[(int) (Math.random() * 4)]);
				}
			}
		}

		void score() {
			for (int i = 0; i < balls.size(); i++) {
				Ball b = balls.get(i);
				if (b.getX() < 0) {
					if (rainbow.isRunning()) score2 *= multiplier;
					else score2++;

					if (i == 0) b.reset();
					else balls.remove(i);
				} else if (b.getX() > 630) {
					if (rainbow.isRunning()) score1 *= multiplier;
					else score1++;

					if (i == 0) b.reset();
					else balls.remove(i);
				}
			}
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals("LONG")) {
				seconds++;
				if (seconds == 10) {
					seconds = 0;
					l1 = l2 = LENGTH;
					p1 = new Rectangle2D.Double(x1, y1, WIDENESS, LENGTH);
					p2 = new Rectangle2D.Double(x2, y2, WIDENESS, LENGTH);
					timer.stop();
				}
			} else {
				rain++;
				if (rain == 10) {
					rain = 0;
					multiplier = 1;
					rainbow.stop();
				}
			}
		}
	}

	static class Ball extends Rectangle {
		final int BSIZE = 15;
		int SPEED = 2;
		int angleY = (Math.rint(Math.random()) == 1) ? -1 : 1;
		int dir = (Math.rint(Math.random()) == 1) ? -1 : 1;

		Ball() {
			this.x = Game.WIDTH / 2 - 27;
			this.y = Game.HEIGHT / 2 - 20;
			this.width = BSIZE;
			this.height = BSIZE;
		}

		void draw(Graphics2D g) {
			g.fill(this);
		}

		void moveBall() {
			this.x = this.x + SPEED * dir;
			this.y = this.y + angleY;
		}

		void changeDir() {
			dir = (this.x < 100) ? 1 : -1;
		}

		void reset() {
			this.x = Game.WIDTH / 2 - 27;
			this.y = Game.HEIGHT / 2 - 20;
			changeDir();
			angleY = (Math.rint(Math.random()) == 1) ? -1 : 1;
			SPEED = 2;
		}

		void changeYDir() {
			angleY = (angleY == -1) ? 1 : -1;
		}

		void changeSpeed() {
			SPEED = (int) (Math.random() * 4 + 1);
		}
	}

	static class Net {
		final static int SSIZE = 10, X = (Game.WIDTH - 50) / 2, Y = 10;

		ArrayList<Rectangle2D.Double> squares = new ArrayList<>(20);
		ArrayList<Rectangle2D.Double> border = new ArrayList<>(2);

		Net() {
			for (int i = 0; i < 15; i++)
				squares.add(new Rectangle2D.Double(X, Y + i * 30, SSIZE, SSIZE));
			border.add(new Rectangle2D.Double(0, 0, Game.WIDTH, 5));
			border.add(new Rectangle2D.Double(0, Game.HEIGHT - 30, Game.WIDTH, 5));
		}

		void draw(Graphics2D g) {
			for (Rectangle2D.Double s : squares)
				g.fill(s);
		}

		boolean intersectsBorder(Shape r) {
			return (r.intersects(border.get(0))) || r.intersects(border.get(1));
		}
	}

	static class PowerUp extends Rectangle {
		Color color;

		public PowerUp() {
			this(Color.MAGENTA);
		}

		PowerUp(Color c) {
			color = c;
			this.x = (int) (Math.random() * (Game.WIDTH - 90) + 40);
			this.y = (int) (Math.random() * (Game.HEIGHT - 110) + 50);
			this.width = 15;
			this.height = 15;
		}

		public void draw(Graphics2D g) {
			g.setColor(color);
			g.fill(this);
		}

		public Color getColor() {
			return color;
		}
	}

	public class KAdapter implements KeyListener {

		private final Set<Integer> pressed = new HashSet<>();

		@Override
		public synchronized void keyPressed(KeyEvent k) {
			pressed.add(k.getKeyCode());
			if (pressed.size() >= 1) {
				for (int key : pressed) {
					if (key == KeyEvent.VK_W && t.isRunning()) p.moveP1(-1);

					else if (key == KeyEvent.VK_S && t.isRunning()) p.moveP1(1);

					else if (key == KeyEvent.VK_SPACE) {
						if (t.isRunning()) {
							t.stop();
							sound.playSong();
						} else {
							sound.stopSong();
							t.start();
						}
						if (tpu.isRunning()) tpu.stop();
						else if (com) tpu.start();
					} else if (key == KeyEvent.VK_UP && !com && t.isRunning()) p.moveP2(-1);

					else if (key == KeyEvent.VK_DOWN && !com & t.isRunning()) p.moveP2(1);
				}
			}
		}

		@Override
		public void keyTyped(KeyEvent e) {

		}

		@Override
		public void keyReleased(KeyEvent e) {
			pressed.remove(e.getKeyCode());
		}
	}
}
