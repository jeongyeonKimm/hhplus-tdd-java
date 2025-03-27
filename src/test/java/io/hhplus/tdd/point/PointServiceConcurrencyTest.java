package io.hhplus.tdd.point;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest
class PointServiceConcurrencyTest {

    @Autowired
    private PointService pointService;

    @Autowired
    private UserPointRepository userPointRepository;

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    @DisplayName("충전/사용 이력이 없는 한 명의 유저가 100번 충전 요청을 하면 충전 금액 * 100 만큼의 포인트가 충전된다.")
    @Test
    void chargePoint_concurrently() throws InterruptedException {
        int threadCount = 100;
        long userId = 1L;
        long initialAmount = 0L;
        long chargeAmount = 1000L;
        userPointRepository.saveUserPoint(userId, initialAmount);

        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    pointService.chargePoint(userId, chargeAmount);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        UserPoint userPoint = userPointRepository.findUserPointById(userId);
        assertThat(userPoint.point()).isEqualTo(initialAmount + (chargeAmount * threadCount));

        List<PointHistory> histories = pointHistoryRepository.findPointHistoriesByUserId(userId);
        assertThat(histories).hasSize(threadCount);
    }

    @DisplayName("충전/사용 이력이 없는 한 명의 유저가 100번 사용 요청을 하면 사용 금액 * 100 만큼의 포인트가 차감된다.")
    @Test
    void usePoint_concurrently() throws InterruptedException {
        int threadCount = 100;
        long userId = 1L;
        long initialAmount = 300_000L;
        long useAmount = 1000L;
        userPointRepository.saveUserPoint(userId, initialAmount);

        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    pointService.usePoint(userId, useAmount);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        UserPoint userPoint = userPointRepository.findUserPointById(userId);
        assertThat(userPoint.point()).isEqualTo(initialAmount - (useAmount * threadCount));

        List<PointHistory> histories = pointHistoryRepository.findPointHistoriesByUserId(userId);
        assertThat(histories).hasSize(threadCount);
    }
}
