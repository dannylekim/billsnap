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
import proj.kedabra.billsnap.presentation.ApiError;
import proj.kedabra.billsnap.presentation.resources.BillResource;
import proj.kedabra.billsnap.presentation.resources.BillSplitResource;
import proj.kedabra.billsnap.presentation.resources.ShortBillResource;
import proj.kedabra.billsnap.security.JwtService;
import proj.kedabra.billsnap.utils.SpringProfiles;

@Tag("integration")
@ActiveProfiles(SpringProfiles.TEST)
@SpringBootTest
@AutoConfigureMockMvc
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
        final MvcResult result = performMvcGetRequest(bearerToken, 200, BILL_ENDPOINT);
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
        result = performMvcGetRequest(bearerToken, 200, BILL_ENDPOINT);
        content = result.getResponse().getContentAsString();
        final List<ShortBillResource> response = mapper.readValue(content, new TypeReference<>() {
        });

        if (response.get(0).getId() == 1L) {
            verifyShortBillResources(billOne, response.get(0));
            verifyShortBillResources(billTwo, response.get(1));
        } else {
            verifyShortBillResources(billTwo, response.get(0));
            verifyShortBillResources(billOne, response.get(1));
        }
    }

    @Test
    @DisplayName("Should return bill according to pagination when sorted by creation")
    void shouldReturnBillAccordingToPaginationWhenSortedByCreation() throws Exception {
        //Given
        final var user = UserFixture.getDefaultWithEmailAndPassword("billPagination@email.com", "notEncrypted");
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        final var path1 = "/bills?statuses=OPEN&&start=2019-01-01T00:00:00.000&end=2020-01-01T00:00:00.000&page_size=2&page_number=0&category=restaurant&sort_by=CREATED&order_by=ASC";
        final var path2 = "/bills?statuses=OPEN&start=2019-01-01T00:00:00.000&end=2020-01-01T00:00:00.000&page_size=2&page_number=0&category=bus&sort_by=CREATED&order_by=ASC";

        //When
        final MvcResult result1 = performMvcGetRequest(bearerToken, 200, path1);
        final String content1 = result1.getResponse().getContentAsString();
        final List<ShortBillResource> response1 = mapper.readValue(content1, new TypeReference<>() {
        });

        final MvcResult result2 = performMvcGetRequest(bearerToken, 200, path2);
        final String content2 = result2.getResponse().getContentAsString();
        final List<ShortBillResource> response2 = mapper.readValue(content2, new TypeReference<>() {
        });

        //Then
        assertThat(response1).hasSize(2);
        assertThat(response1.stream().filter(o -> o.getName().equals("bill pagination 2")).findFirst()).isNotNull();
        assertThat(response1.stream().filter(o -> o.getName().equals("bill pagination 4")).findFirst()).isNotNull();

        assertThat(response2).hasSize(1);
        assertThat(response2.get(0).getName()).isEqualTo("bill pagination 5");
    }

    @Test
    @DisplayName("Should return bill according to pagination when sorted by status")
    void shouldReturnBillAccordingToPaginationWhenSortedByStatus() throws Exception {
        //Given
        final var user = UserFixture.getDefaultWithEmailAndPassword("billPagination@email.com", "notEncrypted");
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        final var path1 = "/bills?statuses=OPEN&statuses=IN_PROGRESS&statuses=RESOLVED&start=2019-01-01T00:00:00.000&end=2020-01-01T00:00:00.000&page_size=2&page_number=0&category=restaurant&sort_by=STATUS&order_by=DESC";
        final var path2 = "/bills?statuses=OPEN&statuses=IN_PROGRESS&statuses=RESOLVED&start=2019-01-01T00:00:00.000&end=2020-01-01T00:00:00.000&page_size=5&page_number=0&category=restaurant&sort_by=STATUS&order_by=ASC";

        //When
        final MvcResult result1 = performMvcGetRequest(bearerToken, 200, path1);
        final String content1 = result1.getResponse().getContentAsString();
        final List<ShortBillResource> response1 = mapper.readValue(content1, new TypeReference<>() {
        });

        final MvcResult result2 = performMvcGetRequest(bearerToken, 200, path2);
        final String content2 = result2.getResponse().getContentAsString();
        final List<ShortBillResource> response2 = mapper.readValue(content2, new TypeReference<>() {
        });

        //Then
        assertThat(response1).hasSize(2);
        assertThat(response1.get(0).getName()).isEqualTo("bill pagination 4");

        assertThat(response2).hasSize(3);
        assertThat(response2.get(0).getName()).isEqualTo("bill pagination 3");
    }

    @Test
    @DisplayName("Should return bill according to pagination when sorted by category")
    void shouldReturnBillAccordingToPaginationWhenSortedByCategory() throws Exception {
        //Given
        final var user = UserFixture.getDefaultWithEmailAndPassword("billPagination@email.com", "notEncrypted");
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        final var path = "/bills?statuses=OPEN&statuses=IN_PROGRESS&statuses=RESOLVED&start=2019-01-01T00:00:00.000&end=2020-01-01T00:00:00.000&page_size=2&page_number=0&sort_by=CATEGORY&order_by=ASC";

        //When
        final MvcResult result = performMvcGetRequest(bearerToken, 200, path);
        final String content = result.getResponse().getContentAsString();
        final List<ShortBillResource> response = mapper.readValue(content, new TypeReference<>() {
        });

        //Then
        assertThat(response).hasSize(2);
        assertThat(response.get(0).getName()).isEqualTo("bill pagination 5");
    }

    private MvcResult performMvcGetRequest(final String bearerToken, final int resultCode, final String status) throws Exception {
        return mockMvc.perform(get(status).header(JWT_HEADER, bearerToken))
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