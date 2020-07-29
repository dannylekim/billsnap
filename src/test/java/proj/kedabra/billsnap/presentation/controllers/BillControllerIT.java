package proj.kedabra.billsnap.presentation.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import proj.kedabra.billsnap.business.model.entities.Bill;
import proj.kedabra.billsnap.business.repository.BillRepository;
import proj.kedabra.billsnap.business.service.CalculatePaymentService;
import proj.kedabra.billsnap.business.utils.enums.BillStatusEnum;
import proj.kedabra.billsnap.business.utils.enums.InvitationStatusEnum;
import proj.kedabra.billsnap.fixtures.AssociateBillFixture;
import proj.kedabra.billsnap.fixtures.BillCreationResourceFixture;
import proj.kedabra.billsnap.fixtures.EditBillResourceFixture;
import proj.kedabra.billsnap.fixtures.InviteRegisteredResourceFixture;
import proj.kedabra.billsnap.fixtures.ItemCreationResourceFixture;
import proj.kedabra.billsnap.fixtures.StartBillResourceFixture;
import proj.kedabra.billsnap.fixtures.UserFixture;
import proj.kedabra.billsnap.presentation.ApiError;
import proj.kedabra.billsnap.presentation.ApiSubError;
import proj.kedabra.billsnap.presentation.resources.AccountResource;
import proj.kedabra.billsnap.presentation.resources.AccountStatusResource;
import proj.kedabra.billsnap.presentation.resources.BillCreationResource;
import proj.kedabra.billsnap.presentation.resources.BillResource;
import proj.kedabra.billsnap.presentation.resources.BillSplitResource;
import proj.kedabra.billsnap.presentation.resources.ItemAssociationSplitResource;
import proj.kedabra.billsnap.presentation.resources.ItemPercentageSplitResource;
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

    private static final String BILL_START_ENDPOINT = "/bills/start";

    private static final String BILL_EDIT_ENDPOINT = "/bills/%d";

    private static final String JWT_HEADER = "Authorization";

    private static final String JWT_PREFIX = "Bearer ";

    private static final String MUST_NOT_BE_BLANK = "must not be blank";

    private static final String MUST_NOT_BE_EMPTY = "must not be empty";

    private static final String MUST_NOT_BE_NULL = "must not be null";

    private static final String INVALID_INPUTS = "Invalid Inputs. Please fix the following errors";

    private static final String WRONG_SIZE_0_TO_50 = "size must be between 0 and 50";

    private static final String WRONG_SIZE_0_TO_30 = "size must be between 0 and 30";

    private static final String WRONG_SIZE_0_TO_20 = "size must be between 0 and 20";

    private static final String NOT_IN_RANGE = "The number must be within 0 to 100";

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
        MvcResult result = performMvcPostRequest(bearerToken, BILL_ENDPOINT, billCreationResource, 201);
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
        MvcResult result = performMvcPostRequest(bearerToken, BILL_ENDPOINT, billCreationResource, 201);
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
        MvcResult result = performMvcPostRequest(bearerToken, BILL_ENDPOINT, billCreationResource, 400);
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
        MvcResult result = performMvcPostRequest(bearerToken, BILL_ENDPOINT, billCreationResource, 400);
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
        MvcResult result = performMvcPostRequest(bearerToken, BILL_ENDPOINT, billCreationResource, 400);
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
        MvcResult result = performMvcPostRequest(bearerToken, BILL_ENDPOINT, billCreationResource, 400);
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
        MvcResult result = performMvcPostRequest(bearerToken, BILL_ENDPOINT, billCreationResource, 400);
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
        MvcResult result = performMvcPostRequest(bearerToken, BILL_ENDPOINT, billCreationResource, 400);
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
        MvcResult result = performMvcPostRequest(bearerToken, BILL_ENDPOINT, billCreationResource, 400);
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
        MvcResult result = performMvcPostRequest(bearerToken, BILL_ENDPOINT, billCreationResource, 400);
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
        MvcResult result = performMvcPostRequest(bearerToken, BILL_ENDPOINT, billCreationResource, 400);
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
        MvcResult result = performMvcPostRequest(bearerToken, BILL_ENDPOINT, billCreationResource, 400);
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
        MvcResult result = performMvcPostRequest(bearerToken, BILL_ENDPOINT, billCreationResource, 400);
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
        MvcResult result = performMvcPostRequest(bearerToken, BILL_ENDPOINT, billCreationResource, 400);
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
        MvcResult result = performMvcPostRequest(bearerToken, BILL_ENDPOINT, billCreationResource, 400);
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
        MvcResult result = performMvcPostRequest(bearerToken, BILL_ENDPOINT, billCreationResource, 400);
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
        MvcResult result = performMvcPostRequest(bearerToken, BILL_ENDPOINT, billCreationResource, 400);
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
        MvcResult result = performMvcPostRequest(bearerToken, BILL_ENDPOINT, billCreationResource, 400);
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
        MvcResult result = performMvcPostRequest(bearerToken, BILL_ENDPOINT, billCreationResource, 400);
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
        MvcResult result = performMvcPostRequest(bearerToken, BILL_ENDPOINT, billCreationResource, 400);
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
        MvcResult result = performMvcPostRequest(bearerToken, BILL_ENDPOINT, billCreationResource, 400);
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
        MvcResult result = performMvcPostRequest(bearerToken, BILL_ENDPOINT, billCreationResource, 400);
        ApiError error = verifyInvalidInputs(result, 2);

        final var errorList = error.getErrors().stream()
                .map(ApiSubError::getMessage)
                .filter(msg -> msg.equals(WRONG_SIZE_0_TO_50)
                        || msg.equals(NOT_IN_EMAIL_FORMAT))
                .collect(java.util.stream.Collectors.toList());

        assertThat(errorList.size()).isEqualTo(error.getErrors().size());
    }


    @Test
    @DisplayName("Should return exception if null bill id is given for PUT /bills")
    void shouldReturnExceptionIfNullBillIdGivenPut() throws Exception {
        //Given
        final var user = UserFixture.getDefault();
        final var associateBillResource = AssociateBillFixture.getDefault();
        associateBillResource.setId(null);
        final var authorities = new ArrayList<GrantedAuthority>();

        //When/Then
        MvcResult result = performMvcPutRequestWithoutBearer(BILL_ENDPOINT, associateBillResource, 403, user.getUsername(), authorities);
        final String content = result.getResponse().getContentAsString();
        final ApiError apiError = mapper.readValue(content, ApiError.class);
        assertThat(apiError.getMessage()).isEqualTo("Access is denied");
    }

    @Test
    @DisplayName("Should return exception if null itemsPerAccount is given for PUT /bills")
    void shouldReturnExceptionIfNullItemsPerAccountGivenPut() throws Exception {
        //Given
        final var user = UserFixture.getDefault();
        final var associateBillResource = AssociateBillFixture.getDefault();
        associateBillResource.setItemsPerAccount(null);

        final var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("RESPONSIBLE_" + associateBillResource.getId().toString()));

        //When/Then
        MvcResult result = performMvcPutRequestWithoutBearer(BILL_ENDPOINT, associateBillResource, 400, user.getUsername(), authorities);
        ApiError error = verifyInvalidInputs(result, 1);
        assertThat(error.getErrors().get(0).getMessage()).isEqualTo(MUST_NOT_BE_NULL);
    }

    @Test
    @DisplayName("Should return exception if null email in ItemAssociationResource is given for PUT /bills")
    void shouldReturnExceptionIfNullEmailInItemAssociationResourceGivenPut() throws Exception {
        //Given
        final var user = UserFixture.getDefault();
        final var associateBillResource = AssociateBillFixture.getDefault();
        associateBillResource.getItemsPerAccount().get(0).setEmail(null);
        final var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("RESPONSIBLE_" + associateBillResource.getId().toString()));

        //When/Then
        MvcResult result = performMvcPutRequestWithoutBearer(BILL_ENDPOINT, associateBillResource, 400, user.getUsername(), authorities);
        ApiError error = verifyInvalidInputs(result, 1);
        assertThat(error.getErrors().get(0).getMessage()).isEqualTo(MUST_NOT_BE_BLANK);
    }

    @Test
    @DisplayName("Should return exception if blank email in ItemAssociationResource is given for PUT /bills")
    void shouldReturnExceptionIfBlankEmailInItemAssociationResourceGivenPut() throws Exception {
        //Given
        final var user = UserFixture.getDefault();
        final var associateBillResource = AssociateBillFixture.getDefault();
        associateBillResource.getItemsPerAccount().get(0).setEmail(" ");
        final var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("RESPONSIBLE_" + associateBillResource.getId()));

        //When/Then
        MvcResult result = performMvcPutRequestWithoutBearer(BILL_ENDPOINT, associateBillResource, 400, user.getUsername(), authorities);
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
        final var associateBillResource = AssociateBillFixture.getDefault();
        associateBillResource.getItemsPerAccount().get(0).setEmail("toolongggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggg@email.com");
        final var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("RESPONSIBLE_" + associateBillResource.getId().toString()));

        //When/Then
        MvcResult result = performMvcPutRequestWithoutBearer(BILL_ENDPOINT, associateBillResource, 400, user.getUsername(), authorities);
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
        final var associateBillResource = AssociateBillFixture.getDefault();
        associateBillResource.getItemsPerAccount().get(0).setItems(null);
        final var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("RESPONSIBLE_" + associateBillResource.getId().toString()));

        //When/Then
        MvcResult result = performMvcPutRequestWithoutBearer(BILL_ENDPOINT, associateBillResource, 400, user.getUsername(), authorities);
        ApiError error = verifyInvalidInputs(result, 1);
        assertThat(error.getErrors().get(0).getMessage()).isEqualTo(MUST_NOT_BE_NULL);
    }

    @Test
    @DisplayName("Should return exception if null itemId in ItemPercentageResource is given for PUT /bills")
    void shouldReturnExceptionIfNullItemIdInItemPercentageResourceGivenPut() throws Exception {
        //Given
        final var user = UserFixture.getDefault();
        final var associateBillResource = AssociateBillFixture.getDefault();
        associateBillResource.getItemsPerAccount().get(0).getItems().get(0).setItemId(null);
        final var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("RESPONSIBLE_" + associateBillResource.getId().toString()));

        //When/Then
        MvcResult result = performMvcPutRequestWithoutBearer(BILL_ENDPOINT, associateBillResource, 400, user.getUsername(), authorities);
        ApiError error = verifyInvalidInputs(result, 1);
        assertThat(error.getErrors().get(0).getMessage()).isEqualTo(MUST_NOT_BE_NULL);
    }

    @Test
    @DisplayName("Should return exception if null percentage in ItemPercentageResource is given for PUT /bills")
    void shouldReturnExceptionIfNullPercentageInItemPercentageResourceGivenPut() throws Exception {
        //Given
        final var user = UserFixture.getDefault();
        final var associateBillResource = AssociateBillFixture.getDefault();
        associateBillResource.getItemsPerAccount().get(0).getItems().get(0).setPercentage(null);
        final var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("RESPONSIBLE_" + associateBillResource.getId()));

        //When/Then
        MvcResult result = performMvcPutRequestWithoutBearer(BILL_ENDPOINT, associateBillResource, 400, user.getUsername(), authorities);
        ApiError error = verifyInvalidInputs(result, 1);
        assertThat(error.getErrors().get(0).getMessage()).isEqualTo(MUST_NOT_BE_NULL);
    }

    @Test
    @DisplayName("Should return exception if over 3 integer percentage in ItemPercentageResource is given for PUT /bills")
    void shouldReturnExceptionIfOver3IntegerPercentageInItemPercentageResourceGivenPut() throws Exception {
        //Given
        final var user = UserFixture.getDefault();
        final var associateBillResource = AssociateBillFixture.getDefault();
        associateBillResource.getItemsPerAccount().get(0).getItems().get(0).setPercentage(BigDecimal.valueOf(1234.50));
        final var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("RESPONSIBLE_" + associateBillResource.getId()));

        //When/Then
        MvcResult result = performMvcPutRequestWithoutBearer(BILL_ENDPOINT, associateBillResource, 400, user.getUsername(), authorities);
        ApiError error = verifyInvalidInputs(result, 1);
        assertThat(error.getErrors().get(0).getMessage()).isEqualTo(NUMBER_OUT_OF_BOUNDS_3_4);
    }

    @Test
    @DisplayName("Should return exception if over 4 decimal percentage in ItemPercentageResource is given for PUT /bills")
    void shouldReturnExceptionIfOver4DecimalPercentageInItemPercentageResourceGivenPut() throws Exception {
        //Given
        final var user = UserFixture.getDefault();
        final var associateBillResource = AssociateBillFixture.getDefault();
        associateBillResource.getItemsPerAccount().get(0).getItems().get(0).setPercentage(BigDecimal.valueOf(50.12345));
        final var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("RESPONSIBLE_" + associateBillResource.getId()));

        //When/Then
        MvcResult result = performMvcPutRequestWithoutBearer(BILL_ENDPOINT, associateBillResource, 400, user.getUsername(), authorities);
        ApiError error = verifyInvalidInputs(result, 1);
        assertThat(error.getErrors().get(0).getMessage()).isEqualTo(NUMBER_OUT_OF_BOUNDS_3_4);
    }

    @Test
    @DisplayName("Should return exception if negative percentage in ItemPercentageResource is given for PUT /bills")
    void shouldReturnExceptionIfNegativePercentageInItemPercentageResourceGivenPut() throws Exception {
        //Given
        final var user = UserFixture.getDefault();
        final var associateBillResource = AssociateBillFixture.getDefault();
        associateBillResource.getItemsPerAccount().get(0).getItems().get(0).setPercentage(BigDecimal.valueOf(-50));
        final var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("RESPONSIBLE_" + associateBillResource.getId()));

        //When/Then
        MvcResult result = performMvcPutRequestWithoutBearer(BILL_ENDPOINT, associateBillResource, 400, user.getUsername(), authorities);
        ApiError error = verifyInvalidInputs(result, 1);
        assertThat(error.getErrors().get(0).getMessage()).isEqualTo(NUMBER_MUST_BE_POSITIVE);
    }

    @Test
    @DisplayName("Should return exception if responsible is not the caller for associate bill")
    void shouldReturnExceptionIfResponsibleIsNotCallerForAssociateBill() throws Exception {
        //Given
        final var associateBillResource = AssociateBillFixture.getDefault();
        final var user = UserFixture.getDefault();

        final var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(associateBillResource.getId().toString()));

        //When/Then
        final MvcResult result = performMvcPutRequestWithoutBearer(BILL_ENDPOINT, associateBillResource, 403, user.getUsername(), authorities);
        final String content = result.getResponse().getContentAsString();
        final ApiError apiError = mapper.readValue(content, ApiError.class);

        assertThat(apiError.getMessage()).isEqualTo("Access is denied");
    }

    @ParameterizedTest
    @EnumSource(value = BillStatusEnum.class, names = {"IN_PROGRESS", "RESOLVED"})
    @DisplayName("Should return exception if Bill is not in Open status for Associate Bills")
    void shouldReturnExceptionIfBillIsNotOpenForAssociateBills(BillStatusEnum status) throws Exception {
        //Given
        final var associateBillResource = AssociateBillFixture.getDefault();
        final Bill billById = billRepository.getBillById(associateBillResource.getId());
        billById.setStatus(status);
        final var user = UserFixture.getDefaultWithEmailAndPassword(billById.getResponsible().getEmail(), "somepass");
        final var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("RESPONSIBLE_" + billById.getId()));

        //When/Then
        final MvcResult result = performMvcPutRequestWithoutBearer(BILL_ENDPOINT, associateBillResource, 405, user.getUsername(), authorities);
        final String content = result.getResponse().getContentAsString();
        final ApiError apiError = mapper.readValue(content, ApiError.class);

        assertThat(apiError.getMessage()).isEqualTo(ErrorMessageEnum.WRONG_BILL_STATUS.getMessage(BillStatusEnum.OPEN.toString()));
    }

    @ParameterizedTest
    @EnumSource(value = BillStatusEnum.class, names = {"IN_PROGRESS", "RESOLVED"})
    @DisplayName("Should return exception if Bill is not in Open status for Invite Registered User To Bill")
    void shouldReturnExceptionIfBillIsNotOpenInInviteRegisteredPost(BillStatusEnum status) throws Exception {
        //Given
        final var user = UserFixture.getDefault();
        final var existentEmail = "test@email.com";
        final var inviteRegisteredResource = InviteRegisteredResourceFixture.getDefault();
        inviteRegisteredResource.setAccounts(List.of(existentEmail));
        final var existentBillId = 1000L;
        final Bill billById = billRepository.getBillById(existentBillId);
        billById.setStatus(status);
        final var path = String.format(BILL_BILLID_ACCOUNTS_ENDPOINT, existentBillId);
        final var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("RESPONSIBLE_" + existentBillId));

        //When/Then
        final MvcResult result = performMvcPostRequestWithoutBearer(path, inviteRegisteredResource, 405, user.getUsername(), authorities);
        final String content = result.getResponse().getContentAsString();
        final ApiError apiError = mapper.readValue(content, ApiError.class);

        assertThat(apiError.getMessage()).isEqualTo(ErrorMessageEnum.WRONG_BILL_STATUS.getMessage(BillStatusEnum.OPEN.toString()));
    }

    @Test
    @DisplayName("Should return 400 exception if 1+ emails in InviteRegisteredResource are not in email format for POST /bills/{billId}/accounts")
    void shouldReturnExceptionIfListEmailsNotEmailFormatInInviteRegisteredResourceGivenPost() throws Exception {
        //Given the User makes a request for a bill where User is the responsible
        final var inviteRegisteredResource = InviteRegisteredResourceFixture.getDefault();
        final var user = UserFixture.getDefault();
        final var existentEmail = "test@email.com";
        final var invalidEmail = "notemailformatcom";
        inviteRegisteredResource.setAccounts(List.of(existentEmail, invalidEmail));
        final var existentBillId = 1000L;
        final var path = String.format(BILL_BILLID_ACCOUNTS_ENDPOINT, existentBillId);
        final var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("RESPONSIBLE_" + existentBillId));

        //When/Then
        final MvcResult result = performMvcPostRequestWithoutBearer(path, inviteRegisteredResource, 400, user.getUsername(), authorities);
        final ApiError error = verifyInvalidInputs(result, 1);
        assertThat(error.getErrors().get(0).getMessage()).isEqualTo(NOT_IN_EMAIL_FORMAT);
    }

    @Test
    @DisplayName("Should return 400 exception if 1+ emails in InviteRegisteredResource is blank for POST /bills/{billId}/accounts")
    void shouldReturnExceptionIfListEmailsBlankInInviteRegisteredResourceGivenPost() throws Exception {
        //Given the User makes a request for a bill where User is the responsible
        final var inviteRegisteredResource = InviteRegisteredResourceFixture.getDefault();
        final var existentEmail = "test@email.com";
        final var invalidEmail = " ";
        inviteRegisteredResource.setAccounts(List.of(existentEmail, invalidEmail));
        final var existentBillId = 1000L;
        final var path = String.format(BILL_BILLID_ACCOUNTS_ENDPOINT, existentBillId);
        final var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("RESPONSIBLE_" + existentBillId));

        //When/Then
        final MvcResult result = performMvcPostRequestWithoutBearer(path, inviteRegisteredResource, 400, existentEmail, authorities);
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
        final var existentEmail = "test@email.com";
        final var invalidEmail = "toolongggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggg@email.com";
        inviteRegisteredResource.setAccounts(List.of(existentEmail, invalidEmail));
        final var existentBillId = 1000L;
        final var path = String.format(BILL_BILLID_ACCOUNTS_ENDPOINT, existentBillId);
        final var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("RESPONSIBLE_" + existentBillId));

        //When/Then
        final MvcResult result = performMvcPostRequestWithoutBearer(path, inviteRegisteredResource, 400, user.getUsername(), authorities);
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
        inviteRegisteredResource.setAccounts(new ArrayList<>());
        final var existentBillId = 1000L;
        final var path = String.format(BILL_BILLID_ACCOUNTS_ENDPOINT, existentBillId);
        final var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("RESPONSIBLE_" + existentBillId));

        //When/Then
        final MvcResult result = performMvcPostRequestWithoutBearer(path, inviteRegisteredResource, 400, user.getUsername(), authorities);
        final ApiError error = verifyInvalidInputs(result, 1);
        assertThat(error.getErrors().get(0).getMessage()).isEqualTo(MUST_NOT_BE_EMPTY);
    }

    @Test
    @DisplayName("Should return 400 exception if accounts list is null in InviteRegisteredResource for POST /bills/{billId}/accounts")
    void shouldReturnExceptionIfListEmailsNullInInviteRegisteredResourceGivenPost() throws Exception {
        //Given the User makes a request for a bill where User is the responsible
        final var inviteRegisteredResource = InviteRegisteredResourceFixture.getDefault();
        final var user = UserFixture.getDefault();
        inviteRegisteredResource.setAccounts(null);
        final var existentBillId = 1000L;
        final var path = String.format(BILL_BILLID_ACCOUNTS_ENDPOINT, existentBillId);
        final var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("RESPONSIBLE_" + existentBillId));

        //When/Then
        final MvcResult result = performMvcPostRequestWithoutBearer(path, inviteRegisteredResource, 400, user.getUsername(), authorities);
        final ApiError error = verifyInvalidInputs(result, 1);
        assertThat(error.getErrors().get(0).getMessage()).isEqualTo(MUST_NOT_BE_EMPTY);
    }

    @Test
    @DisplayName("Should return 200 when adding one existing user not part of Bill in Invite Registered Person to Bill")
    void shouldReturn200ForNormalCaseOneUserInviteRegisteredGivenPost() throws Exception {
        //Given the User makes a request for a bill where User is the responsible
        final var inviteRegisteredResource = InviteRegisteredResourceFixture.getDefault();
        final var user = UserFixture.getDefaultWithEmailAndPassword("test@email.com", "notEncrypted");
        final var accountNotInBill = "nobills@inthisemail.com";
        final var existentBillId = 1000L;
        inviteRegisteredResource.setAccounts(List.of(accountNotInBill));
        final var path = String.format(BILL_BILLID_ACCOUNTS_ENDPOINT, existentBillId);
        final var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("RESPONSIBLE_" + existentBillId));

        //When
        final var mvcResult = performMvcPostRequestWithoutBearer(path, inviteRegisteredResource, 200, user.getUsername(), authorities);
        final String content = mvcResult.getResponse().getContentAsString();
        final BillSplitResource response = mapper.readValue(content, BillSplitResource.class);

        //Then
        assertThat((int) response.getInformationPerAccount().stream().map(ItemAssociationSplitResource::getAccount).map(AccountResource::getEmail)
                .filter(acc -> acc.equals(accountNotInBill)).count())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Should return 200 when adding multiple existing users not part of Bill in Invite Registered Person to Bill")
    void shouldReturn200ForNormalCaseInviteRegisteredGivenPost() throws Exception {
        //Given the User makes a request for a bill where User is the responsible
        final var inviteRegisteredResource = InviteRegisteredResourceFixture.getDefault();
        final var user = UserFixture.getDefaultWithEmailAndPassword("test@email.com", "notEncrypted");
        final var accountNotInBill = "nobills@inthisemail.com";
        final var secondAccountNotInBill = "user@withABill.com";
        final var existentBillId = 1000L;
        inviteRegisteredResource.setAccounts(List.of(accountNotInBill, secondAccountNotInBill));
        final var path = String.format(BILL_BILLID_ACCOUNTS_ENDPOINT, existentBillId);
        final var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("RESPONSIBLE_" + existentBillId));

        //When
        final var mvcResult = performMvcPostRequestWithoutBearer(path, inviteRegisteredResource, 200, user.getUsername(), authorities);
        final String content = mvcResult.getResponse().getContentAsString();
        final BillSplitResource response = mapper.readValue(content, BillSplitResource.class);

        //Then
        assertThat((int) response.getInformationPerAccount().stream().map(ItemAssociationSplitResource::getAccount).map(AccountResource::getEmail)
                .filter(acc -> acc.equals(accountNotInBill) || acc.equals(secondAccountNotInBill)).count())
                .isEqualTo(2);
    }

    @Test
    @DisplayName("Should return 200 when getting successfully detailed bill")
    void shouldReturn200WhenGettingSuccessfullyDetailedBill() throws Exception {
        // Given
        final var email = "user@hasbills.com";
        final var existentBillId = 2000L; //bill with items
        final var path = String.format(BILL_BILLID_ENDPOINT, existentBillId);
        final var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(String.valueOf(existentBillId)));

        // When
        final var mvcResult = performMvcGetRequestWithoutBearer(path, 200, email, authorities);
        final String content = mvcResult.getResponse().getContentAsString();
        final BillSplitResource response = mapper.readValue(content, BillSplitResource.class);

        // Then
        assertThat(response.getId()).isEqualTo(existentBillId);
        assertThat(response.getItems()).isNotEmpty();
    }

    @Test
    @DisplayName("GET billId should return mapped detailed bill after creating one bill")
    void shouldReturnMappedDetailedBillAfterCreatingOneBill() throws Exception {
        //Given
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        final var billCreationResource = BillCreationResourceFixture.getDefault();

        final var result = performMvcPostRequest(bearerToken, BILL_ENDPOINT, billCreationResource, 201);
        final String content = result.getResponse().getContentAsString();
        final BillResource createdBill = mapper.readValue(content, BillResource.class);

        final var path = String.format(BILL_BILLID_ENDPOINT, createdBill.getId());

        final var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(createdBill.getId().toString()));

        //When/Then
        final var mvcResult = performMvcGetRequestWithoutBearer(path, 200, user.getUsername(), authorities);
        final String getContent = mvcResult.getResponse().getContentAsString();
        final BillSplitResource billSplitResource = mapper.readValue(getContent, BillSplitResource.class);

        verifyBillSplitResources(createdBill, billSplitResource);
    }

    @Test
    @DisplayName("Should return 404 when bill is not found with billId")
    void shouldReturn404WhenBillIsNotFoundWithBillId() throws Exception {
        // Given
        final var email = "test@email.com";
        final Long nonExistentBillId = 69420L;
        final var path = String.format(BILL_BILLID_ENDPOINT, nonExistentBillId);
        final var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(String.valueOf(nonExistentBillId)));

        // When
        final var mvcResult = performMvcGetRequestWithoutBearer(path, 404, email, authorities);
        final String content = mvcResult.getResponse().getContentAsString();
        final ApiError error = mapper.readValue(content, ApiError.class);

        // Then
        assertThat(error.getMessage()).isEqualTo(ErrorMessageEnum.BILL_ID_DOES_NOT_EXIST.getMessage(nonExistentBillId.toString()));
    }

    @Test
    @DisplayName("Should return 401 when Token is not valid")
    void shouldReturn401WhenTokenIsNotValid() throws Exception {
        // Given
        final var bearerToken = "tOkEn";
        final var existentBillId = 1000L;
        final var path = String.format(BILL_BILLID_ENDPOINT, existentBillId);

        // When
        final var mvcResult = performMvcGetRequest(bearerToken, path, 401);
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
        final var existentBillId = 1006L;
        final var path = String.format(BILL_BILLID_ENDPOINT, existentBillId);
        final var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("RESPONSIBLE_" + existentBillId));

        // When
        final var mvcResult = performMvcGetRequestWithoutBearer(path, 403, user.getUsername(), authorities);
        final String content = mvcResult.getResponse().getContentAsString();
        final ApiError error = mapper.readValue(content, ApiError.class);

        // Then
        assertThat(error.getMessage()).isEqualTo("Access is denied");
    }

    @Test
    @DisplayName("Should return split bill when start bill")
    void shouldReturnSplitBillWhenStartWhenStartBill() throws Exception {
        // Given
        final var user = UserFixture.getDefaultWithEmailAndPassword("test@email.com", "notEncrypted");
        final var existentBillId = 1000L;
        final var startBillResource = StartBillResourceFixture.getStartBillResourceCustom(existentBillId);
        final var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("RESPONSIBLE_" + existentBillId));

        // When
        final var mvcResult = performMvcPostRequestWithoutBearer(BILL_START_ENDPOINT, startBillResource, 200, user.getUsername(), authorities);
        final var content = mvcResult.getResponse().getContentAsString();
        final BillSplitResource billSplitResource = mapper.readValue(content, BillSplitResource.class);

        // Then
        assertThat(billSplitResource.getId()).isEqualTo(existentBillId);
        assertThat(billSplitResource.getStatus()).isEqualTo(BillStatusEnum.IN_PROGRESS);
    }

    @Test
    @DisplayName("Should return error bill id non existent when bill start")
    void shouldReturnErrorBillIdNonExistentWhenBillStart() throws Exception {
        // Given
        final var user = UserFixture.getDefaultWithEmailAndPassword("test@email.com", "notEncrypted");
        final Long nonExistentBillId = 694206942069420L;
        final var startBillResource = StartBillResourceFixture.getStartBillResourceCustom(nonExistentBillId);
        final var authorities = new ArrayList<GrantedAuthority>();

        // When
        final var mvcResult = performMvcPostRequestWithoutBearer(BILL_START_ENDPOINT, startBillResource, 403, user.getUsername(), authorities);
        final var content = mvcResult.getResponse().getContentAsString();
        final ApiError error = mapper.readValue(content, ApiError.class);

        // Then
        assertThat(error.getMessage()).isEqualTo("Access is denied");
    }

    @Test
    @DisplayName("Should return error bill is not open")
    void shouldReturnErrorBillIsNotOpen() throws Exception {
        // Given
        final var user = UserFixture.getDefaultWithEmailAndPassword("test@email.com", "notEncrypted");
        final var existentBillId = 1000L;
        final var startBillResource = StartBillResourceFixture.getStartBillResourceCustom(existentBillId);
        final var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("RESPONSIBLE_" + existentBillId));

        // When
        performMvcPostRequestWithoutBearer(BILL_START_ENDPOINT, startBillResource, 200, user.getUsername(), authorities);
        final var mvcResult = performMvcPostRequestWithoutBearer(BILL_START_ENDPOINT, startBillResource, 405, user.getUsername(), authorities);
        final var content = mvcResult.getResponse().getContentAsString();
        final ApiError error = mapper.readValue(content, ApiError.class);

        // Then
        assertThat(error.getMessage()).isEqualTo(ErrorMessageEnum.WRONG_BILL_STATUS.getMessage(BillStatusEnum.OPEN.toString()));
    }

    @Test
    @DisplayName("Should edit bill successfully")
    void shouldEditBillSuccessfully() throws Exception {
        // Given
        final var user = UserFixture.getDefaultWithEmailAndPassword("editBill@email.com", "notEncrypted");
        final var existentBillId = 1102L;
        final var editBillResource = EditBillResourceFixture.getDefault();
        editBillResource.setResponsible("editBill@email.com");
        final var endpoint = String.format(BILL_EDIT_ENDPOINT, existentBillId);

        final var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("RESPONSIBLE_" + existentBillId));

        // When
        final var mvcResult = performMvcPutRequestWithoutBearer(endpoint, editBillResource, 200, user.getUsername(), authorities);
        final var content = mvcResult.getResponse().getContentAsString();
        final BillSplitResource billSplit = mapper.readValue(content, BillSplitResource.class);

        // Then
        assertThat(billSplit.getName()).isEqualTo(editBillResource.getName());
        assertThat(billSplit.getResponsible().getEmail()).isEqualTo(editBillResource.getResponsible());
        assertThat(billSplit.getCompany()).isEqualTo(editBillResource.getCompany());
        assertThat(billSplit.getCategory()).isEqualTo(editBillResource.getCategory());

        final var items = billSplit.getInformationPerAccount().get(0).getItems();
        final var firstItemResource = editBillResource.getItems().get(0);
        final var secondItemResource = editBillResource.getItems().get(1);
        final var firstItemPercentageSplitResource = items.get(0);
        final var secondItemPercentageSplitResource = items.get(1);
        if (firstItemPercentageSplitResource.getName().equals(firstItemResource.getName())) {
            assertThat(firstItemPercentageSplitResource.getName()).isEqualTo(firstItemResource.getName());
            assertThat(firstItemPercentageSplitResource.getCost()).isEqualByComparingTo(firstItemResource.getCost());
            assertThat(firstItemPercentageSplitResource.getItemId()).isEqualTo(firstItemResource.getId());
            assertThat(secondItemPercentageSplitResource.getName()).isEqualTo(secondItemResource.getName());
            assertThat(secondItemPercentageSplitResource.getCost()).isEqualByComparingTo(secondItemResource.getCost());
            assertThat(secondItemPercentageSplitResource.getItemId()).isNotNull();
        } else {
            assertThat(secondItemPercentageSplitResource.getName()).isEqualTo(firstItemResource.getName());
            assertThat(secondItemPercentageSplitResource.getCost()).isEqualByComparingTo(firstItemResource.getCost());
            assertThat(secondItemPercentageSplitResource.getItemId()).isEqualTo(firstItemResource.getId());
            assertThat(firstItemPercentageSplitResource.getName()).isEqualTo(secondItemResource.getName());
            assertThat(firstItemPercentageSplitResource.getCost()).isEqualByComparingTo(secondItemResource.getCost());
            assertThat(firstItemPercentageSplitResource.getItemId()).isNotNull();
        }
    }

    @Test
    @DisplayName("Should return error account not in bill when edit bill")
    void shouldReturnErrorAccountNotInBillWhenEditBill() throws Exception {
        // Given
        final var user = UserFixture.getDefaultWithEmailAndPassword("test@email.com", "notEncrypted");
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        final var existentBillId = 1102L;
        final var editBillResource = EditBillResourceFixture.getDefault();
        editBillResource.setResponsible("test@email.com");
        final var endpoint = String.format(BILL_EDIT_ENDPOINT, existentBillId);

        // When
        final var mvcResult = performMvcPutRequest(bearerToken, endpoint, editBillResource, 403);
        final var content = mvcResult.getResponse().getContentAsString();
        final ApiError error = mapper.readValue(content, ApiError.class);

        // Then
        assertThat(error.getMessage()).isEqualTo("Access is denied");
    }

    @Test
    @DisplayName("Should return error when bill already started when editing bill")
    void shouldReturnErrorWhenBillAlreadyStartedWhenEditingBill() throws Exception {
        // Given
        final var user = UserFixture.getDefaultWithEmailAndPassword("editBill@email.com", "notEncrypted");
        final var existentBillId = 1102L;
        final var editBillResource = EditBillResourceFixture.getDefault();
        editBillResource.setResponsible("editBill@email.com");
        final var endpoint = String.format(BILL_EDIT_ENDPOINT, existentBillId);
        final var startBillResource = StartBillResourceFixture.getStartBillResourceCustom(existentBillId);
        final var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("RESPONSIBLE_" + existentBillId));

        performMvcPostRequestWithoutBearer(BILL_START_ENDPOINT, startBillResource, 200, user.getUsername(), authorities);

        // When
        final var mvcResult = performMvcPutRequestWithoutBearer(endpoint, editBillResource, 405, user.getUsername(), authorities);
        final var content = mvcResult.getResponse().getContentAsString();
        final ApiError error = mapper.readValue(content, ApiError.class);

        // Then
        assertThat(error.getMessage()).isEqualTo(ErrorMessageEnum.WRONG_BILL_STATUS.getMessage(BillStatusEnum.OPEN.name()));
    }

    @Test
    @DisplayName("Should return error when responsible is not part of bill when editing bill")
    void shouldReturnErrorWhenResponsibleIsNotPartOfBillWhenEditingBill() throws Exception {
        // Given
        final var user = UserFixture.getDefaultWithEmailAndPassword("editBill@email.com", "notEncrypted");
        final var existentBillId = 1102L;
        final var editBillResource = EditBillResourceFixture.getDefault();
        editBillResource.setResponsible("test@email.com");
        final var endpoint = String.format(BILL_EDIT_ENDPOINT, existentBillId);
        final var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(String.valueOf(existentBillId)));

        // When
        final var mvcResult = performMvcPutRequestWithoutBearer(endpoint, editBillResource, 403, user.getUsername(), authorities);
        final var content = mvcResult.getResponse().getContentAsString();
        final ApiError error = mapper.readValue(content, ApiError.class);

        // Then
        assertThat(error.getMessage()).isEqualTo("Access is denied");
    }

    @Test
    @DisplayName("Should return error if tip format is incorrect with both values when editing bill")
    void shouldReturnErrorIfTipFormatIsIncorrectBothValuesWhenEditingBill() throws Exception {
        // Given
        final var user = UserFixture.getDefaultWithEmailAndPassword("editBill@email.com", "notEncrypted");
        final var existentBillId = 1102L;
        final var editBillResource = EditBillResourceFixture.getDefault();
        editBillResource.setResponsible("editBill@email.com");
        editBillResource.setTipPercent(BigDecimal.TEN);
        editBillResource.setTipAmount(BigDecimal.valueOf(15));
        final var endpoint = String.format(BILL_EDIT_ENDPOINT, existentBillId);
        final var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("RESPONSIBLE_" + existentBillId));

        // When
        final var mvcResult = performMvcPutRequestWithoutBearer(endpoint, editBillResource, 400, user.getUsername(), authorities);
        final var content = mvcResult.getResponse().getContentAsString();
        final ApiError error = mapper.readValue(content, ApiError.class);

        // Then
        assertThat(error.getMessage()).isEqualTo(ErrorMessageEnum.MULTIPLE_TIP_METHOD.getMessage());
    }

    @Test
    @DisplayName("Should return error if tip format is incorrect when editing bill")
    void shouldReturnErrorIfTipBothNullWhenEditingBill() throws Exception {
        // Given
        final var user = UserFixture.getDefaultWithEmailAndPassword("editBill@email.com", "notEncrypted");
        final var existentBillId = 1102L;
        final var editBillResource = EditBillResourceFixture.getDefault();
        editBillResource.setResponsible("editBill@email.com");
        editBillResource.setTipPercent(null);
        editBillResource.setTipAmount(null);
        final var endpoint = String.format(BILL_EDIT_ENDPOINT, existentBillId);
        final var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("RESPONSIBLE_" + existentBillId));

        // When
        final var mvcResult = performMvcPutRequestWithoutBearer(endpoint, editBillResource, 400, user.getUsername(), authorities);
        final var content = mvcResult.getResponse().getContentAsString();
        final ApiError error = mapper.readValue(content, ApiError.class);

        // Then
        assertThat(error.getMessage()).isEqualTo(ErrorMessageEnum.MULTIPLE_TIP_METHOD.getMessage());
    }

    @Test
    @DisplayName("Should return error when edit bill does not have referenced item when editing bill")
    void shouldReturnErrorWhenEditBillDoesNotHaveReferencedItemWhenEditingBill() throws Exception {
        // Given
        final var user = UserFixture.getDefaultWithEmailAndPassword("editBill@email.com", "notEncrypted");
        final var existentBillId = 1102L;
        final var nonExistentItem = 6969L;
        final var editBillResource = EditBillResourceFixture.getDefault();
        editBillResource.setResponsible("editBill@email.com");
        editBillResource.getItems().get(0).setId(nonExistentItem);
        final var endpoint = String.format(BILL_EDIT_ENDPOINT, existentBillId);
        final var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("RESPONSIBLE_" + existentBillId));

        // When
        final var mvcResult = performMvcPutRequestWithoutBearer(endpoint, editBillResource, 404, user.getUsername(), authorities);
        final var content = mvcResult.getResponse().getContentAsString();
        final ApiError error = mapper.readValue(content, ApiError.class);

        // Then
        assertThat(error.getMessage()).isEqualTo(ErrorMessageEnum.ITEM_ID_DOES_NOT_EXIST.getMessage(Long.toString(nonExistentItem)));
    }

    @Test
    @DisplayName("Should return error if taxes are null")
    void shouldReturnErrorIfTaxesAreNull() throws Exception {
        //Given
        final var billCreationResource = BillCreationResourceFixture.getDefault();
        billCreationResource.setTaxes(null);
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);

        //When/Then
        MvcResult result = performMvcPostRequest(bearerToken, BILL_ENDPOINT, billCreationResource, 400);
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);

        assertThat(error.getMessage()).isEqualTo(INVALID_INPUTS);
        assertThat(error.getErrors().size()).isEqualTo(1);
        assertThat(error.getErrors().get(0).getMessage()).isEqualTo(MUST_NOT_BE_NULL);
    }

    @Test
    @DisplayName("Should return error if tax name is null")
    void shouldReturnErrorIfTaxNameIsNull() throws Exception {
        //Given
        final var billCreationResource = BillCreationResourceFixture.getDefault();
        billCreationResource.getTaxes().get(0).setName(null);
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);

        //When/Then
        MvcResult result = performMvcPostRequest(bearerToken, BILL_ENDPOINT, billCreationResource, 400);
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);

        assertThat(error.getMessage()).isEqualTo(INVALID_INPUTS);
        assertThat(error.getErrors().size()).isEqualTo(1);
        assertThat(error.getErrors().get(0).getMessage()).isEqualTo(MUST_NOT_BE_BLANK);
    }

    @Test
    @DisplayName("Should return error if tax percentage is null")
    void shouldReturnErrorIfTaxPercentageIsNull() throws Exception {
        //Given
        final var billCreationResource = BillCreationResourceFixture.getDefault();
        billCreationResource.getTaxes().get(0).setPercentage(null);
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);

        //When/Then
        MvcResult result = performMvcPostRequest(bearerToken, BILL_ENDPOINT, billCreationResource, 400);
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);

        assertThat(error.getMessage()).isEqualTo(INVALID_INPUTS);
        assertThat(error.getErrors().size()).isEqualTo(1);
        assertThat(error.getErrors().get(0).getMessage()).isEqualTo(MUST_NOT_BE_NULL);
    }

    @Test
    @DisplayName("Should return error if tax percentage is negative")
    void shouldReturnErrorIfTaxPercentageIsNegative() throws Exception {
        //Given
        final var billCreationResource = BillCreationResourceFixture.getDefault();
        billCreationResource.getTaxes().get(0).setPercentage(new BigDecimal(-100));
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);

        //When/Then
        MvcResult result = performMvcPostRequest(bearerToken, BILL_ENDPOINT, billCreationResource, 400);
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);

        assertThat(error.getMessage()).isEqualTo(INVALID_INPUTS);
        assertThat(error.getErrors().size()).isEqualTo(1);
        assertThat(error.getErrors().get(0).getMessage()).isEqualTo(NOT_IN_RANGE);
    }

    @Test
    @DisplayName("Should return error if tax percentage exceeds 100")
    void shouldReturnErrorIfTaxPercentageIsGreaterThan100() throws Exception {
        //Given
        final var billCreationResource = BillCreationResourceFixture.getDefault();
        billCreationResource.getTaxes().get(0).setPercentage(new BigDecimal(105));
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);

        //When/Then
        MvcResult result = performMvcPostRequest(bearerToken, BILL_ENDPOINT, billCreationResource, 400);
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);

        assertThat(error.getMessage()).isEqualTo(INVALID_INPUTS);
        assertThat(error.getErrors().size()).isEqualTo(1);
        assertThat(error.getErrors().get(0).getMessage()).isEqualTo(NOT_IN_RANGE);
    }

    @Test
    @DisplayName("Should return error if tax percentage not in bounds")
    void shouldReturnErrorIfTaxPercentageIsNotInBounds() throws Exception {
        //Given
        final var billCreationResource = BillCreationResourceFixture.getDefault();
        billCreationResource.getTaxes().get(0).setPercentage(new BigDecimal("99.15555"));
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);

        //When/Then
        MvcResult result = performMvcPostRequest(bearerToken, BILL_ENDPOINT, billCreationResource, 400);
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);

        assertThat(error.getMessage()).isEqualTo(INVALID_INPUTS);
        assertThat(error.getErrors().size()).isEqualTo(1);
        assertThat(error.getErrors().get(0).getMessage()).isEqualTo(NUMBER_OUT_OF_BOUNDS_3_4);
    }

    @Test
    @DisplayName("Should return error if accounts list is null")
    void shouldReturnErrorIfAccountsListIsNull() throws Exception {
        //Given
        final var billCreationResource = BillCreationResourceFixture.getDefault();
        billCreationResource.setAccountsList(null);
        final var user = UserFixture.getDefault();
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);

        //When/Then
        MvcResult result = performMvcPostRequest(bearerToken, BILL_ENDPOINT, billCreationResource, 400);
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);

        assertThat(error.getMessage()).isEqualTo(INVALID_INPUTS);
        assertThat(error.getErrors().size()).isEqualTo(1);
        assertThat(error.getErrors().get(0).getMessage()).isEqualTo(MUST_NOT_BE_NULL);
    }

    private void verifyShortBillResources(BillResource expectedBillResource, ShortBillResource actualBillResource) {
        assertEquals(expectedBillResource.getId(), actualBillResource.getId());
        assertEquals(expectedBillResource.getName(), actualBillResource.getName());
        assertEquals(expectedBillResource.getCategory(), actualBillResource.getCategory());
        assertEquals(BillStatusEnum.OPEN, actualBillResource.getStatus());
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
        assertNotNull(expectedBillResource.getId());

        final HashSet<ItemPercentageSplitResource> itemsList = new HashSet<>();
        actualBillResource.getInformationPerAccount().forEach(account -> itemsList.addAll(account.getItems()));
        assertEquals(expectedBillResource.getItems().size(), itemsList.size());
        actualBillResource.getInformationPerAccount().forEach(info -> {
            assertThat(info.getSubTotal()).isNotNull();
            assertThat(info.getTaxes()).isNotNull();
            assertThat(info.getTip()).isNotNull();
            assertThat(info.getTotal()).isEqualByComparingTo(info.getSubTotal().add(info.getTaxes()).add(info.getTip()).setScale(CalculatePaymentService.DOLLAR_SCALE, RoundingMode.HALF_UP));
            assertThat(info.getInvitationStatus()).isNotNull();
            assertThat(info.getAmountPaid()).isNotNull();
            assertThat(info.getAmountRemaining()).isNotNull();
            if (info.getInvitationStatus() == InvitationStatusEnum.ACCEPTED && actualBillResource.getStatus() == BillStatusEnum.IN_PROGRESS) {
                assertThat(info.getPaidStatus()).isNotNull();
            } else {
                assertThat(info.getPaidStatus()).isNull();
            }
        });

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
        assertNotNull(response.getId());
    }

    private MvcResult performMvcGetRequest(String bearerToken, String path, int resultCode) throws Exception {
        return mockMvc.perform(get(path).header(JWT_HEADER, bearerToken))
                .andExpect(status().is(resultCode)).andReturn();
    }

    private MvcResult performMvcGetRequestWithoutBearer(String path, int resultCode, String email, List<GrantedAuthority> authorities) throws Exception {
        return mockMvc.perform(get(path).with(user(email).authorities(authorities)))
                .andExpect(status().is(resultCode)).andReturn();
    }

    private <T> MvcResult performMvcPostRequest(String bearerToken, String path, T body, int resultCode) throws Exception {
        return mockMvc.perform(post(path).header(JWT_HEADER, bearerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(mapper.writeValueAsString(body)))
                .andExpect(status().is(resultCode)).andReturn();
    }

    private <T> MvcResult performMvcPostRequestWithoutBearer(String path, T body, int resultCode, String email, List<GrantedAuthority> authorities) throws Exception {
        return mockMvc.perform(post(path).with(user(email).authorities(authorities))
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(mapper.writeValueAsString(body)))
                .andExpect(status().is(resultCode)).andReturn();
    }

    private <T> MvcResult performMvcPutRequest(String bearerToken, String path, T body, int resultCode) throws Exception {
        return mockMvc.perform(put(path).header(JWT_HEADER, bearerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(mapper.writeValueAsString(body)))
                .andExpect(status().is(resultCode)).andReturn();
    }

    private <T> MvcResult performMvcPutRequestWithoutBearer(String path, T body, int resultCode, String email, List<GrantedAuthority> authorities) throws Exception {
        return mockMvc.perform(put(path).with(user(email).authorities(authorities))
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(mapper.writeValueAsString(body)))
                .andExpect(status().is(resultCode)).andReturn();
    }

    private ApiError verifyInvalidInputs(MvcResult result, int expectedErrorsAmount) throws java.io.IOException {
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);

        assertThat(error.getMessage()).isEqualTo(INVALID_INPUTS);
        assertThat(error.getErrors().size()).isEqualTo(expectedErrorsAmount);
        return error;
    }

}