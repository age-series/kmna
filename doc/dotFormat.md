# dot format

We're using dot files as one of the import functions for a circuit schematic.

There are two parts: components and nodes.

Components are represented by lines, nodes are represented by the actual nodes in the graph.

An example set of nodes may look like this:

```
0 [label=VoltageNode, voltage=1.32]
1 [label=VoltageNode, voltage=4.53]
null [label=null]
```

.. and an example set of components may look like this:

```
0 -- 1 [label=Resistor, ohms=100]
```

## Example

This graph here:

```
graph subsystem0 {
    null [label=null]
    0 [label=Node]
    1 [label=Node]
    2 [label=Node]
    0 -- null [label=VoltageSource volts=10]
    0 -- 1 [label=Resistor ohms=10]
    1 -- 2 [label=Resistor ohms=10]
    2 -- null [label=VoltageSource volts=0]
}
```

...represents a circuit like this:

![resistor-circuit](img/resistor-circuit.png)