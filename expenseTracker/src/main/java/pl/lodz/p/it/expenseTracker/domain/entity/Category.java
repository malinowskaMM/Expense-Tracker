package pl.lodz.p.it.expenseTracker.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import pl.lodz.p.it.expenseTracker.utils.etag.Signable;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
@Entity
@Table(name = "category", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name", "group_id"})
})
public class Category extends AbstractEntity implements Signable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Setter
    @Column(name = "name", nullable = false, length = 100)
    @Size(min = 3)
    private String name;

    @Setter
    @Column(name = "color", nullable = false, length = 7)
    private String color;

    @Setter
    @Column(name = "description", nullable = false, length = 300)
    @Size(min = 3)
    private String description;

    @Setter
    @Column(name = "is_default", nullable = false, length = 300, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isDefault;

    @Setter
    @ManyToOne
    @JoinColumn(name = "group_id", updatable = false, referencedColumnName = "id")
    private Group group;

    @Setter
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "category", cascade = {CascadeType.ALL})
    private List<Transaction> transactions;

    public Category(String name, String color, String description, boolean isDefault, Group group) {
        this.name = name;
        this.color = color;
        this.description = description;
        this.isDefault = isDefault;
        this.group = group;
    }

    @Override
    public String messageToSign() {
        return id.toString()
                .concat(name)
                .concat(color)
                .concat(description)
                .concat(getVersion().toString());
    }
}