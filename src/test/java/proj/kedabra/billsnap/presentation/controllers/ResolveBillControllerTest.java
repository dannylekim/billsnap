package proj.kedabra.billsnap.presentation.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

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

    @MockBean
    private PaymentFacade paymentFacade;

    @Captor
    private ArgumentCaptor<PaymentInformationDTO> captor;

    private static final String PAY_BILL_ENDPOINT = "/resolve/bills";

    @Test
    @DisplayName("Should return 400 for null amount")
    void shouldReturn400ForNullAmount() throws Exception {
        //Given
        final var user = UserFixture.getDefault();
        final var param = PaymentResourceFixture.getDefault();
        param.setPaymentAmount(null);
        final var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("123"));

        //when/then
        performMvcPostRequest(PAY_BILL_ENDPOINT, param, 400, user.getUsername(), authorities);
    }

    @Test
    @DisplayName("Should return 400 for negative amount")
    void shouldReturn400ForNegativeAmount() throws Exception {
        //Given
        final var user = UserFixture.getDefault();
        final var param = PaymentResourceFixture.getDefault();
        param.setPaymentAmount(new BigDecimal("-1"));
        final var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("123"));

        //when/then
        performMvcPostRequest(PAY_BILL_ENDPOINT, param, 400, user.getUsername(), authorities);
    }

    @Test
    @DisplayName("Should return 400 for 0 amount")
    void shouldReturn400ForZeroAmount() throws Exception {
        //Given
        final var user = UserFixture.getDefault();
        final var param = PaymentResourceFixture.getDefault();
        param.setPaymentAmount(BigDecimal.ZERO);
        final var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("123"));

        //when/then
        performMvcPostRequest(PAY_BILL_ENDPOINT, param, 400, user.getUsername(), authorities);
    }

    @Test
    @DisplayName("Should return 403 for null id")
    void shouldReturn400ForNullId() throws Exception {
        //Given
        final var user = UserFixture.getDefault();
        final var param = PaymentResourceFixture.getDefault();
        param.setId(null);
        final var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("123"));

        //when/then
        performMvcPostRequest(PAY_BILL_ENDPOINT, param, 403, user.getUsername(), authorities);
    }


    @Test
    @DisplayName("Should return 200 with value")
    void payBillOK() throws Exception {
        //Given
        final var user = UserFixture.getDefault();
        final var param = PaymentResourceFixture.getDefault();
        final var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("123"));
        when(paymentFacade.payBill(any())).thenReturn(BigDecimal.TEN);

        //when
        final var mvcResult = performMvcPostRequest(PAY_BILL_ENDPOINT, param, 200, user.getUsername(), authorities);

        //then
        final var result = mvcResult.getResponse().getContentAsString();
        final var remainingPaymentResource = mapper.readValue(result, RemainingPaymentResource.class);
        assertThat(remainingPaymentResource.getRemainingBalance()).isEqualTo(new BigDecimal("10.00"));
    }

    @Test
    @DisplayName("Should call facade with proper values")
    void shouldCallFacadeWithProperValues() throws Exception {
        //Given
        final var user = UserFixture.getDefault();
        final var param = PaymentResourceFixture.getDefault();
        final var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("123"));
        when(paymentFacade.payBill(any())).thenReturn(BigDecimal.TEN);

        //when / then
        performMvcPostRequest(PAY_BILL_ENDPOINT, param, 200, user.getUsername(), authorities);

        verify(paymentFacade).payBill(captor.capture());
        final PaymentInformationDTO calledArgument = captor.getValue();
        assertThat(calledArgument.getAmount()).isEqualTo(param.getPaymentAmount());
        assertThat(calledArgument.getBillId()).isEqualTo(param.getId());
        assertThat(calledArgument.getEmail()).isEqualTo(UserFixture.getDefault().getUsername());
    }

    private <T> MvcResult performMvcPostRequest(String path, T body, int resultCode, String email, List<GrantedAuthority> authorities) throws Exception {
        return mockMvc.perform(post(path).with(user(email).authorities(authorities))
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(mapper.writeValueAsString(body)))
                .andExpect(status().is(resultCode)).andReturn();
    }


}