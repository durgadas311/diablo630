// Copyright (c) 2026 Douglas Miller <durgadas311@gmail.com>

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;

class FontTest
{
	static Font uf = new Font("Monospaced", Font.PLAIN, 6);

	// This is taken from /usr/share/fonts/type1/urw-base35/NimbusRoman-Regular.afm
	// and assumes we're using the "Serif", Font.PLAIN, 12pt rendering.
	static float[] wt = new float[]{
		0.250f,0.333f,0.408f,0.500f,0.500f,0.833f,0.778f,0.333f,
		0.333f,0.333f,0.500f,0.564f,0.250f,0.333f,0.250f,0.278f,
		0.500f,0.500f,0.500f,0.500f,0.500f,0.500f,0.500f,0.500f,
		0.500f,0.500f,0.278f,0.278f,0.564f,0.564f,0.564f,0.444f,
		0.921f,0.722f,0.667f,0.667f,0.722f,0.611f,0.556f,0.722f,
		0.722f,0.333f,0.389f,0.722f,0.611f,0.889f,0.722f,0.722f,
		0.556f,0.722f,0.667f,0.556f,0.611f,0.722f,0.722f,0.944f,
		0.722f,0.722f,0.611f,0.333f,0.278f,0.333f,0.469f,0.500f,
		0.333f,0.444f,0.500f,0.444f,0.500f,0.444f,0.333f,0.500f,
		0.500f,0.278f,0.278f,0.500f,0.278f,0.778f,0.500f,0.500f,
		0.500f,0.500f,0.333f,0.389f,0.278f,0.500f,0.500f,0.722f,
		0.500f,0.500f,0.444f,0.480f,0.200f,0.480f,0.541f
	};

	static public void paint(Graphics2D g2d, Font font) {
//2,3		FontMetrics fm = g2d.getFontMetrics();
//3		int[] ws = fm.getWidths();

		g2d.setStroke(new BasicStroke(0.25f));
		FontRenderContext frc = g2d.getFontRenderContext();
		char[] c = new char[1];
		c[0] = ' ';
		LineMetrics lm = font.getLineMetrics(c, 0, 1, frc);
		int fa = (int)Math.round(lm.getAscent());

		int x = 50;
		int y = 50;
		for (int b = 0; b < 95; ++b) {
			c[0] = (char)(b + 32);
			String s = new String(c);
			Rectangle2D r = font.getStringBounds(s, frc);
//2			Rectangle2D r = fm.getStringBounds(s, g2d);
			g2d.setColor(Color.red);
			Rectangle2D rr = new Rectangle2D.Double(x, y,
						r.getWidth(), r.getHeight());
//3			Rectangle2D rr = new Rectangle2D.Double(x, y,
//3						(double)ws[b + 32], r.getHeight());
//2			Rectangle2D rr = new Rectangle2D.Double(x, y,
//2						(double)fm.charWidth((char)(b + 32)), r.getHeight());
			g2d.draw(rr);
			g2d.setColor(Color.black);
			g2d.setFont(font);
			g2d.drawString(s, x, y + fa);
			g2d.setFont(uf);
			g2d.drawString(String.format("%.1f", r.getWidth()), x, y - 8);
			g2d.drawString(String.format("%.1f", r.getHeight()), x, y - 2);
			g2d.drawString(String.format("%.1f", wt[b] * 12f), x,
					y + (int)(Math.round(r.getHeight())) + 6);

			x +=  32;
			if ((b & 0x0f) == 0x0f) {
				x = 50;
				y += 50;
			}
		}
	}
}
