package com.playtech.wallet.statistics;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
@Aspect
public class TimingAndParametersPerformanceMonitorAspect {

    private static final Logger logger = LoggerFactory.getLogger(TimingAndParametersPerformanceMonitorAspect.class);

    @Around("execution(* com.playtech.wallet.spring.controller.*.*(..))")
    public Object logControllerMessagesAndTiming(ProceedingJoinPoint joinPoint) throws Throwable {

        long startTime = System.nanoTime();

        Object retVal = joinPoint.proceed();

        long endTime = System.nanoTime();
        long methodCallDurationInNano = endTime - startTime;


        StringBuilder logMessage = new StringBuilder();

        try{
            logMessage.append(joinPoint.getTarget().getClass().getName());
            logMessage.append(".");
            logMessage.append(joinPoint.getSignature().getName());
            logMessage.append("(");
        }
        catch(Exception e){ logger.info(e.toString());}

        // append args
        Object[] args = joinPoint.getArgs();
        for (Object methodArgument : args) {
            logMessage.append(methodArgument).append(",");
        }
        if (args.length > 0) {
            logMessage.deleteCharAt(logMessage.length() - 1);
        }

        //append result data
        logMessage.append(") -> ");
        logMessage.append(retVal);
        logMessage.append(String.format(" in %s ms", methodCallDurationInNano/1000000.0));

        logger.info(logMessage.toString());

        return retVal;
    }

}