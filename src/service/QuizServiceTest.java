package service;

import dao.InMemoryPhraseDao;
import dao.PhraseDao;
import exception.InvalidInputException;
import model.Phrase;
import model.QuizResult;
import java.util.ArrayList;
import java.util.List;

// Console tests for Member 3's classes. Run main(); every line should say PASS.
public class QuizServiceTest {

    static int failed = 0;

    // Prints PASS or FAIL for one check.
    static void check(String name, boolean ok) {
        System.out.println((ok ? "PASS  " : "FAIL  ") + name);
        if (!ok) failed++;
    }

    // Adds a phrase straight into the DAO so the test controls verified true/false.
    static void add(PhraseDao dao, String ph, String tr, String cat, boolean verified) {
        dao.insert(new Phrase(0, ph, tr, ph + "-translit", cat, verified));
    }

    public static void main(String[] args) throws Exception {
        PhraseDao dao = new InMemoryPhraseDao();
        // 4 verified + 1 unverified phrase in a test-only category "QCat"
        add(dao, "p1", "Thank you", "QCat", true);
        add(dao, "p2", "Good morning", "QCat", true);
        add(dao, "p3", "Where is the hospital?", "QCat", true);
        add(dao, "p4", "Call the police", "QCat", true);
        add(dao, "p5", "SECRET unverified", "QCat", false);
        // 2 verified phrases in a second category, for the mixed-category test
        add(dao, "p6", "Fire!", "QOther", true);
        add(dao, "p7", "Help me!", "QOther", true);

        PhraseService ps = new PhraseService(dao);
        QuizService qs = new QuizService(ps);

        // T1: typing quiz of 3 from QCat
        List<Question> t = qs.createQuiz("QCat", 3, "TYPING");
        boolean allTyping = true, noUnverified = true;
        for (Question q : t) {
            if (!(q instanceof TypingQuestion)) allTyping = false;
            if (q.getCorrectAnswer().contains("SECRET")) noUnverified = false;
        }
        check("typing quiz returns 3 TypingQuestions", t.size() == 3 && allTyping);
        check("unverified phrase never appears", noUnverified);

        // T2: multiple choice - correct answer exactly once, no repeated options
        boolean optionsOk = true;
        for (int run = 0; run < 30; run++) {                 // repeat because quizzes are random
            for (Question q : qs.createQuiz("QCat", 4, "choice")) {
                ChoiceQuestion cq = (ChoiceQuestion) q;
                List<String> o = cq.getOptions();
                int hits = 0;
                for (String s : o) if (s.equalsIgnoreCase(cq.getCorrectAnswer())) hits++;
                List<String> lower = new ArrayList<>();
                for (String s : o) lower.add(s.toLowerCase());
                if (hits != 1 || o.size() != 4 || lower.stream().distinct().count() != 4) optionsOk = false;
            }
        }
        check("choice: 4 options, correct exactly once, no repeats (30 runs)", optionsOk);

        // T3: asking for too many gives only what exists
        check("count 100 gives only 4 verified QCat phrases", qs.createQuiz("QCat", 100, "TYPING").size() == 4);
        check("category is case-insensitive", qs.createQuiz("qcat", 2, "TYPING").size() == 2);
        check("ALL uses every verified phrase", qs.createQuiz("ALL", 100, "TYPING").size() == ps.getVerifiedPhrases().size());

        // T4: bad input
        check("unknown category throws", throwsInvalid(qs, "Nope", 3, "TYPING"));
        check("count 0 throws", throwsInvalid(qs, "QCat", 0, "TYPING"));
        check("bad mode throws", throwsInvalid(qs, "QCat", 3, "FLASH"));
        check("null mode throws", throwsInvalid(qs, "QCat", 3, null));

        // T5: answer checking (rule R9)
        Phrase thank = new Phrase(1, "p1", "Thank you", "t", "QCat", true);
        Question tq = new TypingQuestion(thank);
        check("'  THANK YOU. ' accepted", tq.checkAnswer("  THANK YOU. "));
        check("'thank   you' (extra spaces) accepted", tq.checkAnswer("thank   you"));
        check("wrong answer rejected", !tq.checkAnswer("Good morning"));
        check("null answer rejected", !tq.checkAnswer(null));
        Question cq = new ChoiceQuestion(thank, List.of("Thank you", "Hello", "Fire!", "Help me!"));
        check("choice accepts the correct option text", cq.checkAnswer("Thank you"));
        check("choice rejects a wrong option text", !cq.checkAnswer("Hello"));

        // T6: scoring
        List<Question> three = qs.createQuiz("QCat", 3, "TYPING");
        List<String> answers = new ArrayList<>();
        answers.add(three.get(0).getCorrectAnswer());   // right
        answers.add(three.get(1).getCorrectAnswer());   // right
        answers.add("totally wrong");                   // wrong
        QuizResult r = qs.evaluate(three, answers);
        check("score is 2 out of 3", r.getCorrect() == 2 && r.getTotal() == 3);
        check("single-category quiz keeps its category", r.getCategory().equals("QCat"));
        check("percentage is 66.67", Math.abs(r.getPercentage() - 66.666) < 0.01);

        // T7: mixed categories and bad evaluate input
        List<Question> mixed = qs.createQuiz("ALL", 100, "TYPING");
        List<String> blank = new ArrayList<>();
        for (int i = 0; i < mixed.size(); i++) blank.add("");
        check("mixed quiz is saved under ALL", qs.evaluate(mixed, blank).getCategory().equals("ALL"));
        check("answers/questions size mismatch throws", throwsEval(qs, three, new ArrayList<>()));
        check("empty quiz throws", throwsEval(qs, new ArrayList<>(), new ArrayList<>()));

        System.out.println(failed == 0 ? "\nALL TESTS PASSED" : "\n" + failed + " TEST(S) FAILED");
    }

    static boolean throwsInvalid(QuizService qs, String cat, int n, String mode) {
        try { qs.createQuiz(cat, n, mode); return false; }
        catch (InvalidInputException e) { return true; }
    }

    static boolean throwsEval(QuizService qs, List<Question> q, List<String> a) {
        try { qs.evaluate(q, a); return false; }
        catch (InvalidInputException e) { return true; }
    }
}
