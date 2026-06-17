; send Job Cancel to printer

bdos	equ	5

CANJOB	equ	254

lstoutf	equ	5

	org	100h
	mvi	e,CANJOB
	mvi	c,lstoutf
	jmp	bdos

	end
