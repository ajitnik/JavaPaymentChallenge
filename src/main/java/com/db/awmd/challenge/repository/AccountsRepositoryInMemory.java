package com.db.awmd.challenge.repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.TransferAmountDTO;
import com.db.awmd.challenge.exception.AccountNotExistException;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.NegativeBalanceException;
import com.db.awmd.challenge.service.NotificationService;

@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {

	@Autowired
	private NotificationService notificationService;

	private final Map<String, Account> accounts = new ConcurrentHashMap<>();

	@Override
	public void createAccount(Account account) throws DuplicateAccountIdException {
		Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
		if (previousAccount != null) {
			throw new DuplicateAccountIdException("Account id " + account.getAccountId() + " already exists!");
		}
	}

	@Override
	public Account getAccount(String accountId) {
		return accounts.get(accountId);
	}

	@Override
	public void clearAccounts() {
		accounts.clear();
	}

	@Override
	public String transferAmount(TransferAmountDTO transferDto)
			throws AccountNotExistException, NegativeBalanceException {
		Account fromAccount = accounts.get(transferDto.getFromAccountId());
		if (fromAccount == null) {
			throw new AccountNotExistException("Account " + transferDto.getFromAccountId() + " does not exist");
		}

		Account toAccount = accounts.get(transferDto.getToAccountId());
		if (toAccount == null) {
			throw new AccountNotExistException("Account " + transferDto.getToAccountId() + " does not exist");
		}

		synchronized (fromAccount.getAccountId()) {
			synchronized (toAccount.getAccountId()) {
				BigDecimal postTransFromBalance = fromAccount.getBalance().subtract(transferDto.getTransferAmount());
				System.out.println(postTransFromBalance);
				if (postTransFromBalance.compareTo(BigDecimal.ZERO) < 0) {
					throw new NegativeBalanceException(
							"Account " + transferDto.getFromAccountId() + " will have negative balance after transfer");
				}

				fromAccount.setBalance(fromAccount.getBalance().add(transferDto.getTransferAmount().negate()));
				toAccount.setBalance(toAccount.getBalance().add(transferDto.getTransferAmount()));

				accounts.put(fromAccount.getAccountId(), fromAccount);
				accounts.put(toAccount.getAccountId(), toAccount);

				notificationService.notifyAboutTransfer(fromAccount, "Amount " + transferDto.getTransferAmount()
						+ " recieved from account " + toAccount.getAccountId());

				notificationService.notifyAboutTransfer(toAccount, "Amount " + transferDto.getTransferAmount()
						+ " transferd to account " + fromAccount.getAccountId());

				return "Amount " + transferDto.getTransferAmount() + " transferd from account "
						+ transferDto.getFromAccountId() + " to " + transferDto.getToAccountId();
			}
		}

	}

}
