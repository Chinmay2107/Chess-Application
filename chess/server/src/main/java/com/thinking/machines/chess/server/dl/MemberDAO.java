package com.thinking.machines.chess.server.dl;
import java.util.*;
import java.sql.*;
public class MemberDAO implements java.io.Serializable
{
public List<MemberDTO> getAll()
{
List<MemberDTO> members=new LinkedList<>();
try
{
Connection connection=DAOConnection.getConnection();

Statement statement;
statement=connection.createStatement();
ResultSet resultSet;
resultSet=statement.executeQuery("select * from member");
MemberDTO memberDTO;
while(resultSet.next())
{
memberDTO=new MemberDTO();
memberDTO.username=resultSet.getString(1).trim();
memberDTO.password=resultSet.getString(2).trim();
members.add(memberDTO);
}
resultSet.close();
statement.close();
connection.close();
}catch(SQLException sqlException)
{
System.out.println(sqlException);
System.exit(0);
}
return members;
}
}