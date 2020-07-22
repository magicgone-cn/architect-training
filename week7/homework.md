# 性能测试小工具

## 主干流程

主要流程分为三部分

1. 构造请求队列
2. 执行请求队列
3. 生成测试报告

```java
public abstract class PerformanceTester {

    public PerformanceReport test(TestConfig testConfig){

        // 构造请求队列
        RequestQueueConfig requestQueueConfig = new RequestQueueConfig();
        requestQueueConfig.setUrl(testConfig.getUrl());
        requestQueueConfig.setLength(testConfig.getTotalTime());
        RequestQueue requestQueue = getRequestQueue(requestQueueConfig);

        // 执行请求队列
        List<Response> responseList = execute(requestQueue, testConfig.getConcurrentNumber());

        // 生成测试报告
        return report(responseList);

    }

    /**
     * 构造请求队列
     * @param requestQueueConfig
     * @return
     */
    protected abstract RequestQueue getRequestQueue(RequestQueueConfig requestQueueConfig);

    /**
     * 执行请求队列
     * @param requestQueue
     * @param concurrentNumber
     * @return
     */
    protected abstract List<Response> execute(RequestQueue requestQueue, int concurrentNumber);

    /**
     * 生成测试报告
     * @param responseList
     * @return
     */
    protected abstract PerformanceReport report(List<Response> responseList);
```

## 构造请求队列

使用ConcurrentLinkedQueue构造请求队列，保证线程安全

```java
public class ConcurrentRequestQueueFactory implements RequestQueueFactory {
    @Override
    public RequestQueue getRequestQueue(RequestQueueConfig requestQueueConfig) {
        String url = requestQueueConfig.getUrl();
        Integer length = requestQueueConfig.getLength();
        RequestQueue requestQueue = new RequestQueue() {
            private final Queue<Request> requestQueue = new ConcurrentLinkedQueue<>();

            @Override
            public void deposit(Request request) {
                requestQueue.offer(request);
            }

            @Override
            public Request withdraw() {
                return requestQueue.poll();
            }

            @Override
            public int size() {
                return requestQueue.size();
            }
        };
        for (int i = 0; i < length; i++) {
            Request request = new Request();
            request.setUrl(url);
            requestQueue.deposit(request);
        }

        return requestQueue;
    }
}
```

## 执行请求队列

多线程并行执行，保证请求并发数符合要求

```java
@AllArgsConstructor
public class RequestQueueExecutorImpl implements RequestQueueExecutor {

    private final RequestExecutor requestExecutor;

    @Override
    public List<Response> execute(RequestQueue requestQueue, int concurrentNumber) {
        int size = requestQueue.size();
        List<Response> responseList = new ArrayList<>();

        for (int i = 0; i < concurrentNumber; i++) {
            Thread thread = new Thread(() -> {
                while (true) {
                    Request request = requestQueue.withdraw();
                    if (request == null) {
                        break;
                    }
                    Response response = requestExecutor.execute(request);
                    synchronized (responseList){
                        responseList.add(response);
                    }
                }
            },"requestExecutorThread_"+i);
            thread.start();
        }

        while(true){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if(responseList.size() == size){
                return responseList;
            }
        }

    }
}
```

## 生成测试报告

```java
public class PerformanceReporterImpl implements PerformanceReporter {
    @Override
    public PerformanceReport report(List<Response> responseList) {
        PerformanceReport performanceReport = new PerformanceReport();
        performanceReport.setResponseList(responseList);

        // 计算平均响应时长
        Long sum = 0L;
        for(Response response : responseList){
            sum += response.getResponseTime();
        }
        performanceReport.setAverageResponseTime( (float) sum / responseList.size() );

        // 计算95%响应时长
        responseList.sort(Comparator.comparingInt(Response::getResponseTime));
        int pointer = (int) (responseList.size()*0.95);
        performanceReport.setPercent95ResponseTime(responseList.get(pointer).getResponseTime());

        return performanceReport;
    }
}
```

## 测试结果

```java
@SpringBootTest
@Slf4j
class PerformanceTesterTest {

    @Autowired
    private PerformanceTester performanceTester;

    @Test
    void test1() {
        TestConfig testConfig = new TestConfig();
        testConfig.setUrl("https://www.baidu.com");
        testConfig.setTotalTime(1000);
        testConfig.setConcurrentNumber(10);
        PerformanceReport performanceReport = performanceTester.test(testConfig);
        log.info("平均响应时间:{}",performanceReport.getAverageResponseTime());
        log.info("95%响应时间:{}",performanceReport.getPercent95ResponseTime());
    }
}
```

```bash
2020-07-22 21:31:19.330  INFO 44488 --- [           main] c.m.p.PerformanceTesterTest              : 平均响应时间:85.901
2020-07-22 21:31:19.338  INFO 44488 --- [           main] c.m.p.PerformanceTesterTest              : 95%响应时间:129
```

