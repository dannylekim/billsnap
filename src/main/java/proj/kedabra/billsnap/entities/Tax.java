package proj.kedabra.billsnap.entities;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "tax")
@IdClass(TaxId.class)
public class Tax implements Serializable {

    private static final long serialVersionUID = -8132492217154508847L;

    @Id
    @Column(name = "order")
    private Long order;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id")
    private Bill bill;

    @Column(name = "amount", precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(name = "percentage", precision = 6, scale = 4)
    private BigDecimal percentage;

}
