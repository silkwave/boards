package com.nyc.boards.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.nyc.boards.util.GuidQueueUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequiredArgsConstructor
public class HomeController {

    private final GuidQueueUtil guidQueueUtil;

    @GetMapping("/")
    public String index() throws InterruptedException {

        String guid = guidQueueUtil.getGUID();
        log.info("Request GUID for getAllBoards: [{}]", guid);        
                
        return "index";
    } 
    
}
