wallet
======

There are 3 components to this project:

1) COntroller and Wallet service
2) AOP TimingAndParametersPerformanceMonitorAspect that intercepts contoller methods and delegates 
method name, parameters, return data and time that took to process.
3) a: StatisticsQueuePersister persists statistica data every 60 seconds to DB 
including min,max,average time and method name. If a controller is not accessed, then no data is written.
Schechuling is handled by Spring <task:annotation-driven/>
   b: MethodInterseptionLogger - loggs method name, parameters, resturn data and time, it took to for a method to run
