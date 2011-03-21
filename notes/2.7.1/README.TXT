Logisim 2.7.1 fixes bugs, improves file handling

Logisim, a graphical design and simulation tool for logic circuits, is now at version 2.7.1. This release's primary purpose is to address several issue discovered since 2.7.0 was released two weeks ago.

One of the primary bugs addressed in this release is one where a user could inadvertently change a component's attribute to an invalid value. The circuit would typically continue to work normally, but when the file is later reloaded, the file would refuse to load. This release repairs the cause of the invalid value assignments; and it enhances the file-loading process so that a dialog box appears explaining any problems found, and the portion of the file that could be interpreted is still loaded.

Another issue found in 2.7.0 was in the behavior of the transistors. In particular, a transistor would "convert" a floating (Z) value into an error value when told to transmit its source input; now, floating values are transmitted as floating values.

Besides addressing many other issues besides these, this version also adds a "Select Location" attribute to the multiplexer, demultiplexer, and decoder, allowing the user to configure where these components' select (and enable) inputs are located relative to the component.

Finally, this release marks the introduction of a mailing list where users can subscribe to hear about new developments regarding Logisim. We promise that the traffic on this list will average below one message per month. To subscribe, go to https://lists.sourceforge.net/lists/listinfo/circuit-announce

Educational institutions around the world use Logisim as an aid to teaching about digital logic and computer architecture. As a Java application, Logisim can run on most major operating systems. Read more about Logisim at http://www.cburch.com/logisim/, and download it from SourceForge.net at http://sourceforge.net/projects/circuit/.

CHANGE LOG

Feature: When errors are in a file being loaded, the file is still partially loaded and displayed. If multiple errors are found, each is displayed.

Feature: In Plexers library, added Select Location attribute to multiplexer, demultiplexer, and decoder.

Feature: If some components somehow manage to get "off the grid," they are relocated back onto the grid once they are moved. 

Behavior change: With transistors and transmission gates, a floating value is passed through as floating regardless of the value of gate (including a floating or error gate value).

Behavior change: When clicking the currently viewed circuit, the circuit attributes are shown but no tool to add that circuit to itself is selected. This also happens with triple-clicking a circuit: The view is switched to the circuit, but the tool is not selected for adding instances to itself.

Bug fix: The combinational analysis "Product of Sums" option did not work when any variable other than the last one was to appear negated in any of the sums.

Bug fix: In the main editor window, when "Close" was selected from the Window menu, a dialog box popped up giving the Save/Discard/Cancel; but if Cancel was selected, the window was still closed (but without saving).

Bug fix: When "Reset Simulation" was selected while simulation was enabled, the reset values were not propagated through the circuit immediately.

Bug fix: Several types of exceptions could occur during simulation, particularly when poking the simulation while the simulation is busy. Many of these have been removed.

Bug fix: You could copy components from one project and paste them into another, even though the other project's libraries may not support the components being pasted.

Bug fix: Through starting to edit an attribute with a drop-down menu and then switching to view attributes for something else, the newly viewed attributes would actually change to the value of the previous edit.

Bug fix: When editing a circuit's appearances, labels would be given a color based on the fill color for rectangles/ovals/polygons rather than the color selected for text. This color was by default white, and so they appeared invisible.

Bug fix: When editing a circuit's appearance, changing between Border, Fill, Border & Fill didn't update attribute list (whether viewing attributes for a tool or for the current selection).

Bug fix: When editing a circuit's appearance and copying to the clipboard, the anchor's location and facing are stored to the clipboard if the selection includes the anchor.

Bug fix: In rare cases, loading files would show an error reading "Resetting to invalid mark." (This seems to have involved a non-ASCII character in exactly the wrong position.)

Bug fix: The Windows executable's application description was changed so "Open with" context menu gives proper application name.
