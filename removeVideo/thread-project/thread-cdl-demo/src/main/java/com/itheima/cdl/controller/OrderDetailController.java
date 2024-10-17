package com.itheima.cdl.controller;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;


@RestController
@RequestMapping("/order_detail")
@Slf4j
public class OrderDetailController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ExecutorService executorService;


    @SneakyThrows
    @GetMapping("/get/detail_new/{id}")
    public Map<String, Object> getOrderDetailNew() {

        long startTime = System.currentTimeMillis();

        Future<Map<String, Object>> f1 = executorService.submit(() -> {
            Map<String, Object> r =
                    restTemplate.getForObject("http://localhost:9991/order/get/{id}", Map.class, 1);
            return r;
        });
        Future<Map<String, Object>> f2 = executorService.submit(() -> {
            Map<String, Object> r =
                    restTemplate.getForObject("http://localhost:9991/product/get/{id}", Map.class, 1);
            return r;
        });

        Future<Map<String, Object>> f3 = executorService.submit(() -> {
            Map<String, Object> r =
                    restTemplate.getForObject("http://localhost:9991/logistics/get/{id}", Map.class, 1);
            return r;
        });


        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("order", f1.get());
        resultMap.put("product", f2.get());
        resultMap.put("logistics", f3.get());

        long endTime = System.currentTimeMillis();

        log.info("接口调用共耗时:{}毫秒",endTime-startTime);
        return resultMap;
    }

    @SneakyThrows
    @GetMapping("/get/detail/{id}")
    public Map<String, Object> getOrderDetail() {

        long startTime = System.currentTimeMillis();

        Map<String, Object> order = restTemplate.getForObject("http://localhost:9991/order/get/{id}", Map.class, 1);

        Map<String, Object> product = restTemplate.getForObject("http://localhost:9991/product/get/{id}", Map.class, 1);

        Map<String, Object> logistics = restTemplate.getForObject("http://localhost:9991/logistics/get/{id}", Map.class, 1);

        long endTime = System.currentTimeMillis();



        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("order", order);
        resultMap.put("product", product);
        resultMap.put("logistics", logistics);

        log.info("接口调用共耗时:{}毫秒",endTime-startTime);
        return resultMap;
    }


}