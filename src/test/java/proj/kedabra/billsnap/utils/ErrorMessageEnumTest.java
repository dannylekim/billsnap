package proj.kedabra.billsnap.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class ErrorMessageEnumTest {

    @Test
    void ShouldReturnErrorMessageContainingAParameter() {
        //given
        final List<String> invalidAccounts = new ArrayList<>();
        invalidAccounts.add("something@something.com");
        invalidAccounts.add("otherthing@other.com");


        //when
        var errorMessages = ErrorMessageEnum.LIST_ACCOUNT_DOES_NOT_EXIST.getMessage(invalidAccounts.toString());

        //then
        assertThat(errorMessages).isEqualTo("One or more accounts in the list of accounts does not exist: [something@something.com, otherthing@other.com]");


    }
    @Test
    void ShouldReturnErrorMessageContainingParametersAtDifferentPositionsInTheString() {
        //given
        final var param1 = "yes";
        final var param2 = "very yes";
        final var param3 = "also yes";


        //when
        var errorMessages = ErrorMessageEnum.TEST_DIFFERENT_POSITION_PARAMS.getMessage(param1, param2, param3);

        //then
        assertThat(errorMessages).isEqualTo("First Param: yes , Second Param: very yes , Third Param: also yes");


    }

    @Test
    void ShouldReturnErrorMessageWithoutAnyParameters() {
        //given /when
        final var errorMessages = ErrorMessageEnum.ACCOUNT_DOES_NOT_EXIST.getMessage();

        //then
        assertThat(errorMessages).isEqualTo("Account does not exist");
    }
}