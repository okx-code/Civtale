# Getting started

To setup the development environment, you need either Windows or Linux, and Java 25+ installed.

Run `./gradlew.bat downloadServer` to download the Hytale jar and prepare the server. You will need to authenticate during this, so watch for the link

You wil find it will put a server jar and assets in the `run` folder.

To run Hytale, use `./gradlew.bat runServer`.

If you run into an error with the AOTCache, make sure you are using Adoptium as recommended by Hytale

Running gradle in debug mode will open the debugger on port 5005
