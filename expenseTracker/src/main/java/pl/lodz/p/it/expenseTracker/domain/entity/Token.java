package pl.lodz.p.it.expenseTracker.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.lodz.p.it.expenseTracker.domain.enums.TokenTypeEnum;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column
    public String token;

    @Enumerated(EnumType.STRING)
    public TokenTypeEnum type = TokenTypeEnum.BEARER;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    public boolean revoked;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    public boolean expired;

    @ManyToOne
    @JoinColumn(name = "account_id")
    public Account account;

    public Token(String token, TokenTypeEnum type, boolean revoked, boolean expired, Account account) {
        this.token = token;
        this.type = type;
        this.revoked = revoked;
        this.expired = expired;
        this.account = account;
    }
}
