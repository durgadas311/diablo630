// Copyright (c) 2016 Douglas Miller

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.util.Map;
import javax.swing.*;
import javax.print.attribute.standard.*;

class PrinterConsole implements WindowListener
{
	private Cleanup _clp;
	private String _descr;
	private JFrame _frame;
	private JMenuBar _mb;

	private JLabel _chg_txt;
	private JLabel _font_txt;
	private JLabel _pitch_txt;
	private JLabel _file_txt;
	private JLabel _paper_txt;
	private JLabel _sc_txt;
	private JLabel _off_txt;
	private JLabel _pages_txt;
	private JLabel _status_txt;
	private PageProgress _page;

	public JFrame getFrame() { return _frame; }

	public void setChanges(boolean changed) {
		if (changed) {
			_chg_txt.setText("* Changes pending END OF JOB");
		} else {
			_chg_txt.setText(" ");
		}
	}

	public void setFont(Font font) {
		String s = font.getFontName();
		int i = s.indexOf('.');
		if (i >= 0) {
			s = s.substring(0, i);
		}
		s = "PrintWheel: " + s + " " + font.getSize();
		Map<TextAttribute,?> atrs = font.getAttributes();
		if (atrs.containsKey(TextAttribute.WEIGHT)) {
			Object wt = atrs.get(TextAttribute.WEIGHT);
			if (wt == TextAttribute.WEIGHT_BOLD ||
			    wt == TextAttribute.WEIGHT_DEMIBOLD ||
			    wt == TextAttribute.WEIGHT_EXTRABOLD ||
			    wt == TextAttribute.WEIGHT_HEAVY ||
			    wt == TextAttribute.WEIGHT_MEDIUM ||
			    wt == TextAttribute.WEIGHT_SEMIBOLD ||
			    wt == TextAttribute.WEIGHT_ULTRABOLD) {
				s += " BOLD";
			} else if (wt == TextAttribute.WEIGHT_LIGHT ||
			    wt == TextAttribute.WEIGHT_DEMILIGHT ||
			    wt == TextAttribute.WEIGHT_EXTRA_LIGHT) {
				s += " LIGHT";
			}
		}
		if (atrs.containsKey(TextAttribute.POSTURE)) {
			Object po = atrs.get(TextAttribute.POSTURE);
			if (po == TextAttribute.POSTURE_OBLIQUE) {
				s += " ITALIC";
			}
		}
		_font_txt.setText(s);
	}

	public void setPitch(int cpi, int lpi) {
		_pitch_txt.setText("CPI: " + cpi + "    LPI: " + lpi);
	}

	public void setPaper(MediaSizeName ms, OrientationRequested or) {
		_paper_txt.setText("Paper: " + ms.toString() + "/" + or.toString());
	}

	public void setScale(float sc) {
		if (sc <= 0f) sc = 1f;
		_sc_txt.setText("Scaling: " + sc);
	}

	public void setPosition(float lm, float tm) {
		_off_txt.setText("Offsets: left " + lm + ", top " + tm + " (inches)");
	}

	public void setFileName(File file) {
		if (file != null) {
			_file_txt.setText("Output File: " + file.getAbsolutePath());
		} else {
			_file_txt.setText("Output File: none");
		}
	}

	public void setPages(int pgs, int part) {
		_pages_txt.setText("Pages Printed: " + pgs + "." + part);
	}

	public void setStatus(String sts) {
		_status_txt.setText("Printer: " + sts);
	}

	public void setPage(int pw, int ph) {
		if (_page != null) {
			_page.setPage(pw, ph);
		}
	}

	public void newPage() {
		if (_page != null) {
			_page.newPage();
		}
	}

	public void setPrint(int x, int y, int len) {
		if (_page != null) {
			_page.setPrint(x, y, len);
		}
	}

	public PrinterConsole(String descr, Cleanup clp) {
		_clp = clp;
		_descr = descr;
		_frame = new JFrame(_descr);
		_frame.addWindowListener(this);
		GridBagLayout gridbag = new GridBagLayout();
		_frame.setLayout(gridbag);
		GridBagConstraints s = new GridBagConstraints();
		s.fill = GridBagConstraints.NONE;
		s.gridx = 0;
		s.gridy = 0;
		s.weightx = 1;
		s.weighty = 1;
		s.gridwidth = 1;
		s.gridheight = 1;
		s.insets.top = 2;
		s.insets.bottom = 2;
		s.insets.left = 2;
		s.insets.right = 2;
		s.anchor = GridBagConstraints.WEST;

		_page = new PageProgress();

		_chg_txt = new JLabel(" ");
		_chg_txt.setPreferredSize(new Dimension(450, 20));
		gridbag.setConstraints(_chg_txt, s);
		_frame.add(_chg_txt);
		s.gridy += 1;

		_font_txt = new JLabel("Font:");
		_font_txt.setPreferredSize(new Dimension(450, 20));
		gridbag.setConstraints(_font_txt, s);
		_frame.add(_font_txt);
		s.gridy += 1;

		_paper_txt = new JLabel("Paper:");
		_paper_txt.setPreferredSize(new Dimension(450, 20));
		gridbag.setConstraints(_paper_txt, s);
		_frame.add(_paper_txt);
		s.gridy += 1;

		_pitch_txt = new JLabel("Pitch:");
		_pitch_txt.setPreferredSize(new Dimension(450, 20));
		gridbag.setConstraints(_pitch_txt, s);
		_frame.add(_pitch_txt);
		s.gridy += 1;

		_sc_txt = new JLabel("Scaling:");
		_sc_txt.setPreferredSize(new Dimension(450, 20));
		gridbag.setConstraints(_sc_txt, s);
		_frame.add(_sc_txt);
		s.gridy += 1;

		int saveY = s.gridy;
		s.gridheight = s.gridy;
		s.gridy = 0;
		++s.gridx;
		gridbag.setConstraints(_page, s);
		_frame.add(_page);
		++s.gridx;
		JPanel pan = new JPanel();
		pan.setPreferredSize(new Dimension(10, 10));
		gridbag.setConstraints(pan, s);
		_frame.add(pan);
		--s.gridx;
		--s.gridx;
		s.gridheight = 1;
		s.gridy = saveY;
		s.gridwidth = 3;

		_off_txt = new JLabel("Offsets:");
		_off_txt.setPreferredSize(new Dimension(600, 20));
		gridbag.setConstraints(_off_txt, s);
		_frame.add(_off_txt);
		s.gridy += 1;

		_file_txt = new JLabel("Output File:");
		_file_txt.setPreferredSize(new Dimension(600, 20));
		gridbag.setConstraints(_file_txt, s);
		_frame.add(_file_txt);
		s.gridy += 1;

		_pages_txt = new JLabel("Pages Printed:");
		_pages_txt.setPreferredSize(new Dimension(600, 20));
		gridbag.setConstraints(_pages_txt, s);
		_frame.add(_pages_txt);
		s.gridy += 1;

		_status_txt = new JLabel("Printer:");
		_status_txt.setPreferredSize(new Dimension(600, 20));
		gridbag.setConstraints(_status_txt, s);
		_frame.add(_status_txt);

		_mb = new JMenuBar();

		_frame.setJMenuBar(_mb);
		_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		_frame.pack();	// set size according to content...
		_frame.setVisible(true);
	}

	public void addMenu(JMenu mu) {
		_mb.add(mu);
		_frame.pack();	// this is needed to avoid gridbag squeeze
	}

	public void windowActivated(WindowEvent e) { }
	public void windowClosed(WindowEvent e) { }
	public void windowIconified(WindowEvent e) { }
	public void windowOpened(WindowEvent e) { }
	public void windowDeiconified(WindowEvent e) { }
	public void windowDeactivated(WindowEvent e) { }
	public void windowClosing(WindowEvent e) {
		_clp.cleanup();
	}
}
