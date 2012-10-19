# README

## Build using Buildr
This project is built using Apache Buildr. (See Buildfile)

To use Buildr to create a jar file, just type (in this directory):

    buildr clean package

The generated jar will be in the `target` folder.

## Build using Eclipse
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
