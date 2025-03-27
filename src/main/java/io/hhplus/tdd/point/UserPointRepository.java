package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class UserPointRepository {

    private final UserPointTable userPointTable;

    public UserPoint saveUserPoint(Long id, Long amount) {
        return userPointTable.insertOrUpdate(id, amount);
    }

    public UserPoint findUserPointById(Long id) {
        return userPointTable.selectById(id);
    }
}
