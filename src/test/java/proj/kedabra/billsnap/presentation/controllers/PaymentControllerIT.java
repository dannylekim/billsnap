package proj.kedabra.billsnap.presentation.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import proj.kedabra.billsnap.fixtures.PaymentOwedResourceFixture;
import proj.kedabra.billsnap.fixtures.UserFixture;
import proj.kedabra.billsnap.presentation.resources.PaymentOwedResource;
import proj.kedabra.billsnap.security.JwtService;
import proj.kedabra.billsnap.utils.SpringProfiles;

import java.util.List;

@Tag("integration")
@ActiveProfiles(SpringProfiles.TEST)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
public class PaymentControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private JwtService jwtService;

    private static final String JWT_HEADER = "Authorization";

    private static final String JWT_PREFIX = "Bearer ";

    private static final String PAYMENTS_ENDPOINT = "/payments";

    @Test
    @DisplayName("Should return an empty list of amount owed")
    void shouldReturnEmptyListOfAmountOwed() throws Exception{
        //Given
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        final var paymentOwedResource = PaymentOwedResourceFixture.getDefault();

        //When/Then
        final var result = mockMvc.perform(get(PAYMENTS_ENDPOINT).header(JWT_HEADER, bearerToken)
                        .contentType(MediaType.APPLICATION_JSON_VALUE).content(mapper.writeValueAsString(paymentOwedResource)))
                        .andExpect(status().isOk()).andReturn();

        String content = result.getResponse().getContentAsString();

        List<PaymentOwedResource> paymentOwedList = mapper.readValue(content, new TypeReference<List<PaymentOwedResource>>() {
        });

        assertThat(paymentOwedList.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should return a list with 2 amount owed")
    void shouldReturnListWith2AmountOwed() throws Exception{
        //Given
        final var user = UserFixture.getDefaultWithEmailAndPassword("paymentowed@test.com", "notEncrypted");
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        final var paymentOwedResource = PaymentOwedResourceFixture.getDefault();

        //When/Then
        final var result = mockMvc.perform(get(PAYMENTS_ENDPOINT).header(JWT_HEADER, bearerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(mapper.writeValueAsString(paymentOwedResource)))
                .andExpect(status().isOk()).andReturn();

        String content = result.getResponse().getContentAsString();

        List<PaymentOwedResource> paymentOwedList = mapper.readValue(content, new TypeReference<List<PaymentOwedResource>>() {
        });

        assertThat(paymentOwedList.size()).isEqualTo(2);
        assertThat(paymentOwedList.get(0).getEmail()).isEqualTo("user@user.com");
        assertThat(paymentOwedList.get(0).getAmount().toString()).isEqualTo("133.00");
        assertThat(paymentOwedList.get(1).getEmail()).isEqualTo("userdetails@service.com");
        assertThat(paymentOwedList.get(1).getAmount().toString()).isEqualTo("489.00");
    }

}
