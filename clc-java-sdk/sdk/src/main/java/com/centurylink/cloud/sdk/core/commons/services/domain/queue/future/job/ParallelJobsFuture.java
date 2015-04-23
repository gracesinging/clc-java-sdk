package com.centurylink.cloud.sdk.core.commons.services.domain.queue.future.job;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.centurylink.cloud.sdk.core.services.function.Streams.map;
import static com.google.common.collect.Iterables.toArray;
import static java.util.Arrays.asList;

/**
 * @author Ilya Drabenia
 */
public class ParallelJobsFuture implements JobFuture {
    private final List<JobFuture> jobs;

    public ParallelJobsFuture(JobFuture... jobs) {
        this.jobs = asList(jobs);
    }

    public ParallelJobsFuture(List<JobFuture> jobs) {
        this.jobs = new ArrayList<>(jobs);
    }

    @Override
    public void waitUntilComplete() {
        jobs.forEach(JobFuture::waitUntilComplete);
    }

    @Override
    public void waitUntilComplete(Duration timeout) {
        jobs.forEach(j -> j.waitUntilComplete(timeout));
    }

    @Override
    public CompletableFuture<Void> waitAsync() {
        return CompletableFuture.allOf(toArray(
            map(jobs, JobFuture::waitAsync),
            CompletableFuture.class
        ));
    }

}
