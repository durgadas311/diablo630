; remote setup for a Virtual Diablo 630
; SETPRINT <prop-file>	; send properties to printer
; SETPRINT D		; restore defaults in printer

cpm	equ	0
bdos	equ	5
deffcb	equ	5ch
defbuf	equ	80h

CR	equ	13
LF	equ	10
ESC	equ	27
EOF	equ	26

lstoutf	equ	5
printf	equ	9
openf	equ	15
closef	equ	16
readf	equ	20
setdmaf	equ	26

	org	100h
	jmp	start

esc$f:	db	ESC,'F',EOF
esc$g:	db	ESC,'G',EOF
esc$v:	db	ESC,'V',EOF

n$file:	db	'No file$'
help:	db	'Usage: SETPRINT <property-file>',CR,LF
	db	'       SETPRINT D',CR,LF,'$'

start:	lda	deffcb+1
	cpi	' '
	jz	usage
	cpi	'D'
	jnz	dofile
	lda	deffcb+2
	cpi	' '
	jnz	dofile
	lda	deffcb
	ora	a
	jnz	dofile
	; revert back to defaults
	lxi	d,esc$v
	call	lstout
	jmp	cpm

dofile:	lxi	d,deffcb
	mvi	c,openf
	call	bdos
	inr	a
	jz	nofile

	lxi	d,lstbuf
	mvi	c,setdmaf
	call	bdos

	lxi	d,esc$f
	call	lstout

loop:	lxi	d,deffcb
	mvi	c,readf
	call	bdos
	ora	a
	jnz	done
	;
	lxi	d,lstbuf
	call	lstout
	jmp	loop

done:	lxi	d,esc$g
	call	lstout
	lxi	d,deffcb
	mvi	c,closef
	call	bdos
	jmp	cpm

nofile:	lxi	d,n$file
	mvi	c,printf
	call	bdos
	jmp	cpm

usage:	lxi	d,help
	mvi	c,printf
	call	bdos
	jmp	cpm

lstout:	ldax	d
	cpi	EOF
	rz
	push	d
	mov	e,a
	mvi	c,lstoutf
	call	bdos
	pop	d
	inx	d
	jmp	lstout

lstbuf:	ds	128
	db	EOF

	end
