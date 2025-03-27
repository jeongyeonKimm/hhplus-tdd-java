package io.hhplus.tdd.point;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static io.hhplus.tdd.point.TransactionType.CHARGE;
import static io.hhplus.tdd.point.TransactionType.USE;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @InjectMocks
    private PointService pointService;

    @Mock
    private UserPointRepository userPointRepository;

    @Mock
    private PointHistoryRepository pointHistoryRepository;

    @DisplayName("id에 해당하는 사용자 포인트를 조회한다.")
    @Test
    void getUserPoint() {
        long id = 1L;
        long point = 1000L;
        long updateMillis = System.currentTimeMillis();

        UserPoint userPoint = new UserPoint(id, point, updateMillis);
        given(userPointRepository.findUserPointById(id)).willReturn(userPoint);

        UserPoint result = pointService.getUserPoint(id);

        assertThat(result)
                .extracting("id", "point", "updateMillis")
                .contains(id, point, updateMillis);

        verify(userPointRepository, times(1)).findUserPointById(id);
    }

    @DisplayName("유저 아이디로 포인트 충전/사용 내역을 조회한다.")
    @Test
    void getUserPointHistory() {
        long userId = 1L;
        PointHistory pointHistory1 = new PointHistory(2L, userId, 1000L, CHARGE, System.currentTimeMillis());
        PointHistory pointHistory2 = new PointHistory(3L, userId, 500L, USE, System.currentTimeMillis());
        given(pointHistoryRepository.findPointHistoriesByUserId(userId)).willReturn(List.of(pointHistory1, pointHistory2));

        List<PointHistory> results = pointService.getUserPointHistory(userId);

        assertThat(results)
                .extracting("id", "userId", "amount", "type")
                .containsExactlyInAnyOrder(
                        tuple(2L, userId, 1000L, CHARGE),
                        tuple(3L, userId, 500L, USE)
                );

        verify(pointHistoryRepository, times(1)).findPointHistoriesByUserId(userId);
    }

    @DisplayName("0 초과이고 이미 보유한 포인트와의 합이 최대 포인트 이하인 금액만큼 포인트를 충전한다.")
    @Test
    void chargePoint() {
        long userId = 1L;
        long currentPoint = 500_000L;
        long chargeAmount = 300_000L;

        UserPoint userPoint = new UserPoint(userId, currentPoint, System.currentTimeMillis());
        UserPoint chargedUserPoint = userPoint.charge(chargeAmount);
        PointHistory pointHistory = new PointHistory(2L, userId, chargeAmount, CHARGE, System.currentTimeMillis());

        given(userPointRepository.findUserPointById(userId)).willReturn(userPoint);
        given(userPointRepository.saveUserPoint(userId, chargedUserPoint.point())).willReturn(chargedUserPoint);
        given(pointHistoryRepository.savePointHistory(eq(userId), eq(chargeAmount), eq(CHARGE), anyLong())).willReturn(pointHistory);

        UserPoint result = pointService.chargePoint(userId, chargeAmount);

        assertThat(result)
                .extracting("id", "point")
                .contains(userId, chargedUserPoint.point());

        verify(userPointRepository, times(1)).findUserPointById(userId);
        verify(userPointRepository, times(1)).saveUserPoint(userId, chargedUserPoint.point());
        verify(pointHistoryRepository, times(1)).savePointHistory(eq(userId), eq(chargeAmount), eq(CHARGE), anyLong());
    }

    @DisplayName("보유한 포인트 한도 내에서 0을 초과하는 금액만큼 포인트를 사용한다.")
    @Test
    void usePoint() {
        long userId = 1L;
        long currentPoint = 500_000L;
        long useAmount = 300_000L;

        UserPoint userPoint = new UserPoint(userId, currentPoint, System.currentTimeMillis());
        UserPoint usedUserPoint = userPoint.use(useAmount);
        PointHistory pointHistory = new PointHistory(2L, userId, useAmount, USE, System.currentTimeMillis());

        given(userPointRepository.findUserPointById(userId)).willReturn(userPoint);
        given(userPointRepository.saveUserPoint(userId, usedUserPoint.point())).willReturn(usedUserPoint);
        given(pointHistoryRepository.savePointHistory(eq(userId), eq(useAmount), eq(USE), anyLong())).willReturn(pointHistory);

        UserPoint result = pointService.usePoint(userId, useAmount);

        assertThat(result)
                .extracting("id", "point")
                .contains(userId, usedUserPoint.point());

        verify(userPointRepository, times(1)).findUserPointById(userId);
        verify(userPointRepository, times(1)).saveUserPoint(userId, usedUserPoint.point());
        verify(pointHistoryRepository, times(1)).savePointHistory(eq(userId), eq(useAmount), eq(USE), anyLong());
    }
}
