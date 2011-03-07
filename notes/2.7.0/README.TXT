LOGISIM 2.7.0 HAS SIMULATION TREE, GREEK, MUCH MORE

Logisim, a graphical design and simulation tool for logic circuits, is now at version 2.7.0. The new version includes numerous enhancements and fixes; the most radical are a new Wiring library, a new method for drawing splitters, and enhancements to the main window's left-side explorer and attribute pane.

This version introduces the "Wiring" library, into which have been moved the circuit components previously found in "Base": splitter, pin, probe, clock, tunnel, pull resistor, and bit extender (and also constant, previously located in "Gates"). Additionally, the "Wiring" library now includes a transistor, transmission gate, power, and ground.

Another major change was in how splitters are drawn. The new technique conforms more closely to traditional circuit diagrams, and it includes labels showing the bit(s) to which each split end corresponds. The new "Appearance" attribute allows one to continue using the older technique, and projects created in older versions of Logisim will continue to load using this legacy support. However, projects can easily switch to the new appearance with labels by changing the splitters' Appearance attribute to "Centered".

The GUI has also received several updates. The explorer pane in the upper left corner has traditionally shown the current project's circuits and libraries; this is now called its "Toolbox" view. However, one can switch the explorer pane to instead show the "Simulation Tree," which includes a toolbar for controlling the simulation and the hierarchy of subcircuits in the current simulation. This provides a visualization of the full hierarchy as well as an alternative to right-clicking subcircuits (or double-clicking them using the Poke Tool) to descend into viewing their states. Another notable enhancement to the GUI is an addition of a title bar to the attribute table, which indicates whose attributes are currently being displayed.

Additional features include a Greek translation of the GUI elements, labels specific to one instance of a subcircuit, an enable input for multiplexers/demultiplexers/decoders, an Output Value attribute for gates allowing them to emit floating values (permitting wired-and and wired-or logic), and support for product-of-sums expressions in the combinational analysis module. This version also marks the removal of the Legacy library and other code meant for supporting projects built in Logisim 1.0. And the "project toolbar" - previously disabled by default - is now always enabled.

Educational institutions around the world use Logisim as an aid to teaching about digital logic and computer architecture. As a Java application, Logisim can run on most major operating systems. Read more about Logisim at http://www.cburch.com/logisim/, and download it from SourceForge.net at http://sourceforge.net/projects/circuit/.


GUI

Feature: Added the option of switching the "explorer pane" between viewing the "toolbox" (the project's circuits and libraries that has appeared in the explorer pane in previous versions) and a simulation hierarchy.

Feature: A Greek translation of the GUI elements was added. (The documentation is not yet translated.)

Feature: The attribute table contains a header line describing which attributes are being edited.

Feature: Added a "Close" item to the Window menu, which closes the current window. The "Close" item in the File menu continues to close the current project; its shortcut is now Control-Shift-W.

Feature: Added 250%, 300%, 400% to the options for zooming.

Feature: File dialogs "remember" the location of the last-accessed file, and the next file dialog starts from there - even when Logisim is started again.

Feature: During a rectangular selection, the rectangle is drawn with a light blue, and components that will be affected are indicated with a slate-blue outline.

Design change: Removed option to disable the project toolbar.

Bug fix: In moving a component whose output is connected to itself, it was possible for an exception to cause the program to hang.

Bug fix: The "Second radix when wire poked" preference was ignored - the "First radix" was displayed twice when the wire was poked.

Bug fix: Enabled the "-clearprops" command-line option for clearing all application preferences. (The previous version worked with "-clearprefs", but it was described as "-clearprops" in the "-help" text and in the documentation. Both now work.)

Bug fix: The Logging module's window title did not change when the file's name was changed.


CORE

Feature: Saving project files is more cautious, creating a backup before starting to write to disk and recovering from the backup if for some reason the save is detected to fail (whether nothing has been written to the file or an error has occurred while saving).

Feature: Individual subcircuit instances can have a label, in addition to the "shared label" that is displayed in the center of each instance of a subcircuit.

Bug fix: When simulation is disabled, selecting Tick Once will still cause a tick once the simulation is stepped.


LIBRARIES

Feature: Added a Wiring library containing transistor, transmission gate, power and ground components. Also, the splitter, pin, probe, tunnel, clock, pull resistor, and bit extender were moved into the Wiring library from the Base library, and constant was moved into the Wiring library from the Gates library.

Feature: In the Wiring library, splitters can be configured to show which bits correspond to each end using the new Appearance attribute. It uses this new appearance by default.

Feature: In the Wiring library, the pop-up menu associated with splitters includes menu items for distributing the bits in ascending or descending order.

Feature: In the Gates library, the gates (except Controlled Buffer and Controlled Inverter) have a new Output Value attribute so that a gate can output a floating value when the result is true or when the result is false. This is useful for building a wired-or and wired-and connections.

Feature: In the Plexers library, an enable input was added to the multiplexer, demultiplexer, and decoder.

Feature: In the Plexers library, the priority encoder received a new Disabled Output attribute.

Feature: In the I/O library, the LED Matrix's Input Format attribute can be changed after the matrix is added into the circuit.

Design change: Deleted the "Legacy" library for loading files saved in versions 1.x of Logisim.

Design change: For the Memory library's Random component, a reset when the seed is 0 computes a new initial seed based on the current time. (It previously reset to the initial seed for the current simulation.)

Design change: All components except Constant now use the included GIF icons rather than Java code.

Bug fix: In the Wiring library, changing the label for a Tunnel would not immediately affect the values propagated through the circuit.

Bug fix: In the Wiring library, Pull Resistors did not respect "printer view," which is meant to be black and white.



COMBINATIONAL ANALYSIS

Feature: In the Combinational Analysis module, the user can select between using a sum of products or a product of sums.

Feature: The Combinational Analysis module supports apostrophes (as in "x'" for NOT x).

Design change: The Combinational Analysis module's expression pane uses a larger font size than previously.

Bug fix: Some run-time exceptions that would occur upon doing "Analyze Circuit" a second time were removed.

Bug fix: In the Karnaugh maps drawn by the Combinational Analysis module, each implicant was indicated by a red rectangle, but they were meant to use varied colors.

Bug fix: The Combinational Analysis module's "Build Circuit" feature could create excess wire segments and even overlapping wires when a gate with an even number of inputs had several of its first half of inputs wired directly to the circuit inputs.

Bug fix: The "Set As Expression" button would sometimes not be disabled when pressed (particularly when the previous expression was the same except for spaces).

