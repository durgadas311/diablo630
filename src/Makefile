all:

CORE_CLASSES =	\
		Diablo630.class \
		Printer_Paper.class \
		SuffFileChooser.class \
		SuffFileFilter.class \
		Print2DtoStream.class \
		PaperDialog.class \
		FontDialog.class \
		PrinterConsole.class \
		PaperPaintable.class

D630_CLASSES =	\
		printer_fe.class \
		lsfonts.class \
		genprint.class \
		IntelHex.class

SUPP_CLASSES = \
		Diablo630$$Actions.class \
		Diablo630$$DocPaperSize.class \
		Diablo630$$ReqPaperSize.class \
		PaperDialog$$F11x14.class \
		PaperDialog$$M11x14.class \
		Printer_Paper$$plot.class \
		SuffFileChooser$$TapeProt.class

%.class: %.java
	javac $?

jar: Diablo630.jar Diablo630telnet.jar Diablo630Embedded.jar

Diablo630.jar: $(D630_CLASSES) $(CORE_CLASSES) $(SUPP_CLASSES)
	echo "Main-Class: printer_fe" >Manifest.txt
	jar -cfm $@ Manifest.txt $(foreach x,$^,'$(x)')

Diablo630telnet.jar: Diablo630telnet.class $(CORE_CLASSES) $(SUPP_CLASSES)
	echo "Main-Class: Diablo630telnet" >Manifest.txt
	jar -cfm $@ Manifest.txt $(foreach x,$^,'$(x)')


Diablo630Embedded.jar: $(CORE_CLASSES) $(SUPP_CLASSES)
	jar -cMf $@ $(foreach x,$^,'$(x)')

clobber:
	rm -f *.jar *.class

__FRC__:
