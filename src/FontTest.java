// Copyright (c) 2026 Douglas Miller <durgadas311@gmail.com>

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;

class FontTest
{
	static Font uf = new Font("Monospaced", Font.PLAIN, 6);

	static public void paint(Graphics2D g2d, Font font) {
		g2d.setStroke(new BasicStroke(0.25f));
		FontRenderContext frc = g2d.getFontRenderContext();
		char[] c = new char[1];
		c[0] = ' ';
		LineMetrics lm = font.getLineMetrics(c, 0, 1, frc);
		double ld = lm.getLeading();
		int fa = (int)Math.round(lm.getAscent() + 1);

		int x = 50;
		int y = 50;
		for (int b = 0; b < 95; ++b) {
			c[0] = (char)(b + 32);
			String s = new String(c);
			Rectangle2D r = font.getStringBounds(s, frc);
			Rectangle2D rr = new Rectangle2D.Double(x, y,
						r.getWidth(), r.getHeight() - ld);
			g2d.setColor(Color.red);
			g2d.draw(rr);
			g2d.setColor(Color.black);
			g2d.setFont(font);
			g2d.drawString(s, x, y + fa);
			g2d.setFont(uf);
			g2d.drawString(String.format("%.1f", rr.getWidth()), x, y - 8);
			g2d.drawString(String.format("%.1f", rr.getHeight()), x, y - 2);

			x +=  32;
			if ((b & 0x0f) == 0x0f) {
				x = 50;
				y += 50;
			}
		}
	}
}
