package proj.kedabra.billsnap.business.model.entities;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "tax", schema = "public")
public class Tax implements Serializable {

    private static final long serialVersionUID = -8132492217154508847L;

    @EmbeddedId
    private TaxId id = new TaxId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("bill_id")
    private Bill bill;

    @Column(name = "amount", precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(name = "percentage", precision = 7, scale = 4)
    private BigDecimal percentage;

}
