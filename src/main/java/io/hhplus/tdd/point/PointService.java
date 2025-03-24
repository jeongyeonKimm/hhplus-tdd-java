package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PointService {

    private final PointHistoryTable pointHistoryTable;

    public List<PointHistory> getUserPointHistory(long userId) {
        return pointHistoryTable.selectAllByUserId(userId);
    }
}
