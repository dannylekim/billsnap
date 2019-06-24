package proj.kedabra.billsnap.presentation.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
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

    private static final String MUST_NOT_BE_EMPTY = "must not be empty";

    private static final String ENDPOINT = "/register";

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
        assertEquals("must be a well-formed email address", error.getErrors().get(0).getMessage());

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

        List<String> errorMessages = error.getErrors().stream().map(ApiSubError::getMessage).filter(msg -> msg.equals("must be a well-formed email address") || msg.equals("size must be between 0 and 50")).collect(Collectors.toList());
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
        assertEquals(MUST_NOT_BE_EMPTY, error.getErrors().get(0).getMessage());
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
        assertEquals(1, error.getErrors().size());
        assertEquals(MUST_NOT_BE_EMPTY, error.getErrors().get(0).getMessage());
    }

    @Test
    @DisplayName("Should return error for too long of a password")
    void shouldReturnErrorForLongPassword() throws Exception {
        //Given
        var creationResource = AccountCreationResourceFixture.getDefault();
        creationResource.setPassword("00000000000000000000000000000000000000000000000000000000000000000000000000000000000");

        //When/Then
        MvcResult result = mockMvc.perform(post(ENDPOINT).content(mapper.writeValueAsBytes(creationResource)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().is4xxClientError()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);
        assertEquals(INVALID_INPUTS, error.getMessage());
        assertEquals(1, error.getErrors().size());
        assertEquals("size must be between 0 and 20", error.getErrors().get(0).getMessage());
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
        assertEquals(MUST_NOT_BE_EMPTY, error.getErrors().get(0).getMessage());
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
        assertEquals(MUST_NOT_BE_EMPTY, error.getErrors().get(0).getMessage());
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
        assertEquals(3, error.getErrors().size());
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

    @Test
    @DisplayName("Should return a proper reply with 201 status")
    void shouldReturn201NormalCase() throws Exception {
        //Given
        var creationResource = AccountCreationResourceFixture.getDefault();
        creationResource.setEmail("successful@email.com");

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