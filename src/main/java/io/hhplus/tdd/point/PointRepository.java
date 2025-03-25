package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class PointRepository {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    public UserPoint saveUserPoint(Long id, Long amount) {
        return userPointTable.insertOrUpdate(id, amount);
    }

    public UserPoint findUserPointById(Long id) {
        return userPointTable.selectById(id);
    }

    public PointHistory savePointHistory(Long userId, Long amount, TransactionType type, Long updateMillis) {
        return pointHistoryTable.insert(userId, amount, type, updateMillis);
    }

    public List<PointHistory> findPointHistoriesByUserId(Long userId) {
        return pointHistoryTable.selectAllByUserId(userId);
    }
}
