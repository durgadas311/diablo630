// Copyright (c) 2016 Douglas Miller

import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.Arrays;
import java.util.Vector;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.io.*;
import java.awt.*;
import java.awt.print.*;
import java.awt.font.*;
import java.awt.event.*;
import javax.swing.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;

class Diablo630 extends Printer_Paper
	implements ActionListener, Runnable
{
	int _pages;
	int _partial;
	boolean gui = true;
	PrinterConsole _cons;
	private javax.swing.Timer timer;
	private File _file;
	private FileOutputStream _fos;
	private String _fosName;
	private boolean _append;
	private Font _font;
	private PaperDialog _pset;
	private FontDialog _fset;
	private InputStream _inp;
	SimpleDateFormat tagFmt = new SimpleDateFormat("yyyyMMddHHmmss.SSS");

	private boolean _changed;
	private boolean _font_chg;
	private boolean _init;
	private boolean _page_done;
	private boolean _adjacent;
	private boolean _dir;	// print direction
	private int _esc;
	private float _x, _y;
	// These must remain stable during a job
	private float _fw, _fa;
	private float _vsi, _hsi;	// changed by printer commands
	private float _vsx, _hsx;
	// may be changed mid-job without affecting
	PaperDialog.PaperMediaSize _ms;
	OrientationRequested _or;
	PaperPaintable _bkg;
	DocAttributeSet _dset;	// must be setup before Print2DtoStream
	PrintRequestAttributeSet _rset;
	private float _lm;
	private float _tm;
	private int _lpi;
	private int _cpi;
	// These are all derived during init/setPaper, not changed after
	private float _pw, _ph;	// adjusted for orientation
	private float _ppw, _pph;	// _ppw is always short edge
	private int _pwx, _phx;
	private enum Actions { NONE, DISCARD, SAVE, QUEUE };
	private Actions action = Actions.NONE;
	private String[] queueJob = null;

	private PipedInputStream _pipe_i;
	private PipedOutputStream _pipe_o;

	public DocAttributeSet getDocAttrs() {
		return _dset;
	}

	public PrintRequestAttributeSet getPrtAttrs() {
		return _rset;
	}

	public PaperPaintable getBkground() {
		return _bkg;
	}

	private void clearPage() {
		_adjacent = false;
		clear();
		_x = _y = 0;
		_page_done = false;
	}

	public void endJob() {
		if (_cons != null) {
			timer.stop();
		}
		//System.err.println("End of job");
		if (_partial > 0) {
			_partial = 0;
			++_pages;
		}
		if (_cons != null) {
			_cons.setPages(_pages, _partial);
			_cons.setStatus("Done");
		}
		try {
			_fos.close();
		} catch (Exception ee) {}
		_fos = null;
		clearPage();
		// Check for end-of-job actions...
		if (action == Actions.NONE) {
			return;
		}
		if (action == Actions.DISCARD) {
			return;
		}
		if (action == Actions.SAVE) {
			if (_file == null) {
				return;
			}
			_append = false; // does not matter unless rename fails
			String dir = _file.getParent();
			if (dir == null) {
				dir = ".";
			}
			String ren = String.format("%s/job%s.ps", dir,
				tagFmt.format(new Date()));
			File save = new File(ren);
			try {
				_file.renameTo(save);
			} catch (Exception ee) {
				System.err.format("Failed to rename job to %s\n", ren);
			}
			return;
		}
		if (action == Actions.QUEUE && _fosName != null) {
			String[] cmd = new String[queueJob.length];
			for (int x = 0; x < queueJob.length; ++x) {
				cmd[x] = queueJob[x].replaceAll("%f", _fosName);
			}
			try {
				Process p = Runtime.getRuntime().exec(cmd);
				// TODO: need to get stdout/stderr?
				int ret = p.waitFor();
				if (ret != 0) {
					System.err.format("JobEnd exit %d: %s\n", ret, cmd[0]);
				}
			} catch (Exception ee) {
				System.err.format("JobEnd failed: %s - %s\n", cmd[0], ee.getMessage());
			}
			_append = false;
			return;
		}
	}

	// Called at the top of the run() loop, this is our chance
	// to make major changes in the print environment.
	private void setNewFile() {
		try {
			_fos = new FileOutputStream(_file, _append);
			_fosName = _file.getAbsolutePath();
		} catch (Exception ee) {
			_fosName = null;
			_file = null;
			if (_cons != null) {
				_cons.setFileName(_file);
			}
			try {
				_fos = new FileOutputStream("/dev/null");
			} catch (Exception eee) {}
		}
		_append = true;
		if (_changed) {
			_changed = false;
			setupPaper(_ms, _or);
			_pages = _partial = 0;
			if (_cons != null) {
				_cons.setPages(_pages, _partial);
				_cons.setStatus("Idle");
			}
			_init = false;	// causes re-init
		}
	}

	public void reset() {
		// anything?
	}

	class DocPaperSize extends MediaSize implements DocAttribute {
		public DocPaperSize(float w, float h, int units) {
			super(w, h, units);
		}
	}

	class ReqPaperSize extends MediaSize implements PrintRequestAttribute {
		public ReqPaperSize(float w, float h, int units) {
			super(w, h, units);
		}
	}

	// This can change during a print job without affecting it.
	// Called from setup menu when paper has changed.
	public void setupPaper(PaperDialog.PaperMediaSize ms, OrientationRequested or) {
		PaperDialog.PaperDim pd = _pset.paperSize(ms);
		if (pd == null) {
			System.err.println("Unsupported Page Size");
			System.exit(1);
		}
		_ppw = pd.getWidth();	// always the small edge.
		_pph = pd.getHeight();
		if (or == OrientationRequested.LANDSCAPE) {
			_pw = _pph;
			_ph = _ppw;
		} else if (or == OrientationRequested.PORTRAIT) {
			_pw = _ppw;
			_ph = _pph;
		} else {
			System.err.println("Unsupported Page Orientation");
			System.exit(1);
		}
		_dset = new HashDocAttributeSet();
		//_dset.add(ms);
		_dset.add(or);
		_dset.add(new DocPaperSize(_ppw, _pph, Size2DSyntax.INCH));
		_dset.add(new MediaPrintableArea(0.0f, 0.0f, _ppw, _pph,
				MediaPrintableArea.INCH));
		_rset = new HashPrintRequestAttributeSet();
		_rset.add(or);
		_rset.add(new ReqPaperSize(_ppw, _pph, Size2DSyntax.INCH));
		_rset.add(new MediaPrintableArea(0.0f, 0.0f, _ppw, _pph,
				MediaPrintableArea.INCH));
	}

	// _hsx/_vsx must be established prior to calling this
	// also _pw/_ph must have been set.
	public void setPaper(float l, float t) {
		float lm = _hsx * l;
		float tm = _vsx * t;
		// TODO: round values?
		_pwx = (int)Math.floor(_hsx * _pw);
		_phx = (int)Math.floor(_vsx * _ph);
		setScale(_pwx, _phx);
		super.setPage(_pwx, _phx, (int)lm, (int)tm);
	}

	public void init(Graphics g) {
		if (_init) {
			return;
		}
		_init = true;
		if (_cons != null) {
			_cons.setChanges(false);
		}
		Graphics2D g2d = (Graphics2D)g;
		if (_font_chg) {
			// any of font or cpi have changed...
			// cpi/lpi is trivial so always done.
			String fmt = "%0" + _cpi + "d";
			String txt = String.format(fmt, 0);
			FontRenderContext frc = g2d.getFontRenderContext();
			float x = (float)_font.getStringBounds(txt, frc).getWidth();
			LineMetrics lm = _font.getLineMetrics(txt, frc);
			_fa = lm.getAscent();
			// Because we adjust font tracking to match _cpi,
			// _hsi/_hsx become trivial/constant like _vsx/_vsi.
			if (x != 72) {
				float t = (72 - x) / _cpi / _font.getSize2D();
				//System.err.format("Tracking = %f\n", t);
				Map<TextAttribute, Object> atr = new HashMap<TextAttribute, Object>();
				atr.put(TextAttribute.TRACKING, t);
				_font = _font.deriveFont(atr);
			}
			super.init(_font, _fa);
		}
		_hsi = 72 / _cpi;
		_vsi = 72 / _lpi;	// trivial
		_fw = _hsi;	// "natural" pitch
		setPaper(_lm, _tm);
	}

	private int getCpi(String p) {
		int c = 0;
		try {
			c = Integer.valueOf(p);
			if (c != 10 && c != 12 && c != 15) {
				c = 0;
			}
		} catch (Exception ee) {}
		return c;
	}

	private int getLpi(String p) {
		int l = 0;
		try {
			l = Integer.valueOf(p);
			if (l != 6 && l != 8) {
				l = 0;
			}
		} catch (Exception ee) {}
		return l;
	}

	public Diablo630(Properties props, Vector<String> args, InputStream in) {
		// args override props...
		int lpi = 6;
		int cpi = 10;
		Font font = new Font("Monospaced", Font.PLAIN, 12);
		String[] fargs = null;
		String[] pargs = null;
		String p = props.getProperty("diablo630_cpi");
		if (p != null) {
			int c = getCpi(p);
			if (c != 0) {
				cpi = c;
			}
		}
		p = props.getProperty("diablo630_lpi");
		if (p != null) {
			int l = getLpi(p);
			if (l != 0) {
				lpi = l;
			}
		}
		p = props.getProperty("diablo630_font");
		if (p != null) {
			fargs = p.split("\\s");
			// TODO: check validity?
			if (fargs.length != 3) {
			}
		}
		p = props.getProperty("diablo630_paper");
		if (p != null) {
			pargs = p.split("\\s");
		}
		p = props.getProperty("diablo630_nogui");
		if (p != null) {
			// TODO: how to interpret values...
			try {
				gui = !Boolean.valueOf(p);
			} catch (Exception ee) {}
		}
		p = props.getProperty("diablo630_jobend");
		if (p != null) {
			String[] ss = p.split("\\s");
			if (ss.length < 1) {
			} else if (ss[0].equalsIgnoreCase("DISCARD")) {
				action = Actions.DISCARD;
			} else if (ss[0].equalsIgnoreCase("SAVE")) {
				action = Actions.SAVE;
				// TODO: get optional filename pattern
			} else if (ss.length > 1 && ss[0].equalsIgnoreCase("QUEUE")) {
				action = Actions.QUEUE;
				// Replace "%f" with filename at run time...
				// Other replacements?
				queueJob = Arrays.copyOfRange(ss, 1, ss.length);
			} else {
				System.err.format("Invalid jobend action: %s\n", p);
				action = Actions.NONE;
			}
		}
		for (String arg : args) {
			if (arg.startsWith("cpi=")) {
				int c = getCpi(arg.substring(4));
				if (c != 0) {
					cpi = c;
				}
			} else if (arg.startsWith("lpi=")) {
				int l = getLpi(arg.substring(4));
				if (l != 0) {
					lpi = l;
				}
			} else if (arg.startsWith("font=")) {
				fargs = arg.substring(5).split(",");
				if (fargs.length != 3) {
				}
			} else if (arg.startsWith("paper=")) {
				pargs = arg.substring(6).split(",");
			} else if (arg.equals("nogui")) {
				gui = false;
			}
		}
		if (fargs != null) {
			// TODO: fully parse font args
			int fs = Font.PLAIN;
			int fp = 12;
			try {
				fp = Integer.valueOf(fargs[2]);
			} catch (Exception ee) {}
			font = new Font(fargs[0], fs, fp);
		}

		_ms = PaperDialog.PaperMediaSize.NA_LETTER;
		_or = OrientationRequested.PORTRAIT;
		_bkg = null;
		if (pargs != null) {
			for (String parg : pargs) {
				if (parg.equalsIgnoreCase("LETTER")) {
					_ms = PaperDialog.PaperMediaSize.NA_LETTER;
				} else if (parg.equalsIgnoreCase("LEGAL")) {
					_ms = PaperDialog.PaperMediaSize.NA_LEGAL;
				} else if (parg.equalsIgnoreCase("FORMS")) {
					_ms = PaperDialog.PaperMediaSize.NA_FORMS;
				} else if (parg.equalsIgnoreCase("PORTRAIT")) {
					_or = OrientationRequested.PORTRAIT;
				} else if (parg.equalsIgnoreCase("LANDSCAPE")) {
					_or = OrientationRequested.LANDSCAPE;
				}
			}
		}

		_inp = in;
		_font = font;
		_lpi = lpi;
		_cpi = cpi;
		// default to portrait 8.5x11, aligned upper-left corner (no margin)
		_lm = 0.0f;
		_tm = 0.0f;
		_dir = true;

		_vsi = 72 / _lpi; // re-computed later in init()
		_hsi = 72 / _cpi; // re-computed later in init()
		_vsx = 72;
		_hsx = 72;

		_adjacent = true;
		_page_done = false;
		_esc = 0;
		_pages = 0;
		_partial = 0;
		_changed = true;
		_font_chg = true;
		_init = false;
		_append = false; // only 'true' for multiple jobs, same file
		_cons = null;
		// We need this for paper management...
		_pset = new PaperDialog();
		setupPaper(_ms, _or);

		if (gui) {
			_fset = new FontDialog();

			_cons = new PrinterConsole("Diablo 630 Console");
			JMenu mu;
			JMenuItem mi;
			mu = new JMenu("File");
			mi = new JMenuItem("New Output File", KeyEvent.VK_F);
			mi.addActionListener(this);
			mu.add(mi);
			mi = new JMenuItem("Discard", KeyEvent.VK_D);
			mi.addActionListener(this);
			mu.add(mi);
			_cons.addMenu(mu);

			mu = new JMenu("Settings");
			mi = new JMenuItem("Paper", KeyEvent.VK_P);
			mi.addActionListener(this);
			mu.add(mi);
			mi = new JMenuItem("Font", KeyEvent.VK_W);
			mi.addActionListener(this);
			mu.add(mi);
			_cons.addMenu(mu);

			mu = new JMenu("Controls");
			mi = new JMenuItem("Form Feed", KeyEvent.VK_E);
			mi.addActionListener(this);
			mu.add(mi);
			mi = new JMenuItem("End Job", KeyEvent.VK_J);
			mi.addActionListener(this);
			mu.add(mi);
			_cons.addMenu(mu);

			timer = new Timer(50, this);

			_cons.setFont(_font);
			_cons.setPitch(_cpi, _lpi);
			_cons.setPaper(_ms, _or);
			_cons.setPosition(_lm, _tm);
			_cons.setPages(_pages, _partial);
			_cons.setStatus("Idle");
		}

		_pipe_i = new PipedInputStream();
		try {
			_pipe_o = new PipedOutputStream(_pipe_i);
		} catch (Exception ee) {
			System.err.println("Unable to create pipe!");
			System.exit(1);
		}
		Thread t = new Thread(this);
		t.start();
	}

	public void endPage() {
		_page_done = true;
		++_pages;
		_partial = 0;
		if (_cons != null) {
			_cons.setPages(_pages, _partial);
		}
		_y = 0;
	}

	private void index() {
		_y += _vsi;
		if (_y >= _phx) {
			endPage();
		}
	}

	private void revindex() {
		_y -= _vsi;
		if (_y < 0) _y = 0;
	}

	private void space() {
		_x += _hsi;
		if (_x >= _pwx) _x = _pwx - 1;
	}

	private void bkspace() {
		_x -= _hsi;
		if (_x < 0) _x = 0;
	}

	private void forward() {
		if (_dir) {
			space();
		} else {
			bkspace();
		}
	}

	private void backward() {
		if (_dir) {
			bkspace();
		} else {
			space();
		}
	}

	private void tab() {
		// NOTE: TAB does not reverse...
		int x = (int)Math.floor(_x / _fw);
		x &= ~7;
		x += 8;
		_x = x * _fw;
	}

	private String do_esc2(byte b) {
		String ret = null; // non-printable...
		switch(_esc) {
		case 30: // RS - set VSI
			_vsi = ((b - 1) * _vsx) / 48.0f;
			break;
		case 31: // US - set HSI
			_hsi = ((b - 1) * _hsx) / 120.0f;
//System.err.format("_hsi=%f width=%f\n", _hsi, _fw);
			break;
		}
		_esc = 0;
		return ret;
	}

	private String do_esc(byte b) {
		if (_esc > 1) {
			return do_esc2(b);
		}
		String ret = null;
		_esc = 0;
		switch(b) {
		case 30: // RS - set VSI
		case 31: // US - set HSI
			_esc = b;
			break;
		case '5':
			_dir = true; // FWD
			break;
		case '6':
			_dir = false; // BAK
			break;
		case '\n':
			revindex();
			_adjacent = false;
			break;
		// Juki printer extensions
		case 'H':
			ret = "\u00A7";
			break;
		case 'I':
			ret = "\u00A3";
			break;
		case 'J':
			ret = "\u00A8";
			break;
		case 'K':
			ret = "\u00E7";
			break;
		case 'Y':
			ret = "\u00A2";
			break;
		case 'Z':
			ret = "\u00AC";
			break;
		}
		return ret;
	}

	public boolean do_char(byte b) {
		if (_cons != null) {
			if (!timer.isRunning()) {
				_cons.setStatus("Active");
			}
			timer.restart();
		}
		if (_page_done) {
			clearPage();
		}
		String s = null;
		if (_esc > 0) {
			s = do_esc(b);
		} else if (b < ' ') { // control characters... incl BLANK
			s = null;	// not strictly printable...
			switch(b) {
			case '\r':
				_adjacent = false;
				_dir = true;
				// Also resets Print Suppression, Graphics Mode,
				// Offset, Bold, Shadow, Auto Center.
				_x = 0;
				break;
			case '\n':
				_adjacent = false;
				index();
				break;
			case '\b':
				_adjacent = false;
				backward();
				break;
			case ' ':
				_adjacent = false;
				forward();
				break;
			case '\t':
				_adjacent = false;
				tab();
				break;
			case 12:	// FF
				_adjacent = false;
				endPage();
				break;
			case 27:	// ESC
				_esc = 1;
				break;
			case 7:	// BEL
				// TODO: audible alarm?
				break;
			default:
				break;
			}
		} else {
			s = new String();	// assume printable...
			s += (char)b;
		}
		// Assumes progress is always down the page...
		int p = (int)Math.ceil((_y / _phx) * 10);
		if (p > 9) {
			p = 9;
		}
		if (p > _partial) {
			_partial = p;
			if (_cons != null) {
				_cons.setPages(_pages, _partial);
			}
		}
		if (s != null) {
			if (_adjacent) {
				if (_dir) {
					super.appendLastPlot(s, _x, _y);
				} else {
					super.prependLastPlot(s, _x, _y);
				}
			} else {
				super.addPlot(s, _x, _y);
			}
			forward();
			_adjacent = (Math.round(_hsi) == Math.round(_fw));
if (false && !_adjacent) {
System.err.println("Not adjacent: " + _hsi + " vs " + _fw);
}
		}
		return _page_done;
	}

	// Not used when nogui (_cons == null)
	public boolean getFile() {
		boolean chg = false;
		SuffFileChooser ch = new SuffFileChooser("Select", _file);
		int rv = ch.showDialog(_cons.getFrame());
		if (rv == JFileChooser.APPROVE_OPTION) {
			_file = ch.getSelectedFile();
			chg = true;
		}
		return chg;
	}

	public void actionPerformed(ActionEvent e) {
		if (_cons == null) {
			// should never be called anyway
			return;
		}
		if (e.getSource() == timer) {
			timer.stop();
			_cons.setStatus("Idle");
			return;
		}
		if (e.getSource() instanceof JMenuItem) {
			JMenuItem m = (JMenuItem)e.getSource();
			if (m.getMnemonic() == KeyEvent.VK_F) {
				boolean chg = getFile();
				if (chg) {
					_append = false;
					_changed = true;
					_cons.setChanges(true);
					_cons.setFileName(_file);
					inject(0xff);
				}
				return;
			}
			if (m.getMnemonic() == KeyEvent.VK_D) {
				// can't close _fos or else printjob will throw exception...
				// So tell printjob that file changed, and unset _append
				// to cause a truncate.
				_append = false;
				_changed = true;
				_cons.setChanges(true);
				inject(0xff);
				return;
			}
			if (m.getMnemonic() == KeyEvent.VK_P) {
				// Paper settings dialog...
				_pset.setMedia(_ms);
				_pset.setOrient(_or);
				_pset.setLeft(_lm);
				_pset.setTop(_tm);
				boolean chg = _pset.doDialog(_cons.getFrame());
				if (chg) {
					_ms = _pset.getMedia();
					_or = _pset.getOrient();
					_bkg = _pset.getBkground();
					_lm = _pset.getLeft();
					_tm = _pset.getTop();
					_changed = true;
					_cons.setChanges(true);
					_cons.setPaper(_ms, _or);
					_cons.setPosition(_lm, _tm);
					inject(0xff);
				}
				return;
			}
			if (m.getMnemonic() == KeyEvent.VK_W) {
				// font (printwheel) settings dialog...
				_fset.setFont(_font);
				_fset.setPitch(_cpi, _lpi);
				boolean chg = _fset.doDialog(_cons.getFrame());
				if (chg) {
					_font = _fset.getFont();
					_cpi = _fset.getCPI();
					_lpi = _fset.getLPI();
					_changed = true;
					_font_chg = true;
					_cons.setChanges(true);
					_cons.setFont(_font);
					_cons.setPitch(_cpi, _lpi);
					inject(0xff);
				}
				return;
			}
			if (m.getMnemonic() == KeyEvent.VK_E) {
				// Form Feed (Eject page)
				inject(0x0c);
				return;
			}
			if (m.getMnemonic() == KeyEvent.VK_J) {
				// End Job
				inject(0xff);
				return;
			}
		}
		System.err.println("Unknown action event");
	}

	private void inject(int b) {
		try {
			_pipe_o.write((byte)b);
			_pipe_o.flush();
		} catch (Exception ee) { }
	}

	// The input service thread...
	public void run() {
		int b = -1;
		do {
			try {
				b = _inp.read();
			} catch (Exception ee) {
				b = -1;
			}
			if (b >= 0 && b <= 255) {
				try {
					_pipe_o.write((byte)b);
					_pipe_o.flush();
				} catch (Exception ee) {
					b = -1;
				}
			}
		} while (b >= 0);
		try {
			_pipe_o.close();
		} catch (Exception ee) {}
	}

	public int readPrinterStream() {
		int b = -1;
		try {
			b = _pipe_i.read();
		} catch (Exception ee) {
			b = -1;
		}
		return b;
	}

	public void runPrinter(String file) {
		int status = 0;
		_file = new File(file);
		if (_cons != null) {
			_cons.setFileName(_file);
		}
		do {
			setNewFile();	// opens _fos
			Print2DtoStream p2s = new Print2DtoStream(_fos, this);
			endJob();	// closes _fos
			status = p2s.getStatus();
		} while (status >= 0);
	}
}
