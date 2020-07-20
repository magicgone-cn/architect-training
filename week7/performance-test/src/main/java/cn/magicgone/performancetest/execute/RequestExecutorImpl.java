package cn.magicgone.performancetest.execute;

import cn.magicgone.performancetest.factory.Request;
import org.springframework.web.client.RestTemplate;

import java.util.Date;

public class RequestExecutorImpl implements RequestExecutor {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public Response execute(Request request) {

        String url = request.getUrl();
        Date startTime = new Date();
        restTemplate.getForObject(url, String.class);
        Date endTime = new Date();
        Integer responseTime = Math.toIntExact(endTime.getTime() - startTime.getTime());

        Response response = new Response();
        response.setUrl(url);
        response.setStartTime(startTime);
        response.setEndTime(endTime);
        response.setResponseTime(responseTime);

        return response;
    }
}
