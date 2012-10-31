/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.start;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.net.URL;
import java.util.ArrayList;
import static com.cburch.logisim.util.LocaleString._;

import javax.swing.JComponent;

class AboutCredits extends JComponent {
	/** Time to spend freezing the credits before after after scrolling */
	private static final int MILLIS_FREEZE = 1000;
	
	/** Speed of how quickly the scrolling occurs */
	private static final int MILLIS_PER_PIXEL = 20;

	/** Path to Hendrix College's logo - if you want your own logo included,
	 * please add it separately rather than replacing this. */
	private static final String HENDRIX_PATH = "logisim/hendrix.png";
	private static final int HENDRIX_WIDTH = 50;

	private static class CreditsLine {
		private int y;
		private int type;
		private String text;
		private Image img;
		private int imgWidth;
		
		public CreditsLine(int type, String text) {
			this(type, text, null, 0);
		}
		
		public CreditsLine(int type, String text, Image img, int imgWidth) {
			this.y = 0;
			this.type = type;
			this.text = text;
			this.img = img;
			this.imgWidth = imgWidth;
		}
	}
	
	private Color[] colorBase;
	private Paint[] paintSteady;
	private Font[] font;
	
	private int scroll;
	private float fadeStop;
	
	private ArrayList<CreditsLine> lines;
	private int initialLines; // number of lines to show in initial freeze
	private int initialHeight; // computed in code based on above
	private int linesHeight; // computed in code based on above
	
	public AboutCredits() {
		scroll = 0;
		setOpaque(false);
		
		int prefWidth = About.IMAGE_WIDTH + 2 * About.IMAGE_BORDER;
		int prefHeight = About.IMAGE_HEIGHT / 2 + About.IMAGE_BORDER;
		setPreferredSize(new Dimension(prefWidth, prefHeight));
	
		fadeStop = (float) (About.IMAGE_HEIGHT / 4.0);

		colorBase = new Color[] {
				new Color(143, 0, 0),
				new Color(48, 0, 96),
				new Color(48, 0, 96),
		};
		font = new Font[] {
				new Font("Sans Serif", Font.ITALIC, 20),
				new Font("Sans Serif", Font.BOLD, 24),
				new Font("Sans Serif", Font.BOLD, 18),
		};
		paintSteady = new Paint[colorBase.length];
		for (int i = 0; i < colorBase.length; i++) {
			Color hue = colorBase[i];
			paintSteady[i] = new GradientPaint(0.0f, 0.0f, derive(hue, 0),
					0.0f, fadeStop, hue);
		}
		
		URL url = AboutCredits.class.getClassLoader().getResource(HENDRIX_PATH);
		Image hendrixLogo = null;
		if (url != null) {
			hendrixLogo = getToolkit().createImage(url);
		}
		
		// Logisim's policy concerning who is given credit:
		// Past contributors are not acknowledged in the About dialog for the current
		// version, but they do appear in the acknowledgements section of the User's
		// Guide. Current contributors appear in both locations.
		
		lines = new ArrayList<CreditsLine>();
		linesHeight = 0; // computed in paintComponent
		lines.add(new CreditsLine(1, "www.cburch.com/logisim/"));
		lines.add(new CreditsLine(0, _("creditsRoleLead"),
				hendrixLogo, HENDRIX_WIDTH));
		lines.add(new CreditsLine(1, "Carl Burch"));
		lines.add(new CreditsLine(2, "Hendrix College"));
		initialLines = lines.size();
		lines.add(new CreditsLine(0, _("creditsRoleGerman")));
		lines.add(new CreditsLine(1, "Uwe Zimmerman"));
		lines.add(new CreditsLine(2, "Uppsala universitet"));
		lines.add(new CreditsLine(0, _("creditsRoleGreek")));
		lines.add(new CreditsLine(1, "Thanos Kakarountas"));
		lines.add(new CreditsLine(2, "\u03A4.\u0395.\u0399 \u0399\u03BF\u03BD\u03AF\u03C9\u03BD \u039D\u03AE\u03C3\u03C9\u03BD"));
		lines.add(new CreditsLine(0, _("creditsRolePortuguese")));
		lines.add(new CreditsLine(1, "Theldo Cruz Franqueira"));
		lines.add(new CreditsLine(2, "PUC Minas"));
		lines.add(new CreditsLine(0, _("creditsRoleRussian")));
		lines.add(new CreditsLine(1, "Ilia Lilov"));
		lines.add(new CreditsLine(2, "\u041C\u043E\u0441\u043A\u043E\u0432\u0441\u043A\u0438\u0439 \u0433\u043E\u0441\u0443\u0434\u0430\u0440\u0441\u0442\u0432\u0435\u043D\u043D\u044B\u0439"));
		lines.add(new CreditsLine(2, "\u0443\u043D\u0438\u0432\u0435\u0440\u0441\u0438\u0442\u0435\u0442 \u043F\u0435\u0447\u0430\u0442\u0438"));
		lines.add(new CreditsLine(0, _("creditsRoleTesting")));
		lines.add(new CreditsLine(1, "Ilia Lilov"));
		lines.add(new CreditsLine(2, "\u041C\u043E\u0441\u043A\u043E\u0432\u0441\u043A\u0438\u0439 \u0433\u043E\u0441\u0443\u0434\u0430\u0440\u0441\u0442\u0432\u0435\u043D\u043D\u044B\u0439"));
		lines.add(new CreditsLine(2, "\u0443\u043D\u0438\u0432\u0435\u0440\u0441\u0438\u0442\u0435\u0442 \u043F\u0435\u0447\u0430\u0442\u0438"));
		
		/* If you fork Logisim, feel free to change the above lines, but
		 * please do not change these last four lines! */
		lines.add(new CreditsLine(0, _("creditsRoleOriginal"),
				hendrixLogo, HENDRIX_WIDTH));
		lines.add(new CreditsLine(1, "Carl Burch"));
		lines.add(new CreditsLine(2, "Hendrix College"));
		lines.add(new CreditsLine(1, "www.cburch.com/logisim/"));
	}
	
	public void setScroll(int value) {
		scroll = value;
		repaint();
	}
	
	private Color derive(Color base, int alpha) {
		return new Color(base.getRed(), base.getGreen(), base.getBlue(), alpha);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		FontMetrics[] fms = new FontMetrics[font.length];
		for (int i = 0; i < fms.length; i++) {
			fms[i] = g.getFontMetrics(font[i]);
		}
		if (linesHeight == 0) {
			int y = 0;
			int index = -1;
			for (CreditsLine line : lines) {
				index++;
				if (index == initialLines) initialHeight = y;
				if (line.type == 0) y += 10;
				FontMetrics fm = fms[line.type];
				line.y = y + fm.getAscent();
				y += fm.getHeight();
			}
			linesHeight = y;
		}
		
		Paint[] paint = paintSteady;
		int yPos = 0;
		int height = getHeight();
		int initY = Math.min(0, initialHeight - height + About.IMAGE_BORDER);
		int maxY = linesHeight - height - initY;
		int totalMillis = 2 * MILLIS_FREEZE + (linesHeight + height) * MILLIS_PER_PIXEL;
		int offs = scroll % totalMillis;
		if (offs >= 0 && offs < MILLIS_FREEZE) {
			// frozen before starting the credits scroll
			int a = 255 * (MILLIS_FREEZE - offs) / MILLIS_FREEZE;
			if (a > 245) {
				paint = null;
			} else if (a < 15) {
				paint = paintSteady;
			} else {
				paint = new Paint[colorBase.length];
				for (int i = 0; i < paint.length; i++) {
					Color hue = colorBase[i];
					paint[i] = new GradientPaint(0.0f, 0.0f, derive(hue, a),
						0.0f, fadeStop, hue);
				}
			}
			yPos = initY;
		} else if (offs < MILLIS_FREEZE + maxY * MILLIS_PER_PIXEL) {
			// scrolling through credits
			yPos = initY + (offs - MILLIS_FREEZE) / MILLIS_PER_PIXEL;
		} else if (offs < 2 * MILLIS_FREEZE + maxY * MILLIS_PER_PIXEL) {
			// freezing at bottom of scroll
			yPos = initY + maxY;
		} else if (offs < 2 * MILLIS_FREEZE + (linesHeight - initY) * MILLIS_PER_PIXEL) {
			// scrolling bottom off screen
			yPos = initY + (offs - 2 * MILLIS_FREEZE) / MILLIS_PER_PIXEL;
		} else {
			// scrolling next credits onto screen
			int millis = offs - 2 * MILLIS_FREEZE - (linesHeight - initY) * MILLIS_PER_PIXEL;
			paint = null;
			yPos = -height + millis / MILLIS_PER_PIXEL;
		}
		
		int width = getWidth();
		int centerX = width / 2; 
		maxY = getHeight();
		for (CreditsLine line : lines) {
			int y = line.y - yPos;
			if (y < -100 || y > maxY + 50) continue;
			
			int type = line.type;
			if (paint == null) {
				g.setColor(colorBase[type]);
			} else {
				((Graphics2D) g).setPaint(paint[type]);
			}
			g.setFont(font[type]);
			int textWidth = fms[type].stringWidth(line.text);
			g.drawString(line.text, centerX - textWidth / 2, line.y - yPos);
			
			Image img = line.img;
			if (img != null) {
				int x = width - line.imgWidth - About.IMAGE_BORDER;
				int top = y - fms[type].getAscent();
				g.drawImage(img, x, top, this);
			}
		}
	}
}