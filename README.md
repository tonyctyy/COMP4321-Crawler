# COMP4321-Crawler

This repository contains code for a web crawler and indexing system developed as part of the COMP4321 course project. There are 4 main folders in the project:
1. data: Contains the database files used to store the crawled data.
2. lib: Contains the external libraries used in the project.
3. src: Contains the source code for the project.
4. docs: Contains the project documentation. (You can find the "spider.result.txt" and "Database Design.pdf" here.)

## Table of Contents

- [COMP4321-Crawler](#comp4321-crawler)
  - [Table of Contents](#table-of-contents)
  - [Instructions](#instructions)
    - [Building the Project](#building-the-project)
      - [StopStem:](#stopstem)
      - [Indexer:](#indexer)
      - [Spider:](#spider)
      - [CrawlandIndex:](#crawlandindex)
      - [TestProgram:](#testprogram)
      - [Executing the Program](#executing-the-program)
      - [Running the Frontend](#running-the-frontend)

## Instructions

### Building the Project

To compile each Java file in Windows, follow the commands below (Please make sure you are in the src directory):

#### StopStem:
```shell
javac "StopStem.java"
```

#### Indexer:
```shell
javac -cp "../lib/jdbm-1.0.jar;." "Indexer.java"
```

#### Spider:
```shell
javac -cp "../lib/htmlparser.jar;." "Spider.java"
```

#### CrawlandIndex:
```shell
javac -cp "../lib/htmlparser.jar;../lib/jdbm-1.0.jar;." "CrawlandIndex.java"
```

#### TestProgram:
```shell
javac "TestProgram.java"
```

#### Executing the Program
After compiling the Java files, execute the TestProgram to initiate the web crawling and indexing process using the following command:

```shell
java -cp "../lib/htmlparser.jar;../lib/jdbm-1.0.jar;." "TestProgram"
```

After executing the TestProgram, the following files will be generated:
- "database.db" in the backend folder, "../data/" and in the frontend folder, "./apache-tomcat-10.1.20/webapps/comp4321/WEB-INF/database/".
(The database.db should be copied to the frontend folder automatically by the TestProgram. If not, please copy it manually for the frontend to work properly.)


#### Running the Frontend
Before running the frontend, please set up the system environment variables for Apache Tomcat as follows:
1. CATALINA_HOME = "{path to this project}\COMP4321-Crawler\src\apache-tomcat-10.1.20"
2. JAVA_HOME = "{path to your JDK}" (e.g. "C:\Program Files\Eclipse Adoptium\jdk-21.0.2.13-hotspot")

After setting up the system environment variables, run the following command to start the Apache Tomcat server (Please make sure you are in the src directory):

```shell
apache-tomcat-10.1.20/bin/startup.bat
```

Then, open a web browser and navigate to the following URL to access the frontend:
```shell
http://localhost:8080/comp4321/
```