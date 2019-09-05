package proj.kedabra.billsnap.presentation.resources;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class ItemAssociationResource implements Serializable {

    private static final long serialVersionUID = 8993336555723890511L;

    @NotBlank
    @Email(message = "{email.emailFormat}")
    @Size(max = 50)
    private String email;

    @NotNull
    private List<@Valid ItemPercentageResource> items;
}
