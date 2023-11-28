package com.thinking.machines.chess.server.dl;
import java.sql.*;
public class DAOConnection
{
private DAOConnection(){}
public static Connection getConnection()
{
Connection connection=null;
try
{
Class.forName("com.mysql.cj.jdbc.Driver");
connection=DriverManager.getConnection("jdbc:mysql://localhost:3306/chessdb","chess","chess");
}catch(Exception exception)
{
System.out.println(exception.getMessage());
System.exit(0);
}
return connection;
}
}