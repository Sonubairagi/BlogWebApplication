package com.blogapp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/home")
public class HomeController {
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @GetMapping("/test")
    public String transaction() {
        logger.info("INFO: Transaction log is working fine!");
        logger.warn("WARN: Something to check in the transaction log!");
        logger.error("ERROR: Something went wrong in the error endpoint!");
        return " details logged!";
    }

}
