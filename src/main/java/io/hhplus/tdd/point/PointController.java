package io.hhplus.tdd.point;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/point")
public class PointController {

    private static final Logger log = LoggerFactory.getLogger(PointController.class);
    private final PointService pointService;

    @GetMapping("{id}")
    public UserPoint point(
            @PathVariable @Positive(message = "사용자 아이디는 양수입니다.") long id
    ) {
        return pointService.getUserPoint(id);
    }

    @GetMapping("{id}/histories")
    public List<PointHistory> history(
            @PathVariable @Positive(message = "사용자 아이디는 양수입니다.") long id
    ) {
        return pointService.getUserPointHistory(id);
    }

    @PatchMapping("{id}/charge")
    public UserPoint charge(
            @PathVariable @Positive(message = "사용자 아이디는 양수입니다.") long id,
            @RequestBody @Positive(message = "충전할 포인트는 양수입니다.") long amount
    ) {
        return pointService.chargePoint(id, amount);
    }

    @PatchMapping("{id}/use")
    public UserPoint use(
            @PathVariable @Positive(message = "사용자 아이디는 양수입니다.") long id,
            @RequestBody @Positive(message = "사용할 포인트는 양수입니다.") long amount
    ) {
        return pointService.usePoint(id, amount);
    }
}
