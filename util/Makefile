
all: setprint.com


%.cim: %.asm
	zmac --dri -i -8 -c -s -n -P --od . --oo lst,cim $?

%.com: %.cim
	dd if=$? of=$@ bs=128 conv=sync
