package pl.lodz.p.it.expenseTracker.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import pl.lodz.p.it.expenseTracker.domain.enums.AccountRoleEnum;
import pl.lodz.p.it.expenseTracker.utils.etag.Signable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

//TODO: dodac restrykcje na kolumny + poprawic kasady a nie CASCADE.ALL
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "account",
        indexes =  @Index(name = "unique_email", columnList = "email", unique = true)
)
public class Account extends AbstractEntity implements Signable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Setter
    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isActive;

    @Setter
    @Column(name = "is_archived", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isArchived;

    @Setter
    @Column(name = "is_enabled", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean isEnabled;

    @Setter
    @Column(name = "register_date", nullable = false)
    private LocalDateTime registerDate;

    @Setter
    @Column(name = "language_", nullable = false)
    private String language_;

    @OneToOne(mappedBy = "id", cascade = {CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.MERGE})
    private LoginEntity loginEntity;

    @Enumerated(EnumType.STRING)
    private AccountRoleEnum role;

    @Setter
    @Email
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{1,10}$", message = "Invalid email format")
    @Column(name = "email", nullable = false)
    private String email;

    @Setter
    @Column(name = "password", nullable = false, length = 60)
    private String password;

    @Setter
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "account", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    private List<AccountGroupRole> groupRoles;

    @Setter
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "account", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Transaction> transactions;

    public Account(boolean isActive, boolean isArchived, LocalDateTime registerDate, String language_,
                   String email, String password) {
        this.isActive = isActive;
        this.isArchived = isArchived;
        this.isEnabled = true;
        this.registerDate = registerDate;
        this.language_ = language_;
        this.loginEntity = new LoginEntity(this);
        this.email = email;
        this.password = password;
        this.groupRoles = new ArrayList<>();
        this.role = AccountRoleEnum.USER;
    }

    public Account(boolean isActive, boolean isArchived, LocalDateTime registerDate, String language_,
                   String email, String password, AccountRoleEnum role) {
        this.isActive = isActive;
        this.isArchived = isArchived;
        this.isEnabled = true;
        this.registerDate = registerDate;
        this.language_ = language_;
        this.loginEntity = new LoginEntity(this);
        this.email = email;
        this.password = password;
        this.groupRoles = new ArrayList<>();
        this.role = role;
    }

    @Override
    public String messageToSign() {
        return id.toString()
                .concat(email)
                .concat(isActive ? "Y" : "N")
                .concat(isEnabled ? "Y" : "N")
                .concat(registerDate.toString())
                .concat(role.name())
                .concat(language_)
                .concat(getVersion().toString());
    }


}