// Copyright (c) 2016 Douglas Miller

import java.awt.GraphicsEnvironment;

public class lsfonts {
	public static void main(String[] args) {
		String fonts[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		for (int i = 0; i < fonts.length; i++) {
			System.out.println(fonts[i]);
		}
	}
}
