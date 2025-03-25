package io.hhplus.tdd.point;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PointControllerTest {

    @InjectMocks
    private PointController pointController;

    @Mock
    private PointService pointService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(pointController)
                .build();
    }

    @DisplayName("특정 유저의 포인트 충전/사용 내역을 조회한다.")
    @Test
    void history() throws Exception {
        long userId = 1L;
        PointHistory pointHistory1 = new PointHistory(2L, userId, 1000L, TransactionType.CHARGE, System.currentTimeMillis());
        PointHistory pointHistory2 = new PointHistory(3L, userId, 500L, TransactionType.USE, System.currentTimeMillis());
        List<PointHistory> pointHistories = List.of(pointHistory1, pointHistory2);

        given(pointService.getUserPointHistory(1L)).willReturn(pointHistories);

        MvcResult result = mockMvc.perform(
                        get("/point/{id}/histories", 1L)
                )
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        List<PointHistory> body = objectMapper.readValue(
                content,
                new TypeReference<List<PointHistory>>() {}
        );

        assertThat(body).hasSize(pointHistories.size());
        for (int i = 0; i < body.size(); i++) {
            assertThat(body.get(i).id()).isEqualTo(pointHistories.get(i).id());
            assertThat(body.get(i).userId()).isEqualTo(pointHistories.get(i).userId());
            assertThat(body.get(i).amount()).isEqualTo(pointHistories.get(i).amount());
            assertThat(body.get(i).type()).isEqualTo(pointHistories.get(i).type());
        }
    }
    
    @DisplayName("충전/사용 내역이 없는 경우 빈 리스트를 반환한다.")
    @Test
    void getUserPointHistory_returnsEmptyList_whenNoHistoryExists() throws Exception {
        long userId = 1L;
        given(pointService.getUserPointHistory(userId)).willReturn(List.of());

        mockMvc.perform(
                        get("/point/{id}/histories", userId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
