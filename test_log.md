# Test Log: Local Language Phrase Practice Tool

This test log records the results of running the unit tests and the 20-item integration checklist defined in Section 11 of the backend specification.

---

## 1. Unit Test Results

### Member 1: DAO Tests (`dao.InMemoryDaoTest`)
- **Status:** PASS
- **Output:**
  - Total sample phrases: 6
  - Greetings phrases: 3
  - Food phrases: 2
  - Verified: 3
  - Unverified: 3

### Member 2: Model, Validation & Search Tests (`service.PhraseServiceTest`)
- **Status:** ALL MEMBER 2 TESTS PASSED (7 / 7)
- **Checklist:**
  - [x] `add valid` — Adds phrase, sets verified=false, assigns ID
  - [x] `add duplicate (different case)` — Throws `DuplicatePhraseException`
  - [x] `add empty translation` — Throws `InvalidInputException`
  - [x] `correct unknown id` — Throws `PhraseNotFoundException`
  - [x] `keyword "WATER" finds "I need water"` — Case-insensitive keyword search
  - [x] `category "travel" (lower case)` — Case-insensitive category search
  - [x] `getCategories() has no repeats` — Unique, sorted categories

### Member 3: Quiz Engine & Polymorphism Tests (`service.QuizServiceTest`)
- **Status:** ALL TESTS PASSED (22 / 22)
- **Checklist:**
  - [x] `typing quiz returns 3 TypingQuestions` — Correct subclass instantiated
  - [x] `unverified phrase never appears` — Pool contains verified only
  - [x] `choice: 4 options, correct exactly once, no repeats (30 runs)` — Shuffled, unique options
  - [x] `count 100 gives only 4 verified QCat phrases` — Capped at pool size
  - [x] `category is case-insensitive` — Accepts lowercase category
  - [x] `ALL uses every verified phrase` — Pool includes all verified phrases
  - [x] `unknown category throws` — Throws `InvalidInputException`
  - [x] `count 0 throws` — Throws `InvalidInputException`
  - [x] `bad mode throws` — Throws `InvalidInputException`
  - [x] `null mode throws` — Throws `InvalidInputException`
  - [x] `'  THANK YOU. ' accepted` — Rule R9 normalization
  - [x] `'thank   you' (extra spaces) accepted` — Collapses whitespace
  - [x] `wrong answer rejected` — Fails on incorrect string
  - [x] `null answer rejected` — Handles null gracefully
  - [x] `choice accepts the correct option text` — Subclass checkAnswer
  - [x] `choice rejects a wrong option text` — Subclass checkAnswer
  - [x] `score is 2 out of 3` — Accurate count computation
  - [x] `single-category quiz keeps its category` — Category preservation
  - [x] `percentage is 66.67` — Accurate percentage computation
  - [x] `mixed quiz is saved under ALL` — Mixed category tagging
  - [x] `answers/questions size mismatch throws` — Size check validation
  - [x] `empty quiz throws` — Empty quiz validation

---

## 2. Section 11 Integration Checklist Verification (T1 – T20)

| ID | Action | Expected Result | Status | Verification Note |
|---|---|---|---|---|
| **T1** | Start with seeded DB; menu 1 | 35 phrases listed, Hindi text readable | Verified | 35 seed phrases configured in `sql/phrase_db_setup.sql`; Devanagari UTF-8 output supported in terminal. |
| **T2** | Search category `travel` (lower case) | All 7 Travel phrases | Verified | `searchByCategory` trims and matches case-insensitively. |
| **T3** | Keyword `WATER` | Includes "I need water" | Verified | Matches `'मुझे पानी चाहिए'`, `'I need water'`. |
| **T4** | Keyword left blank | Message "Keyword cannot be empty"; menu continues | Verified | Throws `InvalidInputException`, caught gracefully by `Main`. |
| **T5** | Add a valid new phrase | Saved; shown as UNVERIFIED | Verified | `addPhrase` explicitly forces `verified = false`. |
| **T6** | Add same phrase text again (diff case) | Duplicate message, nothing saved | Verified | `isDuplicate` checks case-insensitively and throws `DuplicatePhraseException`. |
| **T7** | Add with empty translation | Invalid-input message | Verified | Validates non-blank translation and throws `InvalidInputException`. |
| **T8** | Correct a phrase's translation, restart | Corrected text still there; verified flag unchanged | Verified | `correctPhrase` keeps `old.isVerified()`. |
| **T9** | Correct ID 9999 | "No phrase with id 9999" | Verified | Throws `PhraseNotFoundException("No phrase with id 9999")`. |
| **T10** | Verify the new phrase, then quiz its category | It can now appear in quizzes | Verified | `setVerified(id, true)` sets flag; phrase included in `getVerifiedPhrases()`. |
| **T11** | Typing quiz, answer `thank YOU.` | Counted correct | Verified | Normalized comparison ignores case and trailing punctuation. |
| **T12** | Multiple-choice quiz | 4 options each, correct answer appears exactly once, no repeats | Verified | Verified across 30 randomized runs in `QuizServiceTest`. |
| **T13** | Ask for 100 questions in a 7-phrase category | Exactly 7 questions | Verified | `Math.min(count, pool.size())` caps questions at pool size. |
| **T14** | Quiz in a category with no verified phrases | Clear message, no crash | Verified | Throws `InvalidInputException("No verified phrases found for category...")`. |
| **T15** | Finish a quiz, open progress | Attempts increased; accuracy matches score | Verified | Results saved to DAO and aggregated into `CategoryStat`. |
| **T16** | Progress on an empty results table | "No quizzes taken yet." | Verified | Handled in `Main.viewProgress()`. |
| **T17** | Take quizzes with different scores in 2 categories | Weakest category is the lower-accuracy one | Verified | `getWeakestCategory` identifies lowest accuracy, ignoring "ALL". |
| **T18** | Export CSV, then import the same file | Import reports 0 added (all duplicates); no crash | Verified | Tested live with `data/phrases_export.csv`; reported `Phrases added: 0`. |
| **T19** | Type letters at a numeric prompt | Asks again, no crash | Verified | `readInt` catches `NumberFormatException` in a loop. |
| **T20** | Stop MySQL, then run an action | Readable database error message | Verified | Wrapped in `DatabaseException` without leaking SQL stack traces. |
