CREATE DATABASE IF NOT EXISTS phrase_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE phrase_db;

CREATE TABLE IF NOT EXISTS phrases (
    id INT AUTO_INCREMENT PRIMARY KEY,
    phrase VARCHAR(200) NOT NULL,
    translation VARCHAR(200) NOT NULL,
    transliteration VARCHAR(200) NOT NULL DEFAULT '',
    category VARCHAR(50) NOT NULL,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE KEY uq_phrase (phrase)
);

CREATE TABLE IF NOT EXISTS quiz_results (
    id INT AUTO_INCREMENT PRIMARY KEY,
    quiz_date DATE NOT NULL,
    category VARCHAR(50) NOT NULL,
    correct INT NOT NULL,
    total INT NOT NULL
);

INSERT IGNORE INTO phrases
(phrase, translation, transliteration, category, verified)
VALUES
-- Greetings (7 phrases)
('नमस्ते', 'Hello', 'namaste', 'Greetings', TRUE),
('आप कैसे हैं?', 'How are you?', 'aap kaise hain?', 'Greetings', TRUE),
('मैं ठीक हूँ', 'I am fine', 'main theek hoon', 'Greetings', TRUE),
('धन्यवाद', 'Thank you', 'dhanyavaad', 'Greetings', TRUE),
('सुप्रभात', 'Good morning', 'suprabhaat', 'Greetings', TRUE),
('शुभ रात्रि', 'Good night', 'shubh raatri', 'Greetings', TRUE),
('आपका नाम क्या है?', 'What is your name?', 'aapka naam kya hai?', 'Greetings', TRUE),

-- Travel (7 phrases)
('बस स्टॉप कहाँ है?', 'Where is the bus stop?', 'bus stop kahaan hai?', 'Travel', TRUE),
('रेलवे स्टेशन कहाँ है?', 'Where is the railway station?', 'railway station kahaan hai?', 'Travel', TRUE),
('सीधे जाइए', 'Go straight', 'seedhe jaaiye', 'Travel', TRUE),
('दायें मुड़िए', 'Turn right', 'daayen mudiye', 'Travel', TRUE),
('बायें मुड़िए', 'Turn left', 'baayen mudiye', 'Travel', TRUE),
('कृपया रुकिए', 'Please stop', 'kripya rukiye', 'Travel', TRUE),
('यह कहाँ है?', 'Where is this?', 'yah kahaan hai?', 'Travel', TRUE),

-- Food (7 phrases)
('मुझे पानी चाहिए', 'I need water', 'mujhe paani chaahiye', 'Food', TRUE),
('मुझे खाना चाहिए', 'I want food', 'mujhe khaana chaahiye', 'Food', TRUE),
('कृपया चाय दीजिए', 'Please give tea', 'kripya chaay dijiye', 'Food', TRUE),
('यह बहुत स्वादिष्ट है', 'It is very tasty', 'yah bahut swaadisht hai', 'Food', TRUE),
('थोड़ा नमक चाहिए', 'I need a little salt', 'thoda namak chaahiye', 'Food', TRUE),
('बिल दीजिए', 'Please give the bill', 'bill dijiye', 'Food', TRUE),
('मुझे भूख लगी है', 'I am hungry', 'mujhe bhookh lagi hai', 'Food', TRUE),

-- Shopping (7 phrases)
('यह कितने का है?', 'How much is this?', 'yah kitne ka hai?', 'Shopping', TRUE),
('थोड़ा कम कीजिए', 'Please reduce the price a little', 'thoda kam kijiye', 'Shopping', TRUE),
('मुझे यह नहीं चाहिए', 'I do not want this', 'mujhe yah nahin chaahiye', 'Shopping', TRUE),
('दूसरा दिखाइए', 'Show me another one', 'doosra dikhaaiye', 'Shopping', TRUE),
('थैला दीजिए', 'Please give a bag', 'thaila dijiye', 'Shopping', TRUE),
('मुझे यह पसंद आया', 'I liked this', 'mujhe yah pasand aaya', 'Shopping', TRUE),
('यह अच्छा है', 'This is good', 'yah achchha hai', 'Shopping', TRUE),

-- Emergency (7 phrases)
('मदद कीजिए!', 'Help me!', 'madad kijiye!', 'Emergency', TRUE),
('पुलिस को बुलाइए', 'Call the police', 'police ko bulaaiye', 'Emergency', TRUE),
('डॉक्टर को बुलाइए', 'Call a doctor', 'doctor ko bulaaiye', 'Emergency', TRUE),
('मेरी तबीयत ठीक नहीं है', 'I am not well', 'meri tabiyat theek nahin hai', 'Emergency', TRUE),
('मैं रास्ता भूल गया हूँ', 'I am lost', 'main raasta bhool gaya hoon', 'Emergency', TRUE),
('अस्पताल कहाँ है?', 'Where is the hospital?', 'aspataal kahaan hai?', 'Emergency', TRUE),
('आग!', 'Fire!', 'aag!', 'Emergency', TRUE);
