package com.nyc.boards.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * GUID를 미리 생성하여 큐에 저장하고, 요청 시 제공하는 싱글톤 클래스입니다.
 * 생산자-소비자 패턴을 사용하여 GUID 생성과 사용을 분리합니다.
 */
@Slf4j
@Component
public class GuidQueue {

    /** Base 26 변환에 사용될 문자 집합 */
    // 0-9, A-P (26자)
    private static final char[] HEX_CHAR = "0123456789ABCDEFGHIJKLMNOP".toCharArray();

    /** Kubernetes Pod 환경에서 각 Pod를 고유하게 식별하기 위한 값 */
    private static final String POD_UNIQUE_IDENTIFIER = PodIdentifierGenerator.generate();

    /** 생성된 GUID를 저장하는 큐 */
    private final LinkedBlockingQueue<String> queue;

    /** GUID 시퀀스 번호를 생성하기 위한 원자적 카운터 */
    private static final AtomicLong atomicCounter = new AtomicLong(0);

    private static final int QUEUE_SIZE = 100; // 큐의 최대 크기
    private static final long COUNTER_RESET_THRESHOLD = 10_000_000L; // 시퀀스 카운터가 초기화되는 임계값
    private static final int BASE26_SEQUENCE_LENGTH = 5; // Base26으로 변환된 시퀀스 번호의 길이

    /** GUID를 생성하는 생산자 스레드를 관리하는 ExecutorService */
    private final ExecutorService producerExecutor;

    /**
     * 생성자: 큐와 ExecutorService를 초기화합니다.
     */
    public GuidQueue() {
        this.queue = new LinkedBlockingQueue<>(QUEUE_SIZE);
        this.producerExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "guid-producer");
            t.setDaemon(true);
            return t;
        });
    }

    @PostConstruct
    public void startGuidProducer() {
        producerExecutor.submit(() -> {
            try {
                produceGuids(); // GUID 생산 시작
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // 인터럽트 상태 복원
                log.warn("GUID 생성 스레드가 중단되었습니다.", e);
            }
        });
    }

    @PreDestroy
    public void shutdown() {
        producerExecutor.shutdownNow();
    }

    /**
     * 지속적으로 GUID를 생성하여 큐에 추가합니다.
     * 큐가 가득 차면 공간이 생길 때까지 대기합니다.
     * 
     * @throws InterruptedException 스레드가 대기 중 중단될 경우
     */
    public void produceGuids() throws InterruptedException {
        do {
            String guid = getPidSeqGUID();
            log.debug("produceGuids GUID: [{}]", guid);
            queue.put(guid); // 큐가 가득 차면 여기서 대기
        } while (true); // Loop indefinitely to keep the queue filled
    }

    /**
     * 프로세스 ID와 시퀀스 번호를 포함한 원본 GUID를 생성합니다.
     * 
     * @return PID와 시퀀스를 포함한 GUID
     */
    public String getPidSeqGUID() {
        return String.format("%s%s", POD_UNIQUE_IDENTIFIER, generateAtomicGUID());
    }

    /**
     * 원자적 연산을 통해 고유한 시퀀스 번호를 생성하고 Base 26으로 변환합니다.
     * 
     * @return Base 26으로 인코딩된 시퀀스 문자열
     */
    private static String generateAtomicGUID() {
        // 카운터 값을 1 증가시킴
        long currentValue = atomicCounter.incrementAndGet();
        // 카운터가 임계값에 도달하면 0으로 초기화
        if (currentValue >= COUNTER_RESET_THRESHOLD) {
            atomicCounter.set(0);
            currentValue = atomicCounter.incrementAndGet();
        }
        // 현재 값을 Base 26 형식으로 변환
        String base26Value = toBase26(currentValue);
        // 5자리로 맞추기 위해 왼쪽에 '0'을 채움
        return String.format("%" + BASE26_SEQUENCE_LENGTH + "s", base26Value).replace(' ', '0');
    }

    /**
     * 큐에서 GUID를 가져와 최종 형식으로 포맷팅하여 반환합니다.
     * 큐가 비어있으면 새로운 GUID가 생성될 때까지 대기합니다.
     * 
     * @return 최종 포맷팅된 32자리 GUID
     * @throws InterruptedException 스레드가 대기 중 중단될 경우
     */
    public String getGUID() throws InterruptedException { // 최종 30자리 GUID 반환
        // 큐에서 원본 GUID를 가져옴 (큐가 비어있으면 여기서 대기)
        String uniquePart = queue.take(); // POD_UNIQUE_IDENTIFIER(11) + 시퀀스(5) = 16자
        // 최종 GUID: [현재시간(14)] + [고유식별부분(16)] = 30자
        String finalGuid = getCurrentDate() + uniquePart;
        // 최종 GUID를 32자리로 포맷팅하여 반환
        return getFormatGUID(finalGuid);
    }

    /**
     * 현재 날짜와 시간을 "yyyyMMddHHmmss" 형식의 문자열로 반환합니다.
     * 
     * @return 포맷팅된 현재 날짜와 시간 문자열
     */
    private static String getCurrentDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    /**
     * GUID를 30자리로 포맷팅합니다.
     * 30자보다 길면 자르고, 짧으면 오른쪽에 '0'을 채웁니다.
     * 
     * @param rawGUID 원본 GUID 문자열
     * @return 30자리로 포맷팅된 GUID
     */
    private static String getFormatGUID(String rawGUID) {
        // 30자리가 안되면 오른쪽을 '0'으로 채워서 30자리로 만들고, 넘으면 30자리로 자름
        return rawGUID.length() >= 30 ? rawGUID.substring(0, 30) : String.format("%-30s", rawGUID).replace(' ', '0');
    }

    /**
     * long 타입의 숫자를 Base 26 문자열로 변환합니다.
     * 
     * @param currentValue 변환할 숫자
     * @return Base 26으로 변환된 문자열
     */
    public static String toBase26(long currentValue) {
        // 음수는 변환할 수 없음
        if (currentValue < 0) {
            throw new IllegalArgumentException("음수는 Base 26으로 변환할 수 없습니다.");
        }
        // 0은 "0"으로 처리
        if (currentValue == 0) {
            return "0";
        }

        StringBuilder result = new StringBuilder(BASE26_SEQUENCE_LENGTH);
        while (currentValue > 0) {
            result.insert(0, HEX_CHAR[(int) (currentValue % 26)]);
            currentValue /= 26;
        }
        return result.toString();
    }

    /**
     * Kubernetes 환경에서 Pod를 고유하게 식별하는 ID를 생성하는 내부 클래스.
     */
    private static class PodIdentifierGenerator {
        private static final int IDENTIFIER_LENGTH = 11; // 13 -> 11로 변경하여 총 GUID 길이를 30으로 맞춤

        public static String generate() {
            // 1. HOSTNAME 환경 변수 (Kubernetes에서 Pod 이름으로 자동 설정됨)
            String hostname = System.getenv("HOSTNAME");
            if (hostname == null || hostname.isEmpty()) {
                log.warn("HOSTNAME 환경 변수를 찾을 수 없습니다. 로컬 개발 환경으로 간주합니다.");
                hostname = "localhost";
            }

            // 2. 애플리케이션 시작 시 생성되는 랜덤 UUID
            String randomId = UUID.randomUUID().toString();

            // 3. 호스트명과 랜덤 ID를 조합하여 해시 생성 후 Base26으로 변환
            long hash = Math.abs((hostname + randomId).hashCode());
            String base26Hash = toBase26(hash);

            // 4. 13자리로 고정
            String identifier = (base26Hash + randomId).replace("-", ""); // 하이픈 제거
            if (identifier.length() > IDENTIFIER_LENGTH) {
                identifier = identifier.substring(0, IDENTIFIER_LENGTH);
            }
            return String.format("%-" + IDENTIFIER_LENGTH + "s", identifier).replace(' ', '0').toUpperCase();
        }
    }
}
