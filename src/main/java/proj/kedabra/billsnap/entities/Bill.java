package proj.kedabra.billsnap.entities;

import lombok.Data;
import proj.kedabra.billsnap.utils.BillStatusEnum;
import proj.kedabra.billsnap.utils.SplitByEnum;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
@Entity
@Table(name = "bill", schema = "public")
public class Bill implements Serializable {
    private static final long serialVersionUID = 5673217785097106248L;

    @Id
    private Long id;

    private String name;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    private Account responsible;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    private Account creator;

    @Enumerated(EnumType.STRING)
    private BillStatusEnum status;

    private ZonedDateTime created;
    private ZonedDateTime updated;
    private String category;
    private String company;
    private int occurrence;
    private BigDecimal tipPercent;
    private BigDecimal tipAmount;

    @Enumerated(EnumType.STRING)
    private SplitByEnum splitBy;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    private Location location;

    private boolean active;




}
