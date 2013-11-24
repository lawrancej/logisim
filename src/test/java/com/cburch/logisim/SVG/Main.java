package com.cburch.logisim.SVG;

import java.awt.Dimension;
import java.awt.Panel;

import javax.swing.JFrame;

public class Main extends Panel implements Runnable {

	private static final long serialVersionUID = 1L;
	private static final int WIDTH = 300;
	private static final int HEIGHT = WIDTH / 16 * 9;
	private static final int SCALE = 3;
	private static final String TITLE = "SVG Test | ";

	private Thread thread;
	private JFrame frame;
	private Mouse mouse;

	private boolean running = false;

	public Main() {
		setPreferredSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
		add(Image.createComponent("andGate.svg"));
	}

	public static void main(String[] args) {
		Main test = new Main();
		test.frame = new JFrame(TITLE);
		test.mouse = new Mouse();
		test.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		test.frame.getContentPane().add(test);
		test.frame.addMouseListener(test.mouse);
		test.frame.pack();
		test.frame.setFocusable(true);
		test.frame.setLocationRelativeTo(null);
		test.frame.setVisible(true);
		test.start();
	}

	private synchronized void start() {
		thread = new Thread(this, "Display");
		running = true;
		thread.start();
	}

	private synchronized void stop() {
		running = false;
		try {
			thread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run() {
		long lastTime = System.nanoTime();
		long now;
		double ns = 1000000000.0 / 60.0;
		double delta = 0.0;
		int updates = 0;
		long timer = System.currentTimeMillis();
		while (running) {
			now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;
			while (delta >= 1) {
				delta--;
				updates++;
				update();
			}
			if ((System.currentTimeMillis() - timer) > 1000) {
				timer += 1000;
				frame.setTitle(TITLE + updates + " ups ");
				updates = 0;
			}
		}
		stop();
	}

	private void update() {
		/*if(!(mouse.getCoords() == null)) {
			add(Image.createComponent("andGate.svg"));
			this.frame.pack();
		}*/
	}

	private void render() {
	}

}
