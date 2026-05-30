// Copyright (c) 2016 Douglas Miller

import java.util.Arrays;
import java.util.Vector;
import java.util.Properties;
import java.awt.*;
import java.io.*;
import java.net.*;

public class Diablo630telnet {
	private static Diablo630 front_end;
	private static Socket sok;

	public static void main(String[] args) {
		new Diablo630telnet()._main(args);
	}
	public void _main(String[] args) {
		Properties props = new Properties();
		String conf = System.getProperty("user.home") + "/.diablo630rc";
		String host = null;
		String file = "out.ps";
		int port = -1;
		InputStream fin = null;
		// TODO: allow for redirect of output to log file.
		for (String arg : args) {
			if (arg.startsWith("conf=")) {
				conf = arg.substring(5);
			}
		}
		try {
			InputStream is = new FileInputStream(conf);
			props.load(is);
		} catch (Exception ee) {
			//ee.printStackTrace();
			System.err.format("No config file \"%s\"\n", conf);
		}
		String s = props.getProperty("diablo630_host");
		if (s != null) {
			host = s;
		}
		s = props.getProperty("diablo630_port");
		if (s != null) {
			port = Integer.decode(s);
		}
		s = props.getProperty("diablo630_file");
		if (s != null) {
			file = s;
		}
		for (String arg : args) {
			if (arg.startsWith("host=")) {
				host = arg.substring(5);
			} else if (arg.startsWith("port=")) {
				port = Integer.decode(arg.substring(5));
			} else if (arg.startsWith("file=")) {
				file = arg.substring(5);
			}
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
		Vector<String> argv = new Vector<String>(Arrays.asList(args));
		front_end = new Diablo630(props, argv, fin);
		front_end.runPrinter(file);
		// If this returns, we are really done...
		System.exit(0);
	}
}
