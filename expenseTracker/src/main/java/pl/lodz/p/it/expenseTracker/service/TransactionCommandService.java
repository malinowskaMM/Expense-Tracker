package pl.lodz.p.it.expenseTracker.service;

import pl.lodz.p.it.expenseTracker.dto.transaction.request.TransactionCyclicEnumDto;
import pl.lodz.p.it.expenseTracker.dto.transaction.request.TransactionPeriodTypeDto;
import pl.lodz.p.it.expenseTracker.dto.transaction.request.TransactionTypeEnumDto;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface TransactionCommandService {
    Void createTransaction(String name, TransactionCyclicEnumDto cycle, String categoryId,
                           TransactionTypeEnumDto type, LocalDate date,
                           BigDecimal value, Integer period, TransactionPeriodTypeDto periodType,
                           String creatorId);

    Void changeTransaction(String id, String name, TransactionCyclicEnumDto cycle, BigDecimal period,
                           TransactionPeriodTypeDto periodType, TransactionTypeEnumDto type,
                           String categoryId, LocalDate date, BigDecimal value, String version,
                           String ifMatchHeader);

    Void deleteTransaction(String id);

    Void stopRecurringTransaction(String id);

    Void renewRecurringTransaction(String id);

}
