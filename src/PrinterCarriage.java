// Copyright (c) 2026 Douglas Miller

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

class PrinterCarriage extends JPanel
{
	static final int win = 100;
	static final int hei = 10;
	static final int wid = 5;
	int pos;

	public PrinterCarriage() {
		super();
		pos = 0;
		setPreferredSize(new Dimension(win + wid, hei));
		setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
	}

	public void setCarriage(float x, int pw) {
		if (x > pw) x = pw;
		pos = (int)Math.round((x * win) / pw);
		repaint();
	}

	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2d = (Graphics2D)g;
		g2d.setColor(Color.red);
		g2d.fillRect(pos, hei - wid, wid, wid);
	}
}
