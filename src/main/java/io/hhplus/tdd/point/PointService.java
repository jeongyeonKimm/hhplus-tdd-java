package io.hhplus.tdd.point;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import io.hhplus.tdd.database.PointHistoryTable;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PointService {

    private final PointRepository pointRepository;
    private final PointHistoryTable pointHistoryTable;

    public UserPoint getUserPoint(long userPointId) {
        return pointRepository.findUserPointById(userPointId);
    }

    public List<PointHistory> getUserPointHistory(long userId) {
        return pointHistoryTable.selectAllByUserId(userId);
    }
}
