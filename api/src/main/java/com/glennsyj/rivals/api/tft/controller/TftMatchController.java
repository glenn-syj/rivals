package com.glennsyj.rivals.api.tft.controller;

import com.glennsyj.rivals.api.tft.service.TftMatchManager;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TftMatchController {

    private TftMatchManager tftMatchManager;

    public TftMatchController(TftMatchManager tftMatchManager) {
        this.tftMatchManager = tftMatchManager;
    }
}
