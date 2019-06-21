package proj.kedabra.billsnap.entities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "example")
public class Example implements Serializable {

    private static final long serialVersionUID = -2507416884148213414L;

    @Id
    private Long id;

    private String firstName;

    private String lastName;

    private String career;

}
