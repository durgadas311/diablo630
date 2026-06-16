# Example 1 - Setting Configuration Remotely

This examples alters the default configuration for printing
on LETTER paper in landscape orientation at a reduced size.
This would allow printing output intended for 14x11 paper on
standard LETTER paper.

This is the configuration being set:

```
A0>type prn.cfg
top = 0.0
left = 0.5
paper = letter landscape
scale = 0.8

A0>
```

The following commands setup the desired configuration (landscape, reduced) and
print an assembler listing to it.

```
A0>setprint prn.cfg

A0>pip lst:=setprint.prn

A0>endlist

A0>
```

See the [results of this print](examples/ex1p1.pdf).

The following commands revert to the default configuration (portrait) and
print an assembler listing to it.

```
A0>setprint d

A0>pip lst:=setprint.prn

A0>endlist

A0>
```

See the [results of this print](examples/ex1p2.pdf).

