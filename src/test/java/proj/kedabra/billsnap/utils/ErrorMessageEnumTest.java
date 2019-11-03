package proj.kedabra.billsnap.utils;

//import static org.junit.jupiter.api.Assertions.*;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ErrorMessageEnumTest {

    @Test
    void ShouldReturnErrorMessageContainingAllParameters() {
        //given
        final var param1 = "something@something.com";

        //when
        var errorMessages = ErrorMessageEnum.NO_USER_FOUND_WITH_EMAIL.getMessage(param1);

        //then
        assertThat(errorMessages).isEqualTo("No user found with email 'something@something.com'");


    }

    @Test
    void ShouldReturnErrorMessageWithoutAnyParameters() {
        //given /when
        var errorMessages = ErrorMessageEnum.ACCOUNT_DOES_NOT_EXIST.getMessage();

        //then
        assertThat(errorMessages).isEqualTo("Account does not exist");
    }
}