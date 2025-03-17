package pl.lodz.p.it.expenseTracker.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.expenseTracker.domain.entity.Account;
import pl.lodz.p.it.expenseTracker.repository.administration.AccountRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.StreamSupport;

@Component
public class SystemScheduler {

  @Autowired
  private AccountRepository accountRepository;

  private final LoggerService logger = new LoggerService();

  @Scheduled(fixedDelay = 24 * 60 * 60 * 1000) // Uruchamiany co 24 godziny
  @Transactional("trackingTransactionManager")
  public void removeUnconfirmedAccounts() {
    List<Account> unconfirmedUsers = StreamSupport.stream(accountRepository.findAll().spliterator(), false)
            .filter(account -> !account.isActive())
            .filter(account -> account.getRegisterDate().isBefore(LocalDateTime.now().minusHours(72)))
            .toList();

    for (Account account : unconfirmedUsers) {
      logger.log("Unconfirmed account with id " + account.getId() + " and registration date " + account.getRegisterDate()
                      + "has been deleted because of not being confirm within 72 hours.",
              "SystemScheduler", LoggerService.LoggerServiceLevel.INFO);
      accountRepository.delete(account);
    }
  }
}
