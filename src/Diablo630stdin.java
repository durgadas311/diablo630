// Copyright (c) 2016 Douglas Miller

import java.util.Arrays;
import java.util.Vector;
import java.util.Properties;
import java.awt.*;
import java.io.*;

public class Diablo630stdin {
	private static Diablo630 front_end;

	public static void main(String[] args) {
		new Diablo630stdin()._main(args);
	}
	public void _main(String[] args) {
		// TODO: make font (cpi/lpi) configurable
		Properties props = new Properties();
		String conf = System.getProperty("user.home") + "/.diablo630rc";
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

		Vector<String> argv = new Vector<String>(Arrays.asList(args));
		// These are the defaults already...
		//argv.add("cpi=10");
		//argv.add("lpi=6");
		//argv.add("font=Monospaced,PLAIN,12");
		front_end = new Diablo630(props, argv, System.in);
		String file = "out.ps";
		for (String arg : args) {
			if (arg.startsWith("file=")) {
				file = arg.substring(5);
				break;
			}
		}
		front_end.runPrinter(file);
		// If this returns, we are really done...
		System.exit(0);
	}
}
