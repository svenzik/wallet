package com.playtech.wallet.service.statistics;

import com.playtech.wallet.repository.MethodStatisticsRepository;
import com.playtech.wallet.statistics.MethodExecutionInterceptionResults;
import com.playtech.wallet.statistics.NotifyOnMethodExecution;
import com.playtech.wallet.domain.TimedMethodStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class StatisticsQueuePersister implements NotifyOnMethodExecution {

    @Autowired
    public StatisticsQueuePersister(MethodStatisticsRepository repository,
                                    TimingAndParametersPerformanceMonitorAspect methodInterceptor) {
        this.persistableSharedQueue = new ConcurrentLinkedQueue<MethodExecutionInterceptionResults>();
        this.repository = repository;
        methodInterceptor.registerForNotifications(this);
    }

    private static final Logger logger = LoggerFactory.getLogger(StatisticsQueuePersister.class);

    private final String NO_METHODS_EXECUTED = "NO_METHODS_EXECUTED";
    private final int SECONDS_TO_SLEEP = 60;

    private Queue<MethodExecutionInterceptionResults> persistableSharedQueue;

    /**
     * Set a concrete implementation of blocking queue. Default is LinkedBlockingQueue.
     * Queue is persisted by TODO: ...
     * @param persistableSharedQueue Thread safe Queue implementation
     */
    public void setSharedMessageQueue(Queue<MethodExecutionInterceptionResults> persistableSharedQueue) {
        this.persistableSharedQueue = persistableSharedQueue;
    }


    MethodStatisticsRepository repository;

    /**
     * This is started by Spring every SECONDS_TO_SLEEP
     * If you want it to wait for completion and then start counting time to repeat, then use fixedDelay
     */
    @Scheduled(fixedRate=SECONDS_TO_SLEEP*1000, initialDelay = SECONDS_TO_SLEEP*1000)
    public void persistQueue() {
        Collection<MethodExecutionInterceptionResults> c = getAllElementsFromPersistableSharedQueue(persistableSharedQueue);
        logger.info("Persisting statistical data with count: {}", c.size());
        TimedMethodStatistics timedMethodStatistics = calculateStatistics(c);
        repository.save(timedMethodStatistics);
    }

    /**
     * Get all collected statistics and clear list
     * @param persistableSharedQueue the list, must be threadsafe
     * @return List of collected method calls
     */
    private Collection<MethodExecutionInterceptionResults> getAllElementsFromPersistableSharedQueue(Queue<MethodExecutionInterceptionResults> persistableSharedQueue) {

        //TODO: check if NULL

        Collection<MethodExecutionInterceptionResults> result = new ArrayList<MethodExecutionInterceptionResults>();

        while(persistableSharedQueue.peek() != null) {
            result.add(persistableSharedQueue.poll());
        }

        return result;
    }

    /**
     * calculate statistcs from Collection of MethodExecutionInterceptionResults, basicly uses getMethodTimeInNanoseconds()
     * @param methodExecutionInterceptionResults List to use as base for calculations
     * @return statistics result
     */
    private TimedMethodStatistics calculateStatistics(Collection<MethodExecutionInterceptionResults> methodExecutionInterceptionResults) {

        Assert.notNull(methodExecutionInterceptionResults);

        if (methodExecutionInterceptionResults.size() == 0) {
            return new TimedMethodStatistics( NO_METHODS_EXECUTED,
                                                BigInteger.valueOf(SECONDS_TO_SLEEP),
                                                BigDecimal.valueOf(0),
                                                BigDecimal.valueOf(0),
                                                BigDecimal.valueOf(0));
        }

        long methodTimeInNanosecondsMinimum = Long.MAX_VALUE;
        long methodTimeInNanosecondsMaximum = Long.MIN_VALUE;
        long methodTimeInNanosecondsSum = 0;
        String methodAndClassFullName = null;

        for (MethodExecutionInterceptionResults parameter : methodExecutionInterceptionResults) {
            if (methodAndClassFullName == null) {
                methodAndClassFullName = parameter.getMethodAndClassFullName();
            }
            methodTimeInNanosecondsMinimum = Math.min(methodTimeInNanosecondsMinimum, parameter.getMethodTimeInNanoseconds());
            methodTimeInNanosecondsMaximum = Math.max(methodTimeInNanosecondsMaximum, parameter.getMethodTimeInNanoseconds());
            methodTimeInNanosecondsSum += parameter.getMethodTimeInNanoseconds();
        }

        return new TimedMethodStatistics( methodAndClassFullName,
                                    BigInteger.valueOf(SECONDS_TO_SLEEP),
                                    BigDecimal.valueOf(methodTimeInNanosecondsMinimum/1000000.0),
                                    BigDecimal.valueOf(methodTimeInNanosecondsMaximum/1000000.0),
                                    BigDecimal.valueOf(methodTimeInNanosecondsSum/ methodExecutionInterceptionResults.size()));
    }

    @Override
    public void notifyOnMethodExecution(MethodExecutionInterceptionResults methodExecutionInterceptionResults) {
        this.persistableSharedQueue.offer(methodExecutionInterceptionResults);
    }
}
