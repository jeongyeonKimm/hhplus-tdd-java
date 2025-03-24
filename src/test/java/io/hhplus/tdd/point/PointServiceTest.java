package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @InjectMocks
    private PointService pointService;

    @Mock
    private PointHistoryTable pointHistoryTable;

    @DisplayName("유저 아이디로 포인트 충전/사용 내역을 조회한다.")
    @Test
    void getUserPointHistory() {
        long userId = 1L;
        PointHistory pointHistory1 = new PointHistory(2L, userId, 1000L, TransactionType.CHARGE, System.currentTimeMillis());
        PointHistory pointHistory2 = new PointHistory(3L, userId, 500L, TransactionType.USE, System.currentTimeMillis());
        given(pointHistoryTable.selectAllByUserId(userId)).willReturn(List.of(pointHistory1, pointHistory2));

        List<PointHistory> results = pointService.getUserPointHistory(userId);

        assertThat(results)
                .extracting("id", "userId", "amount", "type")
                .containsExactlyInAnyOrder(
                        tuple(2L, userId, 1000L, TransactionType.CHARGE),
                        tuple(3L, userId, 500L, TransactionType.USE)
                );

        verify(pointHistoryTable, times(1)).selectAllByUserId(userId);
    }
}
