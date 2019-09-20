package proj.kedabra.billsnap.presentation.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import proj.kedabra.billsnap.fixtures.AccountCreationResourceFixture;
import proj.kedabra.billsnap.presentation.ApiError;
import proj.kedabra.billsnap.presentation.ApiSubError;
import proj.kedabra.billsnap.presentation.resources.AccountResource;
import proj.kedabra.billsnap.utils.SpringProfiles;

@Tag("integration")
@ActiveProfiles(SpringProfiles.TEST)
@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("squid:S00112")
@AutoConfigureTestDatabase
class AccountControllerIT {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    private static final String INVALID_INPUTS = "Invalid Inputs. Please fix the following errors";

    private static final String MUST_NOT_BE_BLANK = "must not be blank";

    private static final String ENDPOINT = "/register";

    private final String CUSTOM_EMAIL_ERROR_MESSAGE = "Must be in an email format. ex: test@email.com.";

    private static final String INVALID_LENGTH_IN_PASSWORD = "size must be between 8 and 20";

    private static final String INVALID_PASSWORD = "Password must contain an upper and lower case, a number, and a symbol.";

    private static UUID uuidv4 = UUID.randomUUID();


    @Test
    @DisplayName("Should return error for invalid email")
    void shouldReturnErrorForInvalidEmail() throws Exception {
        //Given
        var creationResource = AccountCreationResourceFixture.getDefault();
        creationResource.setEmail("INVALID EMAIL");

        //When/Then
        MvcResult result = mockMvc.perform(post(ENDPOINT).content(mapper.writeValueAsBytes(creationResource)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().is4xxClientError()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);
        assertEquals(INVALID_INPUTS, error.getMessage());
        assertEquals(1, error.getErrors().size());
        assertEquals(CUSTOM_EMAIL_ERROR_MESSAGE, error.getErrors().get(0).getMessage());

    }

    @Test
    @DisplayName("Should return error for too long of an email")
    void shouldReturnErrorForLongEmail() throws Exception {
        //Given
        var creationResource = AccountCreationResourceFixture.getDefault();
        creationResource.setEmail("mustasdd00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000@email.com");

        //When/Then
        MvcResult result = mockMvc.perform(post(ENDPOINT).content(mapper.writeValueAsBytes(creationResource)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().is4xxClientError()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);
        assertEquals(INVALID_INPUTS, error.getMessage());

        List<String> errorMessages = error.getErrors().stream().map(ApiSubError::getMessage).filter(msg -> msg.equals(CUSTOM_EMAIL_ERROR_MESSAGE) || msg.equals("size must be between 0 and 50")).collect(Collectors.toList());
        assertEquals(2, errorMessages.size());
    }

    @Test
    @DisplayName("Should return error for empty email")
    void shouldReturnErrorForEmpty() throws Exception {
        //Given
        var creationResource = AccountCreationResourceFixture.getDefault();
        creationResource.setEmail("");

        //When/Then
        MvcResult result = mockMvc.perform(post(ENDPOINT).content(mapper.writeValueAsBytes(creationResource)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().is4xxClientError()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);
        assertEquals(INVALID_INPUTS, error.getMessage());
        assertEquals(1, error.getErrors().size());
        assertEquals(MUST_NOT_BE_BLANK, error.getErrors().get(0).getMessage());
    }

    @Test
    @DisplayName("Should return error for blank email")
    void shouldReturnErrorForBlank() throws Exception {
        //Given
        var creationResource = AccountCreationResourceFixture.getDefault();
        creationResource.setEmail(" ");

        //When/Then
        MvcResult result = mockMvc.perform(post(ENDPOINT).content(mapper.writeValueAsBytes(creationResource)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().is4xxClientError()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);
        assertEquals(INVALID_INPUTS, error.getMessage());
        assertEquals(2, error.getErrors().size());

        final var errorList = error.getErrors().stream().map(ApiSubError::getMessage).filter(MUST_NOT_BE_BLANK::equals).collect(Collectors.toList());

        assertFalse(errorList.isEmpty());

    }

    @Test
    @DisplayName("Should return error for empty password")
    void shouldReturnErrorForEmptyPassword() throws Exception {
        //Given
        var creationResource = AccountCreationResourceFixture.getDefault();
        creationResource.setPassword("");

        //When/Then
        MvcResult result = mockMvc.perform(post(ENDPOINT).content(mapper.writeValueAsBytes(creationResource)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().is4xxClientError()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);
        assertEquals(INVALID_INPUTS, error.getMessage());
        assertEquals(2, error.getErrors().size());

        final var errorList = error.getErrors().stream().map(ApiSubError::getMessage).filter(INVALID_LENGTH_IN_PASSWORD::equals).collect(Collectors.toList());

        assertFalse(errorList.isEmpty());
    }

    @Test
    @DisplayName("Should return error for blank password")
    void shouldReturnErrorForBlankPassword() throws Exception {
        //Given
        var creationResource = AccountCreationResourceFixture.getDefault();
        creationResource.setPassword(" ");

        //When/Then
        MvcResult result = mockMvc.perform(post(ENDPOINT).content(mapper.writeValueAsBytes(creationResource)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().is4xxClientError()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);
        assertEquals(INVALID_INPUTS, error.getMessage());
        assertEquals(2, error.getErrors().size());

        final var errorList = error.getErrors().stream().map(ApiSubError::getMessage).filter(INVALID_LENGTH_IN_PASSWORD::equals).collect(Collectors.toList());

        assertFalse(errorList.isEmpty());
    }

    @Test
    @DisplayName("Should return error for too long of a password")
    void shouldReturnErrorForLongPassword() throws Exception {
        //Given
        var creationResource = AccountCreationResourceFixture.getDefault();
        creationResource.setPassword("00000000000000000000000000000000000000000000000000000000000000000000000000Avc#");

        //When/Then
        MvcResult result = mockMvc.perform(post(ENDPOINT).content(mapper.writeValueAsBytes(creationResource)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().is4xxClientError()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);
        assertEquals(INVALID_INPUTS, error.getMessage());
        assertEquals(1, error.getErrors().size());
        assertEquals(INVALID_LENGTH_IN_PASSWORD, error.getErrors().get(0).getMessage());

    }

    @Test
    @DisplayName("Should return error for too short of a password")
    void shouldReturnErrorForShortPassword() throws Exception {
        //Given
        var creationResource = AccountCreationResourceFixture.getDefault();
        creationResource.setPassword("C@d233");

        //When/Then
        MvcResult result = mockMvc.perform(post(ENDPOINT).content(mapper.writeValueAsBytes(creationResource)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().is4xxClientError()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);
        assertEquals(INVALID_INPUTS, error.getMessage());
        assertEquals(1, error.getErrors().size());
        assertEquals(INVALID_LENGTH_IN_PASSWORD, error.getErrors().get(0).getMessage());

    }

    @Test
    @DisplayName("Should return error for password not containing a symbol")
    void shouldReturnErrorForNoSymbolInPassword() throws Exception {
        //Given
        var creationResource = AccountCreationResourceFixture.getDefault();
        creationResource.setPassword("Abc123456");

        //When/Then
        MvcResult result = mockMvc.perform(post(ENDPOINT).content(mapper.writeValueAsBytes(creationResource)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().is4xxClientError()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);
        assertEquals(INVALID_INPUTS, error.getMessage());
        assertEquals(1, error.getErrors().size());
        assertEquals("Password must contain an upper and lower case, a number, and a symbol.", error.getErrors().get(0).getMessage());
    }

    @Test
    @DisplayName("Should return error for password not containing a number")
    void shouldReturnErrorForNoNumberInPassword() throws Exception {
        //Given
        var creationResource = AccountCreationResourceFixture.getDefault();
        creationResource.setPassword("Abcwerlkjbcx");

        //When/Then
        MvcResult result = mockMvc.perform(post(ENDPOINT).content(mapper.writeValueAsBytes(creationResource)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().is4xxClientError()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);
        assertEquals(INVALID_INPUTS, error.getMessage());
        assertEquals(1, error.getErrors().size());
        assertEquals(INVALID_PASSWORD, error.getErrors().get(0).getMessage());
    }

    @Test
    @DisplayName("Should return error for password not containing a lower case and an upper case character")
    void shouldReturnErrorForNoLowerAndUpperCaseInPassword() throws Exception {
        //Given
        var creationResource = AccountCreationResourceFixture.getDefault();
        creationResource.setPassword("asdf12345");

        //When/Then
        MvcResult result = mockMvc.perform(post(ENDPOINT).content(mapper.writeValueAsBytes(creationResource)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().is4xxClientError()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);
        assertEquals(INVALID_INPUTS, error.getMessage());
        assertEquals(1, error.getErrors().size());
        assertEquals(INVALID_PASSWORD, error.getErrors().get(0).getMessage());
    }

    @Test
    @DisplayName("Should return error for empty first Name")
    void shouldReturnErrorForEmptyFirstName() throws Exception {
        //Given
        var creationResource = AccountCreationResourceFixture.getDefault();
        creationResource.setFirstName("");

        //When/Then
        MvcResult result = mockMvc.perform(post(ENDPOINT).content(mapper.writeValueAsBytes(creationResource)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().is4xxClientError()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);
        assertEquals(INVALID_INPUTS, error.getMessage());
        assertEquals(1, error.getErrors().size());
        assertEquals(MUST_NOT_BE_BLANK, error.getErrors().get(0).getMessage());
    }

    @Test
    @DisplayName("Should return error for blank first Name")
    void shouldReturnErrorForBlankFirstName() throws Exception {
        //Given
        var creationResource = AccountCreationResourceFixture.getDefault();
        creationResource.setFirstName(" ");

        //When/Then
        MvcResult result = mockMvc.perform(post(ENDPOINT).content(mapper.writeValueAsBytes(creationResource)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().is4xxClientError()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);
        assertEquals(INVALID_INPUTS, error.getMessage());
        assertEquals(1, error.getErrors().size());
        assertEquals(MUST_NOT_BE_BLANK, error.getErrors().get(0).getMessage());
    }

    @Test
    @DisplayName("Should return error for too long first Name")
    void shouldReturnErrorForLongFirstName() throws Exception {
        //Given
        var creationResource = AccountCreationResourceFixture.getDefault();
        creationResource.setFirstName("0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");

        //When/Then
        MvcResult result = mockMvc.perform(post(ENDPOINT).content(mapper.writeValueAsBytes(creationResource)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().is4xxClientError()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);
        assertEquals(INVALID_INPUTS, error.getMessage());
        assertEquals(1, error.getErrors().size());
        assertEquals("size must be between 0 and 30", error.getErrors().get(0).getMessage());
    }


    @Test
    @DisplayName("Should return error for empty last name")
    void shouldReturnErrorForEmptyLastName() throws Exception {
        //Given
        var creationResource = AccountCreationResourceFixture.getDefault();
        creationResource.setLastName("");

        //When/Then
        MvcResult result = mockMvc.perform(post(ENDPOINT).content(mapper.writeValueAsBytes(creationResource)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().is4xxClientError()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);
        assertEquals(INVALID_INPUTS, error.getMessage());
        assertEquals(1, error.getErrors().size());
        assertEquals(MUST_NOT_BE_BLANK, error.getErrors().get(0).getMessage());
    }

    @Test
    @DisplayName("Should return error for blank last name")
    void shouldReturnErrorForBlankLastName() throws Exception {
        //Given
        var creationResource = AccountCreationResourceFixture.getDefault();
        creationResource.setLastName(" ");

        //When/Then
        MvcResult result = mockMvc.perform(post(ENDPOINT).content(mapper.writeValueAsBytes(creationResource)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().is4xxClientError()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);
        assertEquals(INVALID_INPUTS, error.getMessage());
        assertEquals(1, error.getErrors().size());
        assertEquals(MUST_NOT_BE_BLANK, error.getErrors().get(0).getMessage());
    }

    @Test
    @DisplayName("Should return error for too long last Name")
    void shouldReturnErrorForLongLastName() throws Exception {
        //Given
        var creationResource = AccountCreationResourceFixture.getDefault();
        creationResource.setLastName("0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");

        //When/Then
        MvcResult result = mockMvc.perform(post(ENDPOINT).content(mapper.writeValueAsBytes(creationResource)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().is4xxClientError()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);
        assertEquals(INVALID_INPUTS, error.getMessage());
        assertEquals(1, error.getErrors().size());
        assertEquals("size must be between 0 and 30", error.getErrors().get(0).getMessage());
    }

    @Test
    @DisplayName("Should return a list of subErrors")
    void shouldReturnListSubErrors() throws Exception {
        //Given
        var creationResource = AccountCreationResourceFixture.getDefault();
        creationResource.setEmail("INVALID EMAIL");
        creationResource.setLastName("");
        creationResource.setPassword("000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");

        //When/Then
        MvcResult result = mockMvc.perform(post(ENDPOINT).content(mapper.writeValueAsBytes(creationResource)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().is4xxClientError()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);
        assertEquals(INVALID_INPUTS, error.getMessage());
        assertEquals(4, error.getErrors().size());
    }

    @Test
    @DisplayName("Should return error 400 for same email in Db")
    void shouldReturn400ForSameEmail() throws Exception {
        //Given
        var creationResource = AccountCreationResourceFixture.getDefault();

        //When/Then
        MvcResult result = mockMvc.perform(post(ENDPOINT).content(mapper.writeValueAsBytes(creationResource)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().is4xxClientError()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);
        assertEquals("This email already exists in the database.", error.getMessage());
    }


    @ParameterizedTest
    @DisplayName("Should return a proper reply with 201 status using various symbols, The following symbols do not work: <,>,{,}, -, _ ")
    @ValueSource(strings = {"!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "+", "=", ".", ",", "|", "~", ";", "[", "]", "/", "\\"})
    void shouldReturn201NormalCaseWithSymbols(String input) throws Exception {
        //Given
        var creationResource = AccountCreationResourceFixture.getDefault();
        creationResource.setEmail(uuidv4 + "@email.com");
        uuidv4 = UUID.randomUUID();
        creationResource.setPassword("Password123" + input);

        //When/Then
        MvcResult result = mockMvc.perform(post(ENDPOINT).content(mapper.writeValueAsBytes(creationResource)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().is2xxSuccessful()).andReturn();
        String content = result.getResponse().getContentAsString();
        AccountResource response = mapper.readValue(content, AccountResource.class);
        assertEquals(response.getEmail(), creationResource.getEmail());
        assertEquals(response.getFirstName(), creationResource.getFirstName());
        assertEquals(response.getLastName(), creationResource.getLastName());
        assertNull(response.getBirthDate());
        assertNull(response.getMiddleName());
        assertNull(response.getLocation());
        assertNull(response.getPhoneNumber());
        assertNull(response.getGender());
        assertNotNull(response.getId());
    }


}