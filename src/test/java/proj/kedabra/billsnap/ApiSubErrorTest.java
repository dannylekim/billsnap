package proj.kedabra.billsnap;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ApiSubErrorTest {


    @Test
    @DisplayName("Should have all FieldError fields mapped")
    void shouldHaveFieldErrorsMapped() {
        //Given
        var fieldError = FieldErrorFixture.getDefault(1);

        //When
        var subError = new ApiSubError(fieldError);

        //Then
        assertEquals("objectName1", subError.getObjectName());
        assertEquals("field1", subError.getField());
        assertEquals("rejectedValue1", subError.getRejectedValue());
        assertEquals("defaultMessage1", subError.getMessage());
        assertEquals("real1", subError.getErrorCode());
        assertArrayEquals(new String[]{"one1", "two1", "three1"}, subError.getArguments());


    }

}