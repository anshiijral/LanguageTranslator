# Local Language Phrase Practice Tool

## Overview
A Java-based phrase management module that stores local-language phrases,
English translations, transliterations, categories, and verification status.

## Technologies
- Java
- MySQL
- JDBC (MySQL Connector/J)

## Project Structure
- src/model/Phrase.java: Phrase data model
- src/dao/PhraseDao.java: DAO interface
- src/dao/PhraseDaoImpl.java: MySQL DAO implementation
- src/dao/InMemoryPhraseDao.java: In-memory DAO implementation
- src/dao/DBConnection.java: Database connection
- src/exception/DatabaseException.java: Database exception handling
- src/dao/DaoTest.java: MySQL DAO test
- src/dao/InMemoryDaoTest.java: In-memory DAO test
- sql/phrase_db_setup.sql: Database and sample-data setup
- lib/: MySQL JDBC driver

## Requirements
- Java JDK
- MySQL Server

## Database Setup
From the project directory, run:

    mysql -u root < sql/phrase_db_setup.sql

If your MySQL account uses a password, use your normal authenticated MySQL
setup instead. Update src/dao/DBConnection.java if your local connection
settings differ.

## Compile
Run from the project directory:

    mkdir -p out
    javac -encoding UTF-8 -d out -cp "lib/mysql-connector-j-26.10.0.jar" $(find src -name "*.java")

## Run Tests
In-memory DAO test:

    java -cp "out:lib/mysql-connector-j-26.10.0.jar" dao.InMemoryDaoTest

MySQL DAO test:

    java -cp "out:lib/mysql-connector-j-26.10.0.jar" dao.DaoTest
