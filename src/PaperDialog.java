// Copyright (c) 2016 Douglas Miller

import java.awt.*;
import java.util.Vector;
import javax.swing.*;
import javax.print.*;
import javax.print.attribute.standard.*;
import javax.print.attribute.Size2DSyntax;

class PaperDialog
{
	JPanel _dia_pn;

	private JComboBox<MediaSizeName> _ms_cb;
	private JComboBox<OrientationRequested> _or_cb;
	private JTextField _lm_txt;
	private JTextField _tm_txt;
	private JTextField _sc_txt;
	private float _lm, _tm, _sc;
	MediaSizeName _ms;
	OrientationRequested _or;
	JOptionPane _prefs;
	Object[] _btns;

	///////////////////////////////////////////////
	// Special case for 11x14 (14x11) "forms" paper
	static private M11x14 _m11x14;
	static private F11x14 _f11x14;

	static {
		_m11x14 = new M11x14();
		_f11x14 = new F11x14();
	}

	static class M11x14 extends MediaSizeName {
		public M11x14() {
			super(311);
		}
		// Override
		public String toString() {
			return "F11x14";
		}
	}

	static class F11x14 extends MediaSize {
		public F11x14() {
			super(11.0f, 14.0f, Size2DSyntax.INCH);
		}
	}

	static public MediaSizeName getForms() { return _m11x14; }

	static public MediaSize paperSize(MediaSizeName ms) {
		if (ms.equals(_m11x14)) {
			return _f11x14;
		} else {
			return MediaSize.getMediaSizeForName(ms);
		}
	}
	///////////////////////////////////////////////

	public void setMedia(MediaSizeName ms) {
		_ms = ms;
		_ms_cb.setSelectedItem(ms);
	}

	public MediaSizeName getMedia() {
		return (MediaSizeName)_ms_cb.getSelectedItem();
	}

	public void setOrient(OrientationRequested or) {
		_or = or;
		_or_cb.setSelectedItem(or);
	}

	public OrientationRequested getOrient() {
		return (OrientationRequested)_or_cb.getSelectedItem();
	}

	public PaperPaintable getBkground() {
		return null;
	}

	// Normalize 1.0 as 0.0 (no scaling)
	public void setScale(float sc) {
		_sc = sc;
		if (sc <= 0f) _sc = 1.0f;
		_sc_txt.setText(Float.toString(sc));
	}

	public float getScale() {
		float sc = -1.0f;
		try {
			sc = Float.parseFloat(_sc_txt.getText());
		} catch (Exception e) {}
		if (sc < 0f || sc == 1f) sc = 0f;
		return sc;
	}

	public void setLeft(float lm) {
		_lm = lm;
		_lm_txt.setText(Float.toString(lm));
	}

	public float getLeft() {
		float lm = -1.0f;
		try {
			lm = Float.parseFloat(_lm_txt.getText());
		} catch (Exception e) {}
		return lm;
	}

	public void setTop(float tm) {
		_tm = tm;
		_tm_txt.setText(Float.toString(tm));
	}

	public float getTop() {
		float tm = -1.0f;
		try {
			tm = Float.parseFloat(_tm_txt.getText());
		} catch (Exception e) {}
		return tm;
	}

	public boolean doDialog(JFrame frame) {
		boolean chg = false;
		Dialog dlg = _prefs.createDialog(frame, "Set Paper Options");
		dlg.setVisible(true);
		Object res = _prefs.getValue();
		if (_btns[0].equals(res)) {
			float f = getLeft();
			if (f >= 0 && f != _lm) {
				chg = true;
			}
			f = getTop();
			if (f >= 0 && f != _tm) {
				chg = true;
			}
			MediaSizeName m = getMedia();
			if (m != _ms) {
				chg = true;
			}
			OrientationRequested o = getOrient();
			if (o != _or) {
				chg = true;
			}
		}
		return chg;
	}

	public PaperDialog() {
		_dia_pn = new JPanel();
		JPanel pn;

		GridBagLayout gridbag = new GridBagLayout();
		_dia_pn.setLayout(gridbag);
		GridBagConstraints s = new GridBagConstraints();
		s.fill = GridBagConstraints.NONE;
		s.gridx = 1;
		s.gridy = 1;
		s.weightx = 1;
		s.weighty = 1;
		s.gridwidth = 1;
		s.gridheight = 1;
		s.insets.top = 0;
		s.insets.bottom = 0;
		s.insets.left = 0;
		s.insets.right = 0;
		s.anchor = GridBagConstraints.WEST;

		Vector<MediaSizeName> mss = new Vector<MediaSizeName>();
		mss.add(_m11x14);
		PrintService prt = PrintServiceLookup.lookupDefaultPrintService();
		if (prt != null) {
			Object attr = prt.getSupportedAttributeValues(Media.class, null, null);
			if (attr instanceof Media[]) {
				Media[] meds = (Media[])attr;
				for (Media media : meds) {
					// Filter for physical paper sizes
					if (media instanceof MediaSizeName) {
						mss.add((MediaSizeName)media);
					}
				}
			}
		} else {
			System.err.format("No Default Printer set in system.\n");
		}
		if (mss.size() == 1) {
			mss.add(MediaSizeName.NA_LETTER);
			mss.add(MediaSizeName.NA_LEGAL);
			mss.add(MediaSizeName.ISO_A4);
		}
		_ms_cb = new JComboBox<MediaSizeName>(mss);
		gridbag.setConstraints(_ms_cb, s);
		_dia_pn.add(_ms_cb);
		s.gridy += 1;

		Vector<OrientationRequested> ors = new Vector<OrientationRequested>();
		ors.add(OrientationRequested.PORTRAIT);
		ors.add(OrientationRequested.LANDSCAPE);
		_or_cb = new JComboBox<OrientationRequested>(ors);
		gridbag.setConstraints(_or_cb, s);
		_dia_pn.add(_or_cb);
		s.gridy += 1;

		_sc_txt = new JTextField();
		_sc_txt.setPreferredSize(new Dimension(50, 20));
		pn = new JPanel();
		pn.add(new JLabel("Scaling:"));
		pn.add(_sc_txt);
		gridbag.setConstraints(pn, s);
		_dia_pn.add(pn);
		s.gridy += 1;

		_lm_txt = new JTextField();
		_lm_txt.setPreferredSize(new Dimension(50, 20));
		pn = new JPanel();
		pn.add(new JLabel("Left Offset:"));
		pn.add(_lm_txt);
		gridbag.setConstraints(pn, s);
		_dia_pn.add(pn);
		s.gridy += 1;

		_tm_txt = new JTextField();
		_tm_txt.setPreferredSize(new Dimension(50, 20));
		pn = new JPanel();
		pn.add(new JLabel("Top Offset:"));
		pn.add(_tm_txt);
		gridbag.setConstraints(pn, s);
		_dia_pn.add(pn);
		s.gridy += 1;

		_btns = new Object[2];
		_btns[0] = "Apply";
		_btns[1] = "Cancel";
		_prefs = new JOptionPane(_dia_pn, JOptionPane.QUESTION_MESSAGE,
				JOptionPane.YES_NO_CANCEL_OPTION, null, _btns);
	}
}
