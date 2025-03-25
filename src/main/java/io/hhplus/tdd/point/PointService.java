package io.hhplus.tdd.point;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
