# Local Language Phrase Practice Tool (Hindi)

Offline phrase-learning tool for Hindi featuring Devanagari script, transliterations, English meanings, randomized quizzes, and category-based progress tracking.

**Java 21 | Plain JDBC | MySQL 8 | Facade Architecture & Console Menu**

---

## 1. Architecture & Module Ownership

| Member | Focus | Files Owned | Key Syllabus Concepts |
|---|---|---|---|
| **M1** | Database & DAO (JDBC) | `sql/phrase_db_setup.sql`<br>`src/dao/DBConnection.java`<br>`src/dao/PhraseDao.java`<br>`src/dao/PhraseDaoImpl.java`<br>`src/dao/InMemoryPhraseDao.java`<br>`src/dao/DaoTest.java` | **Unit III**: JDBC architecture, `DriverManager`, `PreparedStatement`, `ResultSet`, CRUD, try-with-resources, connection pooling readiness |
| **M2** | Phrase Model, Validation & Search | `src/model/Phrase.java`<br>`src/exception/*` (4 custom exceptions)<br>`src/service/PhraseService.java`<br>`src/service/PhraseServiceTest.java` | **Unit I & II**: Encapsulation, constructors, custom checked exceptions, `TreeSet`/`TreeMap` for unique sorted categories, lambda & stream filters |
| **M3** | Quiz Engine | `src/service/Question.java`<br>`src/service/TypingQuestion.java`<br>`src/service/ChoiceQuestion.java`<br>`src/service/QuizService.java`<br>`src/service/QuizServiceTest.java` | **Unit I & II**: Abstract classes, inheritance, method overriding, runtime polymorphism (`Question` hierarchy), `Collections.shuffle`, Generics |
| **M4** | Progress, Files, Facade & Integration | `src/model/QuizResult.java`<br>`src/model/CategoryStat.java`<br>`src/dao/ResultDao.java`<br>`src/dao/ResultDaoImpl.java`<br>`src/dao/InMemoryResultDao.java`<br>`src/service/ProgressService.java`<br>`src/util/CsvUtil.java`<br>`src/app/PhraseApp.java`<br>`src/app/Main.java`<br>`README.md`<br>`test_log.md` | **Unit II & III**: Aggregation using `Map<String, int[]>`, file I/O (`BufferedReader`/`BufferedWriter` with UTF-8), Facade design pattern, integration testing |

### Layered Call Flow
```
Main / Future UI  ───►  PhraseApp (Facade)  ───►  Services (Business Rules R1-R13)  ───►  DAO Interfaces  ───►  MySQL / In-Memory
```
*Rule: Upper layers never execute SQL; DAOs never validate input.*

---

## 2. Project Directory Structure

```
PhraseTool/
 ├─ lib/
 │   └─ mysql-connector-j-26.10.0.jar
 ├─ sql/
 │   └─ phrase_db_setup.sql            (Schema + 35 verified Hindi phrases)
 ├─ data/
 │   └─ phrases_export.csv             (Exported CSV files)
 ├─ Downloads/phrase-tool-frontend/    (Web UI assets: HTML, CSS, JS)
 ├─ test_log.md                        (20-point test checklist verification log)
 └─ src/
     ├─ model/
     │   ├─ Phrase.java                (M2)
     │   ├─ QuizResult.java            (M4)
     │   └─ CategoryStat.java          (M4)
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
         ├─ PhraseApp.java             (M4 Facade)
         └─ Main.java                  (M4 Interactive Console Menu)
```

---

## 3. Business Rules Implementation (R1 – R13)

- **R1 (Input Validation):** Phrase, translation, and category must be non-null and non-blank after trimming. Length constraints: <= 200 chars for phrase/translation/transliteration; <= 50 chars for category.
- **R2 (Duplicate Prevention):** Rejects additions matching existing phrases ignoring case and surrounding whitespace (`DuplicatePhraseException`).
- **R3 (Default Verification):** User additions are always saved as `verified = false`.
- **R4 (Corrections):** Updating preserves the existing verified status.
- **R5 (Verification Toggle):** Explicit verification flip; quizzes only draw from verified phrases.
- **R6 (Case-Insensitive Search):** Search by category or keyword (across phrase, transliteration, and translation).
- **R7 (Quiz Setup):** Validates mode (`TYPING`/`CHOICE`), category, and question count (automatically capped at available pool size).
- **R8 (Direction):** Presents Hindi text (+ transliteration) and checks against English translation.
- **R9 (Answer Normalization):** Trims, lowercases, collapses whitespace, and ignores trailing `.`, `?`, `!`.
- **R10 (Multiple Choice Generation):** Provides up to 4 options (1 correct answer + up to 3 distinct wrong verified translations), randomized.
- **R11 (Polymorphic Scoring):** Evaluates `Question` answers polymorphically without type inspection; tags mixed quizzes as `"ALL"`.
- **R12 (Progress Analytics):** Aggregates quizzes, correct answers, total questions, and identifies weakest category (excluding `"ALL"` and zero-total rows).
- **R13 (RFC-Quoted CSV):** Double-quotes all CSV fields; escapes inner quotes (`""`); deduplicates on import.

---

## 4. Innovations (Section 10)

- **Weak-Category Quiz (`PhraseApp.startWeakCategoryQuiz`):** Automatically creates a practice quiz focusing on the learner's lowest-accuracy category.
- **Phrase of the Day (`PhraseApp.getPhraseOfTheDay`):** Deterministically selects a daily verified phrase using `LocalDate.now().getDayOfYear()`.

---

## 5. Database Setup (MySQL 8)

Run the SQL setup script to initialize the database and seed **35 verified Hindi phrases** (7 phrases across 5 categories: Greetings, Travel, Food, Shopping, Emergency):

```powershell
mysql -u root -p < sql/phrase_db_setup.sql
```

*Note: Update password in `src/dao/DBConnection.java` if your local MySQL root password differs.*

---

## 6. Build & Run

### Compilation (UTF-8)
```powershell
javac -encoding UTF-8 -d out -cp "lib/mysql-connector-j-26.10.0.jar" (Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName })
```

### Running the Application
```powershell
java "-Dfile.encoding=UTF-8" -cp "out;lib/mysql-connector-j-26.10.0.jar" app.Main
```

### Interactive Console Features:
1. **View all phrases:** Lists all entries with verification flag.
2. **Search by category:** Case-insensitive search (e.g. `Greetings`, `food`, or `ALL`).
3. **Search by keyword:** Searches phrase, translation, and transliteration.
4. **Take a quiz:** Choice of Typing mode or Multiple Choice mode.
5. **View progress:** Category summary table showing quizzes taken, correct/total, accuracy %, and weakest category.
6. **Add a phrase:** Adds new phrase (defaults to unverified).
7. **Correct a phrase:** Updates translation/details while preserving verification flag.
8. **Verify / unverify a phrase:** Promotes phrase to verified status for quizzes.
9. **Import phrases from CSV:** Imports CSV files with duplicate handling.
10. **Export phrases to CSV:** Exports all phrases to a standard CSV format.

---

## 7. Test Suites & Verification

### Automated Unit Tests:
```powershell
# 1. Member 1 DAO Tests
java "-Dfile.encoding=UTF-8" -cp "out;lib/mysql-connector-j-26.10.0.jar" dao.InMemoryDaoTest

# 2. Member 2 PhraseService Tests (7/7 PASS)
java "-Dfile.encoding=UTF-8" -cp "out;lib/mysql-connector-j-26.10.0.jar" service.PhraseServiceTest

# 3. Member 3 Quiz Engine Tests (22/22 PASS)
java "-Dfile.encoding=UTF-8" -cp "out;lib/mysql-connector-j-26.10.0.jar" service.QuizServiceTest
```

### 20-Point Integration Test Checklist (Section 11)
See [`test_log.md`](test_log.md) for the verified run log covering tests **T1 through T20**.

---

## 8. Viva Questions & Quick Answers

- **What does your project do?**  
  An offline Hindi phrase trainer: search by situation, take randomized quizzes (typing and multiple-choice), track accuracy per category, and add/correct phrases.
- **What is the architecture?**  
  Layered architecture: `Main/UI` -> `PhraseApp` (facade) -> `Services` (business rules) -> `DAO interfaces` -> `MySQL/InMemory`.
- **Where is polymorphism used?**  
  In the `Question` hierarchy (`TypingQuestion` and `ChoiceQuestion`). `QuizService.evaluate()` calls `q.checkAnswer(a)` polymorphically without knowing the concrete question type.
- **Why wrap SQLException?**  
  `SQLException` is wrapped in unchecked `DatabaseException` so service and UI layers remain decoupled from JDBC.
- **Why use a Facade (`PhraseApp`)?**  
  Provides a clean, single entry point for any UI (console, Swing, or web API) without exposing internal services or DAOs.
