package cn.magicgone.performancetest.execute;

import cn.magicgone.performancetest.factory.Request;
import cn.magicgone.performancetest.factory.RequestQueue;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

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
