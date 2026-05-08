package org.iscalon.demo_batch.partition;

import java.util.HashMap;
import java.util.Map;
import org.springframework.batch.core.partition.Partitioner;
import org.springframework.batch.infrastructure.item.ExecutionContext;

public class UserPartitioner implements Partitioner {

    private final int partitionCount;

    public UserPartitioner(int partitionCount) {
        this.partitionCount = partitionCount;
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> partitions = new HashMap<>();

        for (int bucket = 0; bucket < partitionCount; bucket++) {
            ExecutionContext context = new ExecutionContext();
            context.putInt("bucket", bucket);
            context.putInt("bucketCount", partitionCount);

            partitions.put("partition-" + bucket, context);
        }

        return partitions;
    }
}
