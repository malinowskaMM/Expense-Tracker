package pl.lodz.p.it.expenseTracker.service.converter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.lodz.p.it.expenseTracker.domain.entity.Transaction;
import pl.lodz.p.it.expenseTracker.dto.transaction.response.TransactionListResponseDto;
import pl.lodz.p.it.expenseTracker.dto.transaction.response.TransactionResponseDto;
import pl.lodz.p.it.expenseTracker.utils.etag.MessageSigner;

import java.util.List;

@RequiredArgsConstructor
@Component
public class TransactionServiceConverter {

    private final MessageSigner messageSigner;

    public TransactionResponseDto toTransactionResponseDto(Transaction transaction) {
        return TransactionResponseDto.builder()
                .id(String.valueOf(transaction.getId()))
                .name(transaction.getName())
                .categoryName(transaction.getCategory().getName())
                .categoryColor(transaction.getCategory().getColor())
                .categoryId(String.valueOf(transaction.getCategory().getId()))
                .groupName(transaction.getCategory().getGroup().getName())
                .isCyclic(transaction.getIsCyclic())
                .period(transaction.getPeriod())
                .periodUnit(transaction.getIsCyclic()? transaction.getPeriodUnit().name() : null)
                .date(transaction.getStartDate())
                .endDate(transaction.getEndDate())
                .amount(transaction.getAmount())
                .accountId(String.valueOf(transaction.getAccount().getId()))
                .type(transaction.getTransactionType())
                .sign(messageSigner.sign(transaction))
                .version(String.valueOf(transaction.getVersion()))
                .build();
    }

    public TransactionListResponseDto toTransactionListResponseDto(List<Transaction> transactions) {
        return TransactionListResponseDto.builder()
                .transactions(transactions.stream().map(this::toTransactionResponseDto).toList())
                .build();
    }
}