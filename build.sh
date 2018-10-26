echo Building jar file...
javac program/*.java
jar -cfm BatchDownload.jar MANIFEST.MF program
rm program/*.class
echo Done, go for it: java -jar BatchDownload FILE_INPUT OUTPUT_PATH
