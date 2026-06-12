# Virtual Diablo 630 Printer

This project uses submodules. After clone, run "git submodule init".
Refresh with "git submodule update".

The program accepts Diable 630 (Juki 6100) input and produces PostScript
files. By default provides a GUI to allow basic printer control.
Printing is job oriented, sending a 0FFH character to the printer
indicates the end of a job (and the beginning of a new one). This is the
same character used by CP/NET and MP/M (and ENDLIST.COM) to end a print
job (and release the printer).

![screen-shot](imgs/diablo630.png)

The plain version "Diablo630,jar" will accept printer output on stdin.
You can type output into the progrm, or redirect from a file that contains
Diablo 630 compatible characters and ESC sequences.

ESC command compatibility is [listed here](COMPAT.md).

An [example of output](examples/ws-print.pdf)
from WordStar printing PRINT.TST from distribution disk.
Note: first and last pages are intentionally blank, due to
WordStar using extra Form Feed characters.

A short screencast of the app printing:
[Screencast from 2026-06-12 15-37-12.webm](https://github.com/user-attachments/assets/08b4518e-0995-4835-ab8c-9c58071447a7)
