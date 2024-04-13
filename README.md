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
  - [Bugs](#bugs)
  - [Discussion](#discussion)

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

### Executing the Program
After compiling the Java files, execute the TestProgram to initiate the web crawling and indexing process using the following command:

```shell
java -cp "../lib/htmlparser.jar;../lib/jdbm-1.0.jar;." "TestProgram"
```

## Bugs
#1 WordID 33 is not found in the database. (No word is stored, i.e. " ") (Solved, need to add more special characters to the stopword.txt (e.g. | ))


## Discussion
- How to handle unindexed child pages?
  - Currently, we can't get the child pages id if the child page is not indexed. Then, how can we store the child pages id in the parent page? 

- How to calculate the weighting of query?

- How to store the words of the title in each page? (Solved)
  - Currently, the project extracts words from the body content of each page using the Spider class. We may consider enhancing this process to specifically extract words from the title tag of HTML pages.

- How to manage multiple databases? (Solved)
    - Currently, different RecordManager instances are used during execution, and all database files are stored in the same directory. Alternatively, we can use a single RecordManager and store all tables in the same database file. This approach needs further discussion.