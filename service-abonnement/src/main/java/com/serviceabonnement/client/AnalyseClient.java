package com.serviceabonnement.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;

@FeignClient(name = "service-analyse", url = "${application.config.analyse-service-url}")
public interface AnalyseClient {

    @PostMapping("/api/v1/ingestion/subscriptions")
    void sendEvents(@RequestBody List<java.util.Map<String, Object>> events);
}
