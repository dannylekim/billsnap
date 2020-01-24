package proj.kedabra.billsnap.presentation.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import javax.transaction.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import proj.kedabra.billsnap.business.dto.PaymentInformationDTO;
import proj.kedabra.billsnap.business.facade.PaymentFacade;
import proj.kedabra.billsnap.fixtures.PaymentResourceFixture;
import proj.kedabra.billsnap.fixtures.UserFixture;
import proj.kedabra.billsnap.presentation.resources.RemainingPaymentResource;
import proj.kedabra.billsnap.security.JwtService;
import proj.kedabra.billsnap.utils.SpringProfiles;

@Tag("integration")
@ActiveProfiles(SpringProfiles.TEST)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@Transactional
class ResolveBillControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private PaymentFacade paymentFacade;

    @Captor
    private ArgumentCaptor<PaymentInformationDTO> captor;

    private static final String PAY_BILL_ENDPOINT = "/resolve/bills";

    private static final String JWT_HEADER = "Authorization";

    private static final String JWT_PREFIX = "Bearer ";


    @Test
    @DisplayName("Should return 400 for null amount")
    void shouldReturn400ForNullAmount() throws Exception {
        //Given
        final var param = PaymentResourceFixture.getDefault();
        param.setPaymentAmount(null);
        final var value = mapper.writeValueAsString(param);

        //when/then
        this.mockMvc.perform(post(PAY_BILL_ENDPOINT).contentType(MediaType.APPLICATION_JSON).content(value).header(JWT_HEADER, JWT_PREFIX + jwtService.generateToken(UserFixture.getDefault()))).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 for negative amount")
    void shouldReturn400ForNegativeAmount() throws Exception {
        //Given
        final var param = PaymentResourceFixture.getDefault();
        param.setPaymentAmount(new BigDecimal("-1"));
        final var value = mapper.writeValueAsString(param);

        //when/then
        this.mockMvc.perform(post(PAY_BILL_ENDPOINT).contentType(MediaType.APPLICATION_JSON).content(value).header(JWT_HEADER, JWT_PREFIX + jwtService.generateToken(UserFixture.getDefault()))).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 for 0 amount")
    void shouldReturn400ForZeroAmount() throws Exception {
        //Given
        final var param = PaymentResourceFixture.getDefault();
        param.setPaymentAmount(BigDecimal.ZERO);
        final var value = mapper.writeValueAsString(param);

        //when/then
        this.mockMvc.perform(post(PAY_BILL_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(value).header(JWT_HEADER, JWT_PREFIX + jwtService.generateToken(UserFixture.getDefault())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 for null id")
    void shouldReturn400ForNullId() throws Exception {
        //Given
        final var param = PaymentResourceFixture.getDefault();
        param.setId(null);
        final var value = mapper.writeValueAsString(param);

        //when/then
        this.mockMvc.perform(post(PAY_BILL_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(value).header(JWT_HEADER, JWT_PREFIX + jwtService.generateToken(UserFixture.getDefault())))
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("Should return 200 with value")
    void payBillOK() throws Exception {
        //Given
        final var param = PaymentResourceFixture.getDefault();
        final var value = mapper.writeValueAsString(param);
        when(paymentFacade.payBill(any())).thenReturn(BigDecimal.TEN);

        //when
        final var mvcResult = this.mockMvc.perform(post(PAY_BILL_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(value).header(JWT_HEADER, JWT_PREFIX + jwtService.generateToken(UserFixture.getDefault())))
                .andExpect(status().isOk()).andReturn();

        //then
        final var result = mvcResult.getResponse().getContentAsString();
        final var remainingPaymentResource = mapper.readValue(result, RemainingPaymentResource.class);
        assertThat(remainingPaymentResource.getRemainingBalance()).isEqualTo(BigDecimal.TEN);
    }

    @Test
    @DisplayName("Should call facade with proper values")
    void shouldCallFacadeWithProperValues() throws Exception {
        //Given
        final var param = PaymentResourceFixture.getDefault();
        final var value = mapper.writeValueAsString(param);
        when(paymentFacade.payBill(any())).thenReturn(BigDecimal.TEN);

        //when
        final var mvcResult = this.mockMvc.perform(post(PAY_BILL_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(value).header(JWT_HEADER, JWT_PREFIX + jwtService.generateToken(UserFixture.getDefault())))
                .andExpect(status().isOk()).andReturn();

        //then
        verify(paymentFacade).payBill(captor.capture());
        final PaymentInformationDTO calledArgument = captor.getValue();
        assertThat(calledArgument.getAmount()).isEqualTo(param.getPaymentAmount());
        assertThat(calledArgument.getBillId()).isEqualTo(param.getId());
        assertThat(calledArgument.getEmail()).isEqualTo(UserFixture.getDefault().getUsername());
    }


}