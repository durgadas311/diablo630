// Copyright (c) 2026 Douglas Miller

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.image.BufferedImage;

class PageProgress extends JPanel
{
	int pw;
	int ph;
	BufferedImage img;
	int black = Color.black.getRGB();
	int v;

	public PageProgress() {
		super();
		setPreferredSize(new Dimension(120, 90));
		setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
	}

	public void newPage() {
		if (img == null) return;
		Graphics2D g2d = img.createGraphics();
		g2d.setColor(Color.white);
		g2d.fillRect(0, 0, pw, ph);
		v = 0;
		repaint();
	}

	public void setPrint(int x, int y, int len) {
		if (img == null) return;
		int xx = (int)Math.round((float)x / 10f);
		int yy = (int)Math.round((float)y / 10f);
		v = yy;
		for (int a = 0; a < len; ++a) {
			img.setRGB(xx + a, yy, black);
		}
		repaint();
	}

	public void setPage(int pw, int ph) {
		this.pw = (int)Math.round((float)pw / 10f);
		this.ph = (int)Math.round((float)ph / 10f);
		img = new BufferedImage(this.pw, this.ph, BufferedImage.TYPE_INT_ARGB);
		newPage();
		repaint();
	}

	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2d = (Graphics2D)g;
		if (img != null) {
			g2d.drawImage(img, null, 5, 80 - v);
		}
	}
}
