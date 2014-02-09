package com.playtech.wallet.service.statistics;

import com.playtech.wallet.domain.TimedMethodStatistics;
import com.playtech.wallet.repository.MethodStatisticsRepository;
import com.playtech.wallet.statistics.MethodExecutionInterceptionResult;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TimingAndParametersStatisticsPersisterTest {


    private MethodStatisticsRepository methodStatisticsRepositoryMock = new MethodStatisticsRepository() {

        private List<TimedMethodStatistics> db = new ArrayList<TimedMethodStatistics>();

        @Override
        public List<TimedMethodStatistics> findAll() {
            return db;
        }

        @Override
        public List<TimedMethodStatistics> findAll(Sort orders) {
            return null;
        }

        @Override
        public List<TimedMethodStatistics> findAll(Iterable<BigInteger> bigIntegers) {
            return null;
        }

        @Override
        public <S extends TimedMethodStatistics> List<S> save(Iterable<S> ses) {
            return null;
        }

        @Override
        public void flush() {

        }

        @Override
        public TimedMethodStatistics saveAndFlush(TimedMethodStatistics timedMethodStatistics) {
            return null;
        }

        @Override
        public void deleteInBatch(Iterable<TimedMethodStatistics> timedMethodStatisticses) {

        }

        @Override
        public void deleteAllInBatch() {

        }

        @Override
        public Page<TimedMethodStatistics> findAll(Pageable pageable) {
            return null;
        }

        @Override
        public <S extends TimedMethodStatistics> S save(S s) {
            db.add(s);
            return s;
        }

        @Override
        public TimedMethodStatistics findOne(BigInteger bigInteger) {
            return null;
        }

        @Override
        public boolean exists(BigInteger bigInteger) {
            return false;
        }

        @Override
        public long count() {
            return 0;
        }

        @Override
        public void delete(BigInteger bigInteger) {

        }

        @Override
        public void delete(TimedMethodStatistics timedMethodStatistics) {

        }

        @Override
        public void delete(Iterable<? extends TimedMethodStatistics> timedMethodStatisticses) {

        }

        @Override
        public void deleteAll() {

        }
    };

    private TimingAndParametersPerformanceMonitorAspect methodInterceptor =
                                        new TimingAndParametersPerformanceMonitorAspect();

    private TimingAndParametersStatisticsPersister timingAndParametersStatisticsPersister =
            new TimingAndParametersStatisticsPersister(methodStatisticsRepositoryMock, methodInterceptor);


    @Test
    public void testNotifyOnMethodExecution() throws Exception {

        String methodOneFullName = "Class1.methodOne";
        String methodTwoFullName = "Class2.methodTwo";
        Object [] methodParams = new Object[] {"a", "b","c"};
        Object methodResult = "abc";


        //first method: 1 = returns 10 ms
        timingAndParametersStatisticsPersister.notifyOnMethodExecution(
                new MethodExecutionInterceptionResult(
                        methodOneFullName,
                        methodParams,
                        methodResult,
                        TimeUnit.MILLISECONDS.toNanos(10)
                )
        );

        //first method: 2 = returns 5 ms
        timingAndParametersStatisticsPersister.notifyOnMethodExecution(
                new MethodExecutionInterceptionResult(
                        methodTwoFullName,
                        methodParams,
                        methodResult,
                        TimeUnit.MILLISECONDS.toNanos(5)
                )
        );

        //first method: 2 = returns 7 ms
        timingAndParametersStatisticsPersister.notifyOnMethodExecution(
                new MethodExecutionInterceptionResult(
                        methodTwoFullName,
                        methodParams,
                        methodResult,
                        TimeUnit.MILLISECONDS.toNanos(7)
                )
        );

        //save to db
        timingAndParametersStatisticsPersister.persistQueue();

        //test the result
        List<TimedMethodStatistics> results = methodStatisticsRepositoryMock.findAll();

        Assert.assertEquals(2, results.size());

        TimedMethodStatistics resultOne = results.get(0);

        Assert.assertEquals(methodOneFullName, resultOne.getMethodFullName());
        Assert.assertEquals(BigDecimal.valueOf(10.0), resultOne.getMinimumRequestTimeMs());
        Assert.assertEquals(BigDecimal.valueOf(10.0), resultOne.getMaximumRequestTimeMs());
        Assert.assertEquals(BigDecimal.valueOf(10.0), resultOne.getAverageRequestTimeMs());

        TimedMethodStatistics resultTwo = results.get(1);

        Assert.assertEquals(methodTwoFullName, resultTwo.getMethodFullName());
        Assert.assertEquals(BigDecimal.valueOf(5.0), resultTwo.getMinimumRequestTimeMs());
        Assert.assertEquals(BigDecimal.valueOf(7.0), resultTwo.getMaximumRequestTimeMs());
        Assert.assertEquals(BigDecimal.valueOf((5+7)/2.0), resultTwo.getAverageRequestTimeMs());

    }
}
