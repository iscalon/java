CREATE TABLE IF NOT EXISTS source_data (
    v_ref BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING'
);

CREATE TABLE IF NOT EXISTS calculated_result (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    v_ref BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    calculated_amount DECIMAL(19, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_result_input
    FOREIGN KEY (v_ref)
    REFERENCES source_data(v_ref)
);

-- H2 n'a pas de vraies procédures stockées SQL comme Oracle/PostgreSQL.
-- Pour le squelette local, on simule avec des ALIAS Java appelables via CALL.
CREATE ALIAS IF NOT EXISTS PROC_INIT_1 AS '
    String execute1() {
        System.out.println("Exécution procédure 1"); return "Exécution procédure 1";
    }
';

CREATE ALIAS IF NOT EXISTS PROC_INIT_2 AS '
    String execute2() {
        System.out.println("Exécution procédure 2"); return "Exécution procédure 2";
    }
';
