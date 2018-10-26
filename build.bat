@echo off
echo Building jar file...
javac program\*.java
jar -cfm BatchDownload.jar MANIFEST.MF program
del program\*.class
echo.
echo Done, go for it: java -jar BatchDownload.jar FILE_INPUT OUTPUT_PATH
