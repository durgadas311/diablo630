; send End Job to printer

bdos	equ	5

ENDJOB	equ	255

lstoutf	equ	5

	org	100h
	mvi	e,ENDJOB
	mvi	c,lstoutf
	jmp	bdos

	end
