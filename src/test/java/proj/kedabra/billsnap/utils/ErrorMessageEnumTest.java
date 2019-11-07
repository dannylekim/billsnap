package proj.kedabra.billsnap.utils;

//import static org.junit.jupiter.api.Assertions.*;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ErrorMessageEnumTest {

    @Test
    void ShouldReturnErrorMessageContainingAllParameters() {
        //given
        final var param1 = "something@something.com";
        final var param2 = "otherthing@other.com";


        //when
        var errorMessages = ErrorMessageEnum.LIST_ACCOUNT_DOES_NOT_EXIST.getMessage(param1, param2);

        //then
        assertThat(errorMessages).isEqualTo("One or more accounts in the list of accounts does not exist: something@something.com, otherthing@other.com");


    }

    @Test
    void ShouldReturnErrorMessageWithoutAnyParameters() {
        //given /when
        final var errorMessages = ErrorMessageEnum.ACCOUNT_DOES_NOT_EXIST.getMessage();

        //then
        assertThat(errorMessages).isEqualTo("Account does not exist");
    }
}