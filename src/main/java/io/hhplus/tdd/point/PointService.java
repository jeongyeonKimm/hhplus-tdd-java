package io.hhplus.tdd.point;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PointService {

    private final PointRepository pointRepository;

    public UserPoint getUserPoint(long userPointId) {
        return pointRepository.findUserPointById(userPointId);
    }
}
