# mil-sym-android

## About

mil-sym-android is an Android port of the Java-based MIL-STD rendering libraries that have been used in US Army Mission Command software for years.  In November 2013 Mission Command was given the approval to release and maintain these libraries as public open source.  Eventually work on the 2525C SEC Renderer ended and the project was retired.
The Library is still available here: [mil-sym-android-renderer](https://central.sonatype.com/artifact/io.github.missioncommand/mil-sym-android-renderer)

This is a continuation of that effort and is not currently open source until which time we get the proper approvals in place.
This library aims to support 2525D, 2525E and potentially more future versions.

[Wiki](https://github.com/missioncommand/mil-sym-android/wiki)

### MIL-STD-2525
---
The [MIL-STD-2525] standard defines how to visualize military symbology.  This project provides support for the entire MIL-STD-2525D Change 1.

### Project Structure
---
mil-sym-android has a namespace structure that resembles the java layout although differs where we needed to implement java functionality that wasn't available in the Dalvik VM.


### Tech
---

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
* Android build-tools v30.0.3

External Libraries in use:  
[AndroidSVG](https://bigbadaboom.github.io/androidsvg/index.html) 1.4 using [Apache v2.0 License](http://www.apache.org/licenses/LICENSE-2.0)  
[Geodesy](https://github.com/mgavaghan/geodesy) 1.1.3 using [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0)


Build:
````
./gradlew build
````

Build and install to Maven local:
````
./gradlew build publishToMavenLocal
````
  
