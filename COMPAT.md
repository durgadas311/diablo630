# Compatibility

ESC sequences

Sequence | Meaning | Supported
---------|---------|----------
ESC BS | Micro BS | Yes
ESC TAB n | Absolute tab to col | Yes
ESC LF | Reverse LF | Yes
ESC VT n | Absolute Vert-tab to line | Yes
ESC FF n | Set lines/page | Yes
ESC CR P | Reset | Yes
ESC DC1 n | Character offset | Yes
ESC SUB I | Reset | Yes
ESC RS n | Set VSI | Yes
ESC US n | Set HSI | Yes
ESC 0 | Set Right Margin | Not Yet *
ESC 1 | Set Tab Stop | Not Yet *
ESC 2 | Clear All V/H Tab Stops | Not Yet *
ESC 3 | Graphics spacing | Yes
ESC 4 | Normal spacing | Yes
ESC 5 | Forward Print | Yes
ESC 6 | Backward Print | Yes
ESC 7 | Enable print suppression | Not Yet
ESC 8 | Erase Tab Stop | Not Yet *
ESC 9 | Set Left Margin | Not Yet *
ESC A | Alt Color (Red) | Yes
ESC B | Norm Color (Black) | Yes
ESC C | Clear top/bottom margins | Not Yet *
ESC D | Superscript shift | Yes
ESC E | Begin underscore | Yes
ESC H | (Juki) &#167; | Yes
ESC I | (Juki) &#163; | Yes
ESC J | (Juki) &#168; | Yes
ESC K | (Juki) &#231; | Yes
ESC L | Set Bottom Margin | Not Yet *
ESC M | Auto Justify | Not Yet
ESC N | Clear carriage settle | N/A
ESC O | Begin bold/overstrike | Yes
ESC P | Enable Prop spacing | Not Yet
ESC Q | Disable Prop spacing | Not Yet
ESC R | End Underscore | Yes
ESC S | Reset CSI to dipswitch | Yes
ESC T | Set Top Margin | Not Yet *
ESC U | Subscript shift | Yes
ESC W | Begin shadow | Yes
ESC X | Cancel modes | Yes, mostly
ESC Y | (Juki) &#162; | Yes
ESC Z | (Juki) &#172; | Yes
ESC ? | Enable Auto-CR | Yes
ESC ! | Disable Auto-CR | Yes
ESC - | Set Vert-Tab Stop | Not Yet *
ESC = | Auto Center | Yes (limited)
ESC & | End bold/shadow | Yes
ESC % | Carriage settle time | N/A
ESC < | Enable inverted printing | Not Yet
ESC > | Disable inverted printing | Not Yet
ESC \ | Disable auto-rev-print | N/A
ESC / | Restore auto-rev-print | N/A

(*) Features most/only useful in typewriter case, which is not supported.

- HyPlot is not supported.
- Remote Diagnostics are not supported.
- Feeder Controls are not supported.
- Program mode is not supported.
