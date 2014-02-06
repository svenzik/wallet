package com.playtech.wallet.service.statistics;

import com.playtech.wallet.statistics.MethodExecutionInterceptionResults;
import com.playtech.wallet.statistics.NotifyOnMethodExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Logs method names, parameters, result objects and time
 */
@Component
public class MethodInterseptionLogger implements NotifyOnMethodExecution {

    private static final Logger logger = LoggerFactory.getLogger(MethodInterseptionLogger.class);

    @Autowired
    public MethodInterseptionLogger(TimingAndParametersPerformanceMonitorAspect methodInterceptor) {
        methodInterceptor.registerForNotifications(this);
    }

    @Override
    public void notifyOnMethodExecution(MethodExecutionInterceptionResults methodExecutionInterceptionResults) {
        logger.info(methodExecutionInterceptionResults.toString());
    }
}
