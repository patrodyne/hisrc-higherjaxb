package org.example.checkbook;

public class EntryImpl extends Account implements Entry
{
	private static final long serialVersionUID = 20240501L;
	
	private Amount amount;
	@Override
	public Amount getAmount() { return amount; }
	@Override
	public void setAmount(Amount amount) { this.amount = amount; }

	private Date date;
	@Override
	public Date getDate() { return date; }
	@Override
	public void setDate(Date date) { this.date = date; }
}
