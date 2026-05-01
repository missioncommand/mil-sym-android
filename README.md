# mil-sym-android

## About

mil-sym-android is an Android port of the [Java-based MIL-STD rendering libraries](https://github.com/missioncommand/mil-sym-java) that have been used in US Army Mission Command software for years.  In November 2013 Mission Command was given the approval to release and maintain these libraries as public open source.  Eventually work on the 2525C SEC Renderer ended and the project was retired.
The Library is still available here: [mil-sym-android-renderer](https://central.sonatype.com/artifact/io.github.missioncommand/mil-sym-android-renderer)

This is a continuation of that effort and this library aims to support 2525D, 2525E and potentially more future versions.

[Wiki](https://github.com/missioncommand/mil-sym-android/wiki)  
[JavaDocs](https://missioncommand.github.io/javadoc/2525D/android/index.html)

The old 2525C renderer has been retired but the libraries and usage information are still available here:  
[2525C Renderer Overview](https://github.com/missioncommand/mil-sym-android/wiki/2525C-Renderer-Overview)

Ports
-----------
[Java](https://github.com/missioncommand/mil-sym-java)  
[Android](https://github.com/missioncommand/mil-sym-android)  
[TypeScript](https://github.com/missioncommand/mil-sym-ts)  

MIL-STD-2525
-----------
The MIL-STD-2525 standard defines how to visualize military symbology.  This project provides support for the entire MIL-STD-2525D Change 1 and 2525E Change 1.  APP6D support has been added as well.  Note that this implementation included the latest change proposals so things may not line up if you're just looking at the base APP6D document.  

Features
-----------
* Support for MilStd 2525 Dch1 (11), Ech1(15), NATO APP6D(10) and partial support for APP6Ev2 (16)(icons only)  
* [Rendering icons](https://github.com/missioncommand/mil-sym-java/wiki/2525D--Renderer-Overview/_edit#22-singlepoint-icon-symbology) as a BufferedImage (Java), Bitmap (Android), and SVG.  
* [Rendering of multipoints](https://github.com/missioncommand/mil-sym-java/wiki/2525D--Renderer-Overview/_edit#33-multipoint-symbology) as [GeoJSON](https://github.com/missioncommand/mil-sym-java/wiki/Interpreting-GeoJSON-Output), [GeoSVG](https://github.com/missioncommand/mil-sym-java/wiki/Interpreting-GeoSVG-Output) or as a [MilStdSymbol Object](https://github.com/missioncommand/mil-sym-java/wiki/Making-Use-of-MilStdSymbol) with all the information needed to draw on your map.  
* [MSLookup](https://github.com/missioncommand/mil-sym-java/wiki/2525D--Renderer-Overview#5-mslookup) class to get information on any symbol such as point count, draw rule, applicable modifier, etc...  
* The ability to add [custom icons](https://github.com/missioncommand/mil-sym-java/wiki/Adding-Custom-Symbols) to an existing symbol set.

Project Structure
-----------
mil-sym-android has a namespace structure that resembles the java layout although differs where we needed to implement java functionality that wasn't available in the Dalvik VM.


Tech
-----------

Eclipse with Android plugins
or
Android Studio
or
Gradle based build system.  


### Building
---

Prerequisites:
* Android SDK is installed
* ```ANDROID_HOME``` environment variable pointing to location of the Android SDK
* Android build-tools v35.0.0

External Libraries in use:  
[AndroidSVG](https://bigbadaboom.github.io/androidsvg/index.html) 1.4 using [Apache v2.0 License](http://www.apache.org/licenses/LICENSE-2.0)  
[Geodesy](https://github.com/mgavaghan/geodesy) 1.1.3 using [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0)


Build:
````
./gradlew build
````
  
![Pixel](https://static.scarf.sh/a.png?x-pxid=03fc886e-90c8-4ef2-93e7-102608929701)
