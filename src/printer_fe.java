// Copyright (c) 2016 Douglas Miller

import java.util.Arrays;
import java.util.Vector;
import java.util.Properties;
import java.awt.*;

public class printer_fe {
	private static Diablo630 front_end;

	public static void main(String[] args) {
		new printer_fe()._main(args);
	}
	public void _main(String[] args) {
		// TODO: make font (cpi/lpi) configurable
		Vector<String> argv = new Vector<String>(Arrays.asList(args));
		// These are the defaults already...
		//argv.add("cpi=10");
		//argv.add("lpi=6");
		//argv.add("font=Monospaced,PLAIN,12");
		front_end = new Diablo630(new Properties(), argv, System.in);
		String file = "out.ps";
		for (String arg : args) {
			if (arg.indexOf("=") > 0) continue;
			if (arg.equals("nogui")) continue;
			file = arg;
			break;
		}
		front_end.runPrinter(file);
		// If this returns, we are really done...
		System.exit(0);
	}
}
