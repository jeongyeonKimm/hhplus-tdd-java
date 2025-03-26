package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class PointHistoryRepository {

    private final PointHistoryTable pointHistoryTable;

    public PointHistory savePointHistory(Long userId, Long amount, TransactionType type, Long updateMillis) {
        return pointHistoryTable.insert(userId, amount, type, updateMillis);
    }

    public List<PointHistory> findPointHistoriesByUserId(Long userId) {
        return pointHistoryTable.selectAllByUserId(userId);
    }
}
