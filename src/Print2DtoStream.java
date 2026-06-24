// Copyright (c) 2016 Douglas Miller <durgadas311@gmail.com>

import java.io.*;
import java.awt.*;
import java.awt.print.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;

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

	public Print2DtoStream(OutputStream fos, Diablo630 prtr) {
		_prtr = prtr;
		lastPage = -1;
		PrinterJob pj = PrinterJob.getPrinterJob();
		StreamPrintServiceFactory[] spsf =
			PrinterJob.lookupStreamPrintServices("application/postscript");
		if (spsf.length == 0) {
			status = -1;
			return;
		}
		StreamPrintService ps = spsf[0].getPrintService(fos);
		HashPrintRequestAttributeSet attr = new HashPrintRequestAttributeSet();
		attr.add(new Destination(new File("not.used").toURI()));
		PageFormat pf = prtr.getPageFormat();
		try {
			pj.setPrintService(ps);
			pj.setPrintable(this, pf);
			pj.defaultPage(pf);
			pj.print(attr);
		} catch (Exception ee) {
			ee.printStackTrace();
			status = -1;
		}
	}

	int lastPage;
	int status;
	int did;

	public int getStatus() { return status; }
	public int getCount() { return did; }
	
	public int print(Graphics g, PageFormat pf, int pageIndex) {
		if (status == 0) {
			_prtr.init(g);
			++status;
		}
		if (_prtr.fontTest()) {
			lastPage = pageIndex;
			if (pageIndex == 0) {
				_prtr.print(g, pf, pageIndex, null);
				did = 1;
				return Printable.PAGE_EXISTS;
			} else {
				did = 0;
				status = -1;
				return Printable.NO_SUCH_PAGE;
			}
		}
		if (lastPage != pageIndex) {
			lastPage = pageIndex;
			did = 0;
			while (status <= 10) {
				int b;
				b = _prtr.readPrinterStream();
				if (b < 0) {
					status = -1; // EOF - no more, ever.
					break;
				}
				if (b == 255) {
					if (_prtr.blankPage()) did = 0;
					status = 255; // End Job, start another.
					break;
				}
				if (b == 254) {
					did = 0;
					status = 254; // Cancel Job, start another.
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
