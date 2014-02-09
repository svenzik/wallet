wallet
======

There are 3 components to this project:

1. Controller and Wallet service
2. AOP `TimingAndParametersPerformanceMonitorAspect` that intercepts contoller methods and delegates 
method name, parameters, return data and time that took to process. Configuration: `<aop:aspectj-autoproxy/>`
3. 
   1. `TimingAndParametersStatisticsPersister` persists statistica data every 60 seconds to DB 
including min,max,average time and method name. If a controller is not accessed, then no data is written.
Scheduling is handled by Spring `<task:annotation-driven/>`
   2. `MethodInterseptionLogger` - loggs method name, parameters, resturn data and time, it took to for a method to run
