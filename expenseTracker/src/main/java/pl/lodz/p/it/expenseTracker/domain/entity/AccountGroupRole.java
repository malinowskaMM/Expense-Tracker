package pl.lodz.p.it.expenseTracker.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.lodz.p.it.expenseTracker.domain.enums.GroupRoleEnum;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(discriminatorType = DiscriminatorType.STRING, name = "access_level")
@Entity
@Table(name = "account_group_role", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"group_id", "account_id"})
})
public class AccountGroupRole extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Setter
    @ManyToOne
    @JoinColumn(name = "account_id", updatable = false, nullable = false, referencedColumnName = "id")
    private Account account;

    @Setter
    @ManyToOne
    @JoinColumn(name = "group_id", updatable = false, nullable = false, referencedColumnName = "id")
    private Group group;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    @Setter
    private GroupRoleEnum role;

    public AccountGroupRole(Account account, Group group, GroupRoleEnum role) {
        this.account = account;
        this.group = group;
        this.role = role;
    }
}