package proj.kedabra.billsnap.presentation.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import proj.kedabra.billsnap.business.model.entities.Bill;
import proj.kedabra.billsnap.business.repository.BillRepository;
import proj.kedabra.billsnap.business.utils.enums.BillStatusEnum;
import proj.kedabra.billsnap.business.utils.enums.InvitationStatusEnum;
import proj.kedabra.billsnap.fixtures.AssociateBillFixture;
import proj.kedabra.billsnap.fixtures.BillCreationResourceFixture;
import proj.kedabra.billsnap.fixtures.InviteRegisteredResourceFixture;
import proj.kedabra.billsnap.fixtures.ItemCreationResourceFixture;
import proj.kedabra.billsnap.fixtures.UserFixture;
import proj.kedabra.billsnap.presentation.ApiError;
import proj.kedabra.billsnap.presentation.ApiSubError;
import proj.kedabra.billsnap.presentation.resources.AccountStatusResource;
import proj.kedabra.billsnap.presentation.resources.AssociateBillResource;
import proj.kedabra.billsnap.presentation.resources.BillCreationResource;
import proj.kedabra.billsnap.presentation.resources.BillResource;
import proj.kedabra.billsnap.presentation.resources.BillSplitResource;
import proj.kedabra.billsnap.presentation.resources.InviteRegisteredResource;
import proj.kedabra.billsnap.presentation.resources.ItemPercentageSplitResource;
import proj.kedabra.billsnap.presentation.resources.PendingRegisteredBillSplitResource;
import proj.kedabra.billsnap.presentation.resources.ShortBillResource;
import proj.kedabra.billsnap.security.JwtService;
import proj.kedabra.billsnap.utils.ErrorMessageEnum;
import proj.kedabra.billsnap.utils.SpringProfiles;

@Tag("integration")
@ActiveProfiles(SpringProfiles.TEST)
@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("squid:S00112")
@AutoConfigureTestDatabase
@Transactional
class BillControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private BillRepository billRepository;

    private static final String BILL_ENDPOINT = "/bills";

    private static final String BILL_BILLID_ENDPOINT = "/bills/%d";

    private static final String BILL_BILLID_ACCOUNTS_ENDPOINT = "/bills/%d/accounts";

    private static final String JWT_HEADER = "Authorization";

    private static final String JWT_PREFIX = "Bearer ";

    private static final String MUST_NOT_BE_BLANK = "must not be blank";

    private static final String MUST_NOT_BE_EMPTY = "must not be empty";

    private static final String MUST_NOT_BE_NULL = "must not be null";

    private static final String INVALID_INPUTS = "Invalid Inputs. Please fix the following errors";

    private static final String WRONG_SIZE_0_TO_50 = "size must be between 0 and 50";

    private static final String WRONG_SIZE_0_TO_30 = "size must be between 0 and 30";

    private static final String WRONG_SIZE_0_TO_20 = "size must be between 0 and 20";

    private static final String HAVE_BOTH_TIP_AMOUNT_PERCENT = "Only one type of tipping is supported. Please make sure only either tip amount or tip percent is set.";

    private static final String NUMBER_MUST_BE_POSITIVE = "the number must be positive";

    private static final String NUMBER_OUT_OF_BOUNDS_12_2 = "numeric value out of bounds (<12 digits>.<2 digits> expected)";

    private static final String NUMBER_OUT_OF_BOUNDS_3_4 = "numeric value out of bounds (<3 digits>.<4 digits> expected)";

    private static final String NOT_IN_EMAIL_FORMAT = "Must be in an email format. ex: test@email.com.";

    @Test
    @DisplayName("Should return proper reply with 201 status for POST /bill")
    void ShouldReturn201ForNormalCaseAddBill() throws Exception {
        //Given
        final var billCreationResource = BillCreationResourceFixture.getDefault();
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);

        //When/Then
        MvcResult result = performMvcPostRequest201Created(bearerToken, billCreationResource);
        String content = result.getResponse().getContentAsString();
        BillResource response = mapper.readValue(content, BillResource.class);

        verify201NormalCaseAddBill(user, billCreationResource, response);
        assertThat(response.getAccountsList().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should return proper reply with 201 for POST /bill with accountsList defined")
    void ShouldReturn201NormalCaseAddBillWithAccountsList() throws Exception {
        //Given
        final var billCreationResource = BillCreationResourceFixture.getDefault();
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        final String existentEmail = "test@email.com";
        billCreationResource.setAccountsList(List.of(existentEmail));

        //When/Then
        MvcResult result = performMvcPostRequest201Created(bearerToken, billCreationResource);
        String content = result.getResponse().getContentAsString();
        BillResource response = mapper.readValue(content, BillResource.class);

        verify201NormalCaseAddBill(user, billCreationResource, response);
        assertThat(response.getAccountsList()).isNotEmpty();
        final List<AccountStatusResource> inputtedAccounts = response.getAccountsList().stream()
                .filter(acc -> acc.getAccount().getEmail().equals(existentEmail)).collect(Collectors.toList());
        assertThat(inputtedAccounts.get(0).getStatus()).isEqualTo(InvitationStatusEnum.PENDING);
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
        MvcResult result = performMvcPostRequest4xxFailure(bearerToken, billCreationResource);
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
        MvcResult result = performMvcPostRequest4xxFailure(bearerToken, billCreationResource);
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
        MvcResult result = performMvcPostRequest4xxFailure(bearerToken, billCreationResource);
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
        MvcResult result = performMvcPostRequest4xxFailure(bearerToken, billCreationResource);
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
        final var item = ItemCreationResourceFixture.getDefault();
        item.setName("");
        billCreationResource.setItems(List.of(item));

        //When/Then
        MvcResult result = performMvcPostRequest4xxFailure(bearerToken, billCreationResource);
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
        final var item = ItemCreationResourceFixture.getDefault();
        item.setName("toooooo longggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggg");
        billCreationResource.setItems(List.of(item));

        //When/Then
        MvcResult result = performMvcPostRequest4xxFailure(bearerToken, billCreationResource);
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
        final var item = ItemCreationResourceFixture.getDefault();
        item.setCost(BigDecimal.valueOf(-5));
        billCreationResource.setItems(List.of(item));

        //When/Then
        MvcResult result = performMvcPostRequest4xxFailure(bearerToken, billCreationResource);
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
        final var item = ItemCreationResourceFixture.getDefault();
        item.setCost(BigDecimal.valueOf(1234567898765.75));
        billCreationResource.setItems(List.of(item));

        //When/Then
        MvcResult result = performMvcPostRequest4xxFailure(bearerToken, billCreationResource);
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
        final var item = ItemCreationResourceFixture.getDefault();
        item.setCost(BigDecimal.valueOf(12.345));
        billCreationResource.setItems(List.of(item));

        //When/Then
        MvcResult result = performMvcPostRequest4xxFailure(bearerToken, billCreationResource);
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
        MvcResult result = performMvcPostRequest4xxFailure(bearerToken, billCreationResource);
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
        MvcResult result = performMvcPostRequest4xxFailure(bearerToken, billCreationResource);
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
        MvcResult result = performMvcPostRequest4xxFailure(bearerToken, billCreationResource);
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
        MvcResult result = performMvcPostRequest4xxFailure(bearerToken, billCreationResource);
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
        MvcResult result = performMvcPostRequest4xxFailure(bearerToken, billCreationResource);
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
        MvcResult result = performMvcPostRequest4xxFailure(bearerToken, billCreationResource);
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
        MvcResult result = performMvcPostRequest4xxFailure(bearerToken, billCreationResource);
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
        MvcResult result = performMvcPostRequest4xxFailure(bearerToken, billCreationResource);
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);

        assertEquals(INVALID_INPUTS, error.getMessage());
        assertEquals(1, error.getErrors().size());
        assertEquals(NUMBER_OUT_OF_BOUNDS_3_4, error.getErrors().get(0).getMessage());
    }


    @Test
    @DisplayName("Should return exception if one or more emails in accountsList is not in email format")
    void ShouldReturnExceptionIfOneOrMoreEmailsIsNotEmailFormat() throws Exception {
        //Given
        final var billCreationResource = BillCreationResourceFixture.getDefault();
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        final String existentEmail = "test@email.com";
        final String invalidEmail = "nonexistentcom";
        billCreationResource.setAccountsList(List.of(existentEmail, invalidEmail));

        //When/Then
        MvcResult result = performMvcPostRequest4xxFailure(bearerToken, billCreationResource);
        ApiError error = verifyInvalidInputs(result, 1);
        assertThat(error.getErrors().get(0).getMessage()).isEqualTo(NOT_IN_EMAIL_FORMAT);
    }

    @Test
    @DisplayName("Should return exception if one or more emails in accountsList is blank")
    void ShouldReturnExceptionIfOneOrMoreEmailsIsBlank() throws Exception {
        //Given
        final var billCreationResource = BillCreationResourceFixture.getDefault();
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        final String existentEmail = "test@email.com";
        final String invalidEmail = " ";
        billCreationResource.setAccountsList(List.of(existentEmail, invalidEmail));

        //When/Then
        MvcResult result = performMvcPostRequest4xxFailure(bearerToken, billCreationResource);
        ApiError error = verifyInvalidInputs(result, 2);

        final var errorList = error.getErrors().stream()
                .map(ApiSubError::getMessage)
                .filter(msg -> msg.equals(MUST_NOT_BE_BLANK)
                        || msg.equals(NOT_IN_EMAIL_FORMAT))
                .collect(java.util.stream.Collectors.toList());

        assertThat(errorList.size()).isEqualTo(error.getErrors().size());
    }

    @Test
    @DisplayName("Should return exception if one or more emails in accountsList is too long")
    void ShouldReturnExceptionIfOneOrMoreEmailsIsTooLong() throws Exception {
        //Given
        final var billCreationResource = BillCreationResourceFixture.getDefault();
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        final String existentEmail = "test@email.com";
        final String invalidEmail = "toolongggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggg@email.com";
        billCreationResource.setAccountsList(List.of(existentEmail, invalidEmail));

        //When/Then
        MvcResult result = performMvcPostRequest4xxFailure(bearerToken, billCreationResource);
        ApiError error = verifyInvalidInputs(result, 2);

        final var errorList = error.getErrors().stream()
                .map(ApiSubError::getMessage)
                .filter(msg -> msg.equals(WRONG_SIZE_0_TO_50)
                        || msg.equals(NOT_IN_EMAIL_FORMAT))
                .collect(java.util.stream.Collectors.toList());

        assertThat(errorList.size()).isEqualTo(error.getErrors().size());
    }

    @Test
    @DisplayName("Should return empty List if no bills")
    void shouldReturnEmptyListOfResourceIfNoBills() throws Exception {
        //Given
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);

        //When/Then
        final MvcResult result = performMvcGetRequest200AllBills(bearerToken);
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

        MvcResult result = performMvcPostRequest201Created(bearerToken, billCreationResource);
        String content = result.getResponse().getContentAsString();
        final BillResource billOne = mapper.readValue(content, BillResource.class);

        result = performMvcPostRequest201Created(bearerToken, billCreationResource);
        content = result.getResponse().getContentAsString();
        final BillResource billTwo = mapper.readValue(content, BillResource.class);

        //When/Then
        result = performMvcGetRequest200AllBills(bearerToken);
        content = result.getResponse().getContentAsString();
        final List<ShortBillResource> response = mapper.readValue(content, new TypeReference<>() {
        });

        verifyShortBillResources(billOne, response.get(0), BillStatusEnum.OPEN);
        verifyShortBillResources(billTwo, response.get(1), BillStatusEnum.OPEN);
    }

    @Test
    @DisplayName("Should return exception if null bill id is given for PUT /bills")
    void shouldReturnExceptionIfNullBillIdGivenPut() throws Exception {
        //Given
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        final var associateBillResource = AssociateBillFixture.getDefault();
        associateBillResource.setId(null);

        //When/Then
        MvcResult result = performMvcPutRequest4xxFailure(bearerToken, associateBillResource);
        ApiError error = verifyInvalidInputs(result, 1);
        assertThat(error.getErrors().get(0).getMessage()).isEqualTo(MUST_NOT_BE_NULL);
    }

    @Test
    @DisplayName("Should return exception if null itemsPerAccount is given for PUT /bills")
    void shouldReturnExceptionIfNullItemsPerAccountGivenPut() throws Exception {
        //Given
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        final var associateBillResource = AssociateBillFixture.getDefault();
        associateBillResource.setItemsPerAccount(null);

        //When/Then
        MvcResult result = performMvcPutRequest4xxFailure(bearerToken, associateBillResource);
        ApiError error = verifyInvalidInputs(result, 1);
        assertThat(error.getErrors().get(0).getMessage()).isEqualTo(MUST_NOT_BE_NULL);
    }

    @Test
    @DisplayName("Should return exception if null email in ItemAssociationResource is given for PUT /bills")
    void shouldReturnExceptionIfNullEmailInItemAssociationResourceGivenPut() throws Exception {
        //Given
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        final var associateBillResource = AssociateBillFixture.getDefault();
        associateBillResource.getItemsPerAccount().get(0).setEmail(null);

        //When/Then
        MvcResult result = performMvcPutRequest4xxFailure(bearerToken, associateBillResource);
        ApiError error = verifyInvalidInputs(result, 1);
        assertThat(error.getErrors().get(0).getMessage()).isEqualTo(MUST_NOT_BE_BLANK);
    }

    @Test
    @DisplayName("Should return exception if blank email in ItemAssociationResource is given for PUT /bills")
    void shouldReturnExceptionIfBlankEmailInItemAssociationResourceGivenPut() throws Exception {
        //Given
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        final var associateBillResource = AssociateBillFixture.getDefault();
        associateBillResource.getItemsPerAccount().get(0).setEmail(" ");

        //When/Then
        MvcResult result = performMvcPutRequest4xxFailure(bearerToken, associateBillResource);
        ApiError error = verifyInvalidInputs(result, 2);

        final var errorList = error.getErrors().stream()
                .map(ApiSubError::getMessage)
                .filter(msg -> msg.equals(MUST_NOT_BE_BLANK)
                        || msg.equals(NOT_IN_EMAIL_FORMAT))
                .collect(java.util.stream.Collectors.toList());

        assertThat(errorList.size()).isEqualTo(error.getErrors().size());
    }

    @Test
    @DisplayName("Should return exception if too long of an email in ItemAssociationResource is given for PUT /bills")
    void shouldReturnExceptionIfTooLongEmailInItemAssociationResourceGivenPut() throws Exception {
        //Given
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        final var associateBillResource = AssociateBillFixture.getDefault();
        associateBillResource.getItemsPerAccount().get(0).setEmail("toolongggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggg@email.com");

        //When/Then
        MvcResult result = performMvcPutRequest4xxFailure(bearerToken, associateBillResource);
        ApiError error = verifyInvalidInputs(result, 2);

        final var errorList = error.getErrors().stream()
                .map(ApiSubError::getMessage)
                .filter(msg -> msg.equals(WRONG_SIZE_0_TO_50)
                        || msg.equals(NOT_IN_EMAIL_FORMAT))
                .collect(java.util.stream.Collectors.toList());

        assertThat(errorList.size()).isEqualTo(error.getErrors().size());
    }

    @Test
    @DisplayName("Should return exception if null items list in ItemAssociationResource is given for PUT /bills")
    void shouldReturnExceptionIfNullItemsListInItemAssociationResourceGivenPut() throws Exception {
        //Given
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        final var associateBillResource = AssociateBillFixture.getDefault();
        associateBillResource.getItemsPerAccount().get(0).setItems(null);

        //When/Then
        MvcResult result = performMvcPutRequest4xxFailure(bearerToken, associateBillResource);
        ApiError error = verifyInvalidInputs(result, 1);
        assertThat(error.getErrors().get(0).getMessage()).isEqualTo(MUST_NOT_BE_NULL);
    }

    @Test
    @DisplayName("Should return exception if null itemId in ItemPercentageResource is given for PUT /bills")
    void shouldReturnExceptionIfNullItemIdInItemPercentageResourceGivenPut() throws Exception {
        //Given
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        final var associateBillResource = AssociateBillFixture.getDefault();
        associateBillResource.getItemsPerAccount().get(0).getItems().get(0).setItemId(null);

        //When/Then
        MvcResult result = performMvcPutRequest4xxFailure(bearerToken, associateBillResource);
        ApiError error = verifyInvalidInputs(result, 1);
        assertThat(error.getErrors().get(0).getMessage()).isEqualTo(MUST_NOT_BE_NULL);
    }

    @Test
    @DisplayName("Should return exception if null percentage in ItemPercentageResource is given for PUT /bills")
    void shouldReturnExceptionIfNullPercentageInItemPercentageResourceGivenPut() throws Exception {
        //Given
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        final var associateBillResource = AssociateBillFixture.getDefault();
        associateBillResource.getItemsPerAccount().get(0).getItems().get(0).setPercentage(null);

        //When/Then
        MvcResult result = performMvcPutRequest4xxFailure(bearerToken, associateBillResource);
        ApiError error = verifyInvalidInputs(result, 1);
        assertThat(error.getErrors().get(0).getMessage()).isEqualTo(MUST_NOT_BE_NULL);
    }

    @Test
    @DisplayName("Should return exception if over 3 integer percentage in ItemPercentageResource is given for PUT /bills")
    void shouldReturnExceptionIfOver3IntegerPercentageInItemPercentageResourceGivenPut() throws Exception {
        //Given
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        final var associateBillResource = AssociateBillFixture.getDefault();
        associateBillResource.getItemsPerAccount().get(0).getItems().get(0).setPercentage(BigDecimal.valueOf(1234.50));

        //When/Then
        MvcResult result = performMvcPutRequest4xxFailure(bearerToken, associateBillResource);
        ApiError error = verifyInvalidInputs(result, 1);
        assertThat(error.getErrors().get(0).getMessage()).isEqualTo(NUMBER_OUT_OF_BOUNDS_3_4);
    }

    @Test
    @DisplayName("Should return exception if over 4 decimal percentage in ItemPercentageResource is given for PUT /bills")
    void shouldReturnExceptionIfOver4DecimalPercentageInItemPercentageResourceGivenPut() throws Exception {
        //Given
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        final var associateBillResource = AssociateBillFixture.getDefault();
        associateBillResource.getItemsPerAccount().get(0).getItems().get(0).setPercentage(BigDecimal.valueOf(50.12345));

        //When/Then
        MvcResult result = performMvcPutRequest4xxFailure(bearerToken, associateBillResource);
        ApiError error = verifyInvalidInputs(result, 1);
        assertThat(error.getErrors().get(0).getMessage()).isEqualTo(NUMBER_OUT_OF_BOUNDS_3_4);
    }

    @Test
    @DisplayName("Should return exception if negative percentage in ItemPercentageResource is given for PUT /bills")
    void shouldReturnExceptionIfNegativePercentageInItemPercentageResourceGivenPut() throws Exception {
        //Given
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        final var associateBillResource = AssociateBillFixture.getDefault();
        associateBillResource.getItemsPerAccount().get(0).getItems().get(0).setPercentage(BigDecimal.valueOf(-50));

        //When/Then
        MvcResult result = performMvcPutRequest4xxFailure(bearerToken, associateBillResource);
        ApiError error = verifyInvalidInputs(result, 1);
        assertThat(error.getErrors().get(0).getMessage()).isEqualTo(NUMBER_MUST_BE_POSITIVE);
    }

    @ParameterizedTest
    @EnumSource(value = BillStatusEnum.class, names = {"IN_PROGRESS", "RESOLVED"})
    @DisplayName("Should return exception if Bill is not in Open status for Associate Bills")
    void shouldReturnExceptionIfBillIsNotOpenForAssociateBills(BillStatusEnum status) throws Exception {
        //Given
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        final var associateBillResource = AssociateBillFixture.getDefault();
        final Bill billById = billRepository.getBillById(associateBillResource.getId());
        billById.setStatus(status);

        //When/Then
        final MvcResult result = performMvcPutRequest4xxFailure(bearerToken, associateBillResource);
        final String content = result.getResponse().getContentAsString();
        final ApiError apiError = mapper.readValue(content, ApiError.class);

        assertThat(apiError.getMessage()).isEqualTo(ErrorMessageEnum.BILL_IS_NOT_OPEN.getMessage());
    }

    @ParameterizedTest
    @EnumSource(value = BillStatusEnum.class, names = {"IN_PROGRESS", "RESOLVED"})
    @DisplayName("Should return exception if Bill is not in Open status for Invite Registered User To Bill")
    void shouldReturnExceptionIfBillIsNotOpenInInviteRegisteredPost(BillStatusEnum status) throws Exception {
        //Given
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        final var existentEmail = "test@email.com";
        final var inviteRegisteredResource = InviteRegisteredResourceFixture.getDefault();
        inviteRegisteredResource.setAccounts(List.of(existentEmail));
        final var existentBillId = 1000L;
        final Bill billById = billRepository.getBillById(existentBillId);
        billById.setStatus(status);

        //When/Then
        final MvcResult result = doMvcPostRequestInviteRegistered405Failure(bearerToken, inviteRegisteredResource, existentBillId);
        final String content = result.getResponse().getContentAsString();
        final ApiError apiError = mapper.readValue(content, ApiError.class);

        assertThat(apiError.getMessage()).isEqualTo(ErrorMessageEnum.BILL_IS_NOT_OPEN.getMessage());
    }

    @Test
    @DisplayName("Should return 400 exception if 1+ emails in InviteRegisteredResource are not in email format for POST /bills/{billId}/accounts")
    void shouldReturnExceptionIfListEmailsNotEmailFormatInInviteRegisteredResourceGivenPost() throws Exception {
        //Given the User makes a request for a bill where User is the responsible
        final var inviteRegisteredResource = InviteRegisteredResourceFixture.getDefault();
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        final var existentEmail = "test@email.com";
        final var invalidEmail = "notemailformatcom";
        inviteRegisteredResource.setAccounts(List.of(existentEmail, invalidEmail));
        final var existentBillId = 1000L;

        //When/Then
        final MvcResult result = performMvcPostRequestBadRequestInviteRegistered(bearerToken, inviteRegisteredResource, existentBillId);
        final ApiError error = verifyInvalidInputs(result, 1);
        assertThat(error.getErrors().get(0).getMessage()).isEqualTo(NOT_IN_EMAIL_FORMAT);
    }

    @Test
    @DisplayName("Should return 400 exception if 1+ emails in InviteRegisteredResource is blank for POST /bills/{billId}/accounts")
    void shouldReturnExceptionIfListEmailsBlankInInviteRegisteredResourceGivenPost() throws Exception {
        //Given the User makes a request for a bill where User is the responsible
        final var inviteRegisteredResource = InviteRegisteredResourceFixture.getDefault();
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        final var existentEmail = "test@email.com";
        final var invalidEmail = " ";
        inviteRegisteredResource.setAccounts(List.of(existentEmail, invalidEmail));
        final var existentBillId = 1000L;

        //When/Then
        final MvcResult result = performMvcPostRequestBadRequestInviteRegistered(bearerToken, inviteRegisteredResource, existentBillId);
        final ApiError error = verifyInvalidInputs(result, 2);

        final var errorList = error.getErrors().stream()
                .map(ApiSubError::getMessage)
                .filter(msg -> msg.equals(MUST_NOT_BE_BLANK)
                        || msg.equals(NOT_IN_EMAIL_FORMAT))
                .collect(java.util.stream.Collectors.toList());

        assertThat(errorList.size()).isEqualTo(error.getErrors().size());
    }

    @Test
    @DisplayName("Should return 400 exception if 1+ emails in InviteRegisteredResource is too long for POST /bills/{billId}/accounts")
    void shouldReturnExceptionIfListEmailsTooLongInInviteRegisteredResourceGivenPost() throws Exception {
        //Given the User makes a request for a bill where User is the responsible
        final var inviteRegisteredResource = InviteRegisteredResourceFixture.getDefault();
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        final var existentEmail = "test@email.com";
        final var invalidEmail = "toolongggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggg@email.com";
        inviteRegisteredResource.setAccounts(List.of(existentEmail, invalidEmail));
        final var existentBillId = 1000L;

        //When/Then
        final MvcResult result = performMvcPostRequestBadRequestInviteRegistered(bearerToken, inviteRegisteredResource, existentBillId);
        final ApiError error = verifyInvalidInputs(result, 2);

        final var errorList = error.getErrors().stream()
                .map(ApiSubError::getMessage)
                .filter(msg -> msg.equals(WRONG_SIZE_0_TO_50)
                        || msg.equals(NOT_IN_EMAIL_FORMAT))
                .collect(java.util.stream.Collectors.toList());

        assertThat(errorList.size()).isEqualTo(error.getErrors().size());
    }

    @Test
    @DisplayName("Should return 400 exception if accounts list is empty in InviteRegisteredResource for POST /bills/{billId}/accounts")
    void shouldReturnExceptionIfListEmailsEmptyInInviteRegisteredResourceGivenPost() throws Exception {
        //Given the User makes a request for a bill where User is the responsible
        final var inviteRegisteredResource = InviteRegisteredResourceFixture.getDefault();
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        inviteRegisteredResource.setAccounts(new ArrayList<>());
        final var existentBillId = 1000L;

        //When/Then
        final MvcResult result = performMvcPostRequestBadRequestInviteRegistered(bearerToken, inviteRegisteredResource, existentBillId);
        final ApiError error = verifyInvalidInputs(result, 1);
        assertThat(error.getErrors().get(0).getMessage()).isEqualTo(MUST_NOT_BE_EMPTY);
    }

    @Test
    @DisplayName("Should return 400 exception if accounts list is null in InviteRegisteredResource for POST /bills/{billId}/accounts")
    void shouldReturnExceptionIfListEmailsNullInInviteRegisteredResourceGivenPost() throws Exception {
        //Given the User makes a request for a bill where User is the responsible
        final var inviteRegisteredResource = InviteRegisteredResourceFixture.getDefault();
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        inviteRegisteredResource.setAccounts(null);
        final var existentBillId = 1000L;

        //When/Then
        final MvcResult result = performMvcPostRequestBadRequestInviteRegistered(bearerToken, inviteRegisteredResource, existentBillId);
        final ApiError error = verifyInvalidInputs(result, 1);
        assertThat(error.getErrors().get(0).getMessage()).isEqualTo(MUST_NOT_BE_EMPTY);
    }

    @Test
    @DisplayName("Should return 200 when adding one existing user not part of Bill in Invite Registered Person to Bill")
    void shouldReturn200ForNormalCaseOneUserInviteRegisteredGivenPost() throws Exception {
        //Given the User makes a request for a bill where User is the responsible
        final var inviteRegisteredResource = InviteRegisteredResourceFixture.getDefault();
        final var user = UserFixture.getDefaultWithEmailAndPassword("test@email.com", "notEncrypted");
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        final var accountNotInBill = "nobills@inthisemail.com";
        final var existentBillId = 1000L;
        inviteRegisteredResource.setAccounts(List.of(accountNotInBill));

        //When
        final var mvcResult = performMvcPostRequest200OKInviteRegistered(bearerToken, inviteRegisteredResource, existentBillId);
        final String content = mvcResult.getResponse().getContentAsString();
        final PendingRegisteredBillSplitResource response = mapper.readValue(content, PendingRegisteredBillSplitResource.class);

        //Then
        assertThat((int) response.getPendingAccounts().stream()
                .filter(acc -> acc.equals(accountNotInBill)).count())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Should return 200 when adding multiple existing users not part of Bill in Invite Registered Person to Bill")
    void shouldReturn200ForNormalCaseInviteRegisteredGivenPost() throws Exception {
        //Given the User makes a request for a bill where User is the responsible
        final var inviteRegisteredResource = InviteRegisteredResourceFixture.getDefault();
        final var user = UserFixture.getDefaultWithEmailAndPassword("test@email.com", "notEncrypted");
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        final var accountNotInBill = "nobills@inthisemail.com";
        final var secondAccountNotInBill = "user@withABill.com";
        final var existentBillId = 1000L;
        inviteRegisteredResource.setAccounts(List.of(accountNotInBill, secondAccountNotInBill));

        //When
        final var mvcResult = performMvcPostRequest200OKInviteRegistered(bearerToken, inviteRegisteredResource, existentBillId);
        final String content = mvcResult.getResponse().getContentAsString();
        final PendingRegisteredBillSplitResource response = mapper.readValue(content, PendingRegisteredBillSplitResource.class);

        //Then
        assertThat((int) response.getPendingAccounts().stream()
                .filter(acc -> acc.equals(accountNotInBill) || acc.equals(secondAccountNotInBill)).count())
                .isEqualTo(2);
    }

    @Test
    @DisplayName("Should return 200 when getting successfully detailed bill")
    void shouldReturn200WhenGettingSuccessfullyDetailedBill() throws Exception {
        // Given
        final var user = UserFixture.getDefaultWithEmailAndPassword("test@email.com", "notEncrypted");
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        final var existentBillId = 1000L;

        // When
        final var mvcResult = performMvcGetRequest200(bearerToken, existentBillId);
        final String content = mvcResult.getResponse().getContentAsString();
        final BillSplitResource response = mapper.readValue(content, BillSplitResource.class);

        // Then
        assertThat(response.getId()).isEqualTo(existentBillId);
    }

    @Test
    @DisplayName("GET billId should return mapped detailed bill after creating one bill")
    void shouldReturnMappedDetailedBillAfterCreatingOneBill() throws Exception {
        //Given
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        final var billCreationResource = BillCreationResourceFixture.getDefault();

        final var result = performMvcPostRequest201Created(bearerToken, billCreationResource);
        final String content = result.getResponse().getContentAsString();
        final BillResource createdBill = mapper.readValue(content, BillResource.class);

        //When/Then
        final var mvcResult = performMvcGetRequest200(bearerToken, createdBill.getId());
        final String getContent = mvcResult.getResponse().getContentAsString();
        final BillSplitResource billSplitResource = mapper.readValue(getContent, BillSplitResource.class);

        verifyBillSplitResources(createdBill, billSplitResource);
    }

    @Test
    @DisplayName("Should return 400 when bill is not found with billId")
    void shouldReturn400WhenBillIsNotFoundWithBillId() throws Exception {
        // Given
        final var user = UserFixture.getDefaultWithEmailAndPassword("test@email.com", "notEncrypted");
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        final var nonExistentBillId = 69420L;

        // When
        final var mvcResult = performMvcGetRequest400Failure(bearerToken, nonExistentBillId);
        final String content = mvcResult.getResponse().getContentAsString();
        final ApiError error = mapper.readValue(content, ApiError.class);

        // Then
        assertThat(error.getMessage()).isEqualTo(ErrorMessageEnum.BILL_DOES_NOT_EXIST.getMessage());
    }

    @Test
    @DisplayName("Should return 401 when Token is not valid")
    void shouldReturn401WhenTokenIsNotValid() throws Exception {
        // Given
        final var bearerToken = "tOkEn";
        final var existentBillId = 1000L;

        // When
        final var mvcResult = performMvcGetRequest401Failure(bearerToken, existentBillId);
        final String content = mvcResult.getResponse().getContentAsString();
        final ApiError error = mapper.readValue(content, ApiError.class);

        // Then
        assertThat(error.getMessage()).isEqualTo(ErrorMessageEnum.UNAUTHORIZED_ACCESS.getMessage());
    }

    @Test
    @DisplayName("Should return 403 when getting bill where user requesting is not part of Bill")
    void shouldReturn403GettingBillWhenUserIsNotPartOf() throws Exception {
        // Given
        final var user = UserFixture.getDefaultWithEmailAndPassword("test@email.com", "notEncrypted");
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        final var existentBillId = 1006L;

        // When
        final var mvcResult = performMvcGetRequest403Failure(bearerToken, existentBillId);
        final String content = mvcResult.getResponse().getContentAsString();
        final ApiError error = mapper.readValue(content, ApiError.class);

        // Then
        assertThat(error.getMessage()).isEqualTo(ErrorMessageEnum.ACCOUNT_IS_NOT_ASSOCIATED_TO_BILL.getMessage());
    }

    private void verifyShortBillResources(BillResource expectedBillResource, ShortBillResource actualBillResource, BillStatusEnum status) {
        assertEquals(expectedBillResource.getId(), actualBillResource.getId());
        assertEquals(expectedBillResource.getName(), actualBillResource.getName());
        assertEquals(expectedBillResource.getCategory(), actualBillResource.getCategory());
        assertEquals(status, actualBillResource.getStatus());
        assertEquals(0, expectedBillResource.getBalance().compareTo(actualBillResource.getBalance()));
    }

    private void verifyBillSplitResources(BillResource expectedBillResource, BillSplitResource actualBillResource) {
        assertEquals(expectedBillResource.getId(), actualBillResource.getId());
        assertEquals(expectedBillResource.getName(), actualBillResource.getName());
        assertEquals(expectedBillResource.getCategory(), actualBillResource.getCategory());
        assertEquals(expectedBillResource.getCompany(), actualBillResource.getCompany());
        assertEquals(expectedBillResource.getCreator(), actualBillResource.getCreator());
        assertEquals(expectedBillResource.getResponsible(), actualBillResource.getResponsible());
        assertEquals(BillStatusEnum.OPEN, actualBillResource.getStatus());
        assertEquals(0, expectedBillResource.getBalance().compareTo(actualBillResource.getBalance()));
        assertNotNull(expectedBillResource.getCreated());
        assertNotNull(expectedBillResource.getUpdated());
        assertNotNull(expectedBillResource.getId());

        final HashSet<ItemPercentageSplitResource> itemsList = new HashSet<>();
        actualBillResource.getItemsPerAccount().forEach(account -> itemsList.addAll(account.getItems()));
        assertEquals(expectedBillResource.getItems().size(), itemsList.size());

    }

    private void verify201NormalCaseAddBill(User user, BillCreationResource billCreationResource, BillResource response) {
        assertNotNull(response.getId());
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

    private MvcResult performMvcGetRequest200AllBills(String bearerToken) throws Exception {
        return mockMvc.perform(get(BILL_ENDPOINT).header(JWT_HEADER, bearerToken))
                .andExpect(status().isOk()).andReturn();
    }

    private MvcResult performMvcGetRequest200(String bearerToken, Long billId) throws Exception {
        return mockMvc.perform(get(String.format(BILL_BILLID_ENDPOINT, billId)).header(JWT_HEADER, bearerToken))
                .andExpect(status().isOk()).andReturn();
    }

    private MvcResult performMvcGetRequest400Failure(String bearerToken, Long billId) throws Exception {
        return mockMvc.perform(get(String.format(BILL_BILLID_ENDPOINT, billId)).header(JWT_HEADER, bearerToken))
                .andExpect(status().isBadRequest()).andReturn();
    }

    private MvcResult performMvcGetRequest401Failure(String bearerToken, Long billId) throws Exception {
        return mockMvc.perform(get(String.format(BILL_BILLID_ENDPOINT, billId)).header(JWT_HEADER, bearerToken))
                .andExpect(status().isUnauthorized()).andReturn();
    }

    private MvcResult performMvcGetRequest403Failure(String bearerToken, Long billId) throws Exception {
        return mockMvc.perform(get(String.format(BILL_BILLID_ENDPOINT, billId)).header(JWT_HEADER, bearerToken))
                .andExpect(status().isForbidden()).andReturn();
    }


    private MvcResult performMvcPostRequest201Created(String bearerToken, BillCreationResource billCreationResource) throws Exception {
        return mockMvc.perform(post(BILL_ENDPOINT).header(JWT_HEADER, bearerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(mapper.writeValueAsString(billCreationResource)))
                .andExpect(status().isCreated()).andReturn();
    }

    private MvcResult performMvcPostRequest200OKInviteRegistered(String bearerToken, InviteRegisteredResource inviteRegisteredResource, Long billId) throws Exception {
        return mockMvc.perform(post(String.format(BILL_BILLID_ACCOUNTS_ENDPOINT, billId)).header(JWT_HEADER, bearerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(mapper.writeValueAsString(inviteRegisteredResource)))
                .andExpect(status().isOk()).andReturn();
    }

    private MvcResult performMvcPostRequest4xxFailure(String bearerToken, BillCreationResource billCreationResource) throws Exception {
        return mockMvc.perform(post(BILL_ENDPOINT).header(JWT_HEADER, bearerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(mapper.writeValueAsString(billCreationResource)))
                .andExpect(status().is4xxClientError()).andReturn();
    }

    private MvcResult performMvcPostRequestBadRequestInviteRegistered(String bearerToken, InviteRegisteredResource inviteRegisteredResource, Long billId) throws Exception {
        return mockMvc.perform(post(String.format(BILL_BILLID_ACCOUNTS_ENDPOINT, billId)).header(JWT_HEADER, bearerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(mapper.writeValueAsString(inviteRegisteredResource)))
                .andExpect(status().isBadRequest()).andReturn();
    }

    private MvcResult doMvcPostRequestInviteRegistered405Failure(String bearerToken, InviteRegisteredResource inviteRegisteredResource, Long billId) throws Exception {
        return mockMvc.perform(post(String.format(BILL_BILLID_ACCOUNTS_ENDPOINT, billId)).header(JWT_HEADER, bearerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(mapper.writeValueAsString(inviteRegisteredResource)))
                .andExpect(status().isMethodNotAllowed()).andReturn();
    }

    private MvcResult performMvcPutRequest4xxFailure(String bearerToken, AssociateBillResource associateBillResource) throws Exception {
        return mockMvc.perform(put(BILL_ENDPOINT).header(JWT_HEADER, bearerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(mapper.writeValueAsString(associateBillResource)))
                .andExpect(status().is4xxClientError()).andReturn();
    }

    private ApiError verifyInvalidInputs(MvcResult result, int expectedErrorsAmount) throws java.io.IOException {
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);

        assertThat(error.getMessage()).isEqualTo(INVALID_INPUTS);
        assertThat(error.getErrors().size()).isEqualTo(expectedErrorsAmount);
        return error;
    }

}