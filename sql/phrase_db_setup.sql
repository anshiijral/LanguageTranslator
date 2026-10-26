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
('नमस्ते', 'Hello', 'Namaste', 'Greetings', TRUE),
('आप कैसे हैं?', 'How are you?', 'Aap kaise hain?', 'Greetings', TRUE),
('धन्यवाद', 'Thank you', 'Dhanyavaad', 'Greetings', TRUE),
('कृपया', 'Please', 'Kripya', 'Greetings', TRUE),
('माफ़ कीजिए', 'Excuse me / Sorry', 'Maaf kijiye', 'Greetings', TRUE),
('शुभ प्रभात', 'Good morning', 'Shubh prabhat', 'Greetings', TRUE),
('अलविदा', 'Goodbye', 'Alvida', 'Greetings', TRUE),

('स्टेशन कहाँ है?', 'Where is the station?', 'Station kahan hai?', 'Travel', TRUE),
('मुझे टिकट चाहिए।', 'I need a ticket.', 'Mujhe ticket chahiye.', 'Travel', TRUE),
('यह कितने बजे निकलती है?', 'What time does this leave?', 'Yeh kitne baje nikalti hai?', 'Travel', TRUE),
('मुझे रास्ता बताइए।', 'Please tell me the way.', 'Mujhe raasta bataiye.', 'Travel', TRUE),
('हवाई अड्डा कहाँ है?', 'Where is the airport?', 'Hawai adda kahan hai?', 'Travel', TRUE),
('कितना समय लगेगा?', 'How long will it take?', 'Kitna samay lagega?', 'Travel', TRUE),
('मुझे टैक्सी चाहिए।', 'I need a taxi.', 'Mujhe taxi chahiye.', 'Travel', TRUE),

('मुझे भूख लगी है।', 'I am hungry.', 'Mujhe bhookh lagi hai.', 'Food', TRUE),
('पानी दीजिए।', 'Please give me water.', 'Paani dijiye.', 'Food', TRUE),
('मेन्यू दीजिए।', 'Please give me the menu.', 'Menu dijiye.', 'Food', TRUE),
('यह स्वादिष्ट है।', 'This is delicious.', 'Yeh swadisht hai.', 'Food', TRUE),
('बिना मिर्च के', 'Without chilli', 'Bina mirch ke', 'Food', TRUE),
('बिल दीजिए।', 'Please bring the bill.', 'Bill dijiye.', 'Food', TRUE),
('मुझे शाकाहारी खाना चाहिए।', 'I want vegetarian food.', 'Mujhe shakahari khana chahiye.', 'Food', TRUE),

('इसकी कीमत क्या है?', 'What is its price?', 'Iski keemat kya hai?', 'Shopping', TRUE),
('यह बहुत महँगा है।', 'This is very expensive.', 'Yeh bahut mehenga hai.', 'Shopping', TRUE),
('कुछ सस्ता दिखाइए।', 'Show me something cheaper.', 'Kuch sasta dikhaiye.', 'Shopping', TRUE),
('क्या आप कार्ड लेते हैं?', 'Do you accept cards?', 'Kya aap card lete hain?', 'Shopping', TRUE),
('मुझे यह चाहिए।', 'I want this.', 'Mujhe yeh chahiye.', 'Shopping', TRUE),
('क्या छूट मिलेगी?', 'Can I get a discount?', 'Kya chhoot milegi?', 'Shopping', TRUE),
('दूसरा रंग है?', 'Is another colour available?', 'Doosra rang hai?', 'Shopping', TRUE),

('मदद कीजिए!', 'Help me!', 'Madad kijiye!', 'Emergency', TRUE),
('मुझे डॉक्टर चाहिए।', 'I need a doctor.', 'Mujhe doctor chahiye.', 'Emergency', TRUE),
('पुलिस को बुलाइए।', 'Call the police.', 'Police ko bulaiye.', 'Emergency', TRUE),
('मुझे अस्पताल ले चलिए।', 'Take me to the hospital.', 'Mujhe aspataal le chaliye.', 'Emergency', TRUE),
('मैं खो गया/गई हूँ।', 'I am lost.', 'Main kho gaya/gayi hoon.', 'Emergency', TRUE),
('यह आपातकाल है।', 'This is an emergency.', 'Yeh aapatkaal hai.', 'Emergency', TRUE),
('एम्बुलेंस बुलाइए।', 'Call an ambulance.', 'Ambulance bulaiye.', 'Emergency', TRUE);

