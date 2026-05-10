package org.iscalon.demo_batch.reader;

import org.iscalon.demo_batch.domain.UserWorkUnit;

import java.util.List;

public interface UserLoadingStrategy {

  String key();

  List<UserWorkUnit> load(int bucket, int bucketCount, int pageSize, String lastUserId);
}
