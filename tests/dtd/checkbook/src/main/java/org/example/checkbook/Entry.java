package org.example.checkbook;

public interface Entry
{
	public Amount getAmount();
	public void setAmount(Amount amount);

	public Date getDate();
	public void setDate(Date date);
}