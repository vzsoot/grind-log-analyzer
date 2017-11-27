grind-log-analyzer
==================

Grind Log Analyzer
------------------
Surefire report DB importer for the [dist_test](https://github.com/cloudera/dist_test) tool powered by [grind](https://github.com/cloudera/dist_test/blob/master/docs/grind.md)

Use case
--------
The dist_test utility has output for each test result. This tool parses the surefire results and imports them into a relational database (uses PostgreSQL by default) for future analysis.

Usage
-----
    ```java -jar grind-log-analyzer.jar --dburl=<target jdbc database url> --dbuser=<DB user> --dbpass=<DB password> --grindurl=<source dist-test url>```
    
Additional parameters
---------------------
* Additional parameters and defaults can be found [here](src/main/resources/application-default.properties)

* For example to override the log format:
    ```java -jar ... --logFormat="LEVEL TIMESTAMP - MESSAGE"```

* Running with h2 embedded database with jar: 
    ```java -jar --spring.profiles.active=default,h2 grindurl=<source dist-test url>```

* Running with h2 embedded database with gradle: 
    ```gradle bootRun -Pspring.profiles.active=default,h2 -Pgrindurl=<source dist-test url>```
