package io.hhplus.tdd.point;

import io.hhplus.tdd.UserLockManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static io.hhplus.tdd.point.TransactionType.CHARGE;
import static io.hhplus.tdd.point.TransactionType.USE;

@RequiredArgsConstructor
@Service
public class PointService {

    private final UserPointRepository userPointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final UserLockManager userLockManager;

    public UserPoint getUserPoint(long id) {
        return userPointRepository.findUserPointById(id);
    }

    public List<PointHistory> getUserPointHistory(long userId) {
        return pointHistoryRepository.findPointHistoriesByUserId(userId);
    }

    public UserPoint chargePoint(long id, long amount) {
        userLockManager.lockUser(id);
        try {
            UserPoint userPoint = userPointRepository.findUserPointById(id);

            UserPoint chargedUserPoint = userPoint.charge(amount);

            userPointRepository.saveUserPoint(id, chargedUserPoint.point());
            pointHistoryRepository.savePointHistory(id, amount, CHARGE, System.currentTimeMillis());

            return chargedUserPoint;
        } finally {
            userLockManager.unlockUser(id);
        }
    }

    public UserPoint usePoint(long id, long amount) {
        userLockManager.lockUser(id);
        try {
            UserPoint userPoint = userPointRepository.findUserPointById(id);

            UserPoint usedUserPoint = userPoint.use(amount);

            userPointRepository.saveUserPoint(id, usedUserPoint.point());
            pointHistoryRepository.savePointHistory(id, amount, USE, System.currentTimeMillis());

            return usedUserPoint;
        } finally {
            userLockManager.unlockUser(id);
        }
    }
}
