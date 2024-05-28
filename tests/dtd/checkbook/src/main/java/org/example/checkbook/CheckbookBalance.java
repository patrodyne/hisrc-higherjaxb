package org.example.checkbook;

import static java.math.BigDecimal.ZERO;
import static org.example.checkbook.CheckbookApp.OF;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.ListIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "checkbookbalance")
public class CheckbookBalance extends Checkbook
{
	private static final long serialVersionUID = 20240501L;
	// Represent a SLF4J logger for this class.
    protected static Logger logger = LoggerFactory.getLogger(CheckbookBalance.class);


	public static Balance initializeBalance(Balance balance)
	{
		// Get the current balance of the checkbook
		if ( balance == null )
			balance = OF.createBalance(ZERO);
		
		if ( balance.getvalue() == null )
			balance.setvalue(ZERO);
		
		return balance;
	}
	
	public void balanceCheckbook(Transactions trans) throws Exception
	{
		setBalance(initializeBalance(getBalance()));
		BigDecimal balanceValue = getBalance().getvalue();
		
		// Get the list of transactions from the Transaction object
		List<Account> entryList = trans.getDepositOrCheckOrWithdrawal();
		
		// Iterate through the transaction list, recalculate the balance,
		// and add the transaction to the checkbook
		for (ListIterator<Account> i = entryList.listIterator(); i.hasNext(); )
		{
			Account nextEntry = i.next();
				
			// Initialize a BigDecimal to track the
			// amount of each transaction.
			if ( nextEntry instanceof Deposit )
			{
				Deposit deposit = ((Deposit) nextEntry);
				BigDecimal amt = deposit.getAmount().getvalue();
				logger.trace("Balance Deposit....: {} {} {} {}",
					deposit.getDate().getvalue(), deposit.getAmount().getvalue(),
					deposit.getcategory(), deposit.getName());
				balanceValue = balanceValue.add(amt);
			}
			else if ( nextEntry instanceof Check )
			{
				Check check = ((Check) nextEntry);
				BigDecimal amt = check.getAmount().getvalue();
				logger.trace("Balance Check......: {} {} {} {}",
					check.getDate().getvalue(), check.getAmount().getvalue(),
					check.getcategory(), check.getName());
				balanceValue = balanceValue.subtract(amt);
			}
			else if ( nextEntry instanceof Withdrawal )
			{
				Withdrawal wd = ((Withdrawal) nextEntry);
				BigDecimal amt = wd.getAmount().getvalue();
				balanceValue = balanceValue.subtract(amt);
				logger.trace("Balance Withdrawal.: {} {}",
					wd.getDate().getvalue(), wd.getAmount().getvalue());
			}
			
			getTransactions().getDepositOrCheckOrWithdrawal().add(nextEntry);
		}
		
		// Check if the balance is negative.
		if (balanceValue.compareTo(new BigDecimal(0.00)) == -1)
			logger.warn("You are overdrawn.");
		
		// Output the new balance
		logger.info("Your balance is: "+balanceValue);
		
		// Update the balance in the checkbook.
		getBalance().setvalue(balanceValue);
	}

	@Override
	public void marshal(File file)
	{
		marshal(this, file);
	}

	@Override
	public void validate()
	{
		// TODO Auto-generated method stub
	}
}
