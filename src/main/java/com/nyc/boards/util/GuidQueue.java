package com.nyc.boards.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class GuidQueue {

    // 기본 컨테이너 이름 (현재 프로세스 ID와 스레드 ID 포함)
    private static final String DEFAULT_CONTAINER_NAME = "DPS:" + ProcessHandle.current().pid()
            + String.format(":%d:", Thread.currentThread().getId());

    // GUID 생성에 사용할 문자 집합
    private static final char[] HEX_CHAR = "0123456789ABCDEFGHIJKLMNOP".toCharArray();

    // GUID를 저장할 큐
    private final LinkedBlockingQueue<String> queue;

    // GUID 생성 카운터 (원자적으로 증가)
    private static final AtomicLong atomicCounter = new AtomicLong(0);

    // Singleton 인스턴스
    private static volatile GuidQueue instance;

    /**
     * 생성자: 큐 크기를 지정하여 인스턴스를 초기화
     * 
     * @param queueSize 큐의 크기
     */
    private GuidQueue(int queueSize) {
        this.queue = new LinkedBlockingQueue<>(queueSize);
    }

    /**
     * Singleton 인스턴스를 반환하는 메소드
     * 
     * @param queueSize 큐의 크기
     * @return GuidQueue 인스턴스
     */
    public static GuidQueue getInstance(int queueSize) {
        if (instance == null) {
            synchronized (GuidQueue.class) {
                if (instance == null) {
                    instance = new GuidQueue(queueSize);
                }
            }
        }
        return instance;
    }

    /**
     * GUID를 생산하여 큐에 넣는 메소드
     * 큐가 가득 차지 않도록 GUID를 계속 생성
     * 
     * @throws InterruptedException 큐 작업이 중단될 경우
     */
    public void makeGuidSeq() throws InterruptedException {
        do {
            // PID와 시퀀스를 포함한 GUID 생성
            String guid = getPidSeqGUID();
            // 생성된 GUID를 큐에 추가
            queue.put(guid);
            // } while (queue.remainingCapacity() > 0); // 큐가 가득 차지 않을 때까지 반복
        } while (true); // 큐가 가득 차지 않을 때까지 반복
    }

    /**
     * PID와 시퀀스를 포함한 GUID를 생성
     * 
     * @return PID와 시퀀스를 포함한 GUID
     */
    public String getPidSeqGUID() {
        return String.format("%s%s", DEFAULT_CONTAINER_NAME, generateAtomicGUID()).toUpperCase();
    }

    /**
     * 원자적인 방식으로 GUID 시퀀스를 생성
     * 
     * @return 시퀀스를 포함한 GUID
     */
    private static String generateAtomicGUID() {
        // 카운터 값 증가
        long currentValue = atomicCounter.incrementAndGet();
        // 카운터가 10000000에 도달하면 다시 0으로 초기화
        if (currentValue >= 10000000) {
            atomicCounter.set(0);
            currentValue = atomicCounter.incrementAndGet();
        }
        // 값을 Base 26 형식으로 변환
        return String.format("%5s", toBase26(currentValue));
    }

    /**
     * 큐에서 GUID를 꺼내어 포맷을 지정하여 반환
     * 
     * @return 포맷된 GUID
     * @throws InterruptedException 큐 작업이 중단될 경우
     */
    public String getGUID() throws InterruptedException {
        // 큐에서 GUID를 꺼냄
        String rawGUID = queue.take();
        // 현재 날짜와 포맷된 GUID를 결합하여 반환
        return getFormatGUID(String.format("%s%s", getCurrentDate(), getFormatGUID(rawGUID).toUpperCase()));
    }

    /**
     * 현재 날짜와 시간을 "yyyyMMddHHmmss" 형식으로 반환
     * 
     * @return 현재 날짜와 시간
     */
    private static String getCurrentDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    /**
     * GUID의 형식을 맞춰주는 메소드
     * 32자리로 맞추고, 부족하면 0으로 채움
     * 
     * @param rawGUID 원본 GUID
     * @return 포맷된 GUID
     */
    private static String getFormatGUID(String rawGUID) {
        // 32자리 미만이면 공백을 0으로 채워서 32자리로 반환
        return rawGUID.length() >= 32 ? rawGUID.substring(0, 32) : String.format("%-32s", rawGUID).replace(' ', '0');
    }

    /**
     * 숫자를 Base 26 형식으로 변환하는 메소드
     * 
     * @param currentValue 변환할 숫자
     * @return Base 26 형식으로 변환된 문자열
     */
    public static String toBase26(long currentValue) {
        // 음수는 처리할 수 없음
        if (currentValue < 0) {
            throw new IllegalArgumentException("음수는 변환할 수 없습니다.");
        }
        // 값이 0일 경우 "0" 반환
        if (currentValue == 0) {
            return "0";
        }

        // Base 26 변환 과정
        StringBuilder result = new StringBuilder();
        while (currentValue > 0) {
            result.insert(0, HEX_CHAR[(int) (currentValue % 26)]);
            currentValue /= 26;
        }
        return result.toString();
    }
}
