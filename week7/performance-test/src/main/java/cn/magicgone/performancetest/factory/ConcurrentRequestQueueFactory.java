package cn.magicgone.performancetest.factory;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

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


