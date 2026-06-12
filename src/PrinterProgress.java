// Copyright (c) 2026 Douglas Miller

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

class PrinterProgress extends JPanel
{
	static final int win = 100;
	static final int hei = 10;
	int pos;

	public PrinterProgress() {
		super();
		pos = 0;
		setPreferredSize(new Dimension(win, hei));
		setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
	}

	public void setProgress(float y, int ph) {
		if (y > ph) y = ph;
		pos = (int)Math.round((y * win) / ph);
		repaint();
	}

	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2d = (Graphics2D)g;
		g2d.setColor(Color.black);
		g2d.fillRect(0, 0, pos, hei);
	}
}
