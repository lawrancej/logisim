# README

## Prerequisites
The build script (Buildfile) requires:

* Ruby 1.8+
* Apache Buildr
* Nokogiri
* launch4j

To install Ruby and Buildr, see: <http://buildr.apache.org/installing.html>
After installing Ruby, install Nokogiri:

    gem install nokogiri

To install launch4j, run the installer for launch4j: <http://launch4j.sourceforge.net/>
Also, add launch4j to the PATH environment variable.

## Building Logisim using Apache Buildr
To build logisim, just type:

    buildr clean package

The generated jar (and executable) will be in the `target` folder.

## Building Logisim using Eclipse
If you do not wish to download and install Buildr, you can use Eclipse instead.
But first, you must type in the following:

    mkdir -p ~/.m2/repository/
    cp -r libs/* ~/.m2/repository/

Then, inside Eclipse, import the project as usual.

## Troubleshooting Eclipse
If you see errors in Eclipse, then install the M2E: Maven Integration for Eclipse plugin.

If you don't want to install Maven for Eclipse (plugin) you may be able to get by without it:

1. Right click on logisim (the project) -> Build Path -> Configure Build Path...
2. Click on Add Variable...
3. Click Configure Variables...
4. Click New...

        Name: M2_HOME
        Path: C:\Users\yourName\.m2\repository
