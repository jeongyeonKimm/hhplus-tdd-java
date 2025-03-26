package io.hhplus.tdd.point;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static io.hhplus.tdd.point.TransactionType.CHARGE;
import static io.hhplus.tdd.point.TransactionType.USE;

@RequiredArgsConstructor
@Service
public class PointService {

    private final PointRepository pointRepository;

    public UserPoint getUserPoint(long id) {
        return pointRepository.findUserPointById(id);
    }

    public List<PointHistory> getUserPointHistory(long userId) {
        return pointRepository.findPointHistoriesByUserId(userId);
    }

    public UserPoint chargePoint(long id, long amount) {
        UserPoint userPoint = pointRepository.findUserPointById(id);

        UserPoint chargedUserPoint = userPoint.charge(amount);

        pointRepository.saveUserPoint(id, chargedUserPoint.point());
        pointRepository.savePointHistory(id, amount, CHARGE, System.currentTimeMillis());

        return chargedUserPoint;
    }

    public UserPoint usePoint(long id, long amount) {
        UserPoint userPoint = pointRepository.findUserPointById(id);

        UserPoint usedUserPoint = userPoint.use(amount);

        pointRepository.saveUserPoint(id, usedUserPoint.point());
        pointRepository.savePointHistory(id, amount, USE, System.currentTimeMillis());

        return usedUserPoint;
    }
}
