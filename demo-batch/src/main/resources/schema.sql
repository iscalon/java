CREATE TABLE IF NOT EXISTS source_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING'
);

CREATE TABLE IF NOT EXISTS calculation_result (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    source_data_id BIGINT NOT NULL,
    calculated_amount DECIMAL(19, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- H2 n'a pas de vraies procédures stockées SQL comme Oracle/PostgreSQL.
-- Pour le squelette local, on simule avec des ALIAS Java appelables via CALL.
CREATE ALIAS IF NOT EXISTS PROC_INIT_1 AS '
    String execute1() {
        return "Exécution procédure 1";
    }
';

CREATE ALIAS IF NOT EXISTS PROC_INIT_2 AS '
    String execute2() {
        return "Exécution procédure 2";
    }
';
