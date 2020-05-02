package com.szepetry.rmi;

import java.rmi.*;
import java.util.List;

public class Bank {
    
    public interface RemoteBank extends Remote {
        public void openAccount(String name, String password) 
	    throws RemoteException, BankingException;
	
        public MoneyWrapper closeAccount(String name, String password) 
	    throws RemoteException, BankingException;
	
        public void deposit(String name, String password, MoneyWrapper money)
	    throws RemoteException, BankingException;
	
        public MoneyWrapper withdraw(String name, String password, int amount) 
	    throws RemoteException, BankingException;
	
        public int getBalance(String name, String password) 
	    throws RemoteException, BankingException;
	
        public List getTransactionHistory(String name, String password) 
	    throws RemoteException, BankingException;
        
        public MoneyWrapper moneyTransfer(String name, String password, 
                int amount,String receiverName) //------------------
            throws RemoteException, BankingException;
    }
    
    
    public static class MoneyWrapper implements java.io.Serializable {
        public int amount;
        public MoneyWrapper(int amount) { this.amount = amount; }
    }
    
    public static class BankingException extends Exception {
        public BankingException(String msg) { super(msg); }
    }
    
    
    public static class Client {
        public static void main(String[] args) {
            try {
                String url = System.getProperty("bank", "rmi:///MainBankServer");
                RemoteBank bank = (RemoteBank) Naming.lookup(url);
                String cmd = args[0].toLowerCase();
		
                if (cmd.equals("open")) {
                    bank.openAccount(args[1], args[2]);
                    System.out.println("Account opened.");
                }
                else if (cmd.equals("transfer")) {
                    MoneyWrapper money = bank.moneyTransfer(args[1], args[2],
                            Integer.parseInt(args[3]), args[4]);
                    System.out.println(money.amount +
				       " rupees transfered to "+args[4]+".");
                }
                else if (cmd.equals("close")) {
                    MoneyWrapper money = bank.closeAccount(args[1], args[2]);
                    System.out.println(money.amount +
				       " rupees returned to you.");
                }
                else if (cmd.equals("deposit")) {
                    MoneyWrapper money=new MoneyWrapper(Integer.parseInt(args[3]));
                    bank.deposit(args[1], args[2], money);
                    System.out.println("Deposited " + money.amount +
				       " rupees.");
                }
                else if (cmd.equals("withdraw")) {
                    MoneyWrapper money = bank.withdraw(args[1], args[2], 
						    Integer.parseInt(args[3]));
                    System.out.println("Withdrew " + money.amount +
				       " rupees.");
                }
                else if (cmd.equals("balance")) {
                    int amt = bank.getBalance(args[1], args[2]);
                    System.out.println("You have " + amt +
				       " rupees in your bank account.");
                }
                else if (cmd.equals("history")) {
                    List transactions =
			bank.getTransactionHistory(args[1], args[2]);
                    for(int i = 0; i < transactions.size(); i++)
                        System.out.println(transactions.get(i));
                }
                else System.out.println("Unknown command");
            }
            catch (RemoteException e) { System.err.println(e); }
            catch (BankingException e) { System.err.println(e.getMessage()); }
            catch (Exception e) { 
                System.err.println(e);
                System.err.println("Usage: java [-Dbank=<url>] Bank$Client " + 
				   "<cmd> <name> <password> [<amount>] [<receiverName>]");
                System.err.println("where cmd is: open, close, deposit, " + 
				   "withdraw, balance, history, moneyTransfer");
            }
        }
    }
}
