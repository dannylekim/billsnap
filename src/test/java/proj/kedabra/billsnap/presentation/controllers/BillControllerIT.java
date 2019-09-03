package proj.kedabra.billsnap.presentation.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

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
import proj.kedabra.billsnap.fixtures.ItemResourceFixture;
import proj.kedabra.billsnap.fixtures.UserFixture;
import proj.kedabra.billsnap.presentation.ApiError;
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

    private static final String MUST_NOT_BE_BLANK = "must not be blank";

    private static final String MUST_NOT_BE_NULL = "must not be null";

    private static final String INVALID_INPUTS = "Invalid Inputs. Please fix the following errors";

    private static final String WRONG_SIZE_0_TO_30 = "size must be between 0 and 30";

    private static final String WRONG_SIZE_0_TO_20 = "size must be between 0 and 20";

    private static final String HAVE_BOTH_TIP_AMOUNT_PERCENT = "Only one type of tipping is supported. Please make sure only either tip amount or tip percent is set.";

    private static final String NUMBER_MUST_BE_POSITIVE = "the number must be positive";

    private static final String NUMBER_OUT_OF_BOUNDS_12_2 = "numeric value out of bounds (<12 digits>.<2 digits> expected)";

    private static final String NUMBER_OUT_OF_BOUNDS_3_4 = "numeric value out of bounds (<3 digits>.<4 digits> expected)";

    @Test
    @DisplayName("Should return proper reply with 201 status")
    void ShouldReturn201ForNormalCase() throws Exception {
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

    @Test
    @DisplayName("Should return error for blank bill name")
    void ShouldReturnErrorForBlankBillName() throws Exception {
        //Given
        final var billCreationResource = BillCreationResourceFixture.getDefault();
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        billCreationResource.setName("");

        //When/Then
        MvcResult result = mockMvc.perform(post(BILL_ENDPOINT).header(JWT_HEADER, bearerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(mapper.writeValueAsString(billCreationResource)))
                .andExpect(status().is4xxClientError()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);

        assertEquals(INVALID_INPUTS, error.getMessage());
        assertEquals(1, error.getErrors().size());
        assertEquals(MUST_NOT_BE_BLANK, error.getErrors().get(0).getMessage());
    }

    @Test
    @DisplayName("Should return error for too long of a bill name")
    void ShouldReturnErrorForLongBillName() throws Exception {
        //Given
        final var billCreationResource = BillCreationResourceFixture.getDefault();
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        billCreationResource.setName("toooooo longggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggg");

        //When/Then
        MvcResult result = mockMvc.perform(post(BILL_ENDPOINT).header(JWT_HEADER, bearerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(mapper.writeValueAsString(billCreationResource)))
                .andExpect(status().is4xxClientError()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);

        assertEquals(INVALID_INPUTS, error.getMessage());
        assertEquals(1, error.getErrors().size());
        assertEquals(WRONG_SIZE_0_TO_30, error.getErrors().get(0).getMessage());
    }

    @Test
    @DisplayName("Should return error for too long of a bill category")
    void ShouldReturnErrorForLongBillCategory() throws Exception {
        //Given
        final var billCreationResource = BillCreationResourceFixture.getDefault();
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        billCreationResource.setCategory("toooooo longggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggg");

        //When/Then
        MvcResult result = mockMvc.perform(post(BILL_ENDPOINT).header(JWT_HEADER, bearerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(mapper.writeValueAsString(billCreationResource)))
                .andExpect(status().is4xxClientError()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);

        assertEquals(INVALID_INPUTS, error.getMessage());
        assertEquals(1, error.getErrors().size());
        assertEquals(WRONG_SIZE_0_TO_20, error.getErrors().get(0).getMessage());
    }

    @Test
    @DisplayName("Should return error for too long of a bill company")
    void ShouldReturnErrorForLongBillCompany() throws Exception {
        //Given
        final var billCreationResource = BillCreationResourceFixture.getDefault();
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        billCreationResource.setCompany("toooooo longggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggg");

        //When/Then
        MvcResult result = mockMvc.perform(post(BILL_ENDPOINT).header(JWT_HEADER, bearerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(mapper.writeValueAsString(billCreationResource)))
                .andExpect(status().is4xxClientError()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);

        assertEquals(INVALID_INPUTS, error.getMessage());
        assertEquals(1, error.getErrors().size());
        assertEquals(WRONG_SIZE_0_TO_20, error.getErrors().get(0).getMessage());
    }

    @Test
    @DisplayName("Should return error for blank item name")
    void ShouldReturnErrorBlankItemName() throws Exception {
        //Given
        final var billCreationResource = BillCreationResourceFixture.getDefault();
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        final var item = ItemResourceFixture.getDefault();
        item.setName("");
        billCreationResource.setItems(List.of(item));

        //When/Then
        MvcResult result = mockMvc.perform(post(BILL_ENDPOINT).header(JWT_HEADER, bearerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(mapper.writeValueAsString(billCreationResource)))
                .andExpect(status().is4xxClientError()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);

        assertEquals(INVALID_INPUTS, error.getMessage());
        assertEquals(1, error.getErrors().size());
        assertEquals(MUST_NOT_BE_BLANK, error.getErrors().get(0).getMessage());
    }

    @Test
    @DisplayName("Should return error for too long of an item name")
    void ShouldReturnErrorForLongItemName() throws Exception {
        //Given
        final var billCreationResource = BillCreationResourceFixture.getDefault();
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        final var item = ItemResourceFixture.getDefault();
        item.setName("toooooo longggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggg");
        billCreationResource.setItems(List.of(item));

        //When/Then
        MvcResult result = mockMvc.perform(post(BILL_ENDPOINT).header(JWT_HEADER, bearerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(mapper.writeValueAsString(billCreationResource)))
                .andExpect(status().is4xxClientError()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);

        assertEquals(INVALID_INPUTS, error.getMessage());
        assertEquals(1, error.getErrors().size());
        assertEquals(WRONG_SIZE_0_TO_30, error.getErrors().get(0).getMessage());
    }

    @Test
    @DisplayName("Should return error for negative item cost")
    void ShouldReturnErrorForNegativeItemCost() throws Exception {
        //Given
        final var billCreationResource = BillCreationResourceFixture.getDefault();
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        final var item = ItemResourceFixture.getDefault();
        item.setCost(BigDecimal.valueOf(-5));
        billCreationResource.setItems(List.of(item));

        //When/Then
        MvcResult result = mockMvc.perform(post(BILL_ENDPOINT).header(JWT_HEADER, bearerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(mapper.writeValueAsString(billCreationResource)))
                .andExpect(status().is4xxClientError()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);

        assertEquals(INVALID_INPUTS, error.getMessage());
        assertEquals(1, error.getErrors().size());
        assertEquals(NUMBER_MUST_BE_POSITIVE, error.getErrors().get(0).getMessage());
    }

    @Test
    @DisplayName("Should return error for item cost over 12 total digits")
    void ShouldReturnErrorForItemCostOver12Digits() throws Exception {
        //Given
        final var billCreationResource = BillCreationResourceFixture.getDefault();
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        final var item = ItemResourceFixture.getDefault();
        item.setCost(BigDecimal.valueOf(1234567898765.75));
        billCreationResource.setItems(List.of(item));

        //When/Then
        MvcResult result = mockMvc.perform(post(BILL_ENDPOINT).header(JWT_HEADER, bearerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(mapper.writeValueAsString(billCreationResource)))
                .andExpect(status().is4xxClientError()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);

        assertEquals(INVALID_INPUTS, error.getMessage());
        assertEquals(1, error.getErrors().size());
        assertEquals(NUMBER_OUT_OF_BOUNDS_12_2, error.getErrors().get(0).getMessage());
    }

    @Test
    @DisplayName("Should return error for item cost over 2 decimal points")
    void ShouldReturnErrorForItemCostOver2Decimals() throws Exception {
        //Given
        final var billCreationResource = BillCreationResourceFixture.getDefault();
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        final var item = ItemResourceFixture.getDefault();
        item.setCost(BigDecimal.valueOf(12.345));
        billCreationResource.setItems(List.of(item));

        //When/Then
        MvcResult result = mockMvc.perform(post(BILL_ENDPOINT).header(JWT_HEADER, bearerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(mapper.writeValueAsString(billCreationResource)))
                .andExpect(status().is4xxClientError()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);

        assertEquals(INVALID_INPUTS, error.getMessage());
        assertEquals(1, error.getErrors().size());
        assertEquals(NUMBER_OUT_OF_BOUNDS_12_2, error.getErrors().get(0).getMessage());
    }

    @Test
    @DisplayName("Should return error for null item list")
    void ShouldReturnErrorMissingItemList() throws Exception {
        //Given
        final var billCreationResource = BillCreationResourceFixture.getDefault();
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        billCreationResource.setItems(null);

        //When/Then
        MvcResult result = mockMvc.perform(post(BILL_ENDPOINT).header(JWT_HEADER, bearerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(mapper.writeValueAsString(billCreationResource)))
                .andExpect(status().is4xxClientError()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);

        assertEquals(INVALID_INPUTS, error.getMessage());
        assertEquals(1, error.getErrors().size());
        assertEquals(MUST_NOT_BE_NULL, error.getErrors().get(0).getMessage());
    }

    @Test
    @DisplayName("Should return error when both tip percent and tip amount are present")
    void ShouldReturnErrorWhenTipPercentAndAmount() throws Exception {
        //Given
        final var billCreationResource = BillCreationResourceFixture.getDefault();
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        billCreationResource.setTipAmount(BigDecimal.ONE);
        billCreationResource.setTipPercent(BigDecimal.TEN);

        //When/Then
        MvcResult result = mockMvc.perform(post(BILL_ENDPOINT).header(JWT_HEADER, bearerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(mapper.writeValueAsString(billCreationResource)))
                .andExpect(status().is4xxClientError()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);

        assertEquals(HAVE_BOTH_TIP_AMOUNT_PERCENT, error.getMessage());
        assertTrue(error.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Should return error for a negative tip amount")
    void ShouldReturnErrorForNegativeTipAmount() throws Exception {
        //Given
        final var billCreationResource = BillCreationResourceFixture.getDefault();
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        billCreationResource.setTipAmount(BigDecimal.valueOf(-5));
        billCreationResource.setTipPercent(null);

        //When/Then
        MvcResult result = mockMvc.perform(post(BILL_ENDPOINT).header(JWT_HEADER, bearerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(mapper.writeValueAsString(billCreationResource)))
                .andExpect(status().is4xxClientError()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);

        assertEquals(INVALID_INPUTS, error.getMessage());
        assertEquals(1, error.getErrors().size());
        assertEquals(NUMBER_MUST_BE_POSITIVE, error.getErrors().get(0).getMessage());
    }

    @Test
    @DisplayName("Should return error for tip amount over 12 digits")
    void ShouldReturnErrorForTipAmountOver12Digits() throws Exception {
        //Given
        final var billCreationResource = BillCreationResourceFixture.getDefault();
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        billCreationResource.setTipAmount(BigDecimal.valueOf(1234567898765.75));
        billCreationResource.setTipPercent(null);

        //When/Then
        MvcResult result = mockMvc.perform(post(BILL_ENDPOINT).header(JWT_HEADER, bearerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(mapper.writeValueAsString(billCreationResource)))
                .andExpect(status().is4xxClientError()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);

        assertEquals(INVALID_INPUTS, error.getMessage());
        assertEquals(1, error.getErrors().size());
        assertEquals(NUMBER_OUT_OF_BOUNDS_12_2, error.getErrors().get(0).getMessage());
    }

    @Test
    @DisplayName("Should return error for tip amount over 2 decimal points")
    void ShouldReturnErrorForTipAmountOver2Decimals() throws Exception {
        //Given
        final var billCreationResource = BillCreationResourceFixture.getDefault();
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        billCreationResource.setTipAmount(BigDecimal.valueOf(20.351));
        billCreationResource.setTipPercent(null);

        //When/Then
        MvcResult result = mockMvc.perform(post(BILL_ENDPOINT).header(JWT_HEADER, bearerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(mapper.writeValueAsString(billCreationResource)))
                .andExpect(status().is4xxClientError()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);

        assertEquals(INVALID_INPUTS, error.getMessage());
        assertEquals(1, error.getErrors().size());
        assertEquals(NUMBER_OUT_OF_BOUNDS_12_2, error.getErrors().get(0).getMessage());
    }

    @Test
    @DisplayName("Should return error for a negative tip percent")
    void ShouldReturnErrorForNegativeTipPercent() throws Exception {
        //Given
        final var billCreationResource = BillCreationResourceFixture.getDefault();
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        billCreationResource.setTipAmount(null);
        billCreationResource.setTipPercent(BigDecimal.valueOf(-5));

        //When/Then
        MvcResult result = mockMvc.perform(post(BILL_ENDPOINT).header(JWT_HEADER, bearerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(mapper.writeValueAsString(billCreationResource)))
                .andExpect(status().is4xxClientError()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);

        assertEquals(INVALID_INPUTS, error.getMessage());
        assertEquals(1, error.getErrors().size());
        assertEquals(NUMBER_MUST_BE_POSITIVE, error.getErrors().get(0).getMessage());
    }

    @Test
    @DisplayName("Should return error for tip percent over 3 digits")
    void ShouldReturnErrorForTipPercentOver3Digits() throws Exception {
        //Given
        final var billCreationResource = BillCreationResourceFixture.getDefault();
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        billCreationResource.setTipAmount(null);
        billCreationResource.setTipPercent(BigDecimal.valueOf(1234.56));

        //When/Then
        MvcResult result = mockMvc.perform(post(BILL_ENDPOINT).header(JWT_HEADER, bearerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(mapper.writeValueAsString(billCreationResource)))
                .andExpect(status().is4xxClientError()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);

        assertEquals(INVALID_INPUTS, error.getMessage());
        assertEquals(1, error.getErrors().size());
        assertEquals(NUMBER_OUT_OF_BOUNDS_3_4, error.getErrors().get(0).getMessage());
    }

    @Test
    @DisplayName("Should return error for tip percent over 4 decimal points")
    void ShouldReturnErrorForTipPercentOver4Decimals() throws Exception {
        //Given
        final var billCreationResource = BillCreationResourceFixture.getDefault();
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        billCreationResource.setTipAmount(null);
        billCreationResource.setTipPercent(BigDecimal.valueOf(20.12345));

        //When/Then
        MvcResult result = mockMvc.perform(post(BILL_ENDPOINT).header(JWT_HEADER, bearerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(mapper.writeValueAsString(billCreationResource)))
                .andExpect(status().is4xxClientError()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);

        assertEquals(INVALID_INPUTS, error.getMessage());
        assertEquals(1, error.getErrors().size());
        assertEquals(NUMBER_OUT_OF_BOUNDS_3_4, error.getErrors().get(0).getMessage());
    }
}