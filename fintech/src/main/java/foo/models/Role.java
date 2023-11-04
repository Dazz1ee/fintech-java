package foo.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "roles")
@Data
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    @ToString.Exclude
    @Transient
    private List<CustomUser> users;


    public Role(String name) {
        this.name = name;
        users = new ArrayList<>();
    }

    public Role() {
        users = new ArrayList<>();
    }

    public Role(Integer id, String name) {
        users = new ArrayList<>();
        this.id = id;
        this.name = name;
    }
}
