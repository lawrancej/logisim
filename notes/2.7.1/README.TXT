

Feature: Added Select Location attribute to multiplexer, demultiplexer, (decoder still to do)

Bug fix: In customized circuit appearances, labels would be given a color based on the fill color for rectangles/ovals/polygons rather than the color selected for text. This color was by default white, and so they appeared invisible.

Behavior change: With transistors and transmission gates, a floating value is passed through as floating regardless of the value of gate (including a floating or error gate value).

Behavior change: When clicking the currently viewed circuit, the circuit attributes are shown but no tool to add that circuit to itself is selected. This also happens with triple-clicking a circuit: The view is switched to the circuit, but the tool is not selected for adding instances to itself.


TODO:

Select Location for decoder.

Issue with Minimize/Close menu items being disabled when window closed.

In editing circuit appearance, changing between Border, Fill, Border & Fill doesn't seem to update attribute list properly.

Change application description in Windows executable.

Add "Show Grid" into preferences explicitly.