// Copyright (c) 2016 Douglas Miller

import java.awt.*;
import java.awt.print.*;

class Printer_Paper
{
	static final int A_RED = 0x001;
	static final int A_UNDL = 0x002;
	static final int A_BOLD = 0x004;	// same as A_SHAD?
	static final int A_SHAD = 0x008;
	static final int A_JUST = 0x010;
	static final int A_CNTR = 0x020;

	float _fa;
	int _off_x, _off_y;
	int _pwx, _phx;
	Color _alt = Color.red;

	public void init(Font font, float fa) {
		__font = font;
		__bfont = __font.deriveFont(Font.BOLD);
		_fa = fa;
	}

	public boolean empty() {
		return (_xplots == 0);
	}

	public void setScale(int x, int y) {
		_pwx = x;
		_phx = y;
	}

	void clear() {
		_nplots = 0;
		_xplots = 0;
		//_plotArray.dispose();
		_plotArray = null;
	}

	// x,y are "points" (i.e. pixels)
	public void setPage(int x, int y, int lm, int tm) {
		if (x == y) {}
		_off_x = lm;
		_off_y = tm;
		// makes no sense to keep old plots...
		clear();
	}

	private Font __font;
	private Font __bfont;

	public Font getFont() { return __font; }

	public Printer_Paper() {
		__font = null;
		clear();
	}

	class plot {
		plot(String s_, float x_, float y_, int attr_) {
			s = s_;
			x = x_;
			y = y_;
			attr = attr_;
		}
		public String s;
		public float x;
		public float y;
		public int attr;
	}

	private plot[] _plotArray;
	private int _nplots;
	private int _xplots;
	private int _last = -1;

	public void addPlot(String s, float x, float y, int attr) {
		// _sorted = false; // can be smarter?
		int n = _xplots;
		if (_xplots + 1 > _nplots) {
			int o = _nplots;
			_nplots += 256;
			plot[] p = new plot[_nplots];
			if (o > 0) {
				System.arraycopy(_plotArray, 0, p, 0, o);
			}
			_plotArray = p;
		}
		_plotArray[n] = new plot(s, x + _off_x, y + _off_y, attr);
		++_xplots;
		_last = n;
	}

	public void appendLastPlot(String s, float x, float y, int attr) {
		if (_last < 0) {
			addPlot(s, x, y, attr);
		} else {
			_plotArray[_last].s += s;
			// _plotArray[_last].attr |= attr; // what's right?
		}
	}

	public void prependLastPlot(String s, float x, float y, int attr) {
		if (_last < 0) {
			addPlot(s, x, y, attr);
		} else {
			_plotArray[_last].s = s + _plotArray[_last].s;
			_plotArray[_last].x = x + _off_x;
			// _plotArray[_last].attr |= attr; // what's right?
		}
	}

	public int print(Graphics g, PageFormat pf, int pageIndex, PaperPaintable bkg) {
		boolean red = false;
		if (pageIndex == 0) {}
		Graphics2D g2d = (Graphics2D)g;
		if (bkg != null) {
			bkg.paint(g2d, pf);
		}
		double x0 = pf.getImageableX();
		double y0 = pf.getImageableY();
		double w0 = pf.getImageableWidth();
		double h0 = pf.getImageableHeight();
		g2d.translate(x0, y0);
		g2d.setFont(__font);

		g2d.setColor(Color.black);

		int i = 0;
		for (i = 0; i < _xplots; ++i) {
			if ((_plotArray[i].attr & A_RED) != 0) {
				if (!red) {
					g2d.setColor(_alt);
					red = true;
				}
			} else {
				if (red) {
					g2d.setColor(Color.black);
					red = false;
				}
			}
			if ((_plotArray[i].attr & A_BOLD) != 0) {
				g2d.setFont(__bfont);
			}
			g2d.drawString(_plotArray[i].s, _plotArray[i].x,
					_plotArray[i].y + _fa);
			if ((_plotArray[i].attr & A_BOLD) != 0) {
				g2d.setFont(__font);
			} else if ((_plotArray[i].attr & A_SHAD) != 0) {
				g2d.drawString(_plotArray[i].s, _plotArray[i].x + 0.6f,
						_plotArray[i].y + _fa);
			}
			if ((_plotArray[i].attr & A_UNDL) != 0) {
				int len = _plotArray[i].s.length();
				g2d.drawString(String.valueOf('_').repeat(len),
					_plotArray[i].x, _plotArray[i].y + _fa);
			}
		}
		return Printable.PAGE_EXISTS;
	}
}
