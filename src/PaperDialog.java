// Copyright (c) 2016 Douglas Miller

import java.awt.*;
import java.util.Vector;
import javax.swing.*;
import javax.print.attribute.standard.*;

class PaperDialog
{

	public enum PaperMediaSize {
		NA_LETTER,
		NA_LEGAL,
		NA_FORMS,
	}

	public class PaperDim {
		float _w, _h;
		public PaperDim(float w, float h) {
			_w = w;
			_h = h;
		}
		public float getWidth() { return _w; }
		public float getHeight() { return _h; }
	}

	JPanel _dia_pn;

	private JComboBox<PaperMediaSize> _ms_cb;
	private JComboBox<OrientationRequested> _or_cb;
	private JTextField _lm_txt;
	private JTextField _tm_txt;
	private float _lm, _tm;
	PaperMediaSize _ms;
	OrientationRequested _or;
	JOptionPane _prefs;
	Object[] _btns;


	public void setMedia(PaperMediaSize ms) {
		_ms = ms;
		_ms_cb.setSelectedItem(ms);
	}

	public PaperMediaSize getMedia() {
		return (PaperMediaSize)_ms_cb.getSelectedItem();
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

	public PaperDim paperSize(PaperMediaSize ms) {
		PaperDim d = null;
		if (ms == PaperMediaSize.NA_LETTER) {
			d = new PaperDim(8.5f, 11.0f);
		} else if (ms == PaperMediaSize.NA_LEGAL) {
			d = new PaperDim(8.5f, 14.0f);
		} else if (ms == PaperMediaSize.NA_FORMS) {
			d = new PaperDim(11.0f, 14.0f);
		}
		return d;
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
			PaperMediaSize m = getMedia();
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

		Vector<PaperMediaSize> mss = new Vector<PaperMediaSize>();
		mss.add(PaperMediaSize.NA_LETTER);
		mss.add(PaperMediaSize.NA_LEGAL);
		mss.add(PaperMediaSize.NA_FORMS);
		_ms_cb = new JComboBox<PaperMediaSize>(mss);
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
