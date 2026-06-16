// Copyright (c) 2026 Douglas Miller

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.image.BufferedImage;

class PageProgress extends JPanel
{
	int pw;
	int ph;
	int cur = 0;
	BufferedImage[] img;
	int black = Color.black.getRGB();
	int v;

	public PageProgress() {
		super();
		img = new BufferedImage[2];
		setPreferredSize(new Dimension(120, 90));
		setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
	}

	public void newPage() {
		cur ^= 1;
		if (img[cur] == null) return;
		Graphics2D g2d = img[cur].createGraphics();
		g2d.setColor(Color.white);
		g2d.fillRect(0, 0, pw, ph);
		v = 0;
		repaint();
	}

	public void setPrint(int x, int y, int len) {
		if (img[cur] == null) return;
		int xx = (int)Math.round((float)x / 10f);
		int yy = (int)Math.round((float)y / 10f);
		if (yy >= img[cur].getHeight()) return;
		v = yy;
		for (int a = 0; a < len; ++a) {
			if (xx + a < img[cur].getWidth()) {
				img[cur].setRGB(xx + a, yy, black);
			}
		}
		repaint();
	}

	public void setPage(int pw, int ph) {
		this.pw = (int)Math.round((float)pw / 10f);
		this.ph = (int)Math.round((float)ph / 10f);
		img[0] = new BufferedImage(this.pw, this.ph, BufferedImage.TYPE_INT_ARGB);
		img[1] = new BufferedImage(this.pw, this.ph, BufferedImage.TYPE_INT_ARGB);
		newPage();
		repaint();
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;
		if (img[cur] != null) {
			g2d.drawImage(img[cur], null, 5, 80 - v);
		}
		if (img[cur ^ 1] != null) {
			g2d.drawImage(img[cur ^ 1], null, 5, 80 - v - ph - 3);
		}
	}
}
