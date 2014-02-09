package com.playtech.wallet.service.statistics;

import com.playtech.wallet.statistics.MethodExecutionInterceptionResult;
import com.playtech.wallet.statistics.NotifyOnMethodExecution;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
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

        MethodExecutionInterceptionResult methodExecutionInterceptionResult =
                new MethodExecutionInterceptionResult(methodFullName, joinPoint.getArgs(), retVal, methodCallDurationInNano);

        notifyOnMethodExecution(methodExecutionInterceptionResult);

        return retVal;
    }

    private void notifyOnMethodExecution(MethodExecutionInterceptionResult methodExecutionInterceptionResult) {
        for (NotifyOnMethodExecution notifyMe : notifyMeList) {
            notifyMe.notifyOnMethodExecution(methodExecutionInterceptionResult);
        }
    }

    public void registerForNotifications(NotifyOnMethodExecution objeNotifyOnMethodExecution) {
        notifyMeList.add(objeNotifyOnMethodExecution);
    }
}