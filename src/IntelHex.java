// Copyright (c) 2026 Douglas Miller

import java.io.*;

class IntelHex {
	PrintStream out;
	private int addr;
	private int csum;
	private int num;
	private byte[] line;

	public IntelHex(PrintStream out) {
		this.out = out;
		line = new byte[16];
		addr = -1;
		csum = 0;
		num = 0;
	}

	private void flushLine() {
		out.format(":%02X%02X%02X00", num, (addr >> 8), (addr & 0x0ff));
		csum = (num) + (addr >> 8) + (addr & 0x0ff);
		for (int x = 0; x < num; ++x) {
			csum += line[x];
			out.format("%02X", line[x]);
		}
		out.format("%02X\n", (byte)((-csum) & 0x0ff));
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
