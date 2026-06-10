// Copyright (c) 2016 Douglas Miller

import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.Properties;
import java.awt.*;
import java.io.*;
// See: https://fazecast.github.io/jSerialComm/
// Currently using: jSerialComm-2.6.2.jar
import com.fazecast.jSerialComm.*;
// Run with:
//	java -cp <this-jar>:<jSerialComm-jar> Diablo630tty [args...]

public class Diablo630tty {
	private static Diablo630 front_end;

	List<String> boolArgs = Arrays.asList();
	String[] seqArgs = new String[]{ "tty", "baud" };

	public static void main(String[] args) {
		new Diablo630tty()._main(args);
	}
	public void _main(String[] args) {
		Properties props = new Properties();
		String tty = null;
		int baud = -1;
		InputStream fin = null;
		SerialPort comm;

		// TODO: allow for redirect of output to log file.
		String rc = Diablo630.getConfig(args);
		File conf = new File(rc);
		try {
			InputStream is = new FileInputStream(conf);
			props.load(is);
		} catch (Exception ee) {
			//ee.printStackTrace();
			System.err.format("No config file \"%s\"\n", conf);
		}
		Diablo630.processArgs(props, args, boolArgs, seqArgs);
		// everything recognized is in properties now.
		String s = props.getProperty("diablo630_tty");
		if (s != null) {
			tty = s;
		}
		s = props.getProperty("diablo630_baud");
		if (s != null) {
			baud = Integer.decode(s);
		} else {
			baud = 9600;	// some default
		}
		String file = props.getProperty("diablo630_file");
		if (s == null) {
			file = "out.ps";
		}
		if (tty == null) {
			System.err.format("Usage: Diablo630tty [conf=file] tty=dev baud=num\n");
			System.exit(1);
		}
		comm = getPort(tty, baud);
		if (comm == null) {
			System.err.format("Bad tty: %s (%d)\n", tty, baud);
			System.exit(1);
		}
		try {
			fin = comm.getInputStream();
		} catch (Exception ee) {
			ee.printStackTrace();
			System.exit(1);
		}
		comm.setDTR();  
		comm.setRTS();
		front_end = new Diablo630(props, fin);
		front_end.runPrinter(file);
		// If this returns, we are really done...
		System.exit(0);
	}

	private static SerialPort getPort(String tty, int baud) {
		try {
			SerialPort serialPort = SerialPort.getCommPort(tty);
			if (serialPort == null) {
				return null;
			}
			// TODO: timeout values...
			if (!serialPort.openPort()) {
				return null;
			}
			if (baud > 0) {
				if (!serialPort.setComPortParameters(baud, 8,
							SerialPort.ONE_STOP_BIT,
							SerialPort.NO_PARITY)) {
					serialPort.closePort();
					return null;
				}
			}
			if (!serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING,
					0, 0)) {
				serialPort.closePort();
				return null;
			}
			return serialPort;
		} catch (Exception ee) {
			ee.printStackTrace();
			return null;
		}
	}
}
