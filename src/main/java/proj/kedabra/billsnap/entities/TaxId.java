package proj.kedabra.billsnap.entities;

import java.io.Serializable;

import lombok.Data;

@Data
public class TaxId implements Serializable {

    private static final long serialVersionUID = 7297884521839281261L;

    private Long order;

    private Bill bill;

}
