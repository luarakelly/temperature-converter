CREATE DATABASE IF NOT EXISTS temperature_converter_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE temperature_converter_db;

CREATE TABLE IF NOT EXISTS measurement_category (
    category_code VARCHAR(30) CHARACTER SET utf8mb4 NOT NULL PRIMARY KEY
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS measurement_unit (
    id INT AUTO_INCREMENT PRIMARY KEY,
    unit_key VARCHAR(40) CHARACTER SET utf8mb4 NOT NULL UNIQUE,
    category_code VARCHAR(30) CHARACTER SET utf8mb4 NOT NULL,
    factor_to_base DOUBLE NOT NULL,
    CONSTRAINT fk_unit_category FOREIGN KEY (category_code)
        REFERENCES measurement_category(category_code)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS unit_translation (
    unit_id INT NOT NULL,
    language_code VARCHAR(5) CHARACTER SET ascii NOT NULL,
    name VARCHAR(100) CHARACTER SET utf8mb4 NOT NULL,
    description VARCHAR(255) CHARACTER SET utf8mb4 NOT NULL,
    PRIMARY KEY (unit_id, language_code),
    CONSTRAINT fk_translation_unit FOREIGN KEY (unit_id)
        REFERENCES measurement_unit(id) ON DELETE CASCADE
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS conversion_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    input_value DOUBLE NOT NULL,
    from_unit_id INT NOT NULL,
    to_unit_id INT NOT NULL,
    result_value DOUBLE NOT NULL,
    language_code VARCHAR(5) CHARACTER SET ascii NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_conversion_from FOREIGN KEY (from_unit_id)
        REFERENCES measurement_unit(id) ON DELETE RESTRICT,
    CONSTRAINT fk_conversion_to FOREIGN KEY (to_unit_id)
        REFERENCES measurement_unit(id) ON DELETE RESTRICT
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE temperature_converter_db;

INSERT INTO measurement_category (category_code) VALUES
    ('temperature'), ('speed'), ('distance'), ('time')
ON DUPLICATE KEY UPDATE category_code = VALUES(category_code);

INSERT INTO measurement_unit (unit_key, category_code, factor_to_base) VALUES
    ('celsius', 'temperature', 1),
    ('fahrenheit', 'temperature', 1),
    ('kelvin', 'temperature', 1),
    ('meter_per_second', 'speed', 1),
    ('kilometer_per_hour', 'speed', 0.2777777777777778),
    ('mile_per_hour', 'speed', 0.44704),
    ('meter', 'distance', 1),
    ('kilometer', 'distance', 1000),
    ('mile', 'distance', 1609.344),
    ('second', 'time', 1),
    ('minute', 'time', 60),
    ('hour', 'time', 3600)
ON DUPLICATE KEY UPDATE category_code = VALUES(category_code),
                        factor_to_base = VALUES(factor_to_base);

INSERT INTO unit_translation (unit_id, language_code, name, description)
SELECT id, 'en', name, description FROM (
    SELECT 'celsius' unit_key, 'Celsius' name, 'Temperature scale based on water freezing and boiling points.' description UNION ALL
    SELECT 'fahrenheit', 'Fahrenheit', 'Temperature scale commonly used in the United States.' UNION ALL
    SELECT 'kelvin', 'Kelvin', 'Absolute temperature scale beginning at absolute zero.' UNION ALL
    SELECT 'meter_per_second', 'metre per second', 'Speed of one metre travelled each second.' UNION ALL
    SELECT 'kilometer_per_hour', 'kilometre per hour', 'Speed of one kilometre travelled each hour.' UNION ALL
    SELECT 'mile_per_hour', 'mile per hour', 'Speed of one mile travelled each hour.' UNION ALL
    SELECT 'meter', 'metre', 'Base metric unit of length.' UNION ALL
    SELECT 'kilometer', 'kilometre', 'One thousand metres.' UNION ALL
    SELECT 'mile', 'mile', 'International mile equal to 1,609.344 metres.' UNION ALL
    SELECT 'second', 'second', 'Base SI unit of time.' UNION ALL
    SELECT 'minute', 'minute', 'A period of sixty seconds.' UNION ALL
    SELECT 'hour', 'hour', 'A period of sixty minutes.'
) translations JOIN measurement_unit u USING (unit_key)
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description);

INSERT INTO unit_translation (unit_id, language_code, name, description)
SELECT id, 'fi', name, description FROM (
    SELECT 'celsius' unit_key, 'Celsius' name, 'Lämpötila-asteikko, joka perustuu veden jäätymis- ja kiehumispisteisiin.' description UNION ALL
    SELECT 'fahrenheit', 'Fahrenheit', 'Lämpötila-asteikko, jota käytetään yleisesti Yhdysvalloissa.' UNION ALL
    SELECT 'kelvin', 'Kelvin', 'Absoluuttinen lämpötila-asteikko, joka alkaa absoluuttisesta nollapisteestä.' UNION ALL
    SELECT 'meter_per_second', 'metri sekunnissa', 'Nopeus, jolla kuljetaan yksi metri sekunnissa.' UNION ALL
    SELECT 'kilometer_per_hour', 'kilometri tunnissa', 'Nopeus, jolla kuljetaan yksi kilometri tunnissa.' UNION ALL
    SELECT 'mile_per_hour', 'maili tunnissa', 'Nopeus, jolla kuljetaan yksi maili tunnissa.' UNION ALL
    SELECT 'meter', 'metri', 'Pituuden metrijärjestelmän perusyksikkö.' UNION ALL
    SELECT 'kilometer', 'kilometri', 'Tuhat metriä.' UNION ALL
    SELECT 'mile', 'maili', 'Kansainvälinen maili on 1 609,344 metriä.' UNION ALL
    SELECT 'second', 'sekunti', 'Ajan SI-perusyksikkö.' UNION ALL
    SELECT 'minute', 'minuutti', 'Kuudenkymmenen sekunnin ajanjakso.' UNION ALL
    SELECT 'hour', 'tunti', 'Kuudenkymmenen minuutin ajanjakso.'
) translations JOIN measurement_unit u USING (unit_key)
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description);

INSERT INTO unit_translation (unit_id, language_code, name, description)
SELECT id, 'sv', name, description FROM (
    SELECT 'celsius' unit_key, 'Celsius' name, 'Temperaturskala baserad på vattnets fryspunkt och kokpunkt.' description UNION ALL
    SELECT 'fahrenheit', 'Fahrenheit', 'Temperaturskala som ofta används i USA.' UNION ALL
    SELECT 'kelvin', 'Kelvin', 'Absolut temperaturskala som börjar vid den absoluta nollpunkten.' UNION ALL
    SELECT 'meter_per_second', 'meter per sekund', 'Hastighet på en meter varje sekund.' UNION ALL
    SELECT 'kilometer_per_hour', 'kilometer per timme', 'Hastighet på en kilometer varje timme.' UNION ALL
    SELECT 'mile_per_hour', 'engelsk mil per timme', 'Hastighet på en engelsk mil varje timme.' UNION ALL
    SELECT 'meter', 'meter', 'Metriska grundmåttet för längd.' UNION ALL
    SELECT 'kilometer', 'kilometer', 'Tusen meter.' UNION ALL
    SELECT 'mile', 'engelsk mil', 'En internationell engelsk mil är 1 609,344 meter.' UNION ALL
    SELECT 'second', 'sekund', 'SI-grundenhet för tid.' UNION ALL
    SELECT 'minute', 'minut', 'En tidsperiod på sextio sekunder.' UNION ALL
    SELECT 'hour', 'timme', 'En tidsperiod på sextio minuter.'
) translations JOIN measurement_unit u USING (unit_key)
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description);

INSERT INTO unit_translation (unit_id, language_code, name, description)
SELECT id, 'ja', name, description FROM (
    SELECT 'celsius' unit_key, '摂氏' name, '水の凝固点と沸点を基準とする温度単位です。' description UNION ALL
    SELECT 'fahrenheit', '華氏', '主に米国で使われる温度単位です。' UNION ALL
    SELECT 'kelvin', 'ケルビン', '絶対零度を起点とする絶対温度単位です。' UNION ALL
    SELECT 'meter_per_second', 'メートル毎秒', '1秒間に1メートル進む速さです。' UNION ALL
    SELECT 'kilometer_per_hour', 'キロメートル毎時', '1時間に1キロメートル進む速さです。' UNION ALL
    SELECT 'mile_per_hour', 'マイル毎時', '1時間に1マイル進む速さです。' UNION ALL
    SELECT 'meter', 'メートル', '長さのメートル法の基本単位です。' UNION ALL
    SELECT 'kilometer', 'キロメートル', '1000メートルです。' UNION ALL
    SELECT 'mile', 'マイル', '国際マイルは1609.344メートルです。' UNION ALL
    SELECT 'second', '秒', '時間のSI基本単位です。' UNION ALL
    SELECT 'minute', '分', '60秒の時間単位です。' UNION ALL
    SELECT 'hour', '時間', '60分の時間単位です。'
) translations JOIN measurement_unit u USING (unit_key)
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description);

INSERT INTO unit_translation (unit_id, language_code, name, description)
SELECT id, 'ar', name, description FROM (
    SELECT 'celsius' unit_key, 'درجة مئوية' name, 'مقياس حرارة يعتمد على تجمد الماء وغليانه.' description UNION ALL
    SELECT 'fahrenheit', 'فهرنهايت', 'مقياس حرارة شائع الاستخدام في الولايات المتحدة.' UNION ALL
    SELECT 'kelvin', 'كلفن', 'مقياس حرارة مطلق يبدأ من الصفر المطلق.' UNION ALL
    SELECT 'meter_per_second', 'متر في الثانية', 'سرعة قطع متر واحد كل ثانية.' UNION ALL
    SELECT 'kilometer_per_hour', 'كيلومتر في الساعة', 'سرعة قطع كيلومتر واحد كل ساعة.' UNION ALL
    SELECT 'mile_per_hour', 'ميل في الساعة', 'سرعة قطع ميل واحد كل ساعة.' UNION ALL
    SELECT 'meter', 'متر', 'الوحدة الأساسية المترية للطول.' UNION ALL
    SELECT 'kilometer', 'كيلومتر', 'ألف متر.' UNION ALL
    SELECT 'mile', 'ميل', 'الميل الدولي يساوي 1609.344 مترًا.' UNION ALL
    SELECT 'second', 'ثانية', 'الوحدة الأساسية الدولية للزمن.' UNION ALL
    SELECT 'minute', 'دقيقة', 'فترة زمنية مدتها ستون ثانية.' UNION ALL
    SELECT 'hour', 'ساعة', 'فترة زمنية مدتها ستون دقيقة.'
) translations JOIN measurement_unit u USING (unit_key)
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description);


CREATE USER IF NOT EXISTS 'temperature_app'@'%' IDENTIFIED BY 'temperature_app';
ALTER USER 'temperature_app'@'%' IDENTIFIED BY 'temperature_app';
GRANT ALL PRIVILEGES ON temperature_converter_db.* TO 'temperature_app'@'%';
FLUSH PRIVILEGES;
