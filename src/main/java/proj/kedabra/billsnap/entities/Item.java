package proj.kedabra.billsnap.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "item", schema = "public")
public class Item implements Serializable {

    private static final long serialVersionUID = 5729865586128253732L;

    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "ITEMS_id_seq")
    @SequenceGenerator(name = "ITEMS_id_seq", sequenceName = "ITEMS_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "bill_id", nullable = false)
    private Long billID;

    @Column(name = "name", length = 30, nullable = false)
    private String name;

    @Column(name = "cost", precision = 14, scale = 2, nullable = false)
    private BigDecimal cost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id")
    private Bill bill;

    @OneToMany(mappedBy = "item", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<AccountItem> accounts = new ArrayList<>();
}
