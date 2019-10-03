package proj.kedabra.billsnap.business.model.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "location", schema = "public")
public class Location implements Serializable {

    private static final long serialVersionUID = -4217226004443801760L;

    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "LOCATION_id_seq")
    @SequenceGenerator(name = "LOCATION_id_seq", sequenceName = "LOCATION_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "description", length = 100)
    private String description;

    @Column(name = "address", length = 50)
    private String address;

    @Column(name = "city", length = 20)
    private String city;

    @Column(name = "state", length = 20)
    private String state;

    @Column(name = "country", length = 20)
    private String country;

    @Column(name = "postal_code", length = 10)
    private String postalCode;
}
