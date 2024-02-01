# A Raw UDP Receiver for MaxMSP

#### IN DEVELOPMENT
If you are looking for a general purpose UDP receiver object you might want to check out the [Sadam Library Package](http://sadam.hu/en/software)
<hr> 

This program depends on the [Cycling74](https://cycling74.com/) MaxMSP Java API ditributed with the MaxMSP Application Package.

**Tested on Max8** (but it should work also in earlier Max release)

To compile simply run:
```shell
javac -classpath /Applications/Max8.5.app/Contents/Resources/C74/packages/max-mxj/java-classes/lib/max.jar Pipe.java RawUdpReceiver.java
```

You might want to change the `max.jar` path if different from the one provided

To test the software a `basic.maxpat` patch is provided (the compile `*.class` files should be installed in the same folder of the patch)

**N.B.** This repository contains a `.vscode` configuration folder to let IntelliSense parse your code