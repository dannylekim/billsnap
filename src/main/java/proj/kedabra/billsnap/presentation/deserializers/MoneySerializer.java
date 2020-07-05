package proj.kedabra.billsnap.presentation.deserializers;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import proj.kedabra.billsnap.business.service.CalculatePaymentService;

public class MoneySerializer extends JsonSerializer<BigDecimal> {

    @Override
    public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        final var scaledValue = value.setScale(CalculatePaymentService.DOLLAR_SCALE, RoundingMode.UNNECESSARY);
        gen.writeString(scaledValue.toString());
    }
}
