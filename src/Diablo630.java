// Copyright (c) 2016 Douglas Miller

import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.Arrays;
import java.util.List;
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
	static final int A_X_CLEAR = (Printer_Paper.A_SHAD |
					Printer_Paper.A_BOLD |
					Printer_Paper.A_UNDL);
	static final int A_CR_CLEAR = (Printer_Paper.A_SHAD |
					Printer_Paper.A_BOLD |
					Printer_Paper.A_UNDL);
	static final int A_LF_CLEAR = (Printer_Paper.A_SHAD |
					Printer_Paper.A_BOLD |
					Printer_Paper.A_UNDL);

	static final int ESC_H=0, ESC_I=1, ESC_J=2, ESC_K=3, ESC_Y=4, ESC_Z=5;
	private String[] _sppr = new String[]{ "h", "i", "j", "k", "y", "z" };
	private String[] _spcl = new String[]{
			"\u00A7", "\u00A3", "\u00A8", "\u00E7", "\u00A2", "\u00AC" };

	int _pages;
	int _partial;
	boolean gui = true;
	String teeName = null;
	OutputStream tee = null;
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
	SimpleDateFormat todFmt = new SimpleDateFormat("HHmmss.SSS");
	SimpleDateFormat datFmt = new SimpleDateFormat("yyyyMMdd");

	private boolean _changed;
	private boolean _font_chg;
	private boolean _init;
	private boolean _page_done;
	private boolean _adjacent;
	private boolean _dir;	// print direction
	private boolean _just;
	private boolean _cntr;
	private String _cline;
	private boolean _grph;
	private int _attr;
	private float _off;
	private int _esc;
	private float _x, _y;
	// These must remain stable during a job
	private float _fw, _fa;
	private float _vsi, _hsi;	// changed by printer commands
	private float _vsx, _hsx;
	// may be changed mid-job without affecting
	MediaSizeName _ms;
	OrientationRequested _or;
	PaperPaintable _bkg;
	PageFormat _pf;
	private float _lm;
	private float _tm;
	private int _lpi;
	private int _cpi;
	// These are all derived during init/setPaper, not changed after
	private float _pw, _ph;	// adjusted for orientation
	private float _ppw, _pph;	// _ppw is always short edge
	private enum Actions { NONE, DISCARD, SAVE, QUEUE, SAVE_QUEUE };
	private Actions action = Actions.NONE;
	private String[] queueJob = null;
	private String saveJob = null;
	private int jobId;
	private String jobFile = null;

	private PipedInputStream _pipe_i;
	private PipedOutputStream _pipe_o;

	public PageFormat getPageFormat() { return _pf; }

	private String doSubs(File dir, String pat) {
		String fn = pat;

		if (fn.indexOf("%x") >= 0) {
			String[] ss = fn.split("%x", 2);
			try {
				File tf = File.createTempFile(ss[0], ss[1], dir);
				return tf.getName();
			} catch (Exception ee) { }
			// punt...
			fn = fn.replaceAll("%x", tagFmt.format(new Date()));
		}
		if (pat.indexOf("%j") >= 0) {
			fn = fn.replaceAll("%j", String.format("%d", jobId));
		}
		if (pat.indexOf("%s") >= 0) {
			fn = fn.replaceAll("%s", tagFmt.format(new Date()));
		}
		if (pat.indexOf("%t") >= 0) {
			fn = fn.replaceAll("%t", todFmt.format(new Date()));
		}
		if (pat.indexOf("%d") >= 0) {
			fn = fn.replaceAll("%d", datFmt.format(new Date()));
		}
		if (pat.indexOf("%f") >= 0) {
			fn = fn.replaceAll("%f", jobFile);
		}
		// TODO: more substitutions
		return fn;
	}

	private boolean processJobEnd(String[] ss) {
		for (int x = 0; x < ss.length; ++x) {
			if (ss[x].equalsIgnoreCase("NONE")) {
				action = Actions.NONE;
			} else if (ss[x].equalsIgnoreCase("DISCARD")) {
				action = Actions.DISCARD;
			} else if (ss[x].equalsIgnoreCase("SAVE")) {
				if (action == Actions.QUEUE) {
					action = Actions.SAVE_QUEUE;
				} else {
					action = Actions.SAVE;
				}
				saveJob = "job%d.ps";
				if (x + 1 < ss.length) {
					++x;
					if (!ss[x].equals("-")) {
						saveJob = ss[x];
					}
				}
			} else if (x + 1 < ss.length && ss[x].equalsIgnoreCase("QUEUE")) {
				if (action == Actions.SAVE) {
					action = Actions.SAVE_QUEUE;
				} else {
					action = Actions.QUEUE;
				}
				// Replace "%f" with filename at run time...
				// Other replacements?
				++x;
				int y;
				for (y = x; y < ss.length; ++y) {
					if (ss[y].equals("--")) break;
				}
				queueJob = new String[y - x];
				int z = 0;
				while (x < y) {
					queueJob[z++] = ss[x++];
				}
			} else {
				return true;	// error
			}
		}
		return false;
	}

	public PaperPaintable getBkground() {
		return _bkg;
	}

	private void clearPage() {
		_adjacent = false;
		clear();
		_y = 0;
		_page_done = false;
	}

	public void endJob(int sts) {
		++jobId;
		if (tee != null) {
			try {
				tee.close();
			} catch (Exception ee) {}
			tee = null;
		}
		if (_cons != null) {
			timer.stop();
		}
		//System.err.println("End of job");
		boolean nothing = (_pages == 0 && _partial == 0);
		// reset page count since file is now empty
		_pages = 0;
		_partial = 0;
		if (_cons != null) {
			_cons.setPages(_pages, _partial);
			_cons.setStatus("Done");
		}
		try {
			_fos.close();
		} catch (Exception ee) {}
		_fos = null;
		clearPage();
		_append = false;
		// Check for end-of-job actions...
		// avoid corrupting SAVE file if aborting...
		if (sts < 0 && blankPage() && nothing) {
			return;
		}
		if (action == Actions.DISCARD || action == Actions.NONE) {
			return;
		}
		jobFile = _fosName;
		File dir = _file.getParentFile();
		if (dir == null) {
			dir = new File(System.getProperty("user.dir"));
		}
		if ((action == Actions.SAVE || action == Actions.SAVE_QUEUE) &&
								_file != null) {
			String fn = doSubs(dir, saveJob);
			File save = new File(dir, fn);
			try {
				_file.renameTo(save);
				jobFile = save.getAbsolutePath();;
			} catch (Exception ee) {
				System.err.format("Failed to rename job to %s\n",
							save.getAbsolutePath());
			}
			return;
		}
		if ((action == Actions.QUEUE || action == Actions.SAVE_QUEUE) &&
								jobFile != null) {
			String[] cmd = new String[queueJob.length];
			for (int x = 0; x < queueJob.length; ++x) {
				cmd[x] = doSubs(dir, queueJob[x]);
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
		_attr = 0;
		_dir = true;
		_just = false;
		_cntr = false;
		_cline = "";
		_grph = false;
		_adjacent = false;
		_off = 0;
		_esc = 0; // not needed?
		// _cpi, _lpi, _lm, _tm are set by config or menu - leave alone
		_hsi = 72.0f / _cpi;
		_vsi = 72.0f / _lpi;
		_x = 0;
		_page_done = true; // start over (clearPage()) on next char
		// TODO: avoid extra blank page
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
	public void setupPaper(MediaSizeName ms, OrientationRequested or) {
		MediaSize mz = _pset.paperSize(ms);
		if (mz == null) {
			System.err.println("Unsupported Page Size");
			System.exit(1);
		}
		_ppw = mz.getX(Size2DSyntax.INCH);	// always the small edge.
		_pph = mz.getY(Size2DSyntax.INCH);
		int pfo = PageFormat.LANDSCAPE;
		if (or == OrientationRequested.LANDSCAPE) {
			_pw = _pph;
			_ph = _ppw;
		} else if (or == OrientationRequested.PORTRAIT) {
			pfo = PageFormat.PORTRAIT;
			_pw = _ppw;
			_ph = _pph;
		} else {
			System.err.println("Unsupported Page Orientation");
			System.exit(1);
		}
		Paper ppr = new Paper();
		ppr.setSize(_ppw * 72d, _pph * 72d);
		ppr.setImageableArea(0.0d, 0.0d, _ppw * 72d, _pph * 72d);
		PageFormat pf = new PageFormat();
		pf.setPaper(ppr);
		pf.setOrientation(pfo);
		_pf = pf;
	}

	// _hsx/_vsx must be established prior to calling this
	// also _pw/_ph must have been set.
	public void setPaper(float l, float t) {
		float lm = _hsx * l;
		float tm = _vsx * t;
		// TODO: allow scaling...
		// TODO: round values?
		float pw = _hsx * _pw;
		float ph = _vsx * _ph;
		if (_scale > 0f) {
			pw /= _scale;
			ph /= _scale;
		}
		int pwx = (int)Math.floor(pw);
		int phx = (int)Math.floor(ph);
		setScale(pwx, phx);
		super.setPage(pwx, phx, (int)lm, (int)tm);
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
			_font_chg = false;
		}
		_hsi = 72.0f / _cpi;
		_vsi = 72.0f / _lpi;	// trivial
		_fw = _hsi;	// "natural" pitch
//System.err.format("_hsi=%f width=%f\n", _hsi, _fw);
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

	private void getSpclProp(Properties props, int x) {
		String p = props.getProperty("diablo630_esc_" + _sppr[x]);
		if (p != null) {
			int u = Integer.decode(p);
			_spcl[x] = Character.toString(u);
		}
	}

	public static String getConfig(String[] args) {
		for (String arg : args) {
			if (arg.startsWith("conf=")) {
				return arg.substring(5);
			}
		}
		String rc = System.getenv("DIABLO630_CONFIG");
		if (rc == null) {
			File f = new File("./diablo630.rc");
			if (f.exists()) {
				rc = f.getAbsolutePath();
			}
		}
		if (rc == null) {
			rc = System.getProperty("user.home") + "/.diablo630rc";
		}
		return rc;
	}

	static List<String> bools = Arrays.asList("nogui");
	public static void processArgs(Properties props, String[] args,
				List<String> _bools, String[] seq) {
		int x = 0;
		for (String arg : args) {
			if (arg.indexOf("=") > 0) {
				String[] ss = arg.split("=", 2);
				props.setProperty("diablo630_" + ss[0], ss[1]);
			} else if (bools.contains(arg) || _bools.contains(arg)) {
				props.setProperty("diablo630_" + arg, "true");
			} else if (x < seq.length) {
				props.setProperty("diablo630_" + seq[x++], arg);
			}
		}
	}

	public Diablo630(Properties props, InputStream in) {
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
		p = props.getProperty("diablo630_scale");
		if (p != null) {
			_scale = Float.valueOf(p);
		}
		_lm = 0.0f;
		_tm = 0.0f;
		p = props.getProperty("diablo630_left");
		if (p != null) {
			_lm = Float.valueOf(p);
		}
		p = props.getProperty("diablo630_top");
		if (p != null) {
			_tm = Float.valueOf(p);
		}
		p = props.getProperty("diablo630_font");
		if (p != null) {
			fargs = p.split("\\s");
			// TODO: check validity?
			if (fargs.length != 3) {
			}
		}
		p = props.getProperty("diablo630_alt_color");
		if (p != null) {
			_alt = new Color(Integer.valueOf(p, 16));
		}
		p = props.getProperty("diablo630_paper");
		if (p != null) {
			pargs = p.split("\\s");
		}
		getSpclProp(props, ESC_H);
		getSpclProp(props, ESC_I);
		getSpclProp(props, ESC_J);
		getSpclProp(props, ESC_K);
		getSpclProp(props, ESC_Y);
		getSpclProp(props, ESC_Z);
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
			if (processJobEnd(ss)) {
				System.err.format("Invalid jobend action: %s\n", p);
				action = Actions.NONE;
			}
		}
		p = props.getProperty("diablo630_tee");
		if (p != null) {
			// only first job, unless reset by menu
			teeName = p;
			try {
				tee = new FileOutputStream(teeName);
			} catch (Exception ee) {}
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

		_ms = MediaSizeName.NA_LETTER;
		_or = OrientationRequested.PORTRAIT;
		_bkg = null;
		if (pargs != null) {
			for (String parg : pargs) {
				if (parg.equalsIgnoreCase("LETTER")) {
					_ms = MediaSizeName.NA_LETTER;
				} else if (parg.equalsIgnoreCase("LEGAL")) {
					_ms = MediaSizeName.NA_LEGAL;
				} else if (parg.equalsIgnoreCase("A4")) {
					_ms = MediaSizeName.ISO_A4;
				} else if (parg.equalsIgnoreCase("FORMS")) {
					_ms = PaperDialog.getForms();
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
			if (teeName != null) {
				mi = new JMenuItem("Restart Tee", KeyEvent.VK_T);
				mi.addActionListener(this);
				mu.add(mi);
			}
			_cons.addMenu(mu);

			timer = new Timer(50, this);

			_cons.setFont(_font);
			_cons.setPitch(_cpi, _lpi);
			_cons.setPaper(_ms, _or);
			_cons.setScale(_scale);
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
		// FF at start - ignore
		if (_pages == 0 && _partial == 0) return;
		_page_done = true;
		++_pages;
		_partial = 0;
		if (_cons != null) {
			_cons.setPages(_pages, _partial);
		}
		_y = 0;
	}

	private boolean closeEnough(float a, float b) {
		return (Math.round(a * 10f) == Math.round(b * 10f));
	}

	private void index() {
		if (_grph) {
			_y += 1.5f;	// pts, 1/48"
			if (_y >= _phx) _y = _phx - 1;
			return;
		}
		_y += _vsi;
		if (_y >= _phx) {
			endPage();
		}
	}

	private void revindex() {
		if (_grph) {
			_y -= 1.5f;	// pts, 1/48"
			if (_y < 0) _y = 0;
			return;
		}
		_y -= _vsi;
		if (_y < 0) _y = 0;
	}

	private void fwdHalf() {
		float y = _y + _vsi / 2f;
		if (y >= _phx) return;
		_y = y;
	}

	private void revHalf() {
		_y -= _vsi / 2f;
		if (_y < 0) _y = 0;
	}

	private void space() {
		_x += _hsi + _off;
		if (_x >= _pwx) _x = _pwx - 1;
	}

	private void bk1tic() {
		_x -= 0.6f; // 1/120 == 0.6 pt
		if (_x < 0) _x = 0;
	}

	private void bkspace() {
		_x -= _hsi + _off;
		if (_x < 0) _x = 0;
	}

	private void forward() {
		if (_grph) {
			_x += 1.2f;	// pts, 1/60" (2/120")
			if (_x >= _pwx) _x = _pwx - 1;
		} else if (_dir) {
			space();
		} else {
			bkspace();
		}
	}

	private void advance() {
		if (_grph) return;
		forward();
	}

	private void backward() {
		if (_grph) {
			_x -= 1.2f;	// pts, 1/60" (2/120")
			if (_x < 0) _x = 0;
		} else if (_dir) {
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

	private void gotoCol(int col) {
		float x = (float)col * _hsi;
		if (x >= _pwx) {
			return;
		}
		_x = x;
	}

	private void gotoLine(int line) {
		float y = (float)line * _vsi;
		if (y >= _phx) {
			return;
		}
		_y = y;
	}

	private void doCenter() {
		// TODO: print all chars at standard spacing?
		_cntr = false;
		float len = (float)_cline.length() * _fw;
		float x = (_pwx - len) / 2f;
		super.addPlot(_cline, x, _y, 0);
		_cline = "";
	}

	private String do_esc2(byte b) {
		String ret = null; // non-printable...
		switch(_esc) {
		case '\t': // TAB - tab to col
			gotoCol((b - 1) & 0x7f);
			_adjacent = false;
			break;
		case 12: // FF - set lines/page
			break;
		case 11: // VT - v-tab to line
			gotoLine((b - 1) & 0x7f);
			_adjacent = false;
			break;
		case 17: // DC1 - set horiz offset
			// TODO: need to handle ESC Z = 01111111b?
			_off = ((float)(b & 0x3f) * _hsx) / 120.0f;
			if (_off != 0 && (b & 0x40) != 0) {
				_off = -_off;
			}
			break;
		case 30: // RS - set VSI
			_vsi = ((b - 1) * _vsx) / 48.0f;
			break;
		case 31: // US - set HSI
			_hsi = ((b - 1) * _hsx) / 120.0f;
//System.err.format("_hsi=%f width=%f\n", _hsi, _fw);
			break;
		case '\r': // CR - ignore?
			if (b == 'P') {
				reset();
			}
			break;
		case 26:   // SUB - ignore these 3-char cmds
			if (b == 'I') {
				reset();
			}
			break;
		default:   // ignore all other 3-char cmds
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
		case 12: // FF - set lines/page
		case 11: // VT - v-tab to line
		case '\r': // CR - ignore these 3-char cmds
		case 26:   // SUB - ignore these 3-char cmds
		case 25:   // EM - ignore these 3-char cmds
		case '\t': // TAB - tab to col
		case 17:   // DC1 - offset each char (until CR or ESC X)
		case 30:   // RS - set VSI
		case 31:   // US - set HSI
			_esc = b;
			break;
		case 2: // STX - unknown (emitted by WordStar)
			// TODO: determine what this is supposed to do.
			break;
		case '5':
			_dir = true; // FWD
			_adjacent = false;
			break;
		case '6':
			_dir = false; // BAK
			_adjacent = false;
			break;
		case '<':	// Enable inverted horiz printing
			break;
		case '>':	// Disable inverted horiz printing
			break;
		case '7':	// Enable print suppression
			break;
		case '\n':
			revindex();
			_adjacent = false;
			break;
		case 'E':	// start underscore
			_attr |= Printer_Paper.A_UNDL;
			_adjacent = false;
			break;
		case 'O':	// start bold (double-strike)
			_attr |= Printer_Paper.A_BOLD;
			_adjacent = false;
			break;
		case 'W':	// start shadow (dbl strike +1/120)
			_attr |= Printer_Paper.A_SHAD;
			_adjacent = false;
			break;
		case '&':	// end bold/shadow (or CR)
			_attr &= ~Printer_Paper.A_SHAD;
			_attr &= ~Printer_Paper.A_BOLD;
			_adjacent = false;
			break;
		case 'R':	// end underscore (or CR/LF)
			_attr &= ~Printer_Paper.A_UNDL;
			_adjacent = false;
			break;
		case 'X':	// end bold/shadow/offset (or CR)
			// TODO: finish/cancel any attrs
			// cancel centering, discard chars
			_cntr= false;
			_cline = ""; // discard anything collected
			_off = 0.0f;
			// TODO: cancel underline for all related plots...
			_attr &= ~A_X_CLEAR;
			_adjacent = false;
			break;
		case '\b':	// backspace -1/120
			bk1tic();
			_adjacent = false;
			break;
		case '=':	// auto center (until CR/LF/FF or ESC X discard)
			_cntr = true;
			_cline = "";
			// TODO: store chars until CR/LF/FF, then print
			break;
		case 'M':	// auto justify (until ESC X...)
			_just = true;
			break;
		case '3':	// enter graphics spacing mode
			_grph = true;
			break;
		case '4':	// exit graphics
			_grph = false;
			break;
		case 'A':	// red text
			_attr |= Printer_Paper.A_RED;
			_adjacent = false;
			break;
		case 'B':	// black (normal) text
			_attr &= ~Printer_Paper.A_RED;
			_adjacent = false;
			break;
		case 'P':	// enable prop print
			break;
		case 'Q':	// disable prop print
			break;
		case 'U':	// shift +(_vsi/2) - subscript
			fwdHalf();
			_adjacent = false;
			break;
		case 'D':	// shift -(_vsi/2) - superscript
			revHalf();
			_adjacent = false;
			break;
		case 'T':	// make _y be top margin...
			break;
		case 'L':	// make _y be bottom margin...
			break;
		case 'C':	// clear top/bottom margins
			break;
		case '9':	// make _x be left margin
			break;
		case '0':	// make _x be right margin
			break;
		case '1':	// make _x be tab stop
			break;
		case '8':	// erase _x as tab stop
			break;
		case '-':	// make _x as vert-tab stop
			break;
		case '2':	// clear all vert/horiz tab stops
			break;
		case '?':	// enable auto-CR
			break;
		case '!':	// disable auto-CR
			break;
		// Juki printer extensions
		case 'H':
			ret = _spcl[ESC_H];
			break;
		case 'I':
			ret = _spcl[ESC_I];
			break;
		case 'J':
			ret = _spcl[ESC_J];
			break;
		case 'K':
			ret = _spcl[ESC_K];
			break;
		case 'Y':
			ret = _spcl[ESC_Y];
			break;
		case 'Z':
			ret = _spcl[ESC_Z];
			break;
		default:
			//System.err.format("Unknown ESC %02x\n", b);
			break;
		}
		return ret;
	}

	private void prtChar(String s) {
		if (_adjacent) {
			if (_dir) {
				super.appendLastPlot(s, _x, _y, _attr);
			} else {
				super.prependLastPlot(s, _x, _y, _attr);
			}
		} else {
			super.addPlot(s, _x, _y, _attr);
		}
		advance();
		_adjacent = closeEnough(_hsi + _off, _fw);
	}

	private void doBlank() {
		if (_cntr) { // overrides everything
			_cline += ' ';
		} else if (_grph) {
			_adjacent = false;
			forward();
		} else {
			//prtChar(" "); // not for reverse printing?
			_adjacent = false;
			forward();
		}
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
		} else if (b <= ' ') { // control characters... incl BLANK
			s = null;	// not strictly printable...
			switch(b) {
			case '\r':
				// TODO: finish any attrs
				// TODO: perform underscore
				// TODO: perform centering
				_grph = false;
				_off = 0.0f;
				_attr &= ~A_CR_CLEAR;
				_adjacent = false;
				_dir = true;
				// Also resets Print Suppression, Graphics Mode,
				// Offset, Bold, Shadow, Auto Center.
				if (_cntr) {
					doCenter();
				}
				_x = 0;
				break;
			case '\n':
				// TODO: finish any attrs
				// TODO: perform underscore
				// TODO: perform centering
				_attr &= ~A_LF_CLEAR;
				_adjacent = false;
				if (_cntr) {
					doCenter();
				}
				index();
				break;
			case '\b':
				_adjacent = false;
				backward();
				break;
			case ' ':
				doBlank();
				break;
			case '\t':
				_adjacent = false;
				tab();
				break;
			case 12:	// FF
				// TODO: finish any attrs
				// TODO: perform centering
				_adjacent = false;
				if (_cntr) {
					doCenter();
				}
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
			// TODO: handle _attr changes without !_adjacent?
			if (_cntr) { // overrides everything
				_cline += s;
			} else  {
				prtChar(s);
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
				_pset.setScale(_scale);
				_pset.setLeft(_lm);
				_pset.setTop(_tm);
				boolean chg = _pset.doDialog(_cons.getFrame());
				if (chg) {
					_ms = _pset.getMedia();
					_or = _pset.getOrient();
					_scale = _pset.getScale();
					_lm = _pset.getLeft();
					_tm = _pset.getTop();
					_changed = true;
					_cons.setChanges(true);
					_cons.setPaper(_ms, _or);
					_cons.setScale(_scale);
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
			if (m.getMnemonic() == KeyEvent.VK_T) {
				// Restart Tee file
				// Should never get here if teeName is null,
				// or if tee is open, but...
				if (teeName != null) {
					try {
						if (tee != null) tee.close();
						tee = new FileOutputStream(teeName);
					} catch (Exception ee) { tee = null; }
				}
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
		if (tee != null) try {
			tee.write(b);
		} catch (Exception ee) {}
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
			status = p2s.getStatus();
			endJob(status);	// closes _fos
		} while (status >= 0);
	}
}
