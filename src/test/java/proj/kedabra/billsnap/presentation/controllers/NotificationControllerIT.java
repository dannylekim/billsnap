package proj.kedabra.billsnap.presentation.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.transaction.Transactional;

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

import proj.kedabra.billsnap.business.model.entities.AccountBill;
import proj.kedabra.billsnap.business.model.entities.Notifications;
import proj.kedabra.billsnap.business.repository.NotificationsRepository;
import proj.kedabra.billsnap.business.utils.enums.BillStatusEnum;
import proj.kedabra.billsnap.business.utils.enums.InvitationStatusEnum;
import proj.kedabra.billsnap.fixtures.AnswerNotificationResourceFixture;
import proj.kedabra.billsnap.fixtures.UserFixture;
import proj.kedabra.billsnap.presentation.ApiError;
import proj.kedabra.billsnap.presentation.resources.AnswerNotificationResource;
import proj.kedabra.billsnap.presentation.resources.BillSplitResource;
import proj.kedabra.billsnap.security.JwtService;
import proj.kedabra.billsnap.utils.ErrorMessageEnum;
import proj.kedabra.billsnap.utils.SpringProfiles;

@Tag("integration")
@ActiveProfiles(SpringProfiles.TEST)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@Transactional
class NotificationControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private NotificationsRepository notificationsRepository;

    private static final String INVITATION_INVITATIONID_ENDPOINT = "/invitations/%d";

    private static final String JWT_HEADER = "Authorization";

    private static final String JWT_PREFIX = "Bearer ";

    private static final String INVALID_INPUTS = "Invalid Inputs. Please fix the following errors";

    @Test
    @DisplayName("Should return 200 when answering invitation with ACCEPT")
    void shouldReturn200WhenAnsweringInvitationWithAccept() throws Exception {
        //Given
        final var user = UserFixture.getDefaultWithEmailAndPassword("user@inbill.com", "notEncrypted");
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        final long invitationId = 101L;
        final long billId = 1220L;
        final var path = String.format(INVITATION_INVITATIONID_ENDPOINT, billId);
        final AnswerNotificationResource answer = AnswerNotificationResourceFixture.getDefault();

        //When/Then
        final MvcResult result = performMvcPostRequest(bearerToken, path, answer, 200);
        final String content = result.getResponse().getContentAsString();
        final BillSplitResource response = mapper.readValue(content, BillSplitResource.class);

        assertThat(response).isNotNull();
        final Notifications notification = notificationsRepository.findById(invitationId).orElseThrow();
        final AccountBill accountBill = notification.getBill().getAccountBill(notification.getAccount()).orElseThrow();
        assertThat(accountBill.getStatus()).isEqualTo(InvitationStatusEnum.ACCEPTED);

    }

    @Test
    @DisplayName("Should return 200 when answering invitation with DECLINE")
    void shouldReturn200WhenAnsweringInvitationWithDecline() throws Exception {
        //Given
        final var user = UserFixture.getDefaultWithEmailAndPassword("user@inbill.com", "notEncrypted");
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        final long invitationId = 101L;
        final long billId = 1220L;
        final var path = String.format(INVITATION_INVITATIONID_ENDPOINT, billId);
        final AnswerNotificationResource answer = AnswerNotificationResourceFixture.getDefault();
        answer.setAnswer(false);

        //When/Then
        final MvcResult result = performMvcPostRequest(bearerToken, path, answer, 200);
        final String content = result.getResponse().getContentAsString();

        assertThat(content).isEmpty();
        final Notifications notification = notificationsRepository.findById(invitationId).orElseThrow();
        final AccountBill accountBill = notification.getBill().getAccountBill(notification.getAccount()).orElseThrow();
        assertThat(accountBill.getStatus()).isEqualTo(InvitationStatusEnum.DECLINED);

    }

    @Test
    @DisplayName("Should return 400 when invitation answer is null")
    void shouldReturn400WhenAnswerInvitationNull() throws Exception {
        //Given
        final var user = UserFixture.getDefaultWithEmailAndPassword("user@inbill.com", "notEncrypted");
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        final long billId = 1220L;
        final var path = String.format(INVITATION_INVITATIONID_ENDPOINT, billId);
        final AnswerNotificationResource answer = AnswerNotificationResourceFixture.getDefault();
        answer.setAnswer(null);

        //When/Then
        final MvcResult mvcResult = performMvcPostRequest(bearerToken, path, answer, 400);
        verifyInvalidInputs(mvcResult);
    }

    @Test
    @DisplayName("Should return 403 when user answering invitation is not associated to invitation")
    void shouldReturn403WhenUserAnsweringInvitationNotAssociated() throws Exception {
        //Given
        final var user = UserFixture.getDefaultWithEmailAndPassword("userNot@inbill.com", "notEncrypted");
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        final long billId = 1220L;
        final var path = String.format(INVITATION_INVITATIONID_ENDPOINT, billId);
        final AnswerNotificationResource answer = AnswerNotificationResourceFixture.getDefault();

        //When/Then
        final MvcResult mvcResult = performMvcPostRequest(bearerToken, path, answer, 403);
        final var content = mvcResult.getResponse().getContentAsString();
        final ApiError error = mapper.readValue(content, ApiError.class);
        assertThat(error.getMessage()).isEqualTo(ErrorMessageEnum.ACCOUNT_NOT_ASSOCIATED_TO_NOTIFICATION.getMessage());
    }

    @Test
    @DisplayName("Should return 404 when bill id does not exist")
    void shouldReturn404WhenBillIdDoesNotExist() throws Exception {
        //Given
        final var user = UserFixture.getDefaultWithEmailAndPassword("user@inbill.com", "notEncrypted");
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        final long billId = 123456789L;
        final var path = String.format(INVITATION_INVITATIONID_ENDPOINT, billId);
        final AnswerNotificationResource answer = AnswerNotificationResourceFixture.getDefault();

        //When/Then
        final MvcResult mvcResult = performMvcPostRequest(bearerToken, path, answer, 404);
        final var content = mvcResult.getResponse().getContentAsString();
        final ApiError error = mapper.readValue(content, ApiError.class);
        assertThat(error.getMessage()).isEqualTo(ErrorMessageEnum.BILL_ID_DOES_NOT_EXIST.getMessage(Long.toString(billId)));
    }

    @Test
    @DisplayName("Should return 405 when the bill that the user is invited to is not Open status")
    void shouldReturn405WhenBillNotOpenStatus() throws Exception {
        //Given
        final var user = UserFixture.getDefaultWithEmailAndPassword("user@inbill.com", "notEncrypted");
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        final long billId = 1101L;
        final var path = String.format(INVITATION_INVITATIONID_ENDPOINT, billId);
        final AnswerNotificationResource answer = AnswerNotificationResourceFixture.getDefault();

        //When/Then
        final MvcResult mvcResult = performMvcPostRequest(bearerToken, path, answer, 405);
        final var content = mvcResult.getResponse().getContentAsString();
        final ApiError error = mapper.readValue(content, ApiError.class);
        assertThat(error.getMessage()).isEqualTo(ErrorMessageEnum.WRONG_BILL_STATUS.getMessage(BillStatusEnum.OPEN.toString()));
    }

    @Test
    @DisplayName("Should return 405 when the invitation status is not PENDING")
    void shouldReturn405WhenInvitationStatusNotPending() throws Exception {
        //Given
        final var user = UserFixture.getDefaultWithEmailAndPassword("user@inbill.com", "notEncrypted");
        final var bearerToken = JWT_PREFIX + jwtService.generateToken(user);
        final long billId = 1221L;
        final var path = String.format(INVITATION_INVITATIONID_ENDPOINT, billId);
        final AnswerNotificationResource answer = AnswerNotificationResourceFixture.getDefault();

        //When/Then
        final MvcResult mvcResult = performMvcPostRequest(bearerToken, path, answer, 405);
        final var content = mvcResult.getResponse().getContentAsString();
        final ApiError error = mapper.readValue(content, ApiError.class);
        assertThat(error.getMessage()).isEqualTo(ErrorMessageEnum.WRONG_INVITATION_STATUS.getMessage(InvitationStatusEnum.PENDING.toString()));
    }

    private <T> MvcResult performMvcPostRequest(String bearerToken, String path, T body, int resultCode) throws Exception {
        return mockMvc.perform(post(path).header(JWT_HEADER, bearerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(mapper.writeValueAsString(body)))
                .andExpect(status().is(resultCode)).andReturn();
    }

    private void verifyInvalidInputs(MvcResult result) throws java.io.IOException {
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);

        assertThat(error.getMessage()).isEqualTo(INVALID_INPUTS);
        assertThat(error.getErrors().size()).isEqualTo(1);
    }

}