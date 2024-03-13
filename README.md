# COMP4321-Crawler

This repository contains code for a web crawler and indexing system developed as part of the COMP4321 course project.

## Table of Contents

- [Instructions](#instructions)
  - [Building the Project](#building-the-project)
  - [Executing the Program](#executing-the-program)
- [Bugs](#bugs)
- [Discussion](#discussion)
- [To-Do](#to-do)

## Instructions

### Building the Project

To compile each Java file in Windows, follow the commands below:

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

#### TestProgram:
```shell
javac -cp "../lib/htmlparser.jar;../lib/jdbm-1.0.jar;." "TestProgram.java"
```

### Executing the Program
After compiling the Java files, execute the TestProgram to initiate the web crawling and indexing process using the following command:

```shell
java -cp "../lib/htmlparser.jar;../lib/jdbm-1.0.jar;." TestProgram
```

## Bugs
- WordID 33 is not found in the database. (No word is stored, i.e. " ")

## Discussion
- How to store the words of the title in each page?
  - Currently, the project extracts words from the body content of each page using the Spider class. We may consider enhancing this process to specifically extract words from the title tag of HTML pages.

- How to manage multiple databases?
    - Currently, different RecordManager instances are used during execution, and all database files are stored in the same directory. Alternatively, we can use a single RecordManager and store all tables in the same database file. This approach needs further discussion.

## To-Do
- Indexer
  - Implement logic to check if the existing table is not empty, then update the table instead of creating a new one. (Consider comparing last modified dates if the page is found in the database.)
  - Add code to extract the last modified date and page size if not provided in the header.
```