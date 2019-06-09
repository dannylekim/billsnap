package proj.kedabra.billsnap.entities;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@Entity
@Table(name = "location", schema = "public")
public class Location implements Serializable {

    private static final long serialVersionUID = -4217226004443801760L;

    @Id
    private Long id;
    private String name;
    private String description;
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
}
