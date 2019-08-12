# falstad format

This is a reverse engineering of the falstad format found at http://www.falstad.com/circuit/circuitjs.html

The source code is here: https://github.com/pfalstad/circuitjs1/

## Example

In general, components are first, then scopes, then graphs.

```
$ 1 0.000005 10.20027730826997 50 5 43
r 176 80 384 80 0 10
w 176 80 176 352 0
c 384 352 176 352 0 0.000015 -8.629089383188898
l 384 80 384 352 0 1 0.005228236893882415
o 3 64 0 4099 20 0.05 0 2 3 3
o 2 64 0 4099 20 0.05 1 2 2 3
o 0 64 0 4099 0.625 0.05 2 2 0 3
38 2 0 0.000001 0.000101 Capacitance
38 3 0 0.01 1.01 Inductance
38 0 0 1 101 Resistance
```

In the example above, there are some components. There are:

* resistor - 10 ohms
* wire (no ohms) - this will act like a node in our simulation
* capacitor - 0.000015 farads
* inductor - 1.0 Henries

## Format

### Initial Variables

First, there's the line with the `$`. It appears to have some various properties
* f:
    * int f = (dotsCheckItem.getState()) ? 1 : 0;
     	f |= (smallGridCheckItem.getState()) ? 2 : 0;
     	f |= (voltsCheckItem.getState()) ? 0 : 4;
     	f |= (powerCheckItem.getState()) ? 8 : 0;
     	f |= (showValuesCheckItem.getState()) ? 0 : 16;
* timeStep
* number of iterations
* current speed (visualization)
* This is hardcoded to 5...
* power brightness (visualization)

### Component Format

All of the components start with (x1, y1, x2, y2) to define pins, and a single variable for the flags.
Components by default have no flags set, and don't inherit any from the base class.

Other data can vary after that.

* r - Resistor(x1, y1, x2, y2, flags, **ohms**)
* w - Wire (x1, y1, x2, y2, flags)
* c - Capacitor (x1, y1, x2, y2, **flags**, **farads**, voltddiff)
    * if flags has second bit set, then it will use a trapezoidal approximation, otherwise a backwards euler
* l - Inductor (x1, y1, x2, y2, **flags**, **henries**, current)
    * if flags has second bit set, then it will use a trapezoidal approximation, otherwise a backwards euler
* s - Switch (x1, y1, x2, y2, flags, **position** (0 = closed), **momentary** (1 = yes))
* g - Ground Pin (x1, y1, x2, y2, flags)
* R - Voltage Rail (x1, y1, x2, y2, **flags** \[**waveform**, **frequency**, **maxVoltage**, **bias**, **phaseShift**, **dutyCycle**])
    * 1st flag bit sets this to be a "CLK" (visual only?)
    * 2nd flag bit **sets** the phase shift to pi/2
    * always set 3rd flag bit (indicates a fixed pulse duty cycle) (so add 0x4)
    * Waveform Types:
      * DC = 0
      * AC = 1
      * SQUARE = 2
      * TRIANGLE = 3
      * SAWTOOTH = 4
      * PULSE = 5
      * NOISE = 6
      * VAR = 7 ?
* v - See Voltage Rail
    * I have reason to believe that pin2 is the power, and pin1 is ground

### Scope Format

The scopes (the graphs at the bottom) start with `o`.

### Adjustable Format

The adjustable (sliders on the right side of the UI) start with `38`.

`38 0 0 1 101 Resistance`

* 0 - circuit element number
* 0 - editItem - some kind of key
* 1 - minimum value
* 101 - maximum value
* caption of slider

### Hints

The hints, which start with `h`, sometimes appear at the bottom of the output.
