# Virtual Diablo 630 Printer

This project uses submodules. After clone, run "git submodule init".
Refresh with "git submodule update --remote".

The program accepts Diablo 630 (Juki 6100) input and produces PostScript
files. By default provides a GUI to allow basic printer control.
Printing is job oriented, sending a 0FFH character to the printer
indicates the end of a job (and the beginning of a new one). This is the
same character used by CP/NET and MP/M (and ENDLIST.COM) to end a print
job (and release the printer).

![screen-shot](imgs/diablo630.png)

https://github.com/user-attachments/assets/fc2e1afa-b3a6-42ef-907d-0f540f47e6ed

The plain version "Diablo630,jar" will accept printer output on stdin.
You can type output into the program, or redirect from a file that contains
Diablo 630 compatible characters and ESC sequences. Note, a CR (Ctrl-M) is required
on systems that don't produce that when pressing Enter. That CR may need to be "escaped",
for example on Linux shells you must press Ctrl-V Ctrl-M to insert a CR.
If a file is being redirected to the program, it must contain CRs as appropriate.

ESC command compatibility is [listed here](COMPAT.md).

Subdirectories of interest:
- **java11**  - Contains versions of the JARs compiled for older
Java, for use on platforms with older JVMs/JREs such as Raspberry PI.
- **util** - Contains some CP/M programs for handling the
printer, including SETPRINT.COM to upload a new configuration.
- **cfg** - Contains some sample config/properties files
for various practical scenarios.
- **scripts** - Contains some useful shell scripts for
Linux systems, or as examples for other systems.

An [example of output](examples/ws-print.pdf)
from WordStar printing PRINT.TST from distribution disk.
Note: first and last pages are intentionally blank, due to
WordStar using extra Form Feed characters.

An [example of remote config setting](EXAMPLE1.md), using
new ESC sequences (SETPRINT.COM) to upload config properties form the legacy computer.
