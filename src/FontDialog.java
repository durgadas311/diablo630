// Copyright (c) 2016 Douglas Miller

import java.awt.*;
import java.util.Map;
import java.awt.font.*;
import javax.swing.*;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

// Adjust font spacing:
// Map<TextAttribute, Object> attributes = new HashMap<TextAttribute, Object>();
// attributes.put(TextAttribute.TRACKING, 0.5);
// Font font2 = font.deriveFont(attributes);
// gr.setFont(font2);
// gr.drawString("testing",0,20);

class FontDialog
{
	JPanel _dia_pn;

	private JComboBox<String> _fn_cb;
	private JTextField _fs_txt;
	private JCheckBox _fw_cb;
	private JCheckBox _fp_cb;
	private JRadioButton _cpi_10;
	private JRadioButton _cpi_12;
	private JRadioButton _cpi_15;
	private JRadioButton _lpi_6;
	private JRadioButton _lpi_8;
	private ButtonGroup _cpi_bg;
	private ButtonGroup _lpi_bg;
	String[] _fl;
	String _fn;
	int _fs;
	boolean _fw;	// i.e. BOLD
	boolean _fp;	// i.e. OBLIQUE (italics)
	Font _f;	// only if changed...
	JOptionPane _prefs;
	Object[] _btns;

	public void setFont(Font font) {
		_fn = font.getFontName();
		int i = _fn.indexOf('.');
		if (i >= 0) {
			_fn = _fn.substring(0, i);
		}
		for (i = 0; i < _fl.length; i++) {
			if (_fl[i].equals(_fn)) {
				_fn_cb.setSelectedIndex(i);
				break;
			}
		}
		_fs = font.getSize();
		Map<TextAttribute,?> atrs = font.getAttributes();
		_fw = false; // TextAttribute.WEIGHT_REGULAR;
		_fp = false; // TextAttribute.POSTURE_REGULAR;

		if (atrs.containsKey(TextAttribute.WEIGHT)) {
			Object wt = atrs.get(TextAttribute.WEIGHT);
			if (wt == TextAttribute.WEIGHT_BOLD ||
			    wt == TextAttribute.WEIGHT_DEMIBOLD ||
			    wt == TextAttribute.WEIGHT_EXTRABOLD ||
			    wt == TextAttribute.WEIGHT_HEAVY ||
			    wt == TextAttribute.WEIGHT_MEDIUM ||
			    wt == TextAttribute.WEIGHT_SEMIBOLD ||
			    wt == TextAttribute.WEIGHT_ULTRABOLD) {
				_fw = true;
			}
		}
		if (atrs.containsKey(TextAttribute.POSTURE)) {
			Object po = atrs.get(TextAttribute.POSTURE);
			if (po == TextAttribute.POSTURE_OBLIQUE) {
				_fp = true;
			}
		}
		_fs_txt.setText(Integer.toString(_fs));
		_fw_cb.setSelected(_fw); 
		_fp_cb.setSelected(_fp); 
	}

	public Font getFont() {
		return _f;
	}

	public void setPitch(int cpi, int lpi) {
		switch(cpi) {
		case 15:
			_cpi_15.setSelected(true);
			break;
		case 12:
			_cpi_12.setSelected(true);
			break;
		default:
			_cpi_10.setSelected(true);
			break;
		}
		switch(lpi) {
		case 8:
			_lpi_8.setSelected(true);
			break;
		default:
			_lpi_6.setSelected(true);
			break;
		}
	}

	public int getCPI() {
		ButtonModel bm = _cpi_bg.getSelection();
		try {
			return Integer.parseInt(bm.getActionCommand());
		} catch (Exception ee) {}
		return 10;
	}

	public int getLPI() {
		ButtonModel bm = _lpi_bg.getSelection();
		try {
			return Integer.parseInt(bm.getActionCommand());
		} catch (Exception ee) {}
		return 6;
	}

	public boolean doDialog(JFrame frame) {
		_f = null;
		boolean chg = false;
		ButtonModel cpi_bm = _cpi_bg.getSelection();
		ButtonModel lpi_bm = _lpi_bg.getSelection();
		Dialog dlg = _prefs.createDialog(frame, "Set Font Options");
		dlg.setVisible(true);
		Object res = _prefs.getValue();
		if (_btns[0].equals(res)) {
			String fn = (String)_fn_cb.getSelectedItem();
			if (!fn.equals(_fn)) {
				chg = true;
			}
			int fs = 0;
			try {
				fs = Integer.parseInt(_fs_txt.getText());
			} catch (Exception e) {}
			if (fs > 0 && fs != _fs) {
				chg = true;
			}
			if (_fw != _fw_cb.isSelected()) {
				chg = true;
			}
			if (_fp != _fp_cb.isSelected()) {
				chg = true;
			}
			if (!cpi_bm.equals(_cpi_bg.getSelection())) {
				chg = true;
			}
			if (!lpi_bm.equals(_lpi_bg.getSelection())) {
				chg = true;
			}
			if (fs > 0 && chg) {
				int fa = 0;
				if (_fw_cb.isSelected()) {
					fa |= Font.BOLD;
				}
				if (_fp_cb.isSelected()) {
					fa |= Font.ITALIC;
				}
				if (fa == 0) {
					fa = Font.PLAIN;
				}
				_f = new Font(fn, fa, fs);
			}
			if (fs <= 0 || _f == null) {
System.err.println("Failed to get a matching font");
				chg = false;
			}
		}
		return chg;
	}

	public FontDialog() {
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

		_fl = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		_fn_cb = new JComboBox<String>(_fl);
		gridbag.setConstraints(_fn_cb, s);
		_dia_pn.add(_fn_cb);
		s.gridy += 1;

		_fs_txt = new JTextField();
		_fs_txt.setPreferredSize(new Dimension(50, 20));
		pn = new JPanel();
		pn.add(new JLabel("Point Size:"));
		pn.add(_fs_txt);
		gridbag.setConstraints(pn, s);
		_dia_pn.add(pn);
		s.gridy += 1;

		_fw_cb = new JCheckBox("Bold");
		gridbag.setConstraints(_fw_cb, s);
		_dia_pn.add(_fw_cb);
		s.gridy += 1;

		_fp_cb = new JCheckBox("Italics");
		gridbag.setConstraints(_fp_cb, s);
		_dia_pn.add(_fp_cb);
		s.gridy += 1;

		_cpi_10 = new JRadioButton("10");
		_cpi_10.setActionCommand("10");
		_cpi_12 = new JRadioButton("12");
		_cpi_12.setActionCommand("12");
		_cpi_15 = new JRadioButton("15");
		_cpi_15.setActionCommand("15");
		_cpi_bg = new ButtonGroup();
		_cpi_bg.add(_cpi_10);
		_cpi_bg.add(_cpi_12);
		_cpi_bg.add(_cpi_15);
		pn = new JPanel();
		pn.add(new JLabel("cpi:"));
		pn.add(_cpi_10);
		pn.add(_cpi_12);
		pn.add(_cpi_15);
		gridbag.setConstraints(pn, s);
		_dia_pn.add(pn);
		s.gridy += 1;

		_lpi_6 = new JRadioButton("6");
		_lpi_6.setActionCommand("6");
		_lpi_8 = new JRadioButton("8");
		_lpi_8.setActionCommand("8");
		_lpi_bg = new ButtonGroup();
		_lpi_bg.add(_lpi_6);
		_lpi_bg.add(_lpi_8);
		pn = new JPanel();
		pn.add(new JLabel("lpi:"));
		pn.add(_lpi_6);
		pn.add(_lpi_8);
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
