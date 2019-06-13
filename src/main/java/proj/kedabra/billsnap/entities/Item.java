package proj.kedabra.billsnap.entities;

import lombok.Data;
import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "items", schema = "public")
public class Item implements Serializable {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "bill_id")
    private Integer billID;

    @Column(name = "name")
    private String name;

    @Column(name = "cost")
    private BigDecimal cost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id")
    private Bill bill;

}
