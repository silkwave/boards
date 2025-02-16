package com.nyc.boards.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.nyc.boards.util.GuidQueueUtil;

@Controller
public class HomeController {
    @GetMapping("/")
    public String index() throws InterruptedException {

        String GUID = GuidQueueUtil.processGUIDs(); // 3개의 GUID를 생성하여 출력

        System.out.println(Thread.currentThread().getName() + " GUID: " + GUID); // 현재 스레드 이름과 함께 생성된 GUID 출력      
              
        System.out.println("HomeController.index");
        return "index";
    } 
    
}
