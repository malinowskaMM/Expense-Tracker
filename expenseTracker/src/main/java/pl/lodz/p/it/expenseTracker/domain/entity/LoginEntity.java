package pl.lodz.p.it.expenseTracker.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@NamedQueries({
        @NamedQuery(name = "LoginEntity.findById", query = "SELECT d FROM LoginEntity d WHERE d.id = :id")
})
@Table(name = "login_entity")
public class LoginEntity {

    @Id
    @OneToOne
    @JoinColumn(name = "id", updatable = false, referencedColumnName = "id")
    private Account id;

    @Setter
    @Column(name = "last_valid_login_date")
    private LocalDateTime lastValidLoginDate;

    @Setter
    @Column(name = "last_invalid_login_date")
    private LocalDateTime lastInvalidLoginDate;

    @Setter
    @Min(value = 0)
    @Max(value = 3)
    @Column(name = "invalid_login_counter", columnDefinition = "INTEGER DEFAULT '0'")
    private Integer invalidLoginCounter;

    public LoginEntity(Account id) {
        this.id = id;
        this.invalidLoginCounter = 0;
    }
}