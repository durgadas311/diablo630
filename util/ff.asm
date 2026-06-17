; send FF (form-feed) to printer

bdos	equ	5

FF	equ	12

lstoutf	equ	5

	org	100h
	mvi	e,FF
	mvi	c,lstoutf
	jmp	bdos

	end
