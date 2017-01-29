# Logisim
[![Build Status](https://travis-ci.org/m4droid/logisim.svg?branch=master)](https://travis-ci.org/m4droid/logisim)
[![Coverage Status](https://coveralls.io/repos/m4droid/logisim/badge.svg?branch=master&service=github)](https://coveralls.io/github/m4droid/logisim?branch=master)

Logisim es una herramienta de diseño y simulación de circuitos lógicos computacionales. Logisim tiene la capacidad de crear grandes circuitos a partir de otros más simples. Logisim es una herramienta de distribución libre y es posible tener acceso al codigo fuente desarrollado en Java (https://sourceforge.net/projects/circuit).

El curso de Ingeniería de Software I (CC4401) pretende aplicar extensiones a Logisim como proyecto de aplicación para proveer a la herramienta de funcionalidades que mejoren la experiencia de usuario.

## Proyecto Protoboard

Se requiere implementar un “protoboard” que permita simular una serie de circuitos mediante una interfaz interactiva. Un protoboard es una tableta que permite experimentar con circuitos eléctricos de forma física, sin embargo el proyecto del curso pretende desarrollar un simulador de “protoboard” con las funcionalidades básicas.

Se requiere una implementación básica pero funcional que permita realizar lo siguiente:

1. Una interfaz que permita crear un nuevo “protoboard”.
2. Desarrollar los distintos componentes de protoboard como ser: canal central, buses y pistas.
3. Proponer proporcionar componentes y chips básicos que implementen un circuito en particular como: flip flop, NAND, NOT, etc.
4. La interfaz debe permitir crear un circuito utilizando algunos componentes y ejecutar la simulación del circuito mediante leds, relojes, resistencias, entrada positiva, entrada negativa, botón para activar corriente, pulsor, etc.
5. La interfaz debe permitir construir el circuito de forma sencilla.


Logisim is a circuit simulator, [originally available here](http://www.cburch.com/logisim/).

## Why this fork of Logisim?
Carl Burch, the original author of Logisim, abandoned development in 2011 and moved on to a similar successor project, [Toves](http://www.toves.org/) in 2013, because Logisim's code base is in need of a major overhaul.
Rather than start from scratch ([something you should never do](http://www.joelonsoftware.com/articles/fog0000000069.html)), this fork of Logisim picks up where Dr. Burch left off to incrementally improve Logisim.

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
It is arguably the best free/libre and gratis tool for teaching circuit design.
That is why its development must continue.

## Newest GitHub build
If you just want to test the newest developer version to report issues or for new features, download it here.
[![Build Status](http://84.201.35.242:8080/job/LOGISIM/badge/icon)](http://mechtecs.tk:8080/job/LOGISIM/)
[Download](http://84.201.35.242:8080/job/LOGISIM/)
## Getting started for developers

The build script recognizes the following commands:

	./gradlew build     # Build application jar
	./gradlew eclipse   # Build Eclipse configuration
	./gradlew createExe # Build logisim executable
	./gradlew run       # Run logisim from gradle
	./gradlew sonar     # Examine problems using Sonar


To build the executable for Windows, you must install [launch4j](http://launch4j.sourceforge.net/) and ensure it is in the system path or you could install
launch4j just into the root of the project (build process will find it and it will be ignore by git).

To examine problems with Sonar, you need to download [SonarQube](http://www.sonarqube.org/downloads/) (the server) first into a folder without spaces in it.
Then, run SonarQube for your platform and run `gradlew sonar` and go to [Sonar's page](http://localhost:9000).
See the status of what everybody's working on using the [Logisim Trello Board](https://trello.com/b/GYyiVOWH/logisim).

