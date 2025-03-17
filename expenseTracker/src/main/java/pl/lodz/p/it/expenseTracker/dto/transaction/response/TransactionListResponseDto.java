package pl.lodz.p.it.expenseTracker.dto.transaction.response;

import lombok.*;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class TransactionListResponseDto {

    List<TransactionResponseDto> transactions;
}