package com.nyc.boards;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.nyc.boards.util.GuidQueue;

@SpringBootApplication
public class BoardsApplication {

    private static final int QUEUE_SIZE = 10; // 큐 크기 설정
    public static final GuidQueue guidQueue = GuidQueue.getInstance(QUEUE_SIZE); // 싱글톤 GUID 큐

    public static void main(String[] args) {
        SpringApplication.run(BoardsApplication.class, args);
        startGuidProducerThread();
    }


    
    private static void startGuidProducerThread() {
        Thread producerThread = new Thread(() -> {
            try {
                guidQueue.makeGuidSeq(); // GUID 생산
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // 예외 발생 시 인터럽트 상태 복구
                System.err.println("GUID 생성 중 인터럽트 발생: " + e.getMessage());
            }
        });

        producerThread.setDaemon(true); // 애플리케이션 종료 시 자동 종료되도록 데몬 스레드 설정
        producerThread.start();
    }
}
