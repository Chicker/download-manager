## Description

This is a console utility that downloads files over HTTP protocol.

The utility can download several hyperlinks simultaneously. It has a command-line argument named `-n`, which sets a count of simultaneous execution threads.

There is a possibility to limit download speed overall. For this the program has a command-line argument named `-l`. The limit speed will be divided between execution threads.

A list of hyperlinks should be saved in a simple plain text file that has a specific structure. The path to this file sets by command-line argument named `-f`.

To set the path to the output folder where downloaded files will be saved, you should use the command-line argument named `-o`.

**This project is a gradle-based project.**

### Hyperlinks file structure

In the file every hyperlink should be written in individual line/record. Such record should consist of the following two parts separated by space:

- HTTP URL (for example, `http://example.com/archive.zip`)
- the filename to save that include file extension (for example, `my_archive.zip`).

## Build

To make a standalone jar-file you can run the following gradle task:

```
./gradlew shadowJar
```

The all-in-one jar-file will be placed in `build/libs/` folder. For example, this jar-file may have such name `download-manager-1.0-all.jar`.

## Run

This program can be run as a normal jar-file. E.g,

```
java -jar build/libs/download-manager-1.0-all.jar
```

The program have a several required command-line arguments:

* `-f` - a full path to the file that consists a list of hyperlinks.
* `-l` - an overall bandwidth limit for downloading (bytes / seconds) (you can use mnemonic symbols, e.g. 100k, 1M).
* `-n` - a count of simultaneous downloading threads.
* `-o` - a full path to the folder where downloaded files will be saved (at the run moment this folder must be exist).

The program shipped with a configuration file named `logback.xml`, that determines a logging policy.
