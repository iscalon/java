package org.iscalon.demo_batch.out.storedprocedure;

import java.util.Map;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.object.StoredProcedure;

public class ProcedureInitialisation extends StoredProcedure {

    public ProcedureInitialisation(JdbcTemplate jdbcTemplate, String procName) {
        super(jdbcTemplate, procName);
    }

    public Map<String, Object> execute() throws DataAccessException {
        return super.execute(Map.of());
    }
}
