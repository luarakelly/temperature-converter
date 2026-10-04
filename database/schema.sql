CREATE DATABASE IF NOT EXISTS temperature_converter_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE temperature_converter_db;

CREATE TABLE IF NOT EXISTS temperature_unit (
id INT AUTO_INCREMENT PRIMARY KEY,
unit_name VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS temperature_record (
id INT AUTO_INCREMENT PRIMARY KEY,
input_value DOUBLE NOT NULL,
celsius DOUBLE NOT NULL,
fahrenheit DOUBLE NOT NULL,
kelvin DOUBLE NOT NULL,
input_unit_id INT NOT NULL,
created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

CONSTRAINT fk_temperature_unit
    FOREIGN KEY (input_unit_id)
    REFERENCES temperature_unit(id)
    ON DELETE RESTRICT


);