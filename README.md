# README

Logisim is a circuit simulator, [originally available here](http://ozark.hendrix.edu/~burch/logisim/).

## Why this fork of Logisim?
Carl Burch, the original author of Logisim, abandoned development in 2011 and moved on to a similar successor project, [Toves](http://www.toves.org/) in 2013, because Logisim's code base is in need of a major overhaul. Rather than start from scratch ([something you should never do](http://www.joelonsoftware.com/articles/fog0000000069.html)), this fork of Logisim picks up where Dr. Burch left off to incrementally improve Logisim.

## What's wrong with Logisim?
Logisim's code itself has numerous smells. Here's an incomplete list of these:

* No test suite!
* Undocumented packages, classes, methods
* Duplicated and dead code
* No coherent organization (it should be MVC)
* High coupling, low cohesion (it would benefit from IoC)
* Unnecessarily reimplements functionality found in standard or third party libraries (e.g., it has it's own Toolbar classes)
* Code to draw components is too low-level

Logisim's user interface has numerous gotchas that need to be addressed. Here's some of the more important issues:

* The file format is not merge-friendly, making it impossible for students to collaborate on circuits
* Unreasonable defaults
* Limited interaction styles
* Single (graphical) view of circuit

## What's right with Logisim?
It is arguably the best free tool for teaching circuit design, which is why its development must continue.

## Getting started for developers
Logisim developers: Logisim uses the [Gradle build system](http://www.gradle.org), so set that up first before attempting to develop Logisim. Ensure that the gradle executable is in the system path. To build the executable for Windows, you must install [launch4j](http://launch4j.sourceforge.net/) and ensure it is in the system path.

The build script recognizes the following commands:

	gradle build     # Build application jar
	gradle eclipse   # Build Eclipse configuration
	gradle createExe # Build logisim executable
	gradle run       # Run logisim from gradle
	gradle sonar     # Examine problems using Sonar

To examine problems with Sonar, you need to download and run SonarQube (the server) first.
The server must be in a path without spaces in it, so don't put it in Program Files.
Then, you can run gradle sonar and go to [Sonar's page](http://localhost:9000).
See the status of what everybody's working on using the [Logisim Trello Board](https://trello.com/b/GYyiVOWH/logisim).

