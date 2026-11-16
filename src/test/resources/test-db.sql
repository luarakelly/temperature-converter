CREATE TABLE measurement_category (
    category_code VARCHAR(30) NOT NULL PRIMARY KEY
);

CREATE TABLE measurement_unit (
    id INT AUTO_INCREMENT PRIMARY KEY,
    unit_key VARCHAR(40) NOT NULL UNIQUE,
    category_code VARCHAR(30) NOT NULL,
    factor_to_base DOUBLE NOT NULL,
    CONSTRAINT fk_unit_category FOREIGN KEY (category_code)
        REFERENCES measurement_category(category_code)
);

CREATE TABLE unit_translation (
    unit_id INT NOT NULL,
    language_code VARCHAR(5) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255) NOT NULL,
    PRIMARY KEY (unit_id, language_code),
    CONSTRAINT fk_translation_unit FOREIGN KEY (unit_id)
        REFERENCES measurement_unit(id) ON DELETE CASCADE
);

CREATE TABLE conversion_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    input_value DOUBLE NOT NULL,
    from_unit_id INT NOT NULL,
    to_unit_id INT NOT NULL,
    result_value DOUBLE NOT NULL,
    language_code VARCHAR(5) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_conversion_from FOREIGN KEY (from_unit_id)
        REFERENCES measurement_unit(id),
    CONSTRAINT fk_conversion_to FOREIGN KEY (to_unit_id)
        REFERENCES measurement_unit(id)
);

INSERT INTO measurement_category (category_code) VALUES ('temperature'), ('distance');

INSERT INTO measurement_unit (id, unit_key, category_code, factor_to_base) VALUES
    (1, 'celsius', 'temperature', 1),
    (2, 'fahrenheit', 'temperature', 1),
    (3, 'kilometer', 'distance', 1000),
    (4, 'mile', 'distance', 1609.344);

INSERT INTO unit_translation (unit_id, language_code, name, description) VALUES
    (1, 'en', 'Celsius', 'Temperature scale based on water.'),
    (1, 'ja', '摂氏', '水を基準とする温度単位です。'),
    (1, 'ar', 'درجة مئوية', 'مقياس حرارة يعتمد على الماء.'),
    (2, 'en', 'Fahrenheit', 'Temperature scale used in the United States.'),
    (2, 'ja', '華氏', '主に米国で使われる温度単位です。'),
    (3, 'en', 'kilometre', 'One thousand metres.'),
    (3, 'ja', 'キロメートル', '1000メートルです。');
