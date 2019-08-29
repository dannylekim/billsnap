package proj.kedabra.billsnap.presentation.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

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
import proj.kedabra.billsnap.security.JwtService;
import proj.kedabra.billsnap.utils.SpringProfiles;

@Tag("integration")
@ActiveProfiles(SpringProfiles.TEST)
@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("squid:S00112")
@AutoConfigureTestDatabase
class BillControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private JwtService jwtService;

    private static final String BILL_ENDPOINT = "/bills";

    private static final String JWT_HEADER = "Authorization";

    private static final String JWT_PREFIX = "Bearer ";

    @Test
    @DisplayName("Should return proper reply with 201 status")
    void ShouldReturn201NormalCase() throws Exception {
        //Given
        final var billCreationResource = BillCreationResourceFixture.getDefault();
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);

        //When/Then
        MvcResult result = mockMvc.perform(post(BILL_ENDPOINT).header(JWT_HEADER, bearerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(mapper.writeValueAsString(billCreationResource)))
                .andExpect(status().isCreated()).andReturn();
        String content = result.getResponse().getContentAsString();
        BillResource response = mapper.readValue(content, BillResource.class);

        assertEquals(billCreationResource.getName(), response.getName());
        assertEquals(billCreationResource.getCategory(), response.getCategory());
        assertEquals(billCreationResource.getCompany(), response.getCompany());
        assertEquals(billCreationResource.getItems().size(), response.getItems().size());
        assertEquals(user.getUsername(), response.getCreator().getEmail());
        assertEquals(user.getUsername(), response.getResponsible().getEmail());
        assertEquals(BillStatusEnum.OPEN, response.getStatus());
        assertEquals(-1, BigDecimal.valueOf(0).compareTo(response.getBalance()));
        assertNotNull(response.getCreated());
        assertNotNull(response.getUpdated());
        assertNotNull(response.getId());
    }
}