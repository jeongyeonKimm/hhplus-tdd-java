package io.hhplus.tdd.point;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @InjectMocks
    private PointService pointService;

    @Mock
    private PointRepository pointRepository;

    @DisplayName("id에 해당하는 사용자 포인트를 조회한다.")
    @Test
    void getUserPoint() {
        long updateMillis = System.currentTimeMillis();
        long id = 1L;
        UserPoint userPoint = new UserPoint(id, 1000L, updateMillis);
        given(pointRepository.findUserPointById(id)).willReturn(userPoint);

        UserPoint result = pointService.getUserPoint(id);

        assertThat(result)
                .extracting("id", "point", "updateMillis")
                .contains(id, 1000L, updateMillis);

        verify(pointRepository, times(1)).findUserPointById(id);
    }
}
