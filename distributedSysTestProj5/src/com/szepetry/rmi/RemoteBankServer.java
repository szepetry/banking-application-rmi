package com.szepetry.rmi;
import java.rmi.*;
import java.rmi.server.*;
import java.util.*;
import com.szepetry.rmi.Bank.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RemoteBankServer extends UnicastRemoteObject implements RemoteBank
{
    class Account {
        String password;
        int balance;
        List transactions = new ArrayList();
        Account(String password) {
            this.password = password;
            transactions.add("Account opened at " + new Date());
        }
    }
    
    Map accounts = new HashMap();

    public RemoteBankServer() throws RemoteException { super(); }
    
    public synchronized void openAccount(String name, String password)
	throws RemoteException, BankingException
    {
        if (accounts.get(name) != null) 
            throw new BankingException("Account already exists.");
        Account acct = new Account(password);
        accounts.put(name, acct);
    }
    
    Account verify(String name, String password) throws BankingException {
        synchronized(accounts) {
            Account acct = (Account)accounts.get(name);
            if (acct == null) throw new BankingException("No such account");
            if (!password.equals(acct.password)) 
                throw new BankingException("Invalid password");
            return acct;
        }
    }
    
    public synchronized MoneyWrapper closeAccount(String name, String password)
	throws RemoteException, BankingException
    {
        Account acct;
        acct = verify(name, password);
        accounts.remove(name);
        synchronized (acct) {
            int balance = acct.balance;
            acct.balance = 0;
            return new MoneyWrapper(balance);
        }
    }
    
    public void deposit(String name, String password, MoneyWrapper money) 
	throws RemoteException, BankingException
    {
        Account acct = verify(name, password);
        synchronized(acct) { 
            acct.balance += money.amount; 
            acct.transactions.add("Deposited " + money.amount + 
				  " on " + new Date());
        }
    }
    
    public MoneyWrapper withdraw(String name, String password, int amount)
	throws RemoteException, BankingException
    {
        Account acct = verify(name, password);
        synchronized(acct) {
            if (acct.balance < amount) 
                throw new BankingException("Insufficient Funds");
            acct.balance -= amount;
            acct.transactions.add("Withdrew " + amount + " on "+new Date());
            return new MoneyWrapper(amount);
        }
    }
    
    public MoneyWrapper moneyTransfer(String name, String password,
            int amount, String receiverName)
	throws RemoteException, BankingException
    {
        Account acct = verify(name, password);
        Account acctReceiver=(Account)accounts.get(receiverName);
        if (acctReceiver == null) throw new BankingException("No such account");
        synchronized(acct) {
            if (acct.balance < amount) 
                throw new BankingException("Insufficient Funds");
            acct.balance -= amount;
            acctReceiver.balance += amount;
            acct.transactions.add("Sent " + amount +" to Account "+
                    receiverName+" on "+new Date());
            acctReceiver.transactions.add("Received " +amount+" from Account "+
                    name+" on "+new Date());
            return new MoneyWrapper(amount);
//        }
    }
    }
    
    public int getBalance(String name, String password)
	throws RemoteException, BankingException
    {
        Account acct = verify(name, password);
        synchronized(acct) { return acct.balance; }
    }
    
    public List getTransactionHistory(String name, String password)
	throws RemoteException, BankingException
    {
        Account acct = verify(name, password);
        synchronized(acct) { return acct.transactions; }
    }
    

    public static void main(String[] args) {
        try {
            Registry reg = LocateRegistry.createRegistry(1099);
            RemoteBankServer bank = new RemoteBankServer();
            String name = System.getProperty("bankname", "MainBankServer");
            reg.rebind(name, bank);
            System.out.println(name + " is now running. "
                    + "Please connect using Client.\nUsage: java -jar"
                    + " \"distributedSysTestProj5.jar\" [options]");
            System.out.println("[Options]:\nOPEN ID PASS\nCLOSE ID PASS\n"
                    + "WITHDRAW ID PASS amt\nDEPOSIT ID PASS amt\n"
                    + "HISTORY ID PASS\nBALANCE ID PASS\n"
                    + "TRANSFER ID PASS amt receivername\n");
        }
        catch (Exception e) {
            System.err.println(e);
            System.err.println("Usage: java [-Dbankname=<name>] " +
		            "com.szepetry.rmi.RemoteBankServer");
            System.exit(1);
        }
    }
}
