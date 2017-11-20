JavaPageSize
============

JavaPageSize is an exploration of asynchronous programming using Java. It uses
CompletableFuture's to compose concurrent get of HTML pages and their resources
to compute the total weight of applications.

To run the program, build the uberjar using

```
mvn package
```

and then run it with a list of URLs, or with the ``-f <file>`` argument to 
give a file with a list of URLs (one per line). For example:

```
java -jar target\javapagesize-1.0-SNAPSHOT.jar https://www.yahoo.com/
```

The usage information is:

```
usage: javapagesize [options] [url] ...
 -f,--file <arg>   file name to read URLs from
 -h,--help         print this message
 ```