package com.nyc.boards.util;

public class GuidQueueUtil {

    private static final int QUEUE_SIZE = 10; // 큐 크기 설정    
    // GuidQueue 인스턴스 생성
    private final static GuidQueue guidQueue = GuidQueue.getInstance(QUEUE_SIZE);

    public static String processGUIDs() throws InterruptedException {
        // GuidQueue의 인스턴스를 직접 사용하여 GUID를 가져옴
        String GUID = guidQueue.getGUID(); // 1000은 큐 크기 설정
        System.out.println(Thread.currentThread().getName() + " GUID: " + GUID); // 현재 스레드 이름과 함께 생성된 GUID 출력
        return GUID;
    }
}
