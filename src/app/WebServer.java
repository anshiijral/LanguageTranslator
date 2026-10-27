package app;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import dao.InMemoryPhraseDao;
import dao.InMemoryResultDao;
import exception.DuplicatePhraseException;
import exception.InvalidInputException;
import exception.PhraseNotFoundException;
import model.CategoryStat;
import model.Phrase;
import model.QuizResult;
import service.ChoiceQuestion;
import service.Question;
import service.TypingQuestion;
import util.CsvUtil;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

// Lightweight HTTP server connecting the HTML/CSS/JS frontend to the Java PhraseApp backend.
public class WebServer {

    private static PhraseApp app;
    private static final Map<String, List<Question>> activeQuizzes = new ConcurrentHashMap<>();
    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        System.out.println("Starting Local Language Phrase Practice Web Server...");

        // Try MySQL first; fall back to in-memory if DB is not running.
        try {
            app = new PhraseApp();
            app.getAllPhrases();
            System.out.println("Connected to MySQL database.");
        } catch (Exception e) {
            System.out.println("MySQL unavailable (" + e.getMessage() + "). Initializing in-memory storage with 35 verified phrases.");
            InMemoryPhraseDao pDao = new InMemoryPhraseDao();
            InMemoryResultDao rDao = new InMemoryResultDao();
            seedFullDataset(pDao);
            app = new PhraseApp(pDao, rDao);
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.setExecutor(Executors.newFixedThreadPool(10));

        // API endpoints
        server.createContext("/api/phrases", new PhrasesHandler());
        server.createContext("/api/categories", new CategoriesHandler());
        server.createContext("/api/search", new SearchHandler());
        server.createContext("/api/verify", new VerifyHandler());
        server.createContext("/api/quiz/start", new QuizStartHandler());
        server.createContext("/api/quiz/submit", new QuizSubmitHandler());
        server.createContext("/api/progress", new ProgressHandler());
        server.createContext("/api/weakest", new WeakestHandler());
        server.createContext("/api/export", new ExportHandler());
        server.createContext("/api/import", new ImportHandler());

        // Static file handler (serves frontend)
        server.createContext("/", new StaticFileHandler());

        server.start();
        System.out.println("==================================================");
        System.out.println("  Web App running at: http://localhost:" + PORT + "/");
        System.out.println("  Backend is fully connected to the Frontend UI!");
        System.out.println("==================================================");
    }

    // Pre-loads all 35 verified phrases across 5 categories when running in-memory.
    private static void seedFullDataset(InMemoryPhraseDao dao) {
        String[][] seed = {
            // Greetings (7)
            {"नमस्ते", "Hello", "namaste", "Greetings"},
            {"आप कैसे हैं?", "How are you?", "aap kaise hain?", "Greetings"},
            {"मैं ठीक हूँ", "I am fine", "main theek hoon", "Greetings"},
            {"धन्यवाद", "Thank you", "dhanyavaad", "Greetings"},
            {"सुप्रभात", "Good morning", "suprabhaat", "Greetings"},
            {"शुभ रात्रि", "Good night", "shubh raatri", "Greetings"},
            {"आपका नाम क्या है?", "What is your name?", "aapka naam kya hai?", "Greetings"},
            // Travel (7)
            {"बस स्टॉप कहाँ है?", "Where is the bus stop?", "bus stop kahaan hai?", "Travel"},
            {"रेलवे स्टेशन कहाँ है?", "Where is the railway station?", "railway station kahaan hai?", "Travel"},
            {"सीधे जाइए", "Go straight", "seedhe jaaiye", "Travel"},
            {"दायें मुड़िए", "Turn right", "daayen mudiye", "Travel"},
            {"बायें मुड़िए", "Turn left", "baayen mudiye", "Travel"},
            {"कृपया रुकिए", "Please stop", "kripya rukiye", "Travel"},
            {"यह कहाँ है?", "Where is this?", "yah kahaan hai?", "Travel"},
            // Food (7)
            {"मुझे पानी चाहिए", "I need water", "mujhe paani chaahiye", "Food"},
            {"मुझे खाना चाहिए", "I want food", "mujhe khaana chaahiye", "Food"},
            {"कृपया चाय दीजिए", "Please give tea", "kripya chaay dijiye", "Food"},
            {"यह बहुत स्वादिष्ट है", "It is very tasty", "yah bahut swaadisht hai", "Food"},
            {"थोड़ा नमक चाहिए", "I need a little salt", "thoda namak chaahiye", "Food"},
            {"बिल दीजिए", "Please give the bill", "bill dijiye", "Food"},
            {"मुझे भूख लगी है", "I am hungry", "mujhe bhookh lagi hai", "Food"},
            // Shopping (7)
            {"यह कितने का है?", "How much is this?", "yah kitne ka hai?", "Shopping"},
            {"थोड़ा कम कीजिए", "Please reduce the price a little", "thoda kam kijiye", "Shopping"},
            {"मुझे यह नहीं चाहिए", "I do not want this", "mujhe yah nahin chaahiye", "Shopping"},
            {"दूसरा दिखाइए", "Show me another one", "doosra dikhaaiye", "Shopping"},
            {"थैला दीजिए", "Please give a bag", "thaila dijiye", "Shopping"},
            {"मुझे यह पसंद आया", "I liked this", "mujhe yah pasand aaya", "Shopping"},
            {"यह अच्छा है", "This is good", "yah achchha hai", "Shopping"},
            // Emergency (7)
            {"मदद कीजिए!", "Help me!", "madad kijiye!", "Emergency"},
            {"पुलिस को बुलाइए", "Call the police", "police ko bulaaiye", "Emergency"},
            {"डॉक्टर को बुलाइए", "Call a doctor", "doctor ko bulaaiye", "Emergency"},
            {"मेरी तबीयत ठीक नहीं है", "I am not well", "meri tabiyat theek nahin hai", "Emergency"},
            {"मैं रास्ता भूल गया हूँ", "I am lost", "main raasta bhool gaya hoon", "Emergency"},
            {"अस्पताल कहाँ है?", "Where is the hospital?", "aspataal kahaan hai?", "Emergency"},
            {"आग!", "Fire!", "aag!", "Emergency"}
        };

        // Clear pre-existing initial sample phrases before seeding the complete 35 phrases
        dao.clear();

        for (String[] row : seed) {
            dao.insert(new Phrase(0, row[0], row[1], row[2], row[3], true));
        }
    }

    // Handles GET, POST, PUT on /api/phrases
    static class PhrasesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCorsHeaders(ex);
            String method = ex.getRequestMethod();

            if ("OPTIONS".equalsIgnoreCase(method)) {
                sendResponse(ex, 204, "");
                return;
            }

            if ("GET".equalsIgnoreCase(method)) {
                List<Phrase> phrases = app.getAllPhrases();
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < phrases.size(); i++) {
                    if (i > 0) json.append(",");
                    json.append(phraseToJson(phrases.get(i)));
                }
                json.append("]");
                sendJson(ex, 200, json.toString());
            } else if ("POST".equalsIgnoreCase(method)) {
                String body = readBody(ex);
                Map<String, String> data = parseJsonSimple(body);
                try {
                    Phrase p = new Phrase(
                        data.getOrDefault("phrase", ""),
                        data.getOrDefault("translation", ""),
                        data.getOrDefault("transliteration", ""),
                        data.getOrDefault("category", "")
                    );
                    app.addPhrase(p);
                    sendJson(ex, 200, phraseToJson(p));
                } catch (InvalidInputException | DuplicatePhraseException e) {
                    sendJson(ex, 400, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
                }
            } else if ("PUT".equalsIgnoreCase(method)) {
                String body = readBody(ex);
                Map<String, String> data = parseJsonSimple(body);
                try {
                    int id = Integer.parseInt(data.getOrDefault("id", "0"));
                    Phrase p = new Phrase(
                        id,
                        data.getOrDefault("phrase", ""),
                        data.getOrDefault("translation", ""),
                        data.getOrDefault("transliteration", ""),
                        data.getOrDefault("category", ""),
                        false
                    );
                    app.correctPhrase(p);
                    sendJson(ex, 200, "{\"status\":\"ok\"}");
                } catch (InvalidInputException | PhraseNotFoundException | DuplicatePhraseException e) {
                    sendJson(ex, 400, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
                }
            } else {
                sendResponse(ex, 405, "Method Not Allowed");
            }
        }
    }

    // Handles GET on /api/categories
    static class CategoriesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCorsHeaders(ex);
            if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
                sendResponse(ex, 204, "");
                return;
            }
            List<String> cats = app.getCategories();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < cats.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append("\"").append(escapeJson(cats.get(i))).append("\"");
            }
            sb.append("]");
            sendJson(ex, 200, sb.toString());
        }
    }

    // Handles GET on /api/search?k=... or /api/search?c=...
    static class SearchHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCorsHeaders(ex);
            if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
                sendResponse(ex, 204, "");
                return;
            }
            String query = ex.getRequestURI().getQuery();
            Map<String, String> params = parseQuery(query);
            List<Phrase> list;
            try {
                if (params.containsKey("k")) {
                    list = app.searchByKeyword(params.get("k"));
                } else if (params.containsKey("c")) {
                    list = app.searchByCategory(params.get("c"));
                } else {
                    list = app.getAllPhrases();
                }
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    if (i > 0) json.append(",");
                    json.append(phraseToJson(list.get(i)));
                }
                json.append("]");
                sendJson(ex, 200, json.toString());
            } catch (InvalidInputException e) {
                sendJson(ex, 400, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    // Handles POST on /api/verify
    static class VerifyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCorsHeaders(ex);
            if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
                sendResponse(ex, 204, "");
                return;
            }
            if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
                Map<String, String> data = parseJsonSimple(readBody(ex));
                try {
                    int id = Integer.parseInt(data.getOrDefault("id", "0"));
                    boolean v = Boolean.parseBoolean(data.getOrDefault("verified", "false"));
                    app.setVerified(id, v);
                    sendJson(ex, 200, "{\"status\":\"ok\"}");
                } catch (Exception e) {
                    sendJson(ex, 400, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
                }
            } else {
                sendResponse(ex, 405, "Method Not Allowed");
            }
        }
    }

    // Handles POST on /api/quiz/start
    static class QuizStartHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCorsHeaders(ex);
            if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
                sendResponse(ex, 204, "");
                return;
            }
            if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
                Map<String, String> data = parseJsonSimple(readBody(ex));
                String cat = data.getOrDefault("category", "ALL");
                int count = Integer.parseInt(data.getOrDefault("count", "5"));
                String mode = data.getOrDefault("mode", "TYPING");

                try {
                    List<Question> questions = app.startQuiz(cat, count, mode);
                    String quizId = UUID.randomUUID().toString();
                    activeQuizzes.put(quizId, questions);

                    StringBuilder sb = new StringBuilder("{");
                    sb.append("\"quizId\":\"").append(quizId).append("\",");
                    sb.append("\"questions\":[");
                    for (int i = 0; i < questions.size(); i++) {
                        if (i > 0) sb.append(",");
                        Question q = questions.get(i);
                        sb.append("{");
                        sb.append("\"mode\":\"").append(escapeJson(mode)).append("\",");
                        sb.append("\"text\":\"").append(escapeJson(q.getText())).append("\",");
                        sb.append("\"category\":\"").append(escapeJson(q.getCategory())).append("\",");
                        sb.append("\"answer\":\"").append(escapeJson(q.getCorrectAnswer())).append("\"");
                        if (q instanceof ChoiceQuestion) {
                            ChoiceQuestion cq = (ChoiceQuestion) q;
                            sb.append(",\"options\":[");
                            List<String> opts = cq.getOptions();
                            for (int j = 0; j < opts.size(); j++) {
                                if (j > 0) sb.append(",");
                                sb.append("\"").append(escapeJson(opts.get(j))).append("\"");
                            }
                            sb.append("]");
                        }
                        sb.append("}");
                    }
                    sb.append("]}");
                    sendJson(ex, 200, sb.toString());
                } catch (InvalidInputException e) {
                    sendJson(ex, 400, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
                }
            } else {
                sendResponse(ex, 405, "Method Not Allowed");
            }
        }
    }

    // Handles POST on /api/quiz/submit
    static class QuizSubmitHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCorsHeaders(ex);
            if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
                sendResponse(ex, 204, "");
                return;
            }
            if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
                String body = readBody(ex);
                String quizId = extractJsonField(body, "quizId");
                List<String> answers = extractJsonStringArray(body, "answers");

                List<Question> questions = activeQuizzes.remove(quizId);
                if (questions == null) {
                    sendJson(ex, 400, "{\"error\":\"Quiz session expired or not found\"}");
                    return;
                }

                try {
                    QuizResult r = app.submitQuiz(questions, answers);
                    StringBuilder sb = new StringBuilder("{");
                    sb.append("\"correct\":").append(r.getCorrect()).append(",");
                    sb.append("\"total\":").append(r.getTotal()).append(",");
                    sb.append("\"percentage\":").append(String.format(Locale.US, "%.1f", r.getPercentage())).append(",");
                    sb.append("\"category\":\"").append(escapeJson(r.getCategory())).append("\"");
                    sb.append("}");
                    sendJson(ex, 200, sb.toString());
                } catch (InvalidInputException e) {
                    sendJson(ex, 400, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
                }
            } else {
                sendResponse(ex, 405, "Method Not Allowed");
            }
        }
    }

    // Handles GET on /api/progress
    static class ProgressHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCorsHeaders(ex);
            if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
                sendResponse(ex, 204, "");
                return;
            }
            List<CategoryStat> stats = app.getProgress();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < stats.size(); i++) {
                if (i > 0) sb.append(",");
                CategoryStat s = stats.get(i);
                sb.append("{");
                sb.append("\"category\":\"").append(escapeJson(s.getCategory())).append("\",");
                sb.append("\"quizzes\":").append(s.getQuizzes()).append(",");
                sb.append("\"correct\":").append(s.getCorrect()).append(",");
                sb.append("\"total\":").append(s.getTotal()).append(",");
                sb.append("\"accuracy\":").append(String.format(Locale.US, "%.1f", s.getAccuracy()));
                sb.append("}");
            }
            sb.append("]");
            sendJson(ex, 200, sb.toString());
        }
    }

    // Handles GET on /api/weakest
    static class WeakestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCorsHeaders(ex);
            if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
                sendResponse(ex, 204, "");
                return;
            }
            CategoryStat w = app.getWeakestCategory();
            if (w == null) {
                sendJson(ex, 200, "null");
            } else {
                String json = "{\"category\":\"" + escapeJson(w.getCategory()) + "\",\"accuracy\":"
                    + String.format(Locale.US, "%.1f", w.getAccuracy()) + "}";
                sendJson(ex, 200, json);
            }
        }
    }

    // Handles GET on /api/export
    static class ExportHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCorsHeaders(ex);
            File temp = File.createTempFile("phrases_export_", ".csv");
            try {
                app.exportCsv(temp.getAbsolutePath());
                byte[] bytes = Files.readAllBytes(temp.toPath());
                ex.getResponseHeaders().set("Content-Type", "text/csv; charset=UTF-8");
                ex.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"phrases.csv\"");
                ex.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = ex.getResponseBody()) {
                    os.write(bytes);
                }
            } finally {
                temp.delete();
            }
        }
    }

    // Handles POST on /api/import
    static class ImportHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCorsHeaders(ex);
            if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
                String csv = readBody(ex);
                File temp = File.createTempFile("phrases_import_", ".csv");
                try {
                    Files.writeString(temp.toPath(), csv, StandardCharsets.UTF_8);
                    int added = app.importCsv(temp.getAbsolutePath());
                    sendJson(ex, 200, "{\"added\":" + added + "}");
                } catch (InvalidInputException e) {
                    sendJson(ex, 400, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
                } finally {
                    temp.delete();
                }
            } else {
                sendResponse(ex, 405, "Method Not Allowed");
            }
        }
    }

    // Static file handler that serves HTML, CSS, and JS from the frontend folder.
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            String path = ex.getRequestURI().getPath();
            if (path == null || path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }

            File file = new File("frontend" + path);
            if (!file.exists() || file.isDirectory()) {
                sendResponse(ex, 404, "File Not Found");
                return;
            }

            String mime = "text/plain";
            if (path.endsWith(".html")) mime = "text/html; charset=UTF-8";
            else if (path.endsWith(".css")) mime = "text/css; charset=UTF-8";
            else if (path.endsWith(".js")) mime = "application/javascript; charset=UTF-8";
            else if (path.endsWith(".json")) mime = "application/json; charset=UTF-8";

            byte[] bytes = Files.readAllBytes(file.toPath());
            ex.getResponseHeaders().set("Content-Type", mime);
            addCorsHeaders(ex);
            ex.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = ex.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    // Utility: Formats a Phrase object as a JSON string.
    private static String phraseToJson(Phrase p) {
        return "{"
            + "\"id\":" + p.getId() + ","
            + "\"phrase\":\"" + escapeJson(p.getPhrase()) + "\","
            + "\"translation\":\"" + escapeJson(p.getTranslation()) + "\","
            + "\"transliteration\":\"" + escapeJson(p.getTransliteration()) + "\","
            + "\"category\":\"" + escapeJson(p.getCategory()) + "\","
            + "\"verified\":" + p.isVerified()
            + "}";
    }

    // Utility: Escapes quotes, backslashes, and control characters for JSON.
    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // Utility: Simple key-value JSON string parser.
    private static Map<String, String> parseJsonSimple(String body) {
        Map<String, String> map = new HashMap<>();
        if (body == null || body.trim().isEmpty()) return map;
        String content = body.trim();
        if (content.startsWith("{") && content.endsWith("}")) {
            content = content.substring(1, content.length() - 1);
        }
        // Match key: value pairs
        String[] pairs = content.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        for (String pair : pairs) {
            int colon = pair.indexOf(':');
            if (colon > 0) {
                String k = unquote(pair.substring(0, colon).trim());
                String v = unquote(pair.substring(colon + 1).trim());
                map.put(k, v);
            }
        }
        return map;
    }

    private static String extractJsonField(String body, String fieldName) {
        String target = "\"" + fieldName + "\"";
        int idx = body.indexOf(target);
        if (idx < 0) return "";
        int colon = body.indexOf(':', idx + target.length());
        if (colon < 0) return "";
        int startQuote = body.indexOf('"', colon);
        if (startQuote < 0) return "";
        int endQuote = body.indexOf('"', startQuote + 1);
        if (endQuote < 0) return "";
        return body.substring(startQuote + 1, endQuote);
    }

    private static List<String> extractJsonStringArray(String body, String arrayName) {
        List<String> list = new ArrayList<>();
        String target = "\"" + arrayName + "\"";
        int idx = body.indexOf(target);
        if (idx < 0) return list;
        int openBracket = body.indexOf('[', idx);
        int closeBracket = body.indexOf(']', openBracket);
        if (openBracket < 0 || closeBracket < 0) return list;
        String inner = body.substring(openBracket + 1, closeBracket);
        String[] parts = inner.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        for (String p : parts) {
            String u = unquote(p.trim());
            list.add(u);
        }
        return list;
    }

    private static String unquote(String s) {
        if (s == null) return "";
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            return s.substring(1, s.length() - 1).replace("\\\"", "\"").replace("\\\\", "\\");
        }
        return s;
    }

    private static Map<String, String> parseQuery(String q) {
        Map<String, String> map = new HashMap<>();
        if (q == null || q.isEmpty()) return map;
        for (String pair : q.split("&")) {
            int idx = pair.indexOf('=');
            if (idx > 0) {
                try {
                    String k = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                    String v = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                    map.put(k, v);
                } catch (Exception e) {}
            }
        }
        return map;
    }

    private static void addCorsHeaders(HttpExchange ex) {
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private static void sendJson(HttpExchange ex, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        addCorsHeaders(ex);
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void sendResponse(HttpExchange ex, int status, String text) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String readBody(HttpExchange ex) throws IOException {
        try (InputStream is = ex.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
