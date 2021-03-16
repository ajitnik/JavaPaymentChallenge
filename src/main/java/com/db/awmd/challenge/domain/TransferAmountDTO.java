package com.db.awmd.challenge.domain;

import java.math.BigDecimal;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class TransferAmountDTO {

	@NotNull
	@NotEmpty
	private final String fromAccountId;

	@NotNull
	@NotEmpty
	private final String toAccountId;

	@NotNull
	@Min(value = 0, message = "Transfer amount must be positive.")
	private BigDecimal transferAmount;

	@JsonCreator
	public TransferAmountDTO(@JsonProperty("fromAccountId") String fromAccountId,
			@JsonProperty("toAccountId") String toAccountId,
			@JsonProperty("transferAmount") BigDecimal transferAmount) {
		this.fromAccountId = fromAccountId;
		this.toAccountId = toAccountId;
		this.transferAmount = transferAmount;
	}

}
