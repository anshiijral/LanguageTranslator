# Local Language Phrase Practice Tool (Hindi)

Offline phrase-learning tool for Hindi with Devanagari script, transliterations, English meanings, randomized quizzes, and category progress tracking.

**Java 21 | Plain JDBC | MySQL 8 | Console Menu & Facade Architecture**

---

## 1. Architecture & Module Ownership

| Member | Focus | Files Owned |
|--------|-------|-------------|
| **M1** | Database & DAO (JDBC) | `sql/phrase_db_setup.sql`, `src/dao/DBConnection.java`, `src/dao/PhraseDao.java`, `src/dao/PhraseDaoImpl.java`, `src/dao/InMemoryPhraseDao.java`, `src/dao/DaoTest.java` |
| **M2** | Phrase Model, Validation & Search | `src/model/Phrase.java`, `src/exception/*` (4 custom exceptions), `src/service/PhraseService.java`, `src/service/PhraseServiceTest.java` |
| **M3** | Quiz Engine (OOP & Polymorphism) | `src/service/Question.java`, `src/service/TypingQuestion.java`, `src/service/ChoiceQuestion.java`, `src/service/QuizService.java`, `src/service/QuizServiceTest.java` |
| **M4** | Progress, Files, Facade & Integration | `src/model/QuizResult.java`, `src/model/CategoryStat.java`, `src/dao/ResultDao.java`, `src/dao/ResultDaoImpl.java`, `src/dao/InMemoryResultDao.java`, `src/service/ProgressService.java`, `src/util/CsvUtil.java`, `src/app/PhraseApp.java`, `src/app/Main.java`, `README.md`, `test_log.md` |

### Call Flow
```
Main / Future UI -> PhraseApp (Facade) -> Services (Rules R1-R13) -> DAO Interfaces -> MySQL / In-Memory
```

---

## 2. Project Directory Structure

```
PhraseTool/
 ├─ lib/
 │   └─ mysql-connector-j-26.10.0.jar
 ├─ sql/
 │   └─ phrase_db_setup.sql       (Database schema + 35 verified Hindi phrases)
 ├─ data/
 │   └─ phrases_export.csv        (Exported CSV files)
 └─ src/
     ├─ model/
     │   ├─ Phrase.java           (M2)
     │   ├─ QuizResult.java       (M4)
     │   └─ CategoryStat.java     (M4)
     ├─ exception/
     │   ├─ InvalidInputException.java      (M2)
     │   ├─ DuplicatePhraseException.java   (M2)
     │   ├─ PhraseNotFoundException.java    (M2)
     │   └─ DatabaseException.java          (M2)
     ├─ dao/
     │   ├─ DBConnection.java          (M1)
     │   ├─ PhraseDao.java             (M1)
     │   ├─ PhraseDaoImpl.java         (M1)
     │   ├─ InMemoryPhraseDao.java     (M1)
     │   ├─ ResultDao.java             (M4)
     │   ├─ ResultDaoImpl.java         (M4)
     │   ├─ InMemoryResultDao.java     (M4)
     │   ├─ DaoTest.java               (M1)
     │   └─ InMemoryDaoTest.java       (M1)
     ├─ service/
     │   ├─ PhraseService.java         (M2)
     │   ├─ PhraseServiceTest.java     (M2)
     │   ├─ Question.java              (M3)
     │   ├─ TypingQuestion.java        (M3)
     │   ├─ ChoiceQuestion.java        (M3)
     │   ├─ QuizService.java           (M3)
     │   ├─ QuizServiceTest.java       (M3)
     │   └─ ProgressService.java       (M4)
     ├─ util/
     │   └─ CsvUtil.java               (M4)
     └─ app/
         ├─ PhraseApp.java             (M4 facade)
         └─ Main.java                  (M4 console test menu)
```

---

## 3. Database Setup (MySQL)

From the project root:

```powershell
mysql -u root -p < sql/phrase_db_setup.sql
```

The script sets up:
- Database `phrase_db` with `utf8mb4` encoding
- `phrases` table with unique constraint on phrase text
- `quiz_results` table for tracking score history
- 35 seed phrases across 5 categories (`Greetings`, `Travel`, `Food`, `Shopping`, `Emergency`), all pre-verified.

---

## 4. Compilation

Compile all Java source files with UTF-8 encoding:

```powershell
javac -encoding UTF-8 -d out -cp "lib/mysql-connector-j-26.10.0.jar" (Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName })
```

---

## 5. Running the Application

To run the interactive console menu:

```powershell
java "-Dfile.encoding=UTF-8" -cp "out;lib/mysql-connector-j-26.10.0.jar" app.Main
```

### Menu Options:
1. **View all phrases**: Displays all phrases with ID, script, transliteration, translation, category, and verification status.
2. **Search by category**: Case-insensitive filter (e.g. `Greetings`, `travel`, or `ALL`).
3. **Search by keyword**: Searches Hindi text, English translation, and transliteration.
4. **Take a quiz**: Interactive quiz with Typing (`TYPING`) or Multiple-Choice (`CHOICE`) mode.
5. **View progress**: Tabular display of quizzes taken, correct/total, accuracy %, and weakest category.
6. **Add a phrase**: Validates inputs (length <= 200, category <= 50) and saves as unverified.
7. **Correct a phrase**: Edits phrase text and details while keeping its current verification status.
8. **Verify / unverify a phrase**: Toggles verified status so phrase can appear in quizzes.
9. **Import phrases from CSV**: Imports CSV formatted phrases with duplicate detection.
10. **Export phrases to CSV**: Exports phrases to CSV with double-quoted RFC-compliant format.

---

## 6. Running Unit & Integration Tests

### Member 1 Tests (DAOs)
```powershell
java "-Dfile.encoding=UTF-8" -cp "out;lib/mysql-connector-j-26.10.0.jar" dao.InMemoryDaoTest
```

### Member 2 Tests (PhraseService & Validation)
```powershell
java "-Dfile.encoding=UTF-8" -cp "out;lib/mysql-connector-j-26.10.0.jar" service.PhraseServiceTest
```

### Member 3 Tests (Quiz Engine & Polymorphism)
```powershell
java "-Dfile.encoding=UTF-8" -cp "out;lib/mysql-connector-j-26.10.0.jar" service.QuizServiceTest
```
