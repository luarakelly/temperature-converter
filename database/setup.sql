CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'temperature_app'@'localhost'
IDENTIFIED BY 'temperature_app';

ALTER USER 'temperature_app'@'localhost'
IDENTIFIED BY 'temperature_app';

GRANT ALL PRIVILEGES
ON temperature_converter_db.*
TO 'temperature_app'@'localhost';

FLUSH PRIVILEGES;

USE temperature_converter_db;

SOURCE database/schema.sql;
SOURCE database/data.sql;
