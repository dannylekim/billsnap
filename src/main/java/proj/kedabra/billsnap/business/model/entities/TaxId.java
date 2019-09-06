package proj.kedabra.billsnap.business.model.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.Data;

@Data
@Embeddable
public class TaxId implements Serializable {

    private static final long serialVersionUID = 7297884521839281261L;

    @Column(name = "tax_order", nullable = false)
    private Long taxOrder;

    @Column(name = "bill_id", nullable = false)
    private Long billId;

}
