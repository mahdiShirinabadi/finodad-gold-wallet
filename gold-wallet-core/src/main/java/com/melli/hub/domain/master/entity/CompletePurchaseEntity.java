package com.melli.hub.domain.master.entity;

import lombok.*;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "complete_purchase_part")
public class CompletePurchaseEntity extends BaseEntityAudit implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "complete_purchase_part_seq")
	@SequenceGenerator(name = "complete_purchase_part_seq", sequenceName = "complete_purchase_part_id_seq", allocationSize = 1)
	@Column(name = "id")
	private long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "channel_id", nullable = false)
	private ChannelEntity channelEntity;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "terminal_id", nullable = false)
	private Terminal terminal;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "rrn_id", nullable = false)
	private Rrn rrn;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "wallet_account_id", nullable = false)
	private WalletAccount walletAccount;

	@Column(name = "transaction_time", nullable = false)
	private Date transactionTime;

	@Column(name = "amount")
	private long amount;

	//baraye eenke baraye create kardan bill rahat  tar bashim
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "account_type_id", nullable = false)
	private AccountType accountType;

	@Column(name = "additional_data", nullable = true)
	private String additionalData;

	@Column(name = "settle_time", nullable = true)
	private Date settleTime;

	@Column(name = "settle_status", nullable = true)
	private String settleStatus;

	@Column(name = "mobile", nullable = true)
	private String mobile;

	@Column(name = "channel_code", nullable = true)
	private String channelCode;

	@Column(name = "terminal_bill_id" , nullable = true)
	private long terminalBillId;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public Terminal getTerminal() {
		return terminal;
	}

	public void setTerminal(Terminal terminal) {
		this.terminal = terminal;
	}

	public Rrn getRrn() {
		return rrn;
	}

	public void setRrn(Rrn rrn) {
		this.rrn = rrn;
	}

	public WalletAccount getWalletAccount() {
		return walletAccount;
	}

	public void setWalletAccount(WalletAccount walletAccount) {
		this.walletAccount = walletAccount;
	}

	public Date getTransactionTime() {
		return transactionTime;
	}

	public void setTransactionTime(Date transactionTime) {
		this.transactionTime = transactionTime;
	}

	public long getAmount() {
		return amount;
	}

	public void setAmount(long amount) {
		this.amount = amount;
	}

	public AccountType getAccountType() {
		return accountType;
	}

	public void setAccountType(AccountType accountType) {
		this.accountType = accountType;
	}

	public String getAdditionalData() {
		return additionalData;
	}

	public void setAdditionalData(String additionalData) {
		this.additionalData = additionalData;
	}

	public Date getSettleTime() {
		return settleTime;
	}

	public void setSettleTime(Date settleTime) {
		this.settleTime = settleTime;
	}

	public String getSettleStatus() {
		return settleStatus;
	}

	public void setSettleStatus(String settleStatus) {
		this.settleStatus = settleStatus;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public long getTerminalBillId() {
		return terminalBillId;
	}

	public void setTerminalBillId(long terminalBillId) {
		this.terminalBillId = terminalBillId;
	}

	public String getChannelCode() {
		return channelCode;
	}

	public void setChannelCode(String channelCode) {
		this.channelCode = channelCode;
	}

	public CompletePurchaseEntity() {
	}


	public CompletePurchaseEntity(Channel channel, Terminal terminal, Rrn rrn, WalletAccount walletAccount, Date transactionTime, long amount, AccountType accountType, String additionalData, Date settleTime, String settleStatus, String mobile, String channelCode) {
		this.channel = channel;
		this.terminal = terminal;
		this.rrn = rrn;
		this.walletAccount = walletAccount;
		this.amount = amount;
		this.accountType = accountType;
		this.additionalData = additionalData;
		this.settleTime = settleTime;
		this.settleStatus = settleStatus;
		this.mobile = mobile;
		this.transactionTime = transactionTime;
		this.channelCode = channelCode;
	}

	@Override
	public String toString() {
		return "CompletePurchase{" +
				"id=" + id +
				", channel=" + channel +
				", terminal=" + terminal +
				", rrn=" + rrn +
				", walletAccount=" + walletAccount +
				", transactionTime=" + transactionTime +
				", amount=" + amount +
				", accountType=" + accountType +
				", additionalData='" + additionalData + '\'' +
				", settleTime=" + settleTime +
				", settleStatus='" + settleStatus + '\'' +
				", mobile='" + mobile + '\'' +
				'}';
	}
}
