CREATE TABLE IF NOT EXISTS word (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 number BIGINT NOT NULL UNIQUE,
 text TEXT NOT NULL
);
