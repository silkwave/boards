package com.nyc.boards.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class GuidQueue {

    private static final String DEFAULT_CONTAINER_NAME = "DPS" + ProcessHandle.current().pid()
            + String.format("%d", Thread.currentThread().getId());

    private static final char[] HEX_CHAR = "0123456789ABCDEFGHIJKLMNOP".toCharArray();
    private final LinkedBlockingQueue<String> queue; // 인스턴스 변수로 변경
    private static final AtomicLong atomicCounter = new AtomicLong(0);

    private static volatile GuidQueue instance;

    private GuidQueue(int queueSize) {
        this.queue = new LinkedBlockingQueue<>(queueSize); // 생성자에서 큐 크기 설정
    }

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

    public void makeGuidSeq() throws InterruptedException {
        do {
            String guid = getPidSeqGUID();
            queue.put(guid);
        } while (queue.remainingCapacity() > 0);
        System.out.println("모든 GUID 생산 완료");
    }

    public String getPidSeqGUID() {
        return String.format("%s%s", DEFAULT_CONTAINER_NAME, generateAtomicGUID()).toUpperCase();
    }

    private static String generateAtomicGUID() {
        long currentValue = atomicCounter.incrementAndGet();
        if (currentValue >= 10000000) {
            atomicCounter.set(0);
            currentValue = atomicCounter.incrementAndGet();
        }
        return String.format("%5s", toBase26(currentValue));
    }

    public String getGUID() throws InterruptedException {
        String rawGUID = queue.take();
        return getFormatGUID(String.format("%s%s", getCurrentDate(), getFormatGUID(rawGUID).toUpperCase()));
    }

    private static String getCurrentDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    private static String getFormatGUID(String rawGUID) {
        return rawGUID.length() >= 32 ? rawGUID.substring(0, 32) : String.format("%-32s", rawGUID).replace(' ', '0');
    }

    public static String toBase26(long currentValue) {
        if (currentValue < 0) {
            throw new IllegalArgumentException("음수는 변환할 수 없습니다.");
        }
        if (currentValue == 0) {
            return "0";
        }

        StringBuilder result = new StringBuilder();
        while (currentValue > 0) {
            result.insert(0, HEX_CHAR[(int) (currentValue % 26)]);
            currentValue /= 26;
        }
        return result.toString();
    }
}
