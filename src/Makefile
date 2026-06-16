JAVAC_REL =

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
		PageProgress.class \
		PaperPaintable.class \
		Cleanup.class

D630_CLASSES =	\
		Diablo630stdin.class \
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

JSERIAL = jserialcomm/jSerialComm-2.9.3.jar

%.class: %.java
	javac $(JAVAC_REL) $?

Diablo630tty.class: Diablo630tty.java $(JSERIAL)
	javac $(JAVAC_REL) -cp .:$(JSERIAL) $<

jar: Diablo630.jar Diablo630telnet.jar Diablo630tty.jar Diablo630Embedded.jar

java11:
	mkdir -p temp
	cp -r $$(git ls-files) temp
	make -C temp jar JAVAC_REL="--release 11"
	cp temp/*.jar ../java11
	rm -rf temp

Diablo630.jar: $(D630_CLASSES) $(CORE_CLASSES) $(SUPP_CLASSES)
	echo "Main-Class: Diablo630stdin" >Manifest.txt
	jar -cfm $@ Manifest.txt $(foreach x,$^,'$(x)')

Diablo630telnet.jar: Diablo630telnet.class $(CORE_CLASSES) $(SUPP_CLASSES)
	echo "Main-Class: Diablo630telnet" >Manifest.txt
	jar -cfm $@ Manifest.txt $(foreach x,$^,'$(x)')

Diablo630tty.jar: Diablo630tty.class $(CORE_CLASSES) $(SUPP_CLASSES) $(JSERIAL)
	echo "Main-Class: Diablo630tty" >Manifest.txt
	jar cfm $@ Manifest.txt Diablo630tty.class \
		$(foreach x,$(CORE_CLASSES) $(SUPP_CLASSES),'$(x)')

Diablo630Embedded.jar: $(CORE_CLASSES) $(SUPP_CLASSES)
	jar -cMf $@ $(foreach x,$^,'$(x)')

ship: jar
	cp *.jar ../bin

clobber:
	rm -f *.jar *.class

__FRC__:
