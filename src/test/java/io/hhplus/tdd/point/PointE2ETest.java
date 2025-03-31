package io.hhplus.tdd.point;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.hhplus.tdd.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;

import static io.hhplus.tdd.point.TransactionType.*;
import static io.hhplus.tdd.point.TransactionType.CHARGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PointE2ETest {

    RestClient restClient;

    @Autowired
    private UserPointRepository userPointRepository;

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        String baseUrl = "http://localhost:" + port + "/point/";
        restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @DisplayName("GET /point/{id}로 양수가 아닌 유저 아이디가 들어오면 상태 코드 400을 응답한다.")
    @ValueSource(longs = {0L, -1000L})
    @ParameterizedTest
    void getUserPoint_withNotPositiveUserId(long userId) throws JsonProcessingException {
        try {
            restClient.get()
                    .uri("/" + userId)
                    .retrieve()
                    .toEntity(UserPoint.class);
        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            ErrorResponse error = new ObjectMapper().readValue(e.getResponseBodyAsString(), ErrorResponse.class);
            assertThat(error)
                    .extracting("code", "message")
                    .contains("400", "사용자 아이디는 양수입니다.");
        }
    }

    @DisplayName("GET /point/{id}로 양수의 유저 아이디와 함께 포인트 조회 요청을 보내면 상태 코드 200과 조회된 유저 포인트를 응답한다.")
    @Test
    void getUserPoint_success() {
        long userId = 1L;
        userPointRepository.saveUserPoint(userId, 1000L);

        ResponseEntity<UserPoint> response = restClient.get()
                .uri("/" + userId)
                .retrieve()
                .toEntity(UserPoint.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody())
                .extracting("id", "point")
                .contains(userId, 1000L);
    }

    @DisplayName("GET /point/{id}/histories로 양수가 아닌 유저 아이디가 들어오면 상태 코드 400을 응답한다.")
    @ValueSource(longs = {0L, -1000L})
    @ParameterizedTest
    void getPointHistories_withNotPositiveUserId(long userId) throws JsonProcessingException {
        try {
            restClient.get()
                    .uri("/" + userId + "/histories")
                    .retrieve()
                    .toEntity(UserPoint.class);
        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            ErrorResponse error = new ObjectMapper().readValue(e.getResponseBodyAsString(), ErrorResponse.class);
            assertThat(error)
                    .extracting("code", "message")
                    .contains("400", "사용자 아이디는 양수입니다.");
        }
    }

    @DisplayName("GET /point/{id}/histories로 양수의 유저 아이디와 함께 포인트 충전/사용 내역 조회 요청을 보내면 상태 코드 200과 조회된 충전/사용 내역을 응답한다.")
    @Test
    void getPointHistories_success() {
        long userId = 1L;
        pointHistoryRepository.savePointHistory(userId, 1000L, CHARGE, System.currentTimeMillis());
        pointHistoryRepository.savePointHistory(userId, 500L, USE, System.currentTimeMillis());

        ResponseEntity<List<PointHistory>> response = restClient.get()
                .uri("/" + userId + "/histories")
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody())
                .extracting("userId", "amount", "type")
                .containsExactlyInAnyOrder(
                        tuple(userId, 1000L, CHARGE),
                        tuple(userId, 500L, USE)
                );
    }

    @DisplayName("PATCH /point/{id}/charge로 양수가 아닌 유저 아이디가 들어오면 상태 코드 400을 응답한다.")
    @ValueSource(longs = {0L, -1000L})
    @ParameterizedTest
    void chargePoint_withNotPositiveUserId(long userId) throws JsonProcessingException {
        try {
            long chargeAmount = 1000L;
            restClient.patch()
                    .uri("/" + userId + "/charge")
                    .body(chargeAmount)
                    .retrieve()
                    .toEntity(UserPoint.class);
        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            ErrorResponse error = new ObjectMapper().readValue(e.getResponseBodyAsString(), ErrorResponse.class);
            assertThat(error)
                    .extracting("code", "message")
                    .contains("400", "사용자 아이디는 양수입니다.");
        }
    }

    @DisplayName("PATCH /point/{id}/charge로 양수가 아닌 충전 금액이 들어오면 상태 코드 400을 응답한다.")
    @ValueSource(longs = {0L, -1000L})
    @ParameterizedTest
    void chargePoint_withNotPositiveChargeAmount(long chargeAmount) throws JsonProcessingException {
        try {
            long userId = 1L;
            restClient.patch()
                    .uri("/" + userId + "/charge")
                    .body(chargeAmount)
                    .retrieve()
                    .toEntity(UserPoint.class);
        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            ErrorResponse error = new ObjectMapper().readValue(e.getResponseBodyAsString(), ErrorResponse.class);
            assertThat(error)
                    .extracting("code", "message")
                    .contains("400", "충전할 포인트는 양수입니다.");
        }
    }

    @DisplayName("PATCH /point/{id}/charge로 양수의 유저 아이디, 충전 금액과 함께 포인트 충전 요청을 보내면 상태 코드 200과 충전 후 업데이트 된 유저 포인트를 응답한다.")
    @Test
    void chargePoint_success() {
        long userId = 1L;
        long chargeAmount = 1000L;
        userPointRepository.saveUserPoint(userId, 1000L);

        ResponseEntity<UserPoint> response = restClient.patch()
                .uri("/" + userId + "/charge")
                .body(chargeAmount)
                .retrieve()
                .toEntity(UserPoint.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody())
                .extracting("id", "point")
                .contains(userId, 2000L);
    }

    @DisplayName("PATCH /point/{id}/use로 양수가 아닌 유저 아이디가 들어오면 상태 코드 400을 응답한다.")
    @ValueSource(longs = {0L, -1000L})
    @ParameterizedTest
    void usePoint_withNotPositiveUserId(long userId) throws JsonProcessingException {
        try {
            long useAmount = 1000L;
            restClient.patch()
                    .uri("/" + userId + "/use")
                    .body(useAmount)
                    .retrieve()
                    .toEntity(UserPoint.class);
        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            ErrorResponse error = new ObjectMapper().readValue(e.getResponseBodyAsString(), ErrorResponse.class);
            assertThat(error)
                    .extracting("code", "message")
                    .contains("400", "사용자 아이디는 양수입니다.");
        }
    }

    @DisplayName("PATCH /point/{id}/use로 양수가 아닌 충전 금액이 들어오면 상태 코드 400을 응답한다.")
    @ValueSource(longs = {0L, -1000L})
    @ParameterizedTest
    void usePoint_withNotPositiveUseAmount(long useAmount) throws JsonProcessingException {
        try {
            long userId = 1L;
            restClient.patch()
                    .uri("/" + userId + "/use")
                    .body(useAmount)
                    .retrieve()
                    .toEntity(UserPoint.class);
        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            ErrorResponse error = new ObjectMapper().readValue(e.getResponseBodyAsString(), ErrorResponse.class);
            assertThat(error)
                    .extracting("code", "message")
                    .contains("400", "사용할 포인트는 양수입니다.");
        }
    }

    @DisplayName("PATCH /point/{id}/use로 양수의 유저 아이디, 사용 금액과 함께 포인트 사용 요청을 보내면 상태 코드 200과 사용 후 업데이트 된 유저 포인트를 응답한다.")
    @Test
    void usePoint_success() {
        long userId = 1L;
        long useAmount = 1000L;
        userPointRepository.saveUserPoint(userId, 3000L);

        ResponseEntity<UserPoint> response = restClient.patch()
                .uri("/" + userId + "/use")
                .body(useAmount)
                .retrieve()
                .toEntity(UserPoint.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody())
                .extracting("id", "point")
                .contains(userId, 2000L);
    }

}
