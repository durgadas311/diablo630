// Copyright (c) 2016 Douglas Miller

import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.Properties;
import java.awt.*;
import java.io.*;

public class Diablo630stdin {
	private static Diablo630 front_end;

	List<String> boolArgs = Arrays.asList();
	String[] seqArgs = new String[0];

	public static void main(String[] args) {
		new Diablo630stdin()._main(args);
	}
	public void _main(String[] args) {
		// TODO: make font (cpi/lpi) configurable
		Properties props = new Properties();
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

		front_end = new Diablo630(props, System.in);
		String file = props.getProperty("diablo630_file");
		if (file == null) {
			file = "out.ps";
		}
		front_end.runPrinter(file);
		// If this returns, we are really done...
		System.exit(0);
	}
}
