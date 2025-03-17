package pl.lodz.p.it.expenseTracker.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import pl.lodz.p.it.expenseTracker.utils.etag.Signable;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
@Entity
@Table(name = "group_")
public class Group extends AbstractEntity implements Signable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Setter
    @Size(min = 3)
    private String name;

    @Setter
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "group", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<AccountGroupRole> accountGroupRoles = new ArrayList<>();

    @Setter
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "group", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Category> categories = new ArrayList<>();

    public Group(String name) {
        this.name = name;
    }

    @Override
    public String messageToSign() {
        return id.toString()
                .concat(name)
                .concat(getVersion().toString());
    }
}