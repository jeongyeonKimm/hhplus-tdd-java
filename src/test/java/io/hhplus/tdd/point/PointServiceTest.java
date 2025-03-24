package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.stereotype.Service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @InjectMocks
    private PointService pointService;

    @Mock
    private UserPointTable userPointTable;

    @DisplayName("id에 해당하는 사용자 포인트를 조회한다.")
    @Test
    void getUserPoint() {
        // given
        long updateMillis = System.currentTimeMillis();
        UserPoint userPoint = new UserPoint(1L, 1000L, updateMillis);
        given(userPointTable.selectById(1L)).willReturn(userPoint);

        // when
        UserPoint result = pointService.getUserPoint(1L);

        // then
        assertThat(result)
                .extracting("id", "point", "updateMillis")
                .contains(1L, 1000L, updateMillis);
    }
}
