package proj.kedabra.billsnap.business.model.entities;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "tax", schema = "public")
public class Tax implements Serializable {

    private static final long serialVersionUID = -8132492217154508847L;

    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "tax_id_seq")
    @SequenceGenerator(name = "tax_id_seq", sequenceName = "tax_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false)
    private Bill bill;

    @Column(name = "name", length = 10)
    private String name;

    @Column(name = "percentage", precision = 7, scale = 4)
    private BigDecimal percentage;

}
