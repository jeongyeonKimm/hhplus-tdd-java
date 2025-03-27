package io.hhplus.tdd.point;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PointController.class)
class PointControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PointService pointService;

    @DisplayName("유저 아이디가 양수가 아닌 경우 상태코드 400을 반환한다.")
    @Test
    void shouldReturn400_ifUserIdIsNotPositive() throws Exception {
        mockMvc.perform(get("/point/{id}", -1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("사용자 아이디는 양수입니다."));
    }

    @DisplayName("충전 금액이 0일 경우 상태코드 400을 반환한다.")
    @Test
    void shouldReturn400_ifChargeAmountIsZero() throws Exception {
        mockMvc.perform(patch("/point/{id}/charge", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("충전할 포인트는 양수입니다."));
    }

    @DisplayName("유저 아이디가 양수이고, 충전 금액이 0 보다 크면 상태코드 200과 업데이트된 유저 포인트를 반환한다.")
    @Test
    void shouldReturn200_forValidCharge() throws Exception {
        given(pointService.chargePoint(1L, 1000L))
                .willReturn(new UserPoint(1L, 1000L, System.currentTimeMillis()));

        mockMvc.perform(patch("/point/{id}/charge", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.point").value(1000L));
    }

    @DisplayName("사용 금액이 0일 경우 상태코드 400을 반환한다.")
    @Test
    void shouldReturn400_ifUseAmountIsZero() throws Exception {
        mockMvc.perform(patch("/point/{id}/use", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("사용할 포인트는 양수입니다."));
    }

    @DisplayName("유저 아이디가 양수이고, 사용 금액이 0 보다 크면 상태코드 200과 업데이트된 유저 포인트를 반환한다.")
    @Test
    void shouldReturn200_forValidUse() throws Exception {
        given(pointService.usePoint(1L, 1000L))
                .willReturn(new UserPoint(1L, 0L, System.currentTimeMillis()));

        mockMvc.perform(patch("/point/{id}/use", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.point").value(0L));
    }
}
