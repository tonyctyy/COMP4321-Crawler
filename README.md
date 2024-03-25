# COMP4321-Crawler

This repository contains code for a web crawler and indexing system developed as part of the COMP4321 course project. There are 4 main folders in the project:
1. data: Contains the database files used to store the crawled data.
2. lib: Contains the external libraries used in the project.
3. src: Contains the source code for the project.
4. docs: Contains the project documentation. (You can find the "spider.result.txt" and "Database Design.pdf" here.)

## Table of Contents

- [Instructions](#instructions)
  - [Building the Project](#building-the-project)
  - [Executing the Program](#executing-the-program)
- [Bugs](#bugs)
- [Discussion](#discussion)
- [To-Do](#to-do)
- [Q&As](#qas)

## Instructions

### Prerequisites
- OpenJDK version: 21.0.2


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
- How to store the words of the title in each page? (Solved, we just extract the title and the body through the html tag directly)
  - Currently, the project extracts words from the body content of each page using the Spider class. We may consider enhancing this process to specifically extract words from the title tag of HTML pages.

- How to manage multiple databases?
    - Currently, different RecordManager instances are used during execution, and all database files are stored in the same directory. Alternatively, we can use a single RecordManager and store all tables in the same database file. This approach needs further discussion.
 

## To-Do
- Indexer
  - Implement logic to check if the existing table is not empty, then update the table instead of creating a new one. (Consider comparing last modified dates if the page is found in the database.)
  - Add code to extract the last modified date and page size if not provided in the header.
 

## Q&As
- In our database design, We use id and value to store the info of "key words" and "pages". However, can we add extra tabless for mapping (e.g. "url" as key, and "page_id" as value) to find out the respective "page_id" and "word_id" faster?
- Also, we design the database in a standard format (e.g. "word_id" as key, having different columns as values such as "page_id", "frequenct" ...). Can we combine the values into one string and store it as the value(i.e. similar to JSON format, using "|" and "," as the separator)? On top of it, although we specify the type of elments in the design (string, float, etc.), can we use both "key" and "value" as string in the database?
