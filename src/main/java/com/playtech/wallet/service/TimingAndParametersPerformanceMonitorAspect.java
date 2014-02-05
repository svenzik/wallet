package com.playtech.wallet.service;

import com.playtech.wallet.repository.MethodStatisticsRepository;
import com.playtech.wallet.statistics.MethodExecutionInterceptionResults;
import com.playtech.wallet.statistics.NotifyOnMethodExecution;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


/**
 * Catches all controller executions, fetches in/out variables and hands them registered handlers
 */
@Component
@Aspect
public class TimingAndParametersPerformanceMonitorAspect {

    private List<NotifyOnMethodExecution> notifyMeList = new ArrayList<NotifyOnMethodExecution>();

    @Around("execution(* com.playtech.wallet.spring.controller.*.*(..))")
    public Object logControllerMessagesAndTiming(ProceedingJoinPoint joinPoint) throws Throwable {

        long startTime = System.nanoTime();

        Object retVal = joinPoint.proceed();

        long endTime = System.nanoTime();
        long methodCallDurationInNano = endTime - startTime;

        String methodFullName = joinPoint.getTarget().getClass().getName()+"."+joinPoint.getSignature().getName();

        MethodExecutionInterceptionResults methodExecutionInterceptionResults =
                new MethodExecutionInterceptionResults(methodFullName, joinPoint.getArgs(), retVal, methodCallDurationInNano);

        notifyOnMethodExecution(methodExecutionInterceptionResults);

        return retVal;
    }

    private void notifyOnMethodExecution(MethodExecutionInterceptionResults methodExecutionInterceptionResults) {
        for (NotifyOnMethodExecution notifyMe : notifyMeList) {
            notifyMe.notifyOnMethodExecution(methodExecutionInterceptionResults);
        }
    }

    public void registerForNotifications(NotifyOnMethodExecution objeNotifyOnMethodExecution) {
        notifyMeList.add(objeNotifyOnMethodExecution);
    }
}