package com.thinking.machines.chess.client;
import com.thinking.machines.nframework.client.*;
public class Main
{
public static void main(String args[])
{
try
{
NFrameworkClient client=new NFrameworkClient();
String username=args[0];
String password=args[1];
boolean authentic=(boolean)client.process("/ChessServer/authenticateMember",username,password);
if(authentic)
{
ChessUI chessUI=new ChessUI(username);
chessUI.showUI();
}
else System.out.println("Invalid username/password");
}catch(Throwable t)
{
System.out.println(t);
System.out.println("Some issue");
System.out.println(t.getMessage());
}
}
}