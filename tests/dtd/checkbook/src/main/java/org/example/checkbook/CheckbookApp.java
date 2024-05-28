package org.example.checkbook;

import static org.example.checkbook.TransDate.parseDate;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.ListIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckbookApp
{
	// Represent a SLF4J logger for this class.
    protected static Logger logger = LoggerFactory.getLogger(CheckbookApp.class);
    
    public static final ObjectFactory OF = new ObjectFactory();
    
    private static final File TRANSACTIONS_SOURCE = new File("src/test/samples");
    public static final File TRANSACTIONS_MARCH_A = new File(TRANSACTIONS_SOURCE, "Transactions-200103A.xml");
    public static final File TRANSACTIONS_APRIL_A = new File(TRANSACTIONS_SOURCE, "Transactions-200104A.xml");
    public static final File CHECKBOOK_FEB_A = new File(TRANSACTIONS_SOURCE, "CheckBook-200102A.xml");
    
    private static final File TRANSACTIONS_TARGET = new File("target");
    public static final File TRANSACTIONS_MARCH_B = new File(TRANSACTIONS_TARGET, "Transactions-200103B.xml");
    public static final File TRANSACTIONS_APRIL_B = new File(TRANSACTIONS_TARGET, "Transactions-200104B.xml");
    public static final File CHECKBOOK_FEB_B = new File(TRANSACTIONS_TARGET, "CheckBook-200102B.xml");

	public static Transactions marchTrans = null;
	public static Transactions aprilTrans = null;
	public static CheckbookBalance chBook = null;

	public static void main(String[] args) throws Exception
	{
		try
		{
			// Build the content trees
			buildTrees();
			// Access content of trees
			accessContent();
			// Validate the trees
			validateTrees();
			// Marshal the trees
			marshalTrees();
			// Append the april transactions to the march transactions
			appendTrees();
			// Unmarshal the checkbook subclass
			unmarshalSubclass();
			// Add the transactions to the checkbook and update the balance
			chBook.balanceCheckbook(marchTrans);
			// Validate the updated checkbook and marshal it
			validateAndMarshalCheckbook();
		}
		catch ( Exception ex )
		{
			logger.error("cannot execute main: ", ex);
		}
	}
	
	// Building the content trees
	public static void buildTrees() throws Exception
	{
		marchTrans = new Transactions();
		aprilTrans = new Transactions();
		chBook = new CheckbookBalance();

		// Unmarshal the march.xml file
		marchTrans = marchTrans.unmarshal(TRANSACTIONS_MARCH_A);
		
		// Instantiate a content tree for the April transactions
		List<Account> aprilEntries = aprilTrans.getDepositOrCheckOrWithdrawal();
		Check aprilRentCheck = new Check();
		CheckCategory aprilRent = CheckCategory.RENT;
		aprilRentCheck.setcategory(aprilRent);
		aprilRentCheck.setName("Me");
		aprilRentCheck.setnumber(51);
		aprilRentCheck.setDate(OF.createDate(parseDate("04-12-2001")));
		aprilRentCheck.setAmount(OF.createAmount(new BigDecimal("1500.00")));
		aprilRentCheck.getPendingOrVoidOrCleared().add(OF.createPending());
		aprilEntries.add(aprilRentCheck);
	}
	
	// Access content of trees
	public static void accessContent()
	{
		// Edit the name on the groceries check in the Transaction content tree.
		List<Account> entryList = marchTrans.getDepositOrCheckOrWithdrawal();
		Object entry;
		for (ListIterator<Account> i = entryList.listIterator(); i.hasNext(); )
		{
			entry = i.next();
			if( entry instanceof Check )
			{
				Check check = (Check) entry;
				CheckCategory category = check.getcategory();
				if ( category.equals(CheckCategory.GROCERIES) )
				{
					check.setName("Mom & Pop Foods");
					logger.trace("Edit Check......: {} {} {} {}",
						check.getDate().getvalue(), check.getAmount().getvalue(),
						check.getcategory(), check.getName());					break;
				}
			}
		}
		
		// Edit the rent check in the aprilTrans content tree
		List<Account> aprilEntries = aprilTrans.getDepositOrCheckOrWithdrawal();
		for (ListIterator<Account> i = aprilEntries.listIterator(); i.hasNext(); )
		{
			entry = i.next();
			if ( entry instanceof Check )
			{
				Check check = (Check) entry;
				CheckCategory category = check.getcategory();
				if ( category.equals(CheckCategory.RENT) )
				{
					check.setAmount(OF.createAmount(new BigDecimal("2000.00")));
					logger.trace("Edit Check......: {} {} {} {}",
						check.getDate().getvalue(), check.getAmount().getvalue(),
						check.getcategory(), check.getName());
					break;
				}
			}
		}
	}

	// Validate the trees
	public static void validateTrees() throws Exception
	{
		// Validate the two content trees
		marchTrans.validate();
		aprilTrans.validate();
	}
	
	// Marshal the trees
	public static void marshalTrees() throws Exception
	{
		// Create output files for the two content trees
		// Marshal the two content trees to new XML documents
		marchTrans.marshal(TRANSACTIONS_MARCH_B);
		aprilTrans.marshal(TRANSACTIONS_APRIL_B);
	}
	
	// Append the April transactions to the march transactions
	public static void appendTrees()
	{
		// Append the aprilTrans content tree to the Trans content tree
		List<Account> mEntries = marchTrans.getDepositOrCheckOrWithdrawal();
		List<Account> aEntries = aprilTrans.getDepositOrCheckOrWithdrawal();
		for ( Account account : aEntries )
		{
			// Initialize a BigDecimal to track the
			// amount of each transaction.
			if ( account instanceof Deposit )
			{
				Deposit deposit = ((Deposit) account);
				logger.trace("Balance Deposit....: {} {} {} {}",
					deposit.getDate().getvalue(), deposit.getAmount().getvalue(),
					deposit.getcategory(), deposit.getName());
			}
			else if ( account instanceof Check )
			{
				Check check = ((Check) account);
				logger.trace("Balance Check......: {} {} {} {}",
					check.getDate().getvalue(), check.getAmount().getvalue(),
					check.getcategory(), check.getName());
			}
			else if ( account instanceof Withdrawal )
			{
				Withdrawal wd = ((Withdrawal) account);
				logger.trace("Balance Withdrawal.: {} {}",
					wd.getDate().getvalue(), wd.getAmount().getvalue());
			}
		}
		mEntries.addAll(aEntries);
	}
	
	// Unmarshal the checkbook subclass
	public static void unmarshalSubclass() throws Exception
	{
		// Register the subclass of Checkbook with a dispatcher
		Dispatcher dispatcher = new Dispatcher();
		dispatcher.register(Checkbook.class, CheckbookBalance.class);
		
		// Unmarshal the checkbook.xml file
		// Unmarshal the checkbook file to a CheckbookBalance
		chBook = (CheckbookBalance) (dispatcher.unmarshal(CHECKBOOK_FEB_A));
	}
	
	// Validate the updated checkbook and marshal it
	public static void validateAndMarshalCheckbook() throws Exception
	{
		chBook.validate();
		// Create an output file for the updated checkbook
		// Marshal the updated checkbook
		chBook.marshal(CHECKBOOK_FEB_B);
	}
}
