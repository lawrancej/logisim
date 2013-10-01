# README

Logisim is a circuit simulator, [available here](http://ozark.hendrix.edu/~burch/logisim/).

## Getting started for developers
Logisim developers: Logisim uses the [Gradle build system](http://www.gradle.org), so set that up first before attempting to develop Logisim. Ensure that the gradle executable is in the system path. To build the executable for Windows, you must install [launch4j](http://launch4j.sourceforge.net/) and ensure it is on the system path.

The build script recognizes the following commands:

	gradle build     # Build application jar
	gradle eclipse   # Build Eclipse configuration
	gradle createExe # Build logisim executable
	gradle run       # Run logisim from gradle
	gradle javadoc   # Generate Javadoc

