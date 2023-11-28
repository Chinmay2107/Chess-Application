package com.thinking.machines.chess.server;
import com.thinking.machines.chess.server.dl.*;
import com.thinking.machines.chess.common.*;
import java.util.*;
import com.thinking.machines.nframework.server.*;
import com.thinking.machines.nframework.server.annotations.*;
@Path("/ChessServer")
public class ChessServer
{
private static Map<String,Member> members;
private static Set<String> loggedInMembers;
private static Set<String> playingMembers;
private static Map<String,List<Message>> inboxes;
private static Map<String,Game> games;
private static Map<String,String> ids;
static
{
populateDataStructures();
}
public ChessServer()
{
}
private static void populateDataStructures()
{
MemberDAO memberDAO=new MemberDAO();
List<MemberDTO> membersList=memberDAO.getAll();
Member m;
members=new HashMap<>();
for(MemberDTO member:membersList)
{
m=new Member();
m.username=member.username;
m.password=member.password;
members.put(member.username,m);
}
loggedInMembers=new HashSet<>();
playingMembers=new HashSet<>();
inboxes=new HashMap<>();
games=new HashMap<>();
ids=new HashMap<>();
}
@Path("/authenticateMember")
public boolean isMemberAuthentic(String username,String password)
{
Member member=members.get(username);
if(member==null) return false;
if(member.password.equals(password))
{
if(loggedInMembers.contains(username)) return false;
loggedInMembers.add(username);
return true;
}
return false;
}
@Path("/logout")
public List<String> logout(String username)
{
loggedInMembers.remove(username);
playingMembers.remove(username);
//Game g=games.get(getGameId(username));
if(ids.containsKey(username))
{
String id=ids.get(username);
Game g=games.get(id);
if(g!=null)
{
if(g.player1.equals(username)) g.player1=null;
else g.player2=null;
}
ids.remove(username);
}
if(inboxes.containsKey(username))
{
List<Message> m=inboxes.get(username);
List<String> list=new LinkedList<>();
for(int i=0;i<m.size();i++) list.add(m.get(i).fromUsername);
return list;
}
return null;
}
@Path("/getMembers")
public List<String> getAvailableMembers(String username)
{
List<String> availableMembers=new LinkedList<>();
for(String u:loggedInMembers)
{
if(!playingMembers.contains(u) && !u.equals(username)) availableMembers.add(u);
}
return availableMembers;
}
@Path("/inviteUser")
public void inviteUser(String fromUsername,String toUsername,String messageType)
{
Message message=new Message();
message.fromUsername=fromUsername;
message.toUsername=toUsername;
if(messageType.equals("CHALLENGE")) message.type=MESSAGE_TYPE.CHALLENGE;
else if(messageType.equals("CHALLENGE_ACCEPTED"))
{
message.type=MESSAGE_TYPE.CHALLENGE_ACCEPTED;
playingMembers.add(fromUsername);
playingMembers.add(toUsername);
}
else if(messageType.equals("CHALLENGE_REJECTED")) message.type=MESSAGE_TYPE.CHALLENGE_REJECTED;
else message.type=MESSAGE_TYPE.NOT_AVAILABLE;
System.out.println("~~~~~~"+fromUsername+", "+toUsername+", "+messageType);
List<Message> messages=inboxes.get(toUsername);
if(messages==null)
{
messages=new LinkedList<Message>();
messages.add(message);
inboxes.put(toUsername,messages);
return;
}
messages.add(message);
}
@Path("/isOpponentDisconnected")
public boolean isOpponentDisconnected(String username)
{
String id=ids.get(username);
Game g=games.get(id);
if(g.player1==null || g.player2==null) return true;
return false;
}
@Path("/getMessages")
public Object getMessages(String username)
{
List<Message> messages=inboxes.get(username);
if(messages!=null && messages.size()>0)
{
inboxes.replace(username,new LinkedList<Message>()); //remove all the old messages
}
return messages;
}
@Path("/setGame")
public void setGame(String player1,String player2)
{
if(ids.containsKey(player1) || ids.containsKey(player2)) return;
ids.put(player1,player1+"*"+player2);
ids.put(player2,player1+"*"+player2);
Game game=new Game();
game.id=player1+"*"+player2;
game.player1=player1;
game.player2=player2;
game.board=new byte[8][8];
game.activePlayer=1;
game.moves=new LinkedList<>();
games.put(game.id,game);
}
@Path("/getGameId")
public String getGameId(String username)
{
String gameId=ids.get(username);
if(gameId==null) return "";
return gameId;
}
@Path("/canIPlay")
public boolean canIPlay(String gameId,String username)
{
Game game=games.get(gameId);
if(game.player1==null || game.player2==null) return false;
if(game.player1.equals(username) && game.activePlayer==2) return false;
else if(game.player2.equals(username) && game.activePlayer==1) return false;
return true;
}
@Path("/submitMove")
public void submitMove(String byUsername,String piece,String fromX,String fromY,String toX,String toY,String isLastMove)
{
try
{
String gameId=ids.get(byUsername);
if(gameId==null) return;
Game game=games.get(gameId);
Move move=new Move();
if(game.activePlayer==1) game.activePlayer=2;
else game.activePlayer=1;
if(game.player1.equals(byUsername)) move.player=1;
else move.player=2;
move.piece=Byte.parseByte(piece);
move.fromX=Byte.parseByte(fromX);
move.fromY=Byte.parseByte(fromY);
move.toX=Byte.parseByte(toX);
move.toY=Byte.parseByte(toY);
move.isLastMove=Boolean.parseBoolean(isLastMove);
game.moves.add(move);
}catch(Throwable t)
{
System.out.println(t);
System.out.println(t.getMessage());
}
}
@Path("/getOpponentMove")
public Object getOpponentMove(String username)
{
String gameId=ids.get(username);
if(gameId==null) return null;
Game game=games.get(gameId);
return game.moves.get(game.moves.size()-1);
}

}