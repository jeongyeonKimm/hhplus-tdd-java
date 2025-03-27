package io.hhplus.tdd.point;

import io.hhplus.tdd.exception.InsufficientPointException;
import io.hhplus.tdd.exception.PointLimitExceededException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest
class PointServiceConcurrencyTest {

    @Autowired
    private PointService pointService;

    @Autowired
    private UserPointRepository userPointRepository;

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    @DisplayName("충전/사용 이력이 없는 한 명의 유저가 30번 충전 요청을 하면 충전 금액 * 30 만큼의 포인트가 충전된다.")
    @Test
    void chargePoint_concurrently() throws InterruptedException {
        int threadCount = 30;
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

    @DisplayName("충전/사용 내역이 없고 900_000 포인트를 보유 중인 한 명의 유저가 10_000씩 30번 충전 요청을 하면 10번만 충전이 되고 20번은 PointLimitExceededException이 발생한다.")
    @Test
    void chargePoint_concurrently_exceedMax() throws InterruptedException {
        int threadCount = 30;
        long userId = 1L;
        long initialAmount = 900_000L;
        long chargeAmount = 10_000L;
        userPointRepository.saveUserPoint(userId, initialAmount);

        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    pointService.chargePoint(userId, chargeAmount);
                    successCount.incrementAndGet();
                } catch (PointLimitExceededException e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        UserPoint userPoint = userPointRepository.findUserPointById(userId);
        assertAll(
                () -> assertThat(userPoint.point()).isEqualTo(1_000_000L),
                () -> assertThat(successCount.get()).isEqualTo(10),
                () -> assertThat(failCount.get()).isEqualTo(20)
        );

        List<PointHistory> histories = pointHistoryRepository.findPointHistoriesByUserId(userId);
        assertThat(histories).hasSize(10);
    }

    @DisplayName("충전/사용 이력이 없는 한 명의 유저가 30번 사용 요청을 하면 사용 금액 * 30 만큼의 포인트가 차감된다.")
    @Test
    void usePoint_concurrently() throws InterruptedException {
        int threadCount = 30;
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

    @DisplayName("충전/사용 내역이 없고 200_000 포인트를 보유 중인 한 명의 유저가 10_000씩 30번 사용 요청을 하면 20번만 사용이 되고 10번은 InsufficientPointException이 발생한다.")
    @Test
    void usePoint_concurrently_exceedMax() throws InterruptedException {
        int threadCount = 30;
        long userId = 1L;
        long initialAmount = 200_000L;
        long useAmount = 10_000L;
        userPointRepository.saveUserPoint(userId, initialAmount);

        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    pointService.usePoint(userId, useAmount);
                    successCount.incrementAndGet();
                } catch (InsufficientPointException e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        UserPoint userPoint = userPointRepository.findUserPointById(userId);
        assertAll(
                () -> assertThat(userPoint.point()).isEqualTo(0L),
                () -> assertThat(successCount.get()).isEqualTo(20),
                () -> assertThat(failCount.get()).isEqualTo(10)
        );

        List<PointHistory> histories = pointHistoryRepository.findPointHistoriesByUserId(userId);
        assertThat(histories).hasSize(20);
    }
}
