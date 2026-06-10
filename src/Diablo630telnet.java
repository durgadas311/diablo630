// Copyright (c) 2016 Douglas Miller

import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.Properties;
import java.awt.*;
import java.io.*;
import java.net.*;

public class Diablo630telnet {
	private static Diablo630 front_end;
	private static Socket sok;

	List<String> boolArgs = Arrays.asList();
	String[] seqArgs = new String[]{ "host", "port" };

	public static void main(String[] args) {
		new Diablo630telnet()._main(args);
	}
	public void _main(String[] args) {
		Properties props = new Properties();
		String host = null;
		int port = -1;
		InputStream fin = null;
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
		String s = props.getProperty("diablo630_host");
		if (s != null) {
			host = s;
		}
		s = props.getProperty("diablo630_port");
		if (s != null) {
			port = Integer.decode(s);
		}
		String file = props.getProperty("diablo630_file");
		if (file == null) {
			file = "out.ps";
		}
		if (host == null) {
			System.err.format("Usage: Diablo630telnet [conf=file] host=addr [port=num]\n");
			System.exit(1);
		}
		if (port <= 0) {
			port = 23;	// standard telnet port
		}
		try {
			InetAddress ia = InetAddress.getByName(host);
			sok = new Socket(ia, port);
			fin = sok.getInputStream();
		} catch (Exception ee) {
			ee.printStackTrace();
			System.exit(1);
		}
		front_end = new Diablo630(props, fin);
		front_end.runPrinter(file);
		// If this returns, we are really done...
		System.exit(0);
	}
}
