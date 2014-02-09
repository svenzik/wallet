package com.playtech.wallet.service.statistics;

import com.playtech.wallet.repository.MethodStatisticsRepository;
import com.playtech.wallet.statistics.MethodExecutionInterceptionResult;
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class TimingAndParametersStatisticsPersister implements NotifyOnMethodExecution {

    @Autowired
    public TimingAndParametersStatisticsPersister(MethodStatisticsRepository repository,
                                                  TimingAndParametersPerformanceMonitorAspect methodInterceptor) {
        this.repository = repository;
        methodInterceptor.registerForNotifications(this);
    }

    private static final Logger logger = LoggerFactory.getLogger(TimingAndParametersStatisticsPersister.class);

    private final String NO_METHODS_EXECUTED = "NO_METHODS_EXECUTED";
    private final int SECONDS_TO_SLEEP = 60;

    private MethodStatisticsRepository repository;

    private Map<String, Queue<MethodExecutionInterceptionResult>> methodNameBasedMap =
            new ConcurrentHashMap<String, Queue<MethodExecutionInterceptionResult>>();


    /**
     * This is started by Spring every SECONDS_TO_SLEEP
     * If you want it to wait for completion and then start counting time to repeat, then use fixedDelay
     */
    @Scheduled(fixedRate=SECONDS_TO_SLEEP*1000, initialDelay = SECONDS_TO_SLEEP*1000)
    public void persistQueue() {

        for (Map.Entry<String, Queue<MethodExecutionInterceptionResult>> methodStatistics : methodNameBasedMap.entrySet()) {
            String methodName = methodStatistics.getKey();

            Collection<MethodExecutionInterceptionResult> c =
                    getAllElementsFromPersistableSharedQueue(methodStatistics.getValue());

            logger.debug("Persisting statistical data for {} with count: {}", methodName, c.size());
            TimedMethodStatistics timedMethodStatistics = calculateStatistics(c);

            repository.save(timedMethodStatistics);
        }

    }

    @Override
    public void notifyOnMethodExecution(MethodExecutionInterceptionResult methodExecutionInterceptionResult) {

        String key = methodExecutionInterceptionResult.getMethodAndClassFullName();

        if (!methodNameBasedMap.containsKey(key)) {
            methodNameBasedMap.put(key, new ConcurrentLinkedQueue<MethodExecutionInterceptionResult>());
        }
        methodNameBasedMap.get(key).offer(methodExecutionInterceptionResult);
    }


    /**
     * Get all collected statistics and clear list
     * @param persistableSharedQueue the list, must be threadsafe
     * @return List of collected method calls
     */
    private Collection<MethodExecutionInterceptionResult> getAllElementsFromPersistableSharedQueue(Queue<MethodExecutionInterceptionResult> persistableSharedQueue) {

        if (persistableSharedQueue == null) {
            throw new IllegalArgumentException("Method name based queue persistableSharedQueue cannot be null");
        }

        Collection<MethodExecutionInterceptionResult> result = new ArrayList<MethodExecutionInterceptionResult>();

        while(persistableSharedQueue.peek() != null) {
            result.add(persistableSharedQueue.poll());
        }

        return result;
    }

    /**
     * calculate statistcs from Collection of MethodExecutionInterceptionResult,
     * basicly uses getMethodTimeInNanoseconds()
     * It is assumed that this are only one method based results, name is taken from head of collection
     * @param methodExecutionInterceptionResults List to use as base for calculations
     * @return statistics result
     */
    private TimedMethodStatistics calculateStatistics(Collection<MethodExecutionInterceptionResult> methodExecutionInterceptionResults) {

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

        for (MethodExecutionInterceptionResult parameter : methodExecutionInterceptionResults) {
            if (methodAndClassFullName == null) {
                methodAndClassFullName = parameter.getMethodAndClassFullName();
            }
            methodTimeInNanosecondsMinimum = Math.min(methodTimeInNanosecondsMinimum, parameter.getMethodTimeInNanoseconds());
            methodTimeInNanosecondsMaximum = Math.max(methodTimeInNanosecondsMaximum, parameter.getMethodTimeInNanoseconds());
            methodTimeInNanosecondsSum += parameter.getMethodTimeInNanoseconds();
        }

        long average = methodTimeInNanosecondsSum/methodExecutionInterceptionResults.size();
        //convert all to milliseconds
        return new TimedMethodStatistics(
                        methodAndClassFullName,
                        BigInteger.valueOf(SECONDS_TO_SLEEP),
                        BigDecimal.valueOf(methodTimeInNanosecondsMinimum/1000000.0),
                        BigDecimal.valueOf(methodTimeInNanosecondsMaximum/1000000.0),
                        BigDecimal.valueOf(average/1000000.0)
               );
    }

}
