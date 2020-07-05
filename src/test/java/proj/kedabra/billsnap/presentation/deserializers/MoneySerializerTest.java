package proj.kedabra.billsnap.presentation.deserializers;

import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.math.BigDecimal;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MoneySerializerTest {

    @Mock
    private JsonGenerator gen;

    @Mock
    private SerializerProvider provider;

    private final MoneySerializer moneySerializer = new MoneySerializer();

    @Test
    @DisplayName("Should serialize as a money")
    void shouldSerializePercentage() throws IOException {
        moneySerializer.serialize(BigDecimal.TEN, gen, provider);
        verify(gen).writeString("10.00");
    }


}