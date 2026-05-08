package org.iscalon.demo_batch.partition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.batch.core.partition.Partitioner;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.jdbc.core.JdbcTemplate;

public class UserPartitioner implements Partitioner {

    private final JdbcTemplate jdbcTemplate;

    public UserPartitioner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        List<Long> userIds = jdbcTemplate.queryForList("""
                SELECT DISTINCT user_id
                FROM source_data
                ORDER BY user_id
                """, Long.class);

        Map<String, ExecutionContext> partitions = new HashMap<>();

        for (Long userId : userIds) {
            ExecutionContext context = new ExecutionContext();
            context.putLong("userId", userId);
            partitions.put("user-partition-" + userId, context);
        }

        return partitions;
    }
}
