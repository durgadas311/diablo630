// Copyright (c) 2016 Douglas Miller

import java.io.*;
import java.awt.*;
import java.awt.print.*;
import javax.print.*;


/*
 * Copyright (c) 2001, Oracle and/or its affiliates. All rights reserved.
 *
 * This software is the proprietary information of Oracle.
 * Use is subject to license terms.
 *
 */


/*
 * Use the Java(TM) Print Service API to locate a service which can export
 * 2D graphics to a stream as Postscript. This may be spooled to a
 * Postscript printer, or used in a postscript viewer.
 */
class Print2DtoStream implements Printable {
	Diablo630 _prtr;

	public Print2DtoStream(FileOutputStream fos, Diablo630 prtr) {
		_prtr = prtr;
		lastPage = -1;
		/* Use the pre-defined flavor for a Printable from an InputStream */
		DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
		StreamPrintServiceFactory[] factories;

		factories = StreamPrintServiceFactory.
			lookupStreamPrintServiceFactories(flavor, "application/postscript");
		if (factories.length == 0) {
			// TODO: do something to indicate error...
			return;
		}

		try {
			/* Create a Stream printer for Postscript */
			StreamPrintService sps;
			sps = factories[0].getPrintService(fos);

			/* Create and call a Print Job */
			DocPrintJob pj = sps.createPrintJob();
			Doc doc = new SimpleDoc(this, flavor, _prtr.getDocAttrs());
			status = 0;
			pj.print(doc, _prtr.getPrtAttrs());
		} catch (PrintException pe) { 
			pe.printStackTrace();
			//System.err.println(pe);
		}
	}

	int lastPage;
	int status;

	public int getStatus() { return status; }
	
	public int print(Graphics g, PageFormat pf, int pageIndex) {
		if (status == 0) {
			_prtr.init(g);
			++status;
		}
		if (lastPage != pageIndex) {
			lastPage = pageIndex;
			int did = 0;
			while (status <= 10) {
				int b;
				b = _prtr.readPrinterStream();
				if (b < 0) {
					status = -1; // EOF - no more, ever.
					break;
				}
				if (b == 255) {
					status = 255; // End Job, start another.
					break;
				}
				++did;
				if (_prtr.do_char((byte)b)) {
					// End of Page...
					break;
				}
			}
			if (did > 0) {
				return Printable.PAGE_EXISTS;
			} else {
				return Printable.NO_SUCH_PAGE;
			}
		}
		_prtr.print(g, pf, pageIndex, _prtr.getBkground());
		return Printable.PAGE_EXISTS;
	}
}
