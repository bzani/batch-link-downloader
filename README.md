# About

This simple, powerful and multi-threaded program can read from a text file and download its links via batch.
For now, only works for Zippyshare.

# Usage

- Run the script ```build.sh``` (Linux) or ```build.bat``` (Windows).
- Execute the jar file: ```java -jar BatchDownload.jar <file_input> <output_path>```.

# Tips

Wanna grab a bunch of links from a page? Try using curl:
```curl <PAGE> | grep -P 'zippyshare.com' | grep -oP 'href="\K[^"]+' > download.txt```

