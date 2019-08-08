package proj.kedabra.billsnap.presentation.resources;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class BillCreationResource {

    @NotBlank
    private String name;

    private String category;

    private String company;

    @NotNull
    private List<ItemResource> items;
}
