package com.changeowl.artifactgatewayservice.controller;

import com.changeowl.artifactgatewayservice.service.TickerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ticker")
@RequiredArgsConstructor
public class TickerController {
    private  final TickerService tickerService;

    @GetMapping
    public Object getTicker() {
        return tickerService.getTicker();
    }
}
