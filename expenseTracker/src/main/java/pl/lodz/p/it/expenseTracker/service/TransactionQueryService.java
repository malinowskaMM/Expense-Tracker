package pl.lodz.p.it.expenseTracker.service;

import pl.lodz.p.it.expenseTracker.dto.transaction.response.TransactionListResponseDto;
import pl.lodz.p.it.expenseTracker.dto.transaction.response.TransactionResponseDto;

import java.time.LocalDate;

public interface TransactionQueryService {

    TransactionListResponseDto getTransactionsInCategory(String categoryId);

    TransactionListResponseDto getTransactionsByAccountId(String accountId);

    TransactionResponseDto getTransactionById(String id);

    TransactionListResponseDto getTransactionsByAccountIdAndDate(String accountId, LocalDate data);
}
