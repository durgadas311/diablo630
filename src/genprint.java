// Copyright (c) 2016 Douglas Miller

import java.awt.*;
import java.awt.font.*;

class IntelHex {
	private int addr;
	private int csum;
	private int num;
	private byte[] line;

	public IntelHex() {
		line = new byte[16];
		addr = -1;
		csum = 0;
		num = 0;
	}

	private void flushLine() {
		System.out.format(":%02x%02x%02x00", num, (addr >> 8), (addr & 0x0ff));
		csum = (num) + (addr >> 8) + (addr & 0x0ff);
		for (int x = 0; x < num; ++x) {
			csum += line[x];
			System.out.format("%02x", line[x]);
		}
		System.out.format("%02x\n", (byte)((-csum) & 0x0ff));
		addr += num;
		num = 0;
	}

	private void endLine() {
		if (num == 0) {
			return;
		}
		flushLine();
	}

	public void endFile() {
		if (num > 0) {
			flushLine();
		}
		addr = 0;
		flushLine();
	}

	public void put(int adr, byte b) {
		if (adr != addr + num) {
			// start new line
			endLine();
			addr = adr;
		}
		line[num++] = b;
		if (num >= 16) {
			endLine();
		}
	}
}

public class genprint {
	public static void main(String[] args) {
		int size = 12;
		int vsu = 48;
		int hsu = 120;
		IntelHex toHex = null;
		int x = 0;
		try {
			for (x = 0; x < args.length && args[x].startsWith("-"); ++x) {
				if (args[x].equals("-p")) {
					++x;
					size = Integer.parseInt(args[x]);
				} else if (args[x].equals("-h")) {
					++x;
					hsu = Integer.parseInt(args[x]);
				} else if (args[x].equals("-v")) {
					++x;
					vsu = Integer.parseInt(args[x]);
				} else if (args[x].equals("-x")) {
					toHex = new IntelHex();
				} else {
					System.err.println("Unknown option: " + args[x]);
					System.exit(1);
				}
			}
		} catch (Exception ee) {
			System.err.println("Inalid number: " + args[x]);
			System.exit(1);
		}
		if (x >= args.length) {
			System.err.println("Usage: genprint [-p ptSize] [-h hsu] [-v vsu] [-x] <font>");
			System.exit(1);
		}

		Font font = new Font(args[x], Font.PLAIN, size);
		if (!args[x].equals(font.getFontName())) {
			System.err.println("No font: " + args[x] + " (found: " + font.getFontName() + ")");
			System.err.format("Continue?");
			int resp = 'n';
			try {
				resp = System.in.read();
			} catch (Exception ee) {}
			if (resp != 'y') {
				System.exit(1);
			}
		}
		FontRenderContext frc = new FontRenderContext(null, true, true);
		LineMetrics lm = font.getLineMetrics("Tg", frc);
//System.err.format("emm width = %f\n", font.getStringBounds("m", frc).getWidth());

		if (toHex == null) {
			System.out.println("\torg\t0500h");
			System.out.format("; Font \"%s\", height: %d (1/%din)\n",
				font.getFontName(),
				(int)Math.round((lm.getHeight() * vsu) / 72.0),
				vsu);
			System.out.format("; character width table (1/%din)\n", hsu);
			System.out.println("\tdb\t 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0");
			System.out.println("\tdb\t 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0");
		} else {
			for (x = 0; x < 32; ++x) {
				toHex.put(0x500 + x, (byte)0);
			}
		}
		for (x = 32; x < 128; ++x) {
			String s = new String();
			s += (char)x;
			float w = (float)font.getStringBounds(s, frc).getWidth();
			int iw = (int)Math.round((w * hsu) / 72.0);
			if (toHex == null) {
				if ((x % 16) == 0) {
					System.out.format("\tdb\t%2d", iw);
				} else {
					System.out.format(",%2d", iw);
				}
				if ((x % 16) == 15) {
					System.out.format("\n");
				}
			} else {
				toHex.put(0x500 + x, (byte)iw);
			}
		}

		if (toHex == null) {
			System.out.println("; character mapping table (standard ASCII)");
			System.out.println("\tdb\t0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0");
			System.out.println("\tdb\t0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0");
		} else {
			for (x = 0; x < 32; ++x) {
				toHex.put(0x580 + x, (byte)0);
			}
		}
		for (x = 32; x < 128; ++x) {
			if (toHex == null) {
				if ((x % 16) == 0) {
					System.out.format("\tdb\t'");
				}
				if (x == 0x27) {
					System.out.format("%c%c", x, x);
				} else if (x == 127) {
					System.out.format("',07fh\n");
				} else {
					System.out.format("%c", x);
					if ((x % 16) == 15) {
						System.out.format("'\n");
					}
				}
			} else {
				toHex.put(0x580 + x, (byte)x);
			}
		}
		if (toHex == null) {
			System.out.println("\tend");
		} else {
			toHex.endFile();
		}
		System.exit(0);
	}
}
