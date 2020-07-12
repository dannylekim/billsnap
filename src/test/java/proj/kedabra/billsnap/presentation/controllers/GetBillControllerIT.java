package proj.kedabra.billsnap.presentation.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import javax.transaction.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import proj.kedabra.billsnap.business.utils.enums.BillStatusEnum;
import proj.kedabra.billsnap.fixtures.BillCreationResourceFixture;
import proj.kedabra.billsnap.fixtures.UserFixture;
import proj.kedabra.billsnap.presentation.resources.BillResource;
import proj.kedabra.billsnap.presentation.resources.BillSplitResource;
import proj.kedabra.billsnap.presentation.resources.ShortBillResource;
import proj.kedabra.billsnap.security.JwtService;
import proj.kedabra.billsnap.utils.SpringProfiles;

@Tag("integration")
@ActiveProfiles(SpringProfiles.TEST)
@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("squid:S00112")
@AutoConfigureTestDatabase
@Transactional
class GetBillControllerIT {

    private static final String BILL_ENDPOINT = "/bills";

    private static final String JWT_HEADER = "Authorization";

    private static final String JWT_PREFIX = "Bearer ";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private JwtService jwtService;


    @Test
    @DisplayName("Should return empty List if no bills")
    void shouldReturnEmptyListOfResourceIfNoBills() throws Exception {
        //Given
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);

        //When/Then
        final MvcResult result = performMvcGetRequest(bearerToken, 200);
        final String content = result.getResponse().getContentAsString();
        final List<BillSplitResource> response = mapper.readValue(content, new TypeReference<>() {
        });
        assertThat(response).isEmpty();
    }

    @Test
    @DisplayName("Should return list of 2 ShortBillResources after adding 2 bills")
    void shouldReturnListOf2Resources() throws Exception {
        //Given
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        final var billCreationResource = BillCreationResourceFixture.getDefault();

        MvcResult result = performMvcPostRequest(bearerToken, billCreationResource);
        String content = result.getResponse().getContentAsString();
        final BillResource billOne = mapper.readValue(content, BillResource.class);

        result = performMvcPostRequest(bearerToken, billCreationResource);
        content = result.getResponse().getContentAsString();
        final BillResource billTwo = mapper.readValue(content, BillResource.class);

        //When/Then
        result = performMvcGetRequest(bearerToken, 200);
        content = result.getResponse().getContentAsString();
        final List<ShortBillResource> response = mapper.readValue(content, new TypeReference<>() {
        });

        verifyShortBillResources(billOne, response.get(0));
        verifyShortBillResources(billTwo, response.get(1));
    }

    private MvcResult performMvcGetRequest(final String bearerToken, final int resultCode) throws Exception {
        return mockMvc.perform(get(GetBillControllerIT.BILL_ENDPOINT).header(JWT_HEADER, bearerToken))
                .andExpect(status().is(resultCode)).andReturn();
    }

    private <T> MvcResult performMvcPostRequest(final String bearerToken, final T body) throws Exception {
        return mockMvc.perform(post(GetBillControllerIT.BILL_ENDPOINT).header(JWT_HEADER, bearerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(mapper.writeValueAsString(body)))
                .andExpect(status().is(201)).andReturn();
    }

    private void verifyShortBillResources(final BillResource expectedBillResource, final ShortBillResource actualBillResource) {
        assertEquals(expectedBillResource.getId(), actualBillResource.getId());
        assertEquals(expectedBillResource.getName(), actualBillResource.getName());
        assertEquals(expectedBillResource.getCategory(), actualBillResource.getCategory());
        assertEquals(BillStatusEnum.OPEN, actualBillResource.getStatus());
        assertEquals(0, expectedBillResource.getBalance().compareTo(actualBillResource.getBalance()));
    }
}