package com.thinking.machines.chess.client;
import com.thinking.machines.chess.common.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import com.thinking.machines.nframework.client.*;
public class ChessUI extends JFrame
{
private String username;
private String gameId;
private JTable availableMembersList;
private JScrollPane availableMembersListScrollPane;
private AvailableMembersListModel availableMembersListModel;
private JTable invitationsList;
private JScrollPane invitationsListScrollPane;
private InvitationsListModel invitationsListModel;
private JTable chessBoard;
private static ChessBoardModel chessBoardModel;
private ChessPieceButtonCellEditor chessPieceButtonCellEditor;
private javax.swing.Timer timer;
private javax.swing.Timer timerToFetchMessages;
private javax.swing.Timer canIPlay;
private javax.swing.Timer isOpponentDisconnected;;
private MovesValidation movesValidation;
private Container container;
private NFrameworkClient client;
JPanel p1,p2;
JLabel turnLabel;
private KingValidator kingValidator;
private QueenValidator queenValidator;
private BishopValidator bishopValidator;
private KnightValidator knightValidator;
private RookValidator rookValidator;
private PawnValidator pawnValidator;

public ChessUI(String username)
{
super("Member : "+username);
this.client=new NFrameworkClient();
this.username=username;
initComponents();
setApperance();
addListeners();
Dimension d=Toolkit.getDefaultToolkit().getScreenSize();
int w=1200;
int h=690;
setSize(w,h);
setLocation(d.width/2-w/2,d.height/2-h/2);
}
private void initComponents()
{
p1=new JPanel();
p1.setLayout(new BorderLayout());
p1.add(new JLabel("Members"),BorderLayout.NORTH);
this.availableMembersListModel=new AvailableMembersListModel();
this.availableMembersList=new JTable(this.availableMembersListModel);
this.availableMembersList.getColumn(" ").setCellRenderer(new AvailableMembersListButtonRenderer());
this.availableMembersList.getColumn(" ").setCellEditor(new AvailableMembersListButtonCellEditor());
this.availableMembersListScrollPane=new JScrollPane(this.availableMembersList,ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
p1.add(availableMembersListScrollPane);
turnLabel=new JLabel("");
int lm=0,tm=0;

p2=new JPanel();
p2.setLayout(null);
JLabel l=new JLabel("Invitations");
l.setBounds(lm+445,tm,100,15);
p2.add(l);
this.invitationsListModel=new InvitationsListModel();
this.invitationsList=new JTable(this.invitationsListModel);
this.invitationsList.getColumn(" ").setCellRenderer(new InvitationsListAcceptButtonRenderer());
this.invitationsList.getColumn(" ").setCellEditor(new InvitationsListAcceptButtonCellEditor());
this.invitationsList.getColumn("  ").setCellRenderer(new InvitationsListRejectButtonRenderer());
this.invitationsList.getColumn("  ").setCellEditor(new InvitationsListRejectButtonCellEditor());
this.invitationsList.getColumn("Members").setPreferredWidth(80);
this.invitationsList.getColumn(" ").setPreferredWidth(100);
this.invitationsList.getColumn("  ").setPreferredWidth(100);
this.invitationsListScrollPane=new JScrollPane(this.invitationsList,ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
this.invitationsList.setRowHeight(20);
this.invitationsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
this.invitationsList.getTableHeader().setResizingAllowed(false);
this.invitationsList.getTableHeader().setReorderingAllowed(false);
this.invitationsListScrollPane.setBounds(lm+2,tm+5+15+5,800,600);
p2.add(invitationsListScrollPane);
p2.setPreferredSize(new Dimension(900,700));
p1.setPreferredSize(new Dimension(300,700));
container=getContentPane();
container.setLayout(new BorderLayout());
container.add(p1,BorderLayout.EAST);
container.add(p2,BorderLayout.WEST);
kingValidator=new KingValidator();
queenValidator=new QueenValidator();
bishopValidator=new BishopValidator();
knightValidator=new KnightValidator();
rookValidator=new RookValidator();
pawnValidator=new PawnValidator();
}
private void setApperance()
{}
private void addListeners()
{
timer=new javax.swing.Timer(3000,new ActionListener(){
public void actionPerformed(ActionEvent ae)
{
timer.stop();
try
{
java.util.List<String> members=(java.util.List<String>)client.process("/ChessServer/getMembers",username);
ChessUI.this.availableMembersListModel.setMembers(members);
timer.start();
}catch(Throwable t)
{
JOptionPane.showMessageDialog(ChessUI.this,t.toString());
}
}
});
timerToFetchMessages=new javax.swing.Timer(1000,new ActionListener(){
public void actionPerformed(ActionEvent ae)
{
timerToFetchMessages.stop();
try
{
java.util.List<Message> messages=(java.util.List<Message>)client.process("/ChessServer/getMessages",username);
if(messages==null || messages.size()==0)
{
timerToFetchMessages.start();
return;
}
//done
Object o;
String s,msgType;
Message m;
java.util.List<Message> challengeMessages=new java.util.LinkedList<>();
java.util.List<Message> acceptMessages=new java.util.LinkedList<>();
java.util.List<Message> rejectMessages=new java.util.LinkedList<>();
java.util.List<Message> notAvailableMessages=new java.util.LinkedList<>();
for(int i=0;i<messages.size();i++)
{
o=messages.get(i);
s=o.toString();
//System.out.println("~~~~~~~~~~~~~~~~~~"+s);
m=new Message();
m.fromUsername=s.substring(s.indexOf('=')+1,s.indexOf(','));
//System.out.println("~~~~~~~~~~~~~~~~~~"+m.fromUsername);
m.toUsername=s.substring(s.indexOf('=',s.indexOf('=')+1)+1,s.indexOf(',',s.indexOf(',')+1));
//System.out.println("~~~~~~~~~~~~~~~~~~"+m.toUsername);
msgType=s.substring(s.indexOf('=',s.indexOf("type"))+1,s.indexOf('}'));
//System.out.println("~~~~~~~~~~~~~~~~~~"+msgType);
if(msgType.equals("CHALLENGE"))
{
m.type=MESSAGE_TYPE.CHALLENGE;
challengeMessages.add(m);
}
else if(msgType.equals("CHALLENGE_ACCEPTED")) 
{
m.type=MESSAGE_TYPE.CHALLENGE_ACCEPTED;
acceptMessages.add(m);
}
else if(msgType.equals("CHALLENGE_REJECTED")) 
{
m.type=MESSAGE_TYPE.CHALLENGE_REJECTED;
rejectMessages.add(m);
}
else 
{
m.type=MESSAGE_TYPE.NOT_AVAILABLE;
notAvailableMessages.add(m);
}
}
if(challengeMessages.size()>0) ChessUI.this.invitationsListModel.setMembers(challengeMessages);
else if(acceptMessages.size()>0)
{
timer.stop();
isOpponentDisconnected.start();
timerToFetchMessages.stop();
ChessUI.this.p1.removeAll();
ChessUI.this.p2.removeAll();
ChessUI.this.p1.updateUI();
ChessUI.this.p2.updateUI();
ChessUI.this.container.removeAll();
ChessUI.this.repaint();
ChessUI.this.revalidate();
//System.out.println("````````````Setting Up UI`````````````````");
ChessUI.this.setChessBoard(acceptMessages.get(0).fromUsername,ChessUI.this.username);
movesValidation=new MovesValidation();
movesValidation.playedAMove=false;
return;
}
else if(rejectMessages.size()>0)
{
availableMembersListModel.awaitingInvitationReply=false;
availableMembersListModel.setMembers(new LinkedList<>());
timer.start();
timerToFetchMessages.start();
}
else if(notAvailableMessages.size()>0)
{
ChessUI.this.invitationsListModel.memberDisconnected(notAvailableMessages);
}
timerToFetchMessages.start();
}catch(Throwable t)
{
JOptionPane.showMessageDialog(ChessUI.this,t.toString());
}
}
});
canIPlay=new javax.swing.Timer(1000,new ActionListener(){
public void actionPerformed(ActionEvent ae)
{
canIPlay.stop();
try
{
boolean b=(boolean)client.process("/ChessServer/canIPlay",gameId,username);
if(!b)
{
movesValidation.playedAMove=true;
canIPlay.start();
return;
}
Object o=client.process("/ChessServer/getOpponentMove",username);
String str=o.toString();
//{player=1.0, piece=13.0, fromX=1.0, fromY=4.0, toX=3.0, toY=4.0, isLastMove=false}
//System.out.println("~=~=~=~=~"+str);
Move move=new Move();
move.piece=Byte.parseByte(str.substring(19,str.indexOf(',',19)-2));
move.fromX=(byte)(str.charAt(str.indexOf("fromX")+6)-48);
move.fromY=(byte)(str.charAt(str.indexOf("fromY")+6)-48);
move.toX=(byte)(str.charAt(str.indexOf("toX")+4)-48);
move.toY=(byte)(str.charAt(str.indexOf("toY")+4)-48);
move.isLastMove=Boolean.parseBoolean(str.substring(str.indexOf("is")+11,str.indexOf('}')));
if(move.isLastMove)
{
JOptionPane.showMessageDialog(ChessUI.this,"You Lose!!");
System.exit(0);
}
chessBoardModel.board[move.fromX][move.fromY]=0;
chessBoardModel.board[move.toX][move.toY]=move.piece;
chessBoardModel.pieces[move.toX][move.toY].setIcon(chessBoardModel.pieces[move.fromX][move.fromY].getIcon());
chessBoardModel.pieces[move.fromX][move.fromY].setIcon(null);
chessBoardModel.fireTableDataChanged();
movesValidation.playedAMove=false;
if(turnLabel.getText().startsWith("B"))
{
turnLabel.setText("White's Turn");
movesValidation.whiteTurn=true;
}
else
{
turnLabel.setText("Black's Turn");
movesValidation.whiteTurn=false;
}
movesValidation.playedAMove=false;
}catch(Throwable t)
{
JOptionPane.showMessageDialog(ChessUI.this,t.toString());
}
}
});
isOpponentDisconnected=new javax.swing.Timer(10000,new ActionListener(){
public void actionPerformed(ActionEvent ae)
{
isOpponentDisconnected.stop();
try
{
boolean b=(boolean)client.process("/ChessServer/isOpponentDisconnected",username);
if(b)
{
JOptionPane.showMessageDialog(ChessUI.this,"You Win as opponent has disconnected");
client.process("/ChessServer/logout",username);
System.exit(0);
}
isOpponentDisconnected.start();
}catch(Throwable t)
{
JOptionPane.showMessageDialog(ChessUI.this,t.toString());
}
}
});
addWindowListener(new WindowAdapter(){
public void windowClosing(WindowEvent e)
{
try
{
java.util.List<String> messages=(java.util.List<String>)client.process("/ChessServer/logout",username);
if(messages!=null)
{
for(String member:messages)
{
ChessUI.this.sendInvitation(member,MESSAGE_TYPE.NOT_AVAILABLE);
}
}
}catch(Throwable t)
{
JOptionPane.showMessageDialog(ChessUI.this,t.toString());
}
System.exit(0);
}
});
timer.start();
timerToFetchMessages.start();
}
public void showUI()
{
this.setVisible(true);
}
public void sendInvitation(String toUsername,MESSAGE_TYPE messageType)
{
try
{
if(messageType==MESSAGE_TYPE.CHALLENGE)
{
client.process("/ChessServer/inviteUser",username,toUsername,"CHALLENGE");
JOptionPane.showMessageDialog(this,"Invitation for game sent to : "+toUsername);
}
else if(messageType==MESSAGE_TYPE.CHALLENGE_ACCEPTED) client.process("/ChessServer/inviteUser",username,toUsername,"CHALLENGE_ACCEPTED");
else if(messageType==MESSAGE_TYPE.CHALLENGE_REJECTED) client.process("/ChessServer/inviteUser",username,toUsername,"CHALLENGE_REJECTED");
else client.process("/ChessServer/inviteUser",username,toUsername,"NOT_AVAILABLE");
}catch(Throwable t)
{
JOptionPane.showMessageDialog(this,t);
}
}
public void setChessBoard(String fromUsername,String toUsername)
{
try
{
client.process("/ChessServer/setGame",fromUsername,toUsername);
gameId=(String)client.process("/ChessServer/getGameId",fromUsername);
}catch(Throwable t)
{
JOptionPane.showMessageDialog(this,t);
}
//System.out.println("*-*-*-*-*-*-*-*-*-*-*-*-*");
chessBoardModel=new ChessBoardModel();
chessBoard=new JTable(chessBoardModel);
chessPieceButtonCellEditor=new ChessPieceButtonCellEditor();
for(int x=1;x<=8;x++)
{
chessBoard.getColumn(String.valueOf(x)).setCellRenderer(new ChessPieceButtonRenderer());
chessBoard.getColumn(String.valueOf(x)).setCellEditor(chessPieceButtonCellEditor);
}
container.setLayout(new BorderLayout());
p2=new JPanel();
p1=new JPanel();
turnLabel.setText("White's turn");
p1.setBorder(BorderFactory.createLineBorder(new Color(120,120,120)));
p2.setBorder(BorderFactory.createLineBorder(new Color(120,120,120)));
p2.setPreferredSize(new Dimension(900,700));
p1.setPreferredSize(new Dimension(300,700));
p2.setLayout(null);
chessBoard.setBounds(0,0,900,685);

chessBoard.setRowHeight(82);
p1.add(turnLabel);
p2.add(chessBoard);
chessBoardModel.fireTableDataChanged();
p2.updateUI();
p1.updateUI();
container.setLayout(new BorderLayout());
container.add(p2,BorderLayout.WEST);
container.add(p1,BorderLayout.EAST);
}

//inner classes starts here
//Chess Board class starts
class ChessBoardModel extends AbstractTableModel
{
private byte [][] board;
private String title[]={"1","2","3","4","5","6","7","8"};
private JButton[][] pieces;

ChessBoardModel()
{
board=new byte[8][8];
pieces=new JButton[8][8];
ImageIcon king_white_icon=new ImageIcon(this.getClass().getResource("/icons/king_white.png"));
ImageIcon queen_white_icon=new ImageIcon(this.getClass().getResource("/icons/queen_white.png"));
ImageIcon rook_white_icon=new ImageIcon(this.getClass().getResource("/icons/rook_white.png"));
ImageIcon knight_white_icon=new ImageIcon(this.getClass().getResource("/icons/knight_white.png"));
ImageIcon bishop_white_icon=new ImageIcon(this.getClass().getResource("/icons/bishop_white.png"));
ImageIcon pawn_white_icon=new ImageIcon(this.getClass().getResource("/icons/pawn_white.png"));
ImageIcon king_black_icon=new ImageIcon(this.getClass().getResource("/icons/king_black.png"));
ImageIcon queen_black_icon=new ImageIcon(this.getClass().getResource("/icons/queen_black.png"));
ImageIcon rook_black_icon=new ImageIcon(this.getClass().getResource("/icons/rook_black.png"));
ImageIcon knight_black_icon=new ImageIcon(this.getClass().getResource("/icons/knight_black.png"));
ImageIcon bishop_black_icon=new ImageIcon(this.getClass().getResource("/icons/bishop_black.png"));
ImageIcon pawn_black_icon=new ImageIcon(this.getClass().getResource("/icons/pawn_black.png"));
int i=0;
int j=0;
byte k=9;
byte l=17;
boolean white_color;
boolean black_color;
white_color=true;
while(i<8)
{
j=0;
while(j<8)
{
if(i==0 && j==0)
{
pieces[i][j]=new JButton(rook_white_icon);
board[i][j]=1;
}
else if(i==0 && j==1)
{
pieces[i][j]=new JButton(knight_white_icon);
board[i][j]=2;
}
else if(i==0 && j==2) 
{
pieces[i][j]=new JButton(bishop_white_icon);
board[i][j]=3;
}
else if(i==0 && j==3)
{
pieces[i][j]=new JButton(queen_white_icon);
board[i][j]=4;
}
else if(i==0 && j==4)
{
pieces[i][j]=new JButton(king_white_icon);
board[i][j]=5;
}
else if(i==0 && j==5) 
{
pieces[i][j]=new JButton(bishop_white_icon);
board[i][j]=6;
}
else if(i==0 && j==6) 
{
pieces[i][j]=new JButton(knight_white_icon);
board[i][j]=7;
}
else if(i==0 && j==7) 
{
pieces[i][j]=new JButton(rook_white_icon);
board[i][j]=8;
}
else if(i==1)
{
pieces[i][j]=new JButton(pawn_white_icon);
board[i][j]=k;
k++;
}
else if(i==6)
{
pieces[i][j]=new JButton(pawn_black_icon);
board[i][j]=l;
l++;
}
else if(i==7 && j==0)
{
pieces[i][j]=new JButton(rook_black_icon);
board[i][j]=25;
}
else if(i==7 && j==1)
{
pieces[i][j]=new JButton(knight_black_icon);
board[i][j]=26;
}
else if(i==7 && j==2)
{
pieces[i][j]=new JButton(bishop_black_icon);
board[i][j]=27;
}
else if(i==7 && j==3)
{
pieces[i][j]=new JButton(queen_black_icon);
board[i][j]=28;
}
else if(i==7 && j==4)
{
pieces[i][j]=new JButton(king_black_icon);
board[i][j]=29;
}
else if(i==7 && j==5)
{
pieces[i][j]=new JButton(bishop_black_icon);
board[i][j]=30;
}
else if(i==7 && j==6)
{
pieces[i][j]=new JButton(knight_black_icon);
board[i][j]=31;
}
else if(i==7 && j==7) 
{
pieces[i][j]=new JButton(rook_black_icon);
board[i][j]=32;
}
else
{
pieces[i][j]=new JButton();
}
makeButtonFlat(pieces[i][j]);
if(white_color)
{
pieces[i][j].setBackground(Color.WHITE);
white_color=false;
}
else
{
pieces[i][j].setBackground(new Color(118,150,86));
white_color=true;
}
if(j==7)
{
if(white_color==true) white_color=false;
else white_color=true;
}
j++;
}
i++;
}

}
public JButton getButton(int row,int col)
{
return pieces[row][col];
}
private void makeButtonFlat(JButton button)
{
button.setFocusPainted(false);
button.setRolloverEnabled(false);
button.setContentAreaFilled(false);
button.setOpaque(true);
}
public int getRowCount()
{
return 8;
}
public String getColumnName(int index)
{
return this.title[index];
}
public Object getValueAt(int row,int column)
{
return pieces[row][column];
}
public int getColumnCount()
{
return 8;
}
public boolean isCellEditable(int row,int col)
{
return true;
}
public Class getColumnClass(int c)
{
return JButton.class;
}
public void setValueAt(Object object,int row,int col)
{
movesValidation.validate(row,col);
}
public void setMembers(java.util.List<Message> messages)
{
fireTableDataChanged();
}
}
/*--------------------------RENDERER AND EDITOR CLASS--------------------------*/
class ChessPieceButtonRenderer implements TableCellRenderer
{
public Component getTableCellRendererComponent(JTable table,Object object,boolean a,boolean b,int r,int c)
{
//System.out.println(object.toString());
//System.out.println(r+" "+c);
return (JButton)object;
}
}
class ChessPieceButtonCellEditor extends DefaultCellEditor
{
private JButton button;
private boolean isClicked;
private int row,col;
private ActionListener actionListener;
ChessPieceButtonCellEditor()
{
super(new JCheckBox());
/*button=new JButton();
button.setFocusPainted(false);
button.setRolloverEnabled(false);
button.setContentAreaFilled(false);
button.setOpaque(true);*/

this.actionListener=new ActionListener(){
public void actionPerformed(ActionEvent ae)
{
//System.out.println("Action Performed of button : in Editor Great");
if(movesValidation.playedAMove) return;
movesValidation.setButtonClicked((JButton)ae.getSource());
fireEditingStopped();
}
};
for(int i=0;i<8;i++) for(int j=0;j<8;j++) chessBoardModel.getButton(i,j).addActionListener(this.actionListener);
}
public Component getTableCellEditorComponent(JTable table,Object value,boolean a,int row,int column)
{
this.row=row;
this.col=column;
isClicked=true;
/*button.setFocusPainted(false);
button.setRolloverEnabled(false);
button.setContentAreaFilled(false);
button.setOpaque(true);
button.setIcon(chessBoardModel.getButton(row,column).getIcon());*/
return chessBoardModel.getButton(row,column);
}
public Object getCellEditorValue()
{
//System.out.println("getCellEditorValue got called");
//System.out.println("Button at cell : "+this.row+","+this.col+" got clicked");
return this.row+","+this.col;
}
public boolean stopCellEditing()
{
//System.out.println("stopCellEditing");
isClicked=false;
return super.stopCellEditing();
}
public void fireEditingStopped()
{
//System.out.println("fireEditingStopped");
super.fireEditingStopped();
}
}

/*---------------------------CHESS BOARD CLASS ENDS---------------------------*/
/*--------------------MOVES VALIDATION CLASS STARTS-------------------------*/
class MovesValidation
{
private boolean firstButtonClicked;
private JButton firstButton,secondButton;
private int firstButtonRow,firstButtonColumn;
private boolean whiteTurn;
//private PieceSelectionPanel pieceSelectionPanel;
private boolean isCastlingMoveWhite;
private boolean isCastlingMoveBlack;
private boolean whiteKingCastled;
private boolean blackKingCastled;
private boolean isCheckCondition;
private byte checkDueTo;
private boolean leftWhiteRookMoved;
private boolean leftBlackRookMoved;
private boolean rightWhiteRookMoved;
private boolean rightBlackRookMoved;
private boolean whiteKingMoved;
private boolean blackKingMoved;
private boolean pawnMovedTwoBlocksForward;
private boolean playedAMove;
MovesValidation()
{
checkDueTo=0;
whiteTurn=true;
firstButtonClicked=false;
leftWhiteRookMoved=false;
leftBlackRookMoved=false;
rightWhiteRookMoved=false;
rightBlackRookMoved=false;
whiteKingMoved=false;
blackKingMoved=false;
pawnMovedTwoBlocksForward=false;
//pieceSelectionPanel=new PieceSelectionPanel();
}
public void setButtonClicked(JButton button)
{
//System.out.println("~~~~~setButtonClicked "+firstButtonClicked+" "+playedAMove);
if(playedAMove) return;
if(firstButtonClicked==false)
{
firstButton=button;
firstButtonClicked=true;
secondButton=null;
return;
}
else if(firstButtonClicked) secondButton=button;
}
public void validate(int row,int column)
{
if(secondButton==null)
{
if(chessBoardModel.board[row][column]==0)
{
firstButtonClicked=false;
return;
}
else
{
firstButtonRow=row;
firstButtonColumn=column;
}
}
if(secondButton==null) return;
if(firstButtonClicked)
{
//JButton secondButton=(JButton)ae.getSource();

byte value=chessBoardModel.board[firstButtonRow][firstButtonColumn];
//System.out.println("************"+firstButtonRow+","+firstButtonColumn+","+row+","+column);
//System.out.println("("+value+")");

if(chessBoardModel.board[row][column]==5 || chessBoardModel.board[row][column]==29)
{
firstButtonClicked=false;
return;
}
int [] firstPosition={firstButtonRow,firstButtonColumn};//piecesPositionMap.get(firstButton);
int [] secondPosition={row,column};//piecesPositionMap.get(secondButton);
if(whiteTurn && value>16)
{
firstButtonClicked=false;
return;
}
else if(!whiteTurn && value>0 && value<17)
{
firstButtonClicked=false;
return;
}
//System.out.println("First Poisition : "+firstPosition[0]+" "+firstPosition[1]);
//System.out.println("Second Poisition : "+secondPosition[0]+" "+secondPosition[1]);
if(value>=9 && value<=16)
{
//PawnValidator pawnValidator=new PawnValidator(this,piecesMap,piecesPositionMap);
//pawnValidator.setPositionAndTurn(firstPosition,secondPosition,true);
PawnValidator pawnValidator=new PawnValidator();
pawnValidator.setPositionAndTurn(firstButtonRow,firstButtonColumn,row,column,true);
pawnValidator.start();
try
{
pawnValidator.join();
}catch(Exception e)
{}
boolean pawnValidate=pawnValidator.isMoveValid();
if(pawnValidate==false)
{
firstButtonClicked=false;
return;
}
}
else if(value>=17 && value<=24)
{
//PawnValidator pawnValidator=new PawnValidator(this,piecesMap,piecesPositionMap);
//pawnValidator.setPositionAndTurn(firstPosition,secondPosition,false);
PawnValidator pawnValidator=new PawnValidator();
pawnValidator.setPositionAndTurn(firstButtonRow,firstButtonColumn,row,column,false);
pawnValidator.start();
try
{
pawnValidator.join();
}catch(Exception e)
{}
boolean pawnValidate=pawnValidator.isMoveValid();
if(pawnValidate==false)
{
firstButtonClicked=false;
return;
}
}
else if(value==1 || value==8)
{
//RookValidator rookValidator=new RookValidator(piecesMap,piecesPositionMap);
RookValidator rookValidator=new RookValidator();
rookValidator.setPositionAndTurn(firstButtonRow,firstButtonColumn,row,column,true);
rookValidator.start();
try
{
rookValidator.join();
}catch(Exception e)
{}
boolean rookValidate=rookValidator.isMoveValid();
if(rookValidate==false)
{
firstButtonClicked=false;
return;
}
}
else if(value==2 || value==7)
{
KnightValidator knightValidator=new KnightValidator();
knightValidator.setPositionAndTurn(firstButtonRow,firstButtonColumn,row,column,true);
knightValidator.start();
try
{
knightValidator.join();
}catch(Exception e)
{}
boolean knightValidate=knightValidator.isMoveValid();
if(knightValidate==false)
{
firstButtonClicked=false;
return;
}
}
else if(value==3 || value==6)
{
BishopValidator bishopValidator=new BishopValidator();
bishopValidator.setPositionAndTurn(firstButtonRow,firstButtonColumn,row,column,true);
bishopValidator.start();
try
{
bishopValidator.join();
}catch(Exception e)
{}
boolean bishopValidate=bishopValidator.isMoveValid();
if(bishopValidate==false)
{
firstButtonClicked=false;
return;
}
}
else if(value==4)
{
QueenValidator queenValidator=new QueenValidator();
queenValidator.setPositionAndTurn(firstButtonRow,firstButtonColumn,row,column,true);
queenValidator.start();
try
{
queenValidator.join();
}catch(Exception e)
{}
boolean queenValidate=queenValidator.isMoveValid();
if(queenValidate==false)
{
firstButtonClicked=false;
return;
}
}
else if(value==5)
{
KingValidator kingValidator=new KingValidator();
kingValidator.setPositionAndTurn(firstButtonRow,firstButtonColumn,row,column,true);
kingValidator.start();
try
{
kingValidator.join();
}catch(Exception e)
{}
isCastlingMoveWhite=kingValidator.isCastlingMove(true);
whiteKingCastled=kingValidator.isCastled(true);
boolean kingValidate=kingValidator.isMoveValid();
if(kingValidate==false)
{
firstButtonClicked=false;
return;
}
}
else if(value==25 || value==32)
{
RookValidator rookValidator=new RookValidator();
rookValidator.setPositionAndTurn(firstButtonRow,firstButtonColumn,row,column,false);
rookValidator.start();
try
{
rookValidator.join();
}catch(Exception e)
{}
boolean rookValidate=rookValidator.isMoveValid();
if(rookValidate==false)
{
firstButtonClicked=false;
return;
}
}
else if(value==26 || value==31)
{
KnightValidator knightValidator=new KnightValidator();
knightValidator.setPositionAndTurn(firstButtonRow,firstButtonColumn,row,column,false);
knightValidator.start();
try
{
knightValidator.join();
}catch(Exception e)
{}
boolean knightValidate=knightValidator.isMoveValid();
if(knightValidate==false)
{
firstButtonClicked=false;
return;
}
}
else if(value==27 || value==30)
{
BishopValidator bishopValidator=new BishopValidator();
bishopValidator.setPositionAndTurn(firstButtonRow,firstButtonColumn,row,column,false);
bishopValidator.start();
try
{
bishopValidator.join();
}catch(Exception e)
{}
boolean bishopValidate=bishopValidator.isMoveValid();
if(bishopValidate==false)
{
firstButtonClicked=false;
return;
}
}
else if(value==26)
{
QueenValidator queenValidator=new QueenValidator();
queenValidator.setPositionAndTurn(firstButtonRow,firstButtonColumn,row,column,false);
queenValidator.start();
try
{
queenValidator.join();
}catch(Exception e)
{}
boolean queenValidate=queenValidator.isMoveValid();
if(queenValidate==false)
{
firstButtonClicked=false;
return;
}
}
else if(value==29)
{
KingValidator kingValidator=new KingValidator();
kingValidator.setPositionAndTurn(firstButtonRow,firstButtonColumn,row,column,false);
kingValidator.start();
try
{
kingValidator.join();
}catch(Exception e)
{}
isCastlingMoveBlack=kingValidator.isCastlingMove(false);
blackKingCastled=kingValidator.isCastled(false);
boolean kingValidate=kingValidator.isMoveValid();
if(kingValidate==false)
{
firstButtonClicked=false;
return;
}
}
if(noPossibleMovesLeft(checkDueTo,whiteTurn))
{
//System.out.println("******************************************NO MOVES");
try
{
playedAMove=true;
isOpponentDisconnected.stop();
client.process("/ChessServer/submitMove",username,value+"",firstButtonRow+"",firstButtonColumn+"",row+"",column+"","true");
/*if(turnLabel.getText().startsWith("B")) turnLabel.setText("White's Turn");
else turnLabel.setText("Black's Turn");*/
if(turnLabel.getText().startsWith("B")) JOptionPane.showMessageDialog(ChessUI.this,"Black Wins");
else JOptionPane.showMessageDialog(ChessUI.this,"White Wins");
java.util.List<String> messages=(java.util.List<String>)client.process("/ChessServer/logout",username);
if(messages!=null && messages.size()>0)
{
for(String member:messages)
{
ChessUI.this.sendInvitation(member,MESSAGE_TYPE.NOT_AVAILABLE);
}
}
}catch(Throwable t)
{
JOptionPane.showMessageDialog(ChessUI.this,t.toString());
}
System.exit(0);
/*pieceDetails=new Position();
pieceDetails.firstPosition=firstPosition;
pieceDetails.secondPosition=secondPosition;
pieceDetails.pawnChangedTo="";
pieceDetails.pawnChangedIcon=null;
pieceDetails.noMovesLeft=true;
//sendMove(pieceDetails);
if(whiteTurn) JOptionPane.showMessageDialog(this,"Black Wins");
else JOptionPane.showMessageDialog(this,"White Wins");
System.exit(0);*/
}
Icon img=firstButton.getIcon();
if(isCheckCondition)
{
chessBoardModel.board[firstButtonRow][firstButtonColumn]=0;
byte secondButtonValue=chessBoardModel.board[row][column];
chessBoardModel.board[row][column]=value;
isCheckCondition=isCheckConditionDueToSameColouredPiece(value,whiteTurn);
//System.out.println("SameColouredPiece : "+isCheckCondition);
if(isCheckCondition)
{
firstButtonClicked=false;
chessBoardModel.board[firstButtonRow][firstButtonColumn]=value;
chessBoardModel.board[row][column]=secondButtonValue;
return;
}

firstButton.setIcon(null);
secondButton.setIcon(img);

if(value>=9 && value<=16 && row==7)
{
/*pieceSelectionPanel.setWhoseTurn(secondButton,true);
pieceSelectionPanel.setBounds(85,120,700,300);
getLayeredPane().add(pieceSelectionPanel,JLayeredPane.PALETTE_LAYER);
for(int i=0;i<8;i++) for(int j=0;j<8;j++) pieces[i][j].setEnabled(false);*/
}
else if(value>=17 && value<=24 && row==0)
{
/*pieceSelectionPanel.setWhoseTurn(secondButton,false);
pieceSelectionPanel.setBounds(85,120,700,300);
getLayeredPane().add(pieceSelectionPanel,JLayeredPane.PALETTE_LAYER);
for(int i=0;i<8;i++) for(int j=0;j<8;j++) pieces[i][j].setEnabled(false);*/
}
if(isCastlingMoveWhite && !whiteKingCastled)
{
if(!leftWhiteRookMoved && !whiteKingMoved && row==0 && (column==1 || column==2) && firstButtonColumn==4)
{
firstButtonClicked=false;

chessBoardModel.board[firstButtonRow][firstButtonColumn]=value;
chessBoardModel.board[row][column]=secondButtonValue;
try
{
playedAMove=true;
client.process("/ChessServer/submitMove",username,value+"",firstButtonRow+"",firstButtonColumn+"",row+"",column+"","false");
if(turnLabel.getText().startsWith("B")) turnLabel.setText("White's Turn");
else turnLabel.setText("Black's Turn");
}catch(Throwable t)
{
JOptionPane.showMessageDialog(ChessUI.this,t.toString());
}
canIPlay.start();
/*pieceDetails=new Position();
pieceDetails.firstPosition=firstPosition;
pieceDetails.secondPosition=secondPosition;
pieceDetails.pawnChangedTo="";
pieceDetails.pawnChangedIcon=null;
pieceDetails.noMovesLeft=false;*/
//sendMove(pieceDetails);
return;
}
else if(!rightWhiteRookMoved && !whiteKingMoved && row==0 && column==6  && firstButtonColumn==4)
{
firstButtonClicked=false;
chessBoardModel.board[firstButtonRow][firstButtonColumn]=value;
chessBoardModel.board[row][column]=secondButtonValue;
try
{
playedAMove=true;
client.process("/ChessServer/submitMove",username,value+"",firstButtonRow+"",firstButtonColumn+"",row+"",column+"","false");
if(turnLabel.getText().startsWith("B")) turnLabel.setText("White's Turn");
else turnLabel.setText("Black's Turn");
}catch(Throwable t)
{
JOptionPane.showMessageDialog(ChessUI.this,t.toString());
}
/*pieceDetails=new Position();
pieceDetails.firstPosition=firstPosition;
pieceDetails.secondPosition=secondPosition;
pieceDetails.pawnChangedTo="";
pieceDetails.pawnChangedIcon=null;
pieceDetails.noMovesLeft=false;
//sendMove(pieceDetails);*/
canIPlay.start();
return;
}
}
else if(isCastlingMoveBlack && !blackKingCastled)
{
if(!leftBlackRookMoved && !blackKingMoved && row==7 && (column==1 || column==2) && firstButtonColumn==4)
{
firstButtonClicked=false;
chessBoardModel.board[firstButtonRow][firstButtonColumn]=value;
chessBoardModel.board[row][column]=secondButtonValue;
try
{
playedAMove=true;
client.process("/ChessServer/submitMove",username,value+"",firstButtonRow+"",firstButtonColumn+"",row+"",column+"","false");
if(turnLabel.getText().startsWith("B")) turnLabel.setText("White's Turn");
else turnLabel.setText("Black's Turn");
}catch(Throwable t)
{
JOptionPane.showMessageDialog(ChessUI.this,t.toString());
}
/*pieceDetails=new Position();
pieceDetails.firstPosition=firstPosition;
pieceDetails.secondPosition=secondPosition;
pieceDetails.pawnChangedTo="";
pieceDetails.pawnChangedIcon=null;
pieceDetails.noMovesLeft=false;
//sendMove(pieceDetails);*/
canIPlay.start();
return;
}
else if(!rightBlackRookMoved && !blackKingMoved && row==7 && column==6 && firstButtonColumn==4)
{
firstButtonClicked=false;
chessBoardModel.board[firstButtonRow][firstButtonColumn]=value;
chessBoardModel.board[row][column]=secondButtonValue;
try
{
playedAMove=true;
client.process("/ChessServer/submitMove",username,value+"",firstButtonRow+"",firstButtonColumn+"",row+"",column+"","false");
if(turnLabel.getText().startsWith("B")) turnLabel.setText("White's Turn");
else turnLabel.setText("Black's Turn");
}catch(Throwable t)
{
JOptionPane.showMessageDialog(ChessUI.this,t.toString());
}
/*pieceDetails=new Position();
pieceDetails.firstPosition=firstPosition;
pieceDetails.secondPosition=secondPosition;
pieceDetails.pawnChangedTo="";
pieceDetails.pawnChangedIcon=null;
pieceDetails.noMovesLeft=false;
//sendMove(pieceDetails);*/
canIPlay.start();
return;
}
}
ChessUI.this.repaint();
ChessUI.this.revalidate();
/*if(whiteTurn)
{
//whiteTurn=false;
turnLabel.setText("Black's Turn");
}
else
{
//whiteTurn=true;
turnLabel.setText("White's Turn");
}*/
if(value==1) leftWhiteRookMoved=true;
else if(value==8) rightWhiteRookMoved=true;
else if(value==25) leftBlackRookMoved=true;
else if(value==32) rightBlackRookMoved=true;
else if(value==5) whiteKingMoved=true;
else if(value==29) blackKingMoved=true;
firstButtonClicked=false;
isCheckCondition=false;
checkDueTo=0;
if(noPossibleMovesLeft(checkDueTo,whiteTurn))
{
//System.out.println("******************************************NO MOVES");
try
{
playedAMove=true;
isOpponentDisconnected.stop();
client.process("/ChessServer/submitMove",username,value+"",firstButtonRow+"",firstButtonColumn+"",row+"",column+"","true");
/*if(turnLabel.getText().startsWith("B")) turnLabel.setText("White's Turn");
else turnLabel.setText("Black's Turn");*/
if(turnLabel.getText().startsWith("B")) JOptionPane.showMessageDialog(ChessUI.this,"Black Wins");
else JOptionPane.showMessageDialog(ChessUI.this,"White Wins");
java.util.List<String> messages=(java.util.List<String>)client.process("/ChessServer/logout",username);
if(messages!=null && messages.size()>0)
{
for(String member:messages)
{
ChessUI.this.sendInvitation(member,MESSAGE_TYPE.NOT_AVAILABLE);
}
}
}catch(Throwable t)
{
JOptionPane.showMessageDialog(ChessUI.this,t.toString());
}
System.exit(0);
/*pieceDetails=new Position();
pieceDetails.firstPosition=firstPosition;
pieceDetails.secondPosition=secondPosition;
pieceDetails.pawnChangedTo="";
pieceDetails.pawnChangedIcon=null;
pieceDetails.noMovesLeft=true;
//sendMove(pieceDetails);
if(whiteTurn) JOptionPane.showMessageDialog(this,"Black Wins");
else JOptionPane.showMessageDialog(this,"White Wins");
System.exit(0);*/
}
//Maybe here code to submit move
try
{
playedAMove=true;
client.process("/ChessServer/submitMove",username,value+"",firstButtonRow+"",firstButtonColumn+"",row+"",column+"","false");
if(turnLabel.getText().startsWith("B")) turnLabel.setText("White's Turn");
else turnLabel.setText("Black's Turn");
}catch(Throwable t)
{
JOptionPane.showMessageDialog(ChessUI.this,t.toString());
}
/*pieceDetails=new Position();
pieceDetails.firstPosition=firstPosition;
pieceDetails.secondPosition=secondPosition;
pieceDetails.pawnChangedTo="";
pieceDetails.pawnChangedIcon=null;
pieceDetails.noMovesLeft=false;
//sendMove(pieceDetails);*/
canIPlay.start();
return;
}

chessBoardModel.board[firstButtonRow][firstButtonColumn]=0;
byte secondButtonValue=chessBoardModel.board[row][column];
chessBoardModel.board[row][column]=value;
//System.out.println("1.Value "+value+" "+firstPosition[0]+","+firstPosition[1]);
isCheckCondition=isCheckCondition(value,firstButtonRow,firstButtonColumn,row,column,whiteTurn,false);
//System.out.println("~~~~~~"+isCheckCondition);

if(isCheckCondition && (value==5 || value==29))
{
firstButtonClicked=false;
chessBoardModel.board[firstButtonRow][firstButtonColumn]=value;
chessBoardModel.board[row][column]=secondButtonValue;
checkDueTo=0;
isCheckCondition=false;
return;
}
if(isCheckCondition==false)
{
isCheckCondition=isCheckConditionDueToSameColouredPiece(value,whiteTurn);
//System.out.println("SameColouredPiece : "+isCheckCondition);
//System.out.println("~`~`~`~`~`~`~`~`"+isCheckCondition+value+"~~"+whiteTurn+"Yaha se return");
if(isCheckCondition)
{
firstButtonClicked=false;
chessBoardModel.board[firstButtonRow][firstButtonColumn]=value;
chessBoardModel.board[row][column]=secondButtonValue;
checkDueTo=0;
isCheckCondition=false;
return;
}
}

firstButton.setIcon(null);
secondButton.setIcon(img);

if(value>=9 && value<=16 && row==7)
{
//System.out.println("WP");
/*pieceSelectionPanel.setWhoseTurn(secondButton,true);
pieceSelectionPanel.setBounds(85,120,700,300);
getLayeredPane().add(pieceSelectionPanel,JLayeredPane.PALETTE_LAYER);
for(int i=0;i<8;i++) for(int j=0;j<8;j++) pieces[i][j].setEnabled(false);*/
}
else if(value>=17 && value<=24 && row==0)
{
/*pieceSelectionPanel.setWhoseTurn(secondButton,false);
pieceSelectionPanel.setBounds(85,120,700,300);
getLayeredPane().add(pieceSelectionPanel,JLayeredPane.PALETTE_LAYER);
for(int i=0;i<8;i++) for(int j=0;j<8;j++) pieces[i][j].setEnabled(false);*/
}
if(isCastlingMoveWhite && !whiteKingCastled)
{
if(!leftWhiteRookMoved && !whiteKingMoved && row==0 && (column==1 || column==2) && firstButtonColumn==4)
{
chessBoardModel.pieces[0][2].setIcon(chessBoardModel.pieces[0][0].getIcon());
chessBoardModel.pieces[0][0].setIcon(null);
chessBoardModel.board[0][2]=1;
chessBoardModel.board[0][0]=0;
whiteKingCastled=true;
}
else if(!rightWhiteRookMoved && !whiteKingMoved && row==0 && column==6 && firstButtonColumn==4)
{
chessBoardModel.pieces[0][5].setIcon(chessBoardModel.pieces[0][7].getIcon());
chessBoardModel.pieces[0][7].setIcon(null);
chessBoardModel.board[0][5]=8;
chessBoardModel.board[0][7]=0;
whiteKingCastled=true;
}
}
else if(isCastlingMoveBlack && !blackKingCastled)
{
if(!leftBlackRookMoved && !blackKingMoved && row==7 && (column==1 || column==2) && firstButtonColumn==4)
{
chessBoardModel.pieces[7][2].setIcon(chessBoardModel.pieces[7][0].getIcon());
chessBoardModel.pieces[7][0].setIcon(null);
chessBoardModel.board[7][2]=25;
chessBoardModel.board[7][0]=0;
blackKingCastled=true;
}
else if(!rightBlackRookMoved && !blackKingMoved && row==7 && column==6 && firstButtonColumn==4)
{
chessBoardModel.pieces[7][5].setIcon(chessBoardModel.pieces[7][7].getIcon());
chessBoardModel.pieces[7][7].setIcon(null);
chessBoardModel.board[7][5]=32;
chessBoardModel.board[7][7]=0;
blackKingCastled=true;
}
}
ChessUI.this.repaint();
ChessUI.this.revalidate();
/*if(whiteTurn)
{
//whiteTurn=false;
turnLabel.setText("Black's Turn");
}
else
{
//whiteTurn=true;
turnLabel.setText("White's Turn");
}*/
pawnMovedTwoBlocksForward=false;
if(value==1) leftWhiteRookMoved=true;
else if(value==8) rightWhiteRookMoved=true;
else if(value==25) leftBlackRookMoved=true;
else if(value==32) rightBlackRookMoved=true;
else if(value==5) whiteKingMoved=true;
else if(value==29) blackKingMoved=true;
else if(value>=9 && value<=16)
{
if(firstButtonRow==1 && row==3) pawnMovedTwoBlocksForward=true;
}
else if(value>=17 && value<=24)
{
if(firstButtonRow==6 && row==4) pawnMovedTwoBlocksForward=true;
}
if(noPossibleMovesLeft(checkDueTo,whiteTurn))
{
//System.out.println("******************************************NO MOVES BAAD");
try
{
playedAMove=true;
isOpponentDisconnected.stop();
client.process("/ChessServer/submitMove",username,value+"",firstButtonRow+"",firstButtonColumn+"",row+"",column+"","true");
/*if(turnLabel.getText().startsWith("B")) turnLabel.setText("White's Turn");
else turnLabel.setText("Black's Turn");*/
if(turnLabel.getText().startsWith("B")) JOptionPane.showMessageDialog(ChessUI.this,"Black Wins");
else JOptionPane.showMessageDialog(ChessUI.this,"White Wins");
java.util.List<String> messages=(java.util.List<String>)client.process("/ChessServer/logout",username);
if(messages!=null && messages.size()>0)
{
for(String member:messages)
{
ChessUI.this.sendInvitation(member,MESSAGE_TYPE.NOT_AVAILABLE);
}
}
}catch(Throwable t)
{
JOptionPane.showMessageDialog(ChessUI.this,t.toString());
}
System.exit(0);
//System.out.println("Check "+checkDueTo+", ");
/*pieceDetails=new Position();
pieceDetails.firstPosition=firstPosition;
pieceDetails.secondPosition=secondPosition;
pieceDetails.pawnChangedTo="";
pieceDetails.pawnChangedIcon=null;
pieceDetails.noMovesLeft=true;
//sendMove(pieceDetails);*/

}
firstButtonClicked=false;
try
{
playedAMove=true;
client.process("/ChessServer/submitMove",username,value+"",firstButtonRow+"",firstButtonColumn+"",row+"",column+"","false");
if(turnLabel.getText().startsWith("B")) turnLabel.setText("White's Turn");
else turnLabel.setText("Black's Turn");
}catch(Throwable t)
{
JOptionPane.showMessageDialog(ChessUI.this,t.toString());
}
canIPlay.start();
/*pieceDetails=new Position();
pieceDetails.firstPosition=firstPosition;
pieceDetails.secondPosition=secondPosition;
pieceDetails.pawnChangedTo="";
pieceDetails.pawnChangedIcon=null;
pieceDetails.noMovesLeft=false;
//sendMove(pieceDetails);*/
}


}//validate ends
/*--------------Check Condition & Possible Moves Left Methods start-------------*/
public boolean noPossibleMovesLeft(byte checkDueTo,boolean whiteTurn)
{
int i,j,k;
int kingPosition[]=null;
int checkDueToPosition[]=null;
int queenPosition[]=null;
int leftKnightPosition[]=null;
int rightKnightPosition[]=null;
int leftBishopPosition[]=null;
int rightBishopPosition[]=null;
int leftRookPosition[]=null;
int rightRookPosition[]=null;
int pawnOnePosition[]=null,pawnTwoPosition[]=null,pawnThreePosition[]=null,pawnFourPosition[]=null;
int pawnFivePosition[]=null,pawnSixPosition[]=null,pawnSevenPosition[]=null,pawnEightPosition[]=null;
if(whiteTurn)
{
for(i=0;i<8;i++)
{
for(j=0;j<8;j++)
{
if(chessBoardModel.board[i][j]==5)
{
kingPosition=new int[2];
kingPosition[0]=i;
kingPosition[1]=j;
}
else if(checkDueTo!=0 && chessBoardModel.board[i][j]==checkDueTo)
{
checkDueToPosition=new int[2];
checkDueToPosition[0]=i;
checkDueToPosition[1]=j;
}
else if(chessBoardModel.board[i][j]==4)
{
queenPosition=new int[2];
queenPosition[0]=i;
queenPosition[1]=j;
}
else if(chessBoardModel.board[i][j]==3)
{
leftBishopPosition=new int[2];
leftBishopPosition[0]=i;
leftBishopPosition[1]=j;
}
else if(chessBoardModel.board[i][j]==6)
{
rightBishopPosition=new int[2];
rightBishopPosition[0]=i;
rightBishopPosition[1]=j;
}
else if(chessBoardModel.board[i][j]==2)
{
leftKnightPosition=new int[2];
leftKnightPosition[0]=i;
leftKnightPosition[1]=j;
}
else if(chessBoardModel.board[i][j]==7)
{
rightKnightPosition=new int[2];
rightKnightPosition[0]=i;
rightKnightPosition[1]=j;
}
else if(chessBoardModel.board[i][j]==1)
{
leftRookPosition=new int[2];
leftRookPosition[0]=i;
leftRookPosition[1]=j;
}
else if(chessBoardModel.board[i][j]==8)
{
rightRookPosition=new int[2];
rightRookPosition[0]=i;
rightRookPosition[1]=j;
}
else if(chessBoardModel.board[i][j]==9)
{
pawnOnePosition=new int[2];
pawnOnePosition[0]=i;
pawnOnePosition[1]=j;
}
else if(chessBoardModel.board[i][j]==10)
{
pawnTwoPosition=new int[2];
pawnTwoPosition[0]=i;
pawnTwoPosition[1]=j;
}
else if(chessBoardModel.board[i][j]==11)
{
pawnThreePosition=new int[2];
pawnThreePosition[0]=i;
pawnThreePosition[1]=j;
}
else if(chessBoardModel.board[i][j]==12)
{
pawnFourPosition=new int[2];
pawnFourPosition[0]=i;
pawnFourPosition[1]=j;
}
else if(chessBoardModel.board[i][j]==13)
{
pawnFivePosition=new int[2];
pawnFivePosition[0]=i;
pawnFivePosition[1]=j;
}
else if(chessBoardModel.board[i][j]==14)
{
pawnSixPosition=new int[2];
pawnSixPosition[0]=i;
pawnSixPosition[1]=j;
}
else if(chessBoardModel.board[i][j]==15)
{
pawnSevenPosition=new int[2];
pawnSevenPosition[0]=i;
pawnSevenPosition[1]=j;
}
else if(chessBoardModel.board[i][j]==16)
{
pawnEightPosition=new int[2];
pawnEightPosition[0]=i;
pawnEightPosition[1]=j;
}
}
}
}
else
{
for(i=0;i<8;i++)
{
for(j=0;j<8;j++)
{
if(chessBoardModel.board[i][j]==29)
{
kingPosition=new int[2];
kingPosition[0]=i;
kingPosition[1]=j;
}
else if(checkDueTo!=0 && chessBoardModel.board[i][j]==checkDueTo)
{
checkDueToPosition=new int[2];
checkDueToPosition[0]=i;
checkDueToPosition[1]=j;
}
else if(chessBoardModel.board[i][j]==28)
{
queenPosition=new int[2];
queenPosition[0]=i;
queenPosition[1]=j;
}
else if(chessBoardModel.board[i][j]==27)
{
leftBishopPosition=new int[2];
leftBishopPosition[0]=i;
leftBishopPosition[1]=j;
}
else if(chessBoardModel.board[i][j]==30)
{
rightBishopPosition=new int[2];
rightBishopPosition[0]=i;
rightBishopPosition[1]=j;
}
else if(chessBoardModel.board[i][j]==26)
{
leftKnightPosition=new int[2];
leftKnightPosition[0]=i;
leftKnightPosition[1]=j;
}
else if(chessBoardModel.board[i][j]==31)
{
rightKnightPosition=new int[2];
rightKnightPosition[0]=i;
rightKnightPosition[1]=j;
}
else if(chessBoardModel.board[i][j]==25)
{
leftRookPosition=new int[2];
leftRookPosition[0]=i;
leftRookPosition[1]=j;
}
else if(chessBoardModel.board[i][j]==32)
{
rightRookPosition=new int[2];
rightRookPosition[0]=i;
rightRookPosition[1]=j;
}
else if(chessBoardModel.board[i][j]==17)
{
pawnOnePosition=new int[2];
pawnOnePosition[0]=i;
pawnOnePosition[1]=j;
}
else if(chessBoardModel.board[i][j]==18)
{
pawnTwoPosition=new int[2];
pawnTwoPosition[0]=i;
pawnTwoPosition[1]=j;
}
else if(chessBoardModel.board[i][j]==19)
{
pawnThreePosition=new int[2];
pawnThreePosition[0]=i;
pawnThreePosition[1]=j;
}
else if(chessBoardModel.board[i][j]==20)
{
pawnFourPosition=new int[2];
pawnFourPosition[0]=i;
pawnFourPosition[1]=j;
}
else if(chessBoardModel.board[i][j]==21)
{
pawnFivePosition=new int[2];
pawnFivePosition[0]=i;
pawnFivePosition[1]=j;
}
else if(chessBoardModel.board[i][j]==22)
{
pawnSixPosition=new int[2];
pawnSixPosition[0]=i;
pawnSixPosition[1]=j;
}
else if(chessBoardModel.board[i][j]==23)
{
pawnSevenPosition=new int[2];
pawnSevenPosition[0]=i;
pawnSevenPosition[1]=j;
}
else if(chessBoardModel.board[i][j]==24)
{
pawnEightPosition=new int[2];
pawnEightPosition[0]=i;
pawnEightPosition[1]=j;
}
}
}
}
int checkPositions[][]=null;
int possibleKingPosition[][]=kingValidator.getPossiblePositions(kingPosition,whiteTurn);
int possibleQueenPosition[][]=queenValidator.getPossiblePositions(queenPosition,whiteTurn);
int possibleLeftKnightPosition[][]=knightValidator.getPossiblePositions(leftKnightPosition,whiteTurn);
int possibleRightKnightPosition[][]=knightValidator.getPossiblePositions(rightKnightPosition,whiteTurn);
int possibleLeftBishopPosition[][]=bishopValidator.getPossiblePositions(leftBishopPosition,whiteTurn);
int possibleRightBishopPosition[][]=bishopValidator.getPossiblePositions(rightBishopPosition,whiteTurn);
int possibleLeftRookPosition[][]=rookValidator.getPossiblePositions(leftRookPosition,whiteTurn);
int possibleRightRookPosition[][]=rookValidator.getPossiblePositions(rightRookPosition,whiteTurn);
int possiblePawnOnePosition[][]=pawnValidator.getPossiblePositions(pawnOnePosition,whiteTurn);
int possiblePawnTwoPosition[][]=pawnValidator.getPossiblePositions(pawnTwoPosition,whiteTurn);
int possiblePawnThreePosition[][]=pawnValidator.getPossiblePositions(pawnThreePosition,whiteTurn);
int possiblePawnFourPosition[][]=pawnValidator.getPossiblePositions(pawnFourPosition,whiteTurn);
int possiblePawnFivePosition[][]=pawnValidator.getPossiblePositions(pawnFivePosition,whiteTurn);
int possiblePawnSixPosition[][]=pawnValidator.getPossiblePositions(pawnSixPosition,whiteTurn);
int possiblePawnSevenPosition[][]=pawnValidator.getPossiblePositions(pawnSevenPosition,whiteTurn);
int possiblePawnEightPosition[][]=pawnValidator.getPossiblePositions(pawnEightPosition,whiteTurn);
byte checkDueToOld;
boolean isCheck=false;
for(i=0;i<possibleKingPosition.length;i++)
{
if(possibleKingPosition[i][0]==-1) break;
byte value=chessBoardModel.board[kingPosition[0]][kingPosition[1]];
chessBoardModel.board[kingPosition[0]][kingPosition[1]]=0;
byte secondButtonValue=chessBoardModel.board[possibleKingPosition[i][0]][possibleKingPosition[i][1]];
checkDueToOld=checkDueTo;
if(whiteTurn)
{
chessBoardModel.board[possibleKingPosition[i][0]][possibleKingPosition[i][1]]=5;
isCheck=isCheckConditionDueToSameColouredPiece((byte)5,whiteTurn);
}
else
{
chessBoardModel.board[possibleKingPosition[i][0]][possibleKingPosition[i][1]]=29;
isCheck=isCheckConditionDueToSameColouredPiece((byte)29,whiteTurn);
}
chessBoardModel.board[kingPosition[0]][kingPosition[1]]=value;
chessBoardModel.board[possibleKingPosition[i][0]][possibleKingPosition[i][1]]=secondButtonValue;
checkDueTo=checkDueToOld;
if(!isCheck) return false;
}
if(!isCheckCondition)
{
if(possibleQueenPosition!=null)
{
for(j=0;j<possibleQueenPosition.length;j++)
{
if(possibleQueenPosition[j][0]==-1) break;
byte value=chessBoardModel.board[queenPosition[0]][queenPosition[1]];
chessBoardModel.board[queenPosition[0]][queenPosition[1]]=0;
byte secondButtonValue=chessBoardModel.board[possibleQueenPosition[j][0]][possibleQueenPosition[j][1]];
checkDueToOld=checkDueTo;
if(whiteTurn)
{
chessBoardModel.board[possibleQueenPosition[j][0]][possibleQueenPosition[j][1]]=4;
isCheck=isCheckConditionDueToSameColouredPiece((byte)4,whiteTurn);
}
else
{
chessBoardModel.board[possibleQueenPosition[j][0]][possibleQueenPosition[j][1]]=28;
isCheck=isCheckConditionDueToSameColouredPiece((byte)28,whiteTurn);
}
chessBoardModel.board[queenPosition[0]][queenPosition[1]]=value;
chessBoardModel.board[possibleQueenPosition[j][0]][possibleQueenPosition[j][1]]=secondButtonValue;
checkDueTo=checkDueToOld;
if(!isCheck) return false;
}
}
if(possibleLeftKnightPosition!=null)
{
for(j=0;j<possibleLeftKnightPosition.length;j++)
{
if(possibleLeftKnightPosition[j][0]==-1) break;
byte value=chessBoardModel.board[leftKnightPosition[0]][leftKnightPosition[1]];
chessBoardModel.board[leftKnightPosition[0]][leftKnightPosition[1]]=0;
byte secondButtonValue=chessBoardModel.board[possibleLeftKnightPosition[j][0]][possibleLeftKnightPosition[j][1]];
checkDueToOld=checkDueTo;
if(whiteTurn)
{
chessBoardModel.board[possibleLeftKnightPosition[j][0]][possibleLeftKnightPosition[j][1]]=2;
isCheck=isCheckConditionDueToSameColouredPiece((byte)2,whiteTurn);
}
else
{
chessBoardModel.board[possibleLeftKnightPosition[j][0]][possibleLeftKnightPosition[j][1]]=26;
isCheck=isCheckConditionDueToSameColouredPiece((byte)26,whiteTurn);
}
chessBoardModel.board[leftKnightPosition[0]][leftKnightPosition[1]]=value;
chessBoardModel.board[possibleLeftKnightPosition[j][0]][possibleLeftKnightPosition[j][1]]=secondButtonValue;
checkDueTo=checkDueToOld;
if(!isCheck) return false;
}
}
if(possibleRightKnightPosition!=null)
{
for(j=0;j<possibleRightKnightPosition.length;j++)
{
if(possibleRightKnightPosition[j][0]==-1) break;
byte value=chessBoardModel.board[rightKnightPosition[0]][rightKnightPosition[1]];
chessBoardModel.board[rightKnightPosition[0]][rightKnightPosition[1]]=0;
byte secondButtonValue=chessBoardModel.board[possibleRightKnightPosition[j][0]][possibleRightKnightPosition[j][1]];
checkDueToOld=checkDueTo;
if(whiteTurn)
{
chessBoardModel.board[possibleRightKnightPosition[j][0]][possibleRightKnightPosition[j][1]]=7;
isCheck=isCheckConditionDueToSameColouredPiece((byte)7,whiteTurn);
}
else
{
chessBoardModel.board[possibleRightKnightPosition[j][0]][possibleRightKnightPosition[j][1]]=31;
isCheck=isCheckConditionDueToSameColouredPiece((byte)31,whiteTurn);
}
chessBoardModel.board[leftKnightPosition[0]][leftKnightPosition[1]]=value;
chessBoardModel.board[possibleLeftKnightPosition[j][0]][possibleLeftKnightPosition[j][1]]=secondButtonValue;
checkDueTo=checkDueToOld;
if(!isCheck) return false;
}
}
if(possibleLeftBishopPosition!=null)
{
for(j=0;j<possibleLeftBishopPosition.length;j++)
{
if(possibleLeftBishopPosition[j][0]==-1) break;
byte value=chessBoardModel.board[leftBishopPosition[0]][leftBishopPosition[1]];
chessBoardModel.board[leftBishopPosition[0]][leftBishopPosition[1]]=0;
byte secondButtonValue=chessBoardModel.board[possibleLeftBishopPosition[j][0]][possibleLeftBishopPosition[j][1]];
checkDueToOld=checkDueTo;
if(whiteTurn)
{
chessBoardModel.board[possibleLeftBishopPosition[j][0]][possibleLeftBishopPosition[j][1]]=3;
isCheck=isCheckConditionDueToSameColouredPiece((byte)3,whiteTurn);
}
else
{
chessBoardModel.board[possibleLeftBishopPosition[j][0]][possibleLeftBishopPosition[j][1]]=27;
isCheck=isCheckConditionDueToSameColouredPiece((byte)27,whiteTurn);
}
chessBoardModel.board[leftBishopPosition[0]][leftBishopPosition[1]]=value;
chessBoardModel.board[possibleLeftBishopPosition[j][0]][possibleLeftBishopPosition[j][1]]=secondButtonValue;
checkDueTo=checkDueToOld;
if(!isCheck) return false;
}
}
if(possibleRightBishopPosition!=null)
{
for(j=0;j<possibleRightBishopPosition.length;j++)
{
if(possibleRightBishopPosition[j][0]==-1) break;
byte value=chessBoardModel.board[rightBishopPosition[0]][rightBishopPosition[1]];
chessBoardModel.board[rightBishopPosition[0]][rightBishopPosition[1]]=0;
byte secondButtonValue=chessBoardModel.board[possibleRightBishopPosition[j][0]][possibleRightBishopPosition[j][1]];
checkDueToOld=checkDueTo;
if(whiteTurn)
{
chessBoardModel.board[possibleRightBishopPosition[j][0]][possibleRightBishopPosition[j][1]]=6;
isCheck=isCheckConditionDueToSameColouredPiece((byte)6,whiteTurn);
}
else
{
chessBoardModel.board[possibleRightBishopPosition[j][0]][possibleRightBishopPosition[j][1]]=30;
isCheck=isCheckConditionDueToSameColouredPiece((byte)30,whiteTurn);
}
chessBoardModel.board[rightBishopPosition[0]][rightBishopPosition[1]]=value;
chessBoardModel.board[possibleRightBishopPosition[j][0]][possibleRightBishopPosition[j][1]]=secondButtonValue;
checkDueTo=checkDueToOld;
if(!isCheck) return false;
}
}
if(possibleLeftRookPosition!=null)
{
for(j=0;j<possibleLeftRookPosition.length;j++)
{
if(possibleLeftRookPosition[j][0]==-1) break;
byte value=chessBoardModel.board[leftRookPosition[0]][leftRookPosition[1]];
chessBoardModel.board[leftRookPosition[0]][leftRookPosition[1]]=0;
byte secondButtonValue=chessBoardModel.board[possibleLeftRookPosition[j][0]][possibleLeftRookPosition[j][1]];
checkDueToOld=checkDueTo;
if(whiteTurn)
{
chessBoardModel.board[possibleLeftRookPosition[j][0]][possibleLeftRookPosition[j][1]]=1;
isCheck=isCheckConditionDueToSameColouredPiece((byte)1,whiteTurn);
}
else
{
chessBoardModel.board[possibleLeftRookPosition[j][0]][possibleLeftRookPosition[j][1]]=25;
isCheck=isCheckConditionDueToSameColouredPiece((byte)25,whiteTurn);
}
chessBoardModel.board[leftRookPosition[0]][leftRookPosition[1]]=value;
chessBoardModel.board[possibleLeftRookPosition[j][0]][possibleLeftRookPosition[j][1]]=secondButtonValue;
checkDueTo=checkDueToOld;
if(!isCheck) return false;
}
}
if(possibleRightRookPosition!=null)
{
for(j=0;j<possibleRightRookPosition.length;j++)
{
if(possibleRightRookPosition[j][0]==-1) break;
byte value=chessBoardModel.board[rightRookPosition[0]][rightRookPosition[1]];
chessBoardModel.board[rightRookPosition[0]][rightRookPosition[1]]=0;
byte secondButtonValue=chessBoardModel.board[possibleRightRookPosition[j][0]][possibleRightRookPosition[j][1]];
checkDueToOld=checkDueTo;
if(whiteTurn)
{
chessBoardModel.board[possibleRightRookPosition[j][0]][possibleRightRookPosition[j][1]]=8;
isCheck=isCheckConditionDueToSameColouredPiece((byte)8,whiteTurn);
}
else
{
chessBoardModel.board[possibleRightRookPosition[j][0]][possibleRightRookPosition[j][1]]=32;
isCheck=isCheckConditionDueToSameColouredPiece((byte)32,whiteTurn);
}
chessBoardModel.board[rightRookPosition[0]][rightRookPosition[1]]=value;
chessBoardModel.board[possibleRightRookPosition[j][0]][possibleRightRookPosition[j][1]]=secondButtonValue;
checkDueTo=checkDueToOld;
if(!isCheck) return false;
}
}
if(possiblePawnOnePosition!=null)
{
for(j=0;j<possiblePawnOnePosition.length;j++)
{
if(possiblePawnOnePosition[j][0]==-1) break;
byte value=chessBoardModel.board[pawnOnePosition[0]][pawnOnePosition[1]];
chessBoardModel.board[pawnOnePosition[0]][pawnOnePosition[1]]=0;
byte secondButtonValue=chessBoardModel.board[possiblePawnOnePosition[j][0]][possiblePawnOnePosition[j][1]];
checkDueToOld=checkDueTo;
if(whiteTurn)
{
chessBoardModel.board[possiblePawnOnePosition[j][0]][possiblePawnOnePosition[j][1]]=9;
isCheck=isCheckConditionDueToSameColouredPiece((byte)9,whiteTurn);
}
else
{
chessBoardModel.board[possiblePawnOnePosition[j][0]][possiblePawnOnePosition[j][1]]=17;
isCheck=isCheckConditionDueToSameColouredPiece((byte)17,whiteTurn);
}
chessBoardModel.board[pawnOnePosition[0]][pawnOnePosition[1]]=value;
chessBoardModel.board[possiblePawnOnePosition[j][0]][possiblePawnOnePosition[j][1]]=secondButtonValue;
checkDueTo=checkDueToOld;
if(!isCheck) return false;
}
}
if(possiblePawnTwoPosition!=null)
{
for(j=0;j<possiblePawnTwoPosition.length;j++)
{
if(possiblePawnTwoPosition[j][0]==-1) break;
byte value=chessBoardModel.board[pawnTwoPosition[0]][pawnTwoPosition[1]];
chessBoardModel.board[pawnTwoPosition[0]][pawnTwoPosition[1]]=0;
byte secondButtonValue=chessBoardModel.board[possiblePawnTwoPosition[j][0]][possiblePawnTwoPosition[j][1]];
checkDueToOld=checkDueTo;
if(whiteTurn)
{
chessBoardModel.board[possiblePawnTwoPosition[j][0]][possiblePawnTwoPosition[j][1]]=10;
isCheck=isCheckConditionDueToSameColouredPiece((byte)10,whiteTurn);
}
else
{
chessBoardModel.board[possiblePawnTwoPosition[j][0]][possiblePawnTwoPosition[j][1]]=18;
isCheck=isCheckConditionDueToSameColouredPiece((byte)18,whiteTurn);
}
chessBoardModel.board[pawnTwoPosition[0]][pawnTwoPosition[1]]=value;
chessBoardModel.board[possiblePawnTwoPosition[j][0]][possiblePawnTwoPosition[j][1]]=secondButtonValue;
checkDueTo=checkDueToOld;
if(!isCheck) return false;
}
}
if(possiblePawnThreePosition!=null)
{
for(j=0;j<possiblePawnThreePosition.length;j++)
{
if(possiblePawnThreePosition[j][0]==-1) break;
byte value=chessBoardModel.board[pawnThreePosition[0]][pawnThreePosition[1]];
chessBoardModel.board[pawnThreePosition[0]][pawnThreePosition[1]]=0;
byte secondButtonValue=chessBoardModel.board[possiblePawnThreePosition[j][0]][possiblePawnThreePosition[j][1]];
checkDueToOld=checkDueTo;
if(whiteTurn)
{
chessBoardModel.board[possiblePawnThreePosition[j][0]][possiblePawnThreePosition[j][1]]=11;
isCheck=isCheckConditionDueToSameColouredPiece((byte)11,whiteTurn);
}
else
{
chessBoardModel.board[possiblePawnThreePosition[j][0]][possiblePawnThreePosition[j][1]]=19;
isCheck=isCheckConditionDueToSameColouredPiece((byte)19,whiteTurn);
}
chessBoardModel.board[pawnThreePosition[0]][pawnThreePosition[1]]=value;
chessBoardModel.board[possiblePawnThreePosition[j][0]][possiblePawnThreePosition[j][1]]=secondButtonValue;
checkDueTo=checkDueToOld;
if(!isCheck) return false;
}
}
if(possiblePawnFourPosition!=null)
{
for(j=0;j<possiblePawnFourPosition.length;j++)
{
if(possiblePawnFourPosition[j][0]==-1) break;
byte value=chessBoardModel.board[pawnFourPosition[0]][pawnFourPosition[1]];
chessBoardModel.board[pawnFourPosition[0]][pawnFourPosition[1]]=0;
byte secondButtonValue=chessBoardModel.board[possiblePawnFourPosition[j][0]][possiblePawnFourPosition[j][1]];
checkDueToOld=checkDueTo;
if(whiteTurn)
{
chessBoardModel.board[possiblePawnFourPosition[j][0]][possiblePawnFourPosition[j][1]]=12;
isCheck=isCheckConditionDueToSameColouredPiece((byte)12,whiteTurn);
}
else
{
chessBoardModel.board[possiblePawnFourPosition[j][0]][possiblePawnFourPosition[j][1]]=20;
isCheck=isCheckConditionDueToSameColouredPiece((byte)20,whiteTurn);
}
chessBoardModel.board[pawnFourPosition[0]][pawnFourPosition[1]]=value;
chessBoardModel.board[possiblePawnFourPosition[j][0]][possiblePawnFourPosition[j][1]]=secondButtonValue;
checkDueTo=checkDueToOld;
if(!isCheck) return false;
}
}
if(possiblePawnFivePosition!=null)
{
for(j=0;j<possiblePawnFivePosition.length;j++)
{
if(possiblePawnFivePosition[j][0]==-1) break;
byte value=chessBoardModel.board[pawnFivePosition[0]][pawnFivePosition[1]];
chessBoardModel.board[pawnFivePosition[0]][pawnFivePosition[1]]=0;
byte secondButtonValue=chessBoardModel.board[possiblePawnFivePosition[j][0]][possiblePawnFivePosition[j][1]];
checkDueToOld=checkDueTo;
if(whiteTurn)
{
chessBoardModel.board[possiblePawnFivePosition[j][0]][possiblePawnFivePosition[j][1]]=13;
isCheck=isCheckConditionDueToSameColouredPiece((byte)13,whiteTurn);
}
else
{
chessBoardModel.board[possiblePawnFivePosition[j][0]][possiblePawnFivePosition[j][1]]=21;
isCheck=isCheckConditionDueToSameColouredPiece((byte)21,whiteTurn);
}
chessBoardModel.board[pawnFivePosition[0]][pawnFivePosition[1]]=value;
chessBoardModel.board[possiblePawnFivePosition[j][0]][possiblePawnFivePosition[j][1]]=secondButtonValue;
checkDueTo=checkDueToOld;
if(!isCheck) return false;
}
}
if(possiblePawnSixPosition!=null)
{
for(j=0;j<possiblePawnSixPosition.length;j++)
{
if(possiblePawnSixPosition[j][0]==-1) break;
byte value=chessBoardModel.board[pawnSixPosition[0]][pawnSixPosition[1]];
chessBoardModel.board[pawnSixPosition[0]][pawnSixPosition[1]]=0;
byte secondButtonValue=chessBoardModel.board[possiblePawnSixPosition[j][0]][possiblePawnSixPosition[j][1]];
checkDueToOld=checkDueTo;
if(whiteTurn)
{
chessBoardModel.board[possiblePawnSixPosition[j][0]][possiblePawnSixPosition[j][1]]=14;
isCheck=isCheckConditionDueToSameColouredPiece((byte)14,whiteTurn);
}
else
{
chessBoardModel.board[possiblePawnSixPosition[j][0]][possiblePawnSixPosition[j][1]]=22;
isCheck=isCheckConditionDueToSameColouredPiece((byte)22,whiteTurn);
}
chessBoardModel.board[pawnSixPosition[0]][pawnSixPosition[1]]=value;
chessBoardModel.board[possiblePawnSixPosition[j][0]][possiblePawnSixPosition[j][1]]=secondButtonValue;
checkDueTo=checkDueToOld;
if(!isCheck) return false;
}
}
if(possiblePawnSevenPosition!=null)
{
for(j=0;j<possiblePawnSevenPosition.length;j++)
{
if(possiblePawnSevenPosition[j][0]==-1) break;
byte value=chessBoardModel.board[pawnSevenPosition[0]][pawnSevenPosition[1]];
chessBoardModel.board[pawnSevenPosition[0]][pawnSevenPosition[1]]=0;
byte secondButtonValue=chessBoardModel.board[possiblePawnSevenPosition[j][0]][possiblePawnSevenPosition[j][1]];
checkDueToOld=checkDueTo;
if(whiteTurn)
{
chessBoardModel.board[possiblePawnSevenPosition[j][0]][possiblePawnSevenPosition[j][1]]=15;
isCheck=isCheckConditionDueToSameColouredPiece((byte)15,whiteTurn);
}
else
{
chessBoardModel.board[possiblePawnSevenPosition[j][0]][possiblePawnSevenPosition[j][1]]=23;
isCheck=isCheckConditionDueToSameColouredPiece((byte)23,whiteTurn);
}
chessBoardModel.board[pawnSevenPosition[0]][pawnSevenPosition[1]]=value;
chessBoardModel.board[possiblePawnSevenPosition[j][0]][possiblePawnSevenPosition[j][1]]=secondButtonValue;
checkDueTo=checkDueToOld;
if(!isCheck) return false;
}
}
if(possiblePawnEightPosition!=null)
{
for(j=0;j<possiblePawnEightPosition.length;j++)
{
if(possiblePawnEightPosition[j][0]==-1) break;
byte value=chessBoardModel.board[pawnEightPosition[0]][pawnEightPosition[1]];
chessBoardModel.board[pawnEightPosition[0]][pawnEightPosition[1]]=0;
byte secondButtonValue=chessBoardModel.board[possiblePawnEightPosition[j][0]][possiblePawnEightPosition[j][1]];
checkDueToOld=checkDueTo;
if(whiteTurn)
{
chessBoardModel.board[possiblePawnEightPosition[j][0]][possiblePawnEightPosition[j][1]]=16;
isCheck=isCheckConditionDueToSameColouredPiece((byte)16,whiteTurn);
}
else
{
chessBoardModel.board[possiblePawnEightPosition[j][0]][possiblePawnEightPosition[j][1]]=24;
isCheck=isCheckConditionDueToSameColouredPiece((byte)24,whiteTurn);
}
chessBoardModel.board[pawnEightPosition[0]][pawnEightPosition[1]]=value;
chessBoardModel.board[possiblePawnEightPosition[j][0]][possiblePawnEightPosition[j][1]]=secondButtonValue;
checkDueTo=checkDueToOld;
if(!isCheck) return false;
}
}
return true;
}
if(checkDueTo==1 || checkDueTo==25)
{
checkPositions=new int[8][2];
for(i=0;i<checkPositions.length;i++)
{
checkPositions[i][0]=-1;
checkPositions[i][1]=-1;
}
if(kingPosition[0]==checkDueToPosition[0])
{
if(kingPosition[1]>checkDueToPosition[1])
{
for(i=checkDueToPosition[1],j=0;i<kingPosition[1];i++)
{
checkPositions[j][0]=checkDueToPosition[0];
checkPositions[j++][1]=i;
}
}
else
{
for(i=checkDueToPosition[1],j=0;i>kingPosition[1];i--)
{
checkPositions[j][0]=checkDueToPosition[0];
checkPositions[j++][1]=i;
}
}
}
else
{
if(kingPosition[0]>checkDueToPosition[0])
{
for(i=checkDueToPosition[0],j=0;i<kingPosition[0];i++)
{
checkPositions[j][0]=i;
checkPositions[j++][1]=checkDueToPosition[1];
}
}
else
{
for(i=checkDueToPosition[0],j=0;i>kingPosition[0];i--)
{
checkPositions[j][0]=i;
checkPositions[j++][1]=checkDueToPosition[1];
}
}
}

}
else if(checkDueTo==8 || checkDueTo==32)
{
checkPositions=new int[8][2];
for(i=0;i<checkPositions.length;i++)
{
checkPositions[i][0]=-1;
checkPositions[i][1]=-1;
}
if(kingPosition[0]==checkDueToPosition[0])
{
if(kingPosition[1]>checkDueToPosition[1])
{
for(i=checkDueToPosition[1],j=0;i<kingPosition[1];i++)
{
checkPositions[j][0]=checkDueToPosition[0];
checkPositions[j++][1]=i;
}
}
else
{
for(i=checkDueToPosition[1],j=0;i>kingPosition[1];i--)
{
checkPositions[j][0]=checkDueToPosition[0];
checkPositions[j++][1]=i;
}
}
}
else
{
if(kingPosition[0]>checkDueToPosition[0])
{
for(i=checkDueToPosition[0],j=0;i<kingPosition[0];i++)
{
checkPositions[j][0]=i;
checkPositions[j++][1]=rightRookPosition[1];
}
}
else
{
for(i=checkDueToPosition[0],j=0;i>kingPosition[0];i--)
{
checkPositions[j][0]=i;
checkPositions[j++][1]=checkDueToPosition[1];
}
}
}

}
else if(checkDueTo==3 || checkDueTo==27)
{
checkPositions=new int[8][2];
for(i=0;i<checkPositions.length;i++)
{
checkPositions[i][0]=-1;
checkPositions[i][1]=-1;
}
if(kingPosition[0]<checkDueToPosition[0])
{
if(kingPosition[1]<checkDueToPosition[1])
{
for(i=kingPosition[0]+1,j=kingPosition[1]+1,k=0;i<=checkDueToPosition[0];i++,j++)
{
checkPositions[k][0]=i;
checkPositions[k++][1]=j;
}
}
else
{
for(i=kingPosition[0]+1,j=kingPosition[1]-1,k=0;i<=checkDueToPosition[0];i++,j--)
{
checkPositions[k][0]=i;
checkPositions[k++][1]=j;
}
}
}
else
{
if(kingPosition[1]<checkDueToPosition[1])
{
for(i=kingPosition[0]-1,j=kingPosition[1]+1,k=0;i>=checkDueToPosition[0];i--,j++)
{
checkPositions[k][0]=i;
checkPositions[k++][1]=j;
}
}
else
{
for(i=kingPosition[0]-1,j=kingPosition[1]-1,k=0;i>=checkDueToPosition[0];i--,j--)
{
checkPositions[k][0]=i;
checkPositions[k++][1]=j;
}
}
}
}
else if(checkDueTo==6 || checkDueTo==30)
{
checkPositions=new int[8][2];
for(i=0;i<checkPositions.length;i++)
{
checkPositions[i][0]=-1;
checkPositions[i][1]=-1;
}
if(kingPosition[0]<checkDueToPosition[0])
{
if(kingPosition[1]<checkDueToPosition[1])
{
for(i=kingPosition[0]+1,j=kingPosition[1]+1,k=0;i<=checkDueToPosition[0];i++,j++)
{
checkPositions[k][0]=i;
checkPositions[k++][1]=j;
}
}
else
{
for(i=kingPosition[0]+1,j=kingPosition[1]-1,k=0;i<=checkDueToPosition[0];i++,j--)
{
checkPositions[k][0]=i;
checkPositions[k++][1]=j;
}
}
}
else
{
if(kingPosition[1]<checkDueToPosition[1])
{
for(i=kingPosition[0]-1,j=kingPosition[1]+1,k=0;i>=checkDueToPosition[0];i--,j++)
{
checkPositions[k][0]=i;
checkPositions[k++][1]=j;
}
}
else
{
for(i=kingPosition[0]-1,j=kingPosition[1]-1,k=0;i>=checkDueToPosition[0];i--,j--)
{
checkPositions[k][0]=i;
checkPositions[k++][1]=j;
}
}
}
}
else if(checkDueTo==2 || checkDueTo==26)
{
checkPositions=new int[1][2];
checkPositions[0][0]=checkDueToPosition[0];
checkPositions[0][1]=checkDueToPosition[1];
}
else if(checkDueTo==7 || checkDueTo==31)
{
checkPositions=new int[1][2];
checkPositions[0][0]=checkDueToPosition[0];
checkPositions[0][1]=checkDueToPosition[1];
}
else if(checkDueTo==4 || checkDueTo==28)
{
checkPositions=new int[8][2];
for(i=0;i<checkPositions.length;i++)
{
checkPositions[i][0]=-1;
checkPositions[i][1]=-1;
}
if(kingPosition[0]==checkDueToPosition[0])
{
if(kingPosition[1]>checkDueToPosition[1])
{
for(i=checkDueToPosition[1],j=0;i<kingPosition[1];i++)
{
checkPositions[j][0]=checkDueToPosition[0];
checkPositions[j++][1]=i;
}
}
else
{
for(i=checkDueToPosition[1],j=0;i>kingPosition[1];i--)
{
checkPositions[j][0]=checkDueToPosition[0];
checkPositions[j++][1]=i;
}
}
}
else if(kingPosition[1]==checkDueToPosition[1])
{
if(kingPosition[0]>checkDueToPosition[0])
{
for(i=checkDueToPosition[0],j=0;i<kingPosition[0];i++)
{
checkPositions[j][0]=i;
checkPositions[j++][1]=checkDueToPosition[1];
}
}
else
{
for(i=checkDueToPosition[0],j=0;i>kingPosition[0];i--)
{
checkPositions[j][0]=i;
checkPositions[j++][1]=checkDueToPosition[1];
}
}
}
else if(kingPosition[0]<checkDueToPosition[0])
{
if(kingPosition[1]<checkDueToPosition[1])
{
for(i=kingPosition[0]+1,j=kingPosition[1]+1,k=0;i<=checkDueToPosition[0];i++,j++)
{
checkPositions[k][0]=i;
checkPositions[k++][1]=j;
}
}
else
{
for(i=kingPosition[0]+1,j=kingPosition[1]-1,k=0;i<=checkDueToPosition[0];i++,j--)
{
checkPositions[k][0]=i;
checkPositions[k++][1]=j;
}
}
}
else
{
if(kingPosition[1]<checkDueToPosition[1])
{
for(i=kingPosition[0]-1,j=kingPosition[1]+1,k=0;i>=checkDueToPosition[0];i--,j++)
{
checkPositions[k][0]=i;
checkPositions[k++][1]=j;
}
}
else
{
for(i=kingPosition[0]-1,j=kingPosition[1]-1,k=0;i>=checkDueToPosition[0];i--,j--)
{
checkPositions[k][0]=i;
checkPositions[k++][1]=j;
}
}
}
}
else if(checkDueTo>=9 && checkDueTo<=16 || checkDueTo>=17 && checkDueTo<=24)
{
checkPositions=new int[1][2];
if(checkDueTo==9 || checkDueTo==17)
{
checkPositions[0][0]=checkDueToPosition[0];
checkPositions[0][1]=checkDueToPosition[1];
}
else if(checkDueTo==10 || checkDueTo==18)
{
checkPositions[0][0]=checkDueToPosition[0];
checkPositions[0][1]=checkDueToPosition[1];
}
else if(checkDueTo==11 || checkDueTo==19)
{
checkPositions[0][0]=checkDueToPosition[0];
checkPositions[0][1]=checkDueToPosition[1];
}
else if(checkDueTo==12 || checkDueTo==20)
{
checkPositions[0][0]=checkDueToPosition[0];
checkPositions[0][1]=checkDueToPosition[1];
}
else if(checkDueTo==13 || checkDueTo==21)
{
checkPositions[0][0]=checkDueToPosition[0];
checkPositions[0][1]=checkDueToPosition[1];
}
else if(checkDueTo==14 || checkDueTo==22)
{
checkPositions[0][0]=checkDueToPosition[0];
checkPositions[0][1]=checkDueToPosition[1];
}
else if(checkDueTo==15 || checkDueTo==23)
{
checkPositions[0][0]=checkDueToPosition[0];
checkPositions[0][1]=checkDueToPosition[1];
}
else if(checkDueTo==16 || checkDueTo==24)
{
checkPositions[0][0]=checkDueToPosition[0];
checkPositions[0][1]=checkDueToPosition[1];
}
}
//for(i=0;i<checkPositions.length;i++) System.out.println(checkPositions[i][0]+" "+checkPositions[i][1]);
for(i=0;i<checkPositions.length;i++)
{
if(possibleQueenPosition!=null)
{
for(j=0;j<possibleQueenPosition.length;j++)
{
if(possibleQueenPosition[j][0]==-1) break;
if(checkPositions[i][0]==possibleQueenPosition[j][0] && checkPositions[i][1]==possibleQueenPosition[j][1])
{
byte value=chessBoardModel.board[queenPosition[0]][queenPosition[1]];
chessBoardModel.board[queenPosition[0]][queenPosition[1]]=0;
byte secondButtonValue=chessBoardModel.board[possibleQueenPosition[j][0]][possibleQueenPosition[j][1]];
checkDueToOld=checkDueTo;
if(whiteTurn)
{
chessBoardModel.board[possibleQueenPosition[j][0]][possibleQueenPosition[j][1]]=4;
isCheck=isCheckConditionDueToSameColouredPiece((byte)4,whiteTurn);
}
else
{
chessBoardModel.board[possibleQueenPosition[j][0]][possibleQueenPosition[j][1]]=28;
isCheck=isCheckConditionDueToSameColouredPiece((byte)28,whiteTurn);
}
chessBoardModel.board[queenPosition[0]][queenPosition[1]]=value;
chessBoardModel.board[possibleQueenPosition[j][0]][possibleQueenPosition[j][1]]=secondButtonValue;
checkDueTo=checkDueToOld;
if(!isCheck) return false;
}
}
}
if(possibleLeftKnightPosition!=null)
{
for(j=0;j<possibleLeftKnightPosition.length;j++)
{
if(possibleLeftKnightPosition[j][0]==-1) break;
if(checkPositions[i][0]==possibleLeftKnightPosition[j][0] && checkPositions[i][1]==possibleRightKnightPosition[j][1])
{
byte value=chessBoardModel.board[leftKnightPosition[0]][leftKnightPosition[1]];
chessBoardModel.board[leftKnightPosition[0]][leftKnightPosition[1]]=0;
byte secondButtonValue=chessBoardModel.board[possibleLeftKnightPosition[j][0]][possibleLeftKnightPosition[j][1]];
checkDueToOld=checkDueTo;
if(whiteTurn)
{
chessBoardModel.board[possibleLeftKnightPosition[j][0]][possibleLeftKnightPosition[j][1]]=2;
isCheck=isCheckConditionDueToSameColouredPiece((byte)2,whiteTurn);
}
else
{
chessBoardModel.board[possibleLeftKnightPosition[j][0]][possibleLeftKnightPosition[j][1]]=26;
isCheck=isCheckConditionDueToSameColouredPiece((byte)26,whiteTurn);
}
chessBoardModel.board[leftKnightPosition[0]][leftKnightPosition[1]]=value;
chessBoardModel.board[possibleLeftKnightPosition[j][0]][possibleLeftKnightPosition[j][1]]=secondButtonValue;
checkDueTo=checkDueToOld;
if(!isCheck) return false;
}
}
}
if(possibleRightKnightPosition!=null)
{
for(j=0;j<possibleRightKnightPosition.length;j++)
{
if(possibleRightKnightPosition[j][0]==-1) break;
if(checkPositions[i][0]==possibleRightKnightPosition[j][0] && checkPositions[i][1]==possibleRightKnightPosition[j][1])
{
byte value=chessBoardModel.board[rightKnightPosition[0]][rightKnightPosition[1]];
chessBoardModel.board[rightKnightPosition[0]][rightKnightPosition[1]]=0;
byte secondButtonValue=chessBoardModel.board[possibleRightKnightPosition[j][0]][possibleRightKnightPosition[j][1]];
checkDueToOld=checkDueTo;
if(whiteTurn)
{
chessBoardModel.board[possibleRightKnightPosition[j][0]][possibleRightKnightPosition[j][1]]=7;
isCheck=isCheckConditionDueToSameColouredPiece((byte)7,whiteTurn);
}
else
{
chessBoardModel.board[possibleRightKnightPosition[j][0]][possibleRightKnightPosition[j][1]]=31;
isCheck=isCheckConditionDueToSameColouredPiece((byte)31,whiteTurn);
}
chessBoardModel.board[leftKnightPosition[0]][leftKnightPosition[1]]=value;
chessBoardModel.board[possibleLeftKnightPosition[j][0]][possibleLeftKnightPosition[j][1]]=secondButtonValue;
checkDueTo=checkDueToOld;
if(!isCheck) return false;
}
}
}
if(possibleLeftBishopPosition!=null)
{
for(j=0;j<possibleLeftBishopPosition.length;j++)
{
if(possibleLeftBishopPosition[j][0]==-1) break;
if(checkPositions[i][0]==possibleLeftBishopPosition[j][0] && checkPositions[i][1]==possibleLeftBishopPosition[j][1])
{
byte value=chessBoardModel.board[leftBishopPosition[0]][leftBishopPosition[1]];
chessBoardModel.board[leftBishopPosition[0]][leftBishopPosition[1]]=0;
byte secondButtonValue=chessBoardModel.board[possibleLeftBishopPosition[j][0]][possibleLeftBishopPosition[j][1]];
checkDueToOld=checkDueTo;
if(whiteTurn)
{
chessBoardModel.board[possibleLeftBishopPosition[j][0]][possibleLeftBishopPosition[j][1]]=3;
isCheck=isCheckConditionDueToSameColouredPiece((byte)3,whiteTurn);
}
else
{
chessBoardModel.board[possibleLeftBishopPosition[j][0]][possibleLeftBishopPosition[j][1]]=27;
isCheck=isCheckConditionDueToSameColouredPiece((byte)27,whiteTurn);
}
chessBoardModel.board[leftBishopPosition[0]][leftBishopPosition[1]]=value;
chessBoardModel.board[possibleLeftBishopPosition[j][0]][possibleLeftBishopPosition[j][1]]=secondButtonValue;
checkDueTo=checkDueToOld;
if(!isCheck) return false;
}
}
}
if(possibleRightBishopPosition!=null)
{
for(j=0;j<possibleRightBishopPosition.length;j++)
{
if(possibleRightBishopPosition[j][0]==-1) break;
if(checkPositions[i][0]==possibleRightBishopPosition[j][0] && checkPositions[i][1]==possibleRightBishopPosition[j][1])
{
if(possibleRightBishopPosition[j][0]==-1) break;
byte value=chessBoardModel.board[rightBishopPosition[0]][rightBishopPosition[1]];
chessBoardModel.board[rightBishopPosition[0]][rightBishopPosition[1]]=0;
byte secondButtonValue=chessBoardModel.board[possibleRightBishopPosition[j][0]][possibleRightBishopPosition[j][1]];
checkDueToOld=checkDueTo;
if(whiteTurn)
{
chessBoardModel.board[possibleRightBishopPosition[j][0]][possibleRightBishopPosition[j][1]]=6;
isCheck=isCheckConditionDueToSameColouredPiece((byte)6,whiteTurn);
}
else
{
chessBoardModel.board[possibleRightBishopPosition[j][0]][possibleRightBishopPosition[j][1]]=30;
isCheck=isCheckConditionDueToSameColouredPiece((byte)30,whiteTurn);
}
chessBoardModel.board[rightBishopPosition[0]][rightBishopPosition[1]]=value;
chessBoardModel.board[possibleRightBishopPosition[j][0]][possibleRightBishopPosition[j][1]]=secondButtonValue;
checkDueTo=checkDueToOld;
if(!isCheck) return false;
}
}
}
if(possibleLeftRookPosition!=null)
{
for(j=0;j<possibleLeftRookPosition.length;j++)
{
if(possibleLeftRookPosition[j][0]==-1) break;
if(checkPositions[i][0]==possibleLeftRookPosition[j][0] && checkPositions[i][1]==possibleLeftRookPosition[j][1])
{
byte value=chessBoardModel.board[leftRookPosition[0]][leftRookPosition[1]];
chessBoardModel.board[leftRookPosition[0]][leftRookPosition[1]]=0;
byte secondButtonValue=chessBoardModel.board[possibleLeftRookPosition[j][0]][possibleLeftRookPosition[j][1]];
checkDueToOld=checkDueTo;
if(whiteTurn)
{
chessBoardModel.board[possibleLeftRookPosition[j][0]][possibleLeftRookPosition[j][1]]=1;
isCheck=isCheckConditionDueToSameColouredPiece((byte)1,whiteTurn);
}
else
{
chessBoardModel.board[possibleLeftRookPosition[j][0]][possibleLeftRookPosition[j][1]]=25;
isCheck=isCheckConditionDueToSameColouredPiece((byte)25,whiteTurn);
}
chessBoardModel.board[leftRookPosition[0]][leftRookPosition[1]]=value;
chessBoardModel.board[possibleLeftRookPosition[j][0]][possibleLeftRookPosition[j][1]]=secondButtonValue;
checkDueTo=checkDueToOld;
if(!isCheck) return false;
}
}
}
if(possibleRightRookPosition!=null)
{
for(j=0;j<possibleRightRookPosition.length;j++)
{
if(possibleRightRookPosition[j][0]==-1) break;
if(checkPositions[i][0]==possibleRightRookPosition[j][0] && checkPositions[i][1]==possibleRightRookPosition[j][1])
{
byte value=chessBoardModel.board[rightRookPosition[0]][rightRookPosition[1]];
chessBoardModel.board[rightRookPosition[0]][rightRookPosition[1]]=0;
byte secondButtonValue=chessBoardModel.board[possibleRightRookPosition[j][0]][possibleRightRookPosition[j][1]];
checkDueToOld=checkDueTo;
if(whiteTurn)
{
chessBoardModel.board[possibleRightRookPosition[j][0]][possibleRightRookPosition[j][1]]=8;
isCheck=isCheckConditionDueToSameColouredPiece((byte)8,whiteTurn);
}
else
{
chessBoardModel.board[possibleRightRookPosition[j][0]][possibleRightRookPosition[j][1]]=32;
isCheck=isCheckConditionDueToSameColouredPiece((byte)32,whiteTurn);
}
chessBoardModel.board[rightRookPosition[0]][rightRookPosition[1]]=value;
chessBoardModel.board[possibleRightRookPosition[j][0]][possibleRightRookPosition[j][1]]=secondButtonValue;
checkDueTo=checkDueToOld;
if(!isCheck) return false;
}
}
}
if(possiblePawnOnePosition!=null)
{
for(j=0;j<possiblePawnOnePosition.length;j++)
{
if(possiblePawnOnePosition[j][0]==-1) break;
if(checkPositions[i][0]==possiblePawnOnePosition[j][0] && checkPositions[i][1]==possiblePawnOnePosition[j][1])
{
byte value=chessBoardModel.board[pawnOnePosition[0]][pawnOnePosition[1]];
chessBoardModel.board[pawnOnePosition[0]][pawnOnePosition[1]]=0;
byte secondButtonValue=chessBoardModel.board[possiblePawnOnePosition[j][0]][possiblePawnOnePosition[j][1]];
checkDueToOld=checkDueTo;
if(whiteTurn)
{
chessBoardModel.board[possiblePawnOnePosition[j][0]][possiblePawnOnePosition[j][1]]=9;
isCheck=isCheckConditionDueToSameColouredPiece((byte)9,whiteTurn);
}
else
{
chessBoardModel.board[possiblePawnOnePosition[j][0]][possiblePawnOnePosition[j][1]]=17;
isCheck=isCheckConditionDueToSameColouredPiece((byte)17,whiteTurn);
}
chessBoardModel.board[pawnOnePosition[0]][pawnOnePosition[1]]=value;
chessBoardModel.board[possiblePawnOnePosition[j][0]][possiblePawnOnePosition[j][1]]=secondButtonValue;
checkDueTo=checkDueToOld;
if(!isCheck) return false;
}
}
}
if(possiblePawnTwoPosition!=null)
{
for(j=0;j<possiblePawnTwoPosition.length;j++)
{
if(possiblePawnTwoPosition[j][0]==-1) break;
if(checkPositions[i][0]==possiblePawnTwoPosition[j][0] && checkPositions[i][1]==possiblePawnTwoPosition[j][1])
{
byte value=chessBoardModel.board[pawnTwoPosition[0]][pawnTwoPosition[1]];
chessBoardModel.board[pawnTwoPosition[0]][pawnTwoPosition[1]]=0;
byte secondButtonValue=chessBoardModel.board[possiblePawnTwoPosition[j][0]][possiblePawnTwoPosition[j][1]];
checkDueToOld=checkDueTo;
if(whiteTurn)
{
chessBoardModel.board[possiblePawnTwoPosition[j][0]][possiblePawnTwoPosition[j][1]]=10;
isCheck=isCheckConditionDueToSameColouredPiece((byte)10,whiteTurn);
}
else
{
chessBoardModel.board[possiblePawnTwoPosition[j][0]][possiblePawnTwoPosition[j][1]]=18;
isCheck=isCheckConditionDueToSameColouredPiece((byte)18,whiteTurn);
}
chessBoardModel.board[pawnTwoPosition[0]][pawnTwoPosition[1]]=value;
chessBoardModel.board[possiblePawnTwoPosition[j][0]][possiblePawnTwoPosition[j][1]]=secondButtonValue;
checkDueTo=checkDueToOld;
if(!isCheck) return false;
}
}
}
if(possiblePawnThreePosition!=null)
{
for(j=0;j<possiblePawnThreePosition.length;j++)
{
if(possiblePawnThreePosition[j][0]==-1) break;
if(checkPositions[i][0]==possiblePawnThreePosition[j][0] && checkPositions[i][1]==possiblePawnThreePosition[j][1])
{
byte value=chessBoardModel.board[pawnThreePosition[0]][pawnThreePosition[1]];
chessBoardModel.board[pawnThreePosition[0]][pawnThreePosition[1]]=0;
byte secondButtonValue=chessBoardModel.board[possiblePawnThreePosition[j][0]][possiblePawnThreePosition[j][1]];
checkDueToOld=checkDueTo;
if(whiteTurn)
{
chessBoardModel.board[possiblePawnThreePosition[j][0]][possiblePawnThreePosition[j][1]]=11;
isCheck=isCheckConditionDueToSameColouredPiece((byte)11,whiteTurn);
}
else
{
chessBoardModel.board[possiblePawnThreePosition[j][0]][possiblePawnThreePosition[j][1]]=19;
isCheck=isCheckConditionDueToSameColouredPiece((byte)19,whiteTurn);
}
chessBoardModel.board[pawnThreePosition[0]][pawnThreePosition[1]]=value;
chessBoardModel.board[possiblePawnThreePosition[j][0]][possiblePawnThreePosition[j][1]]=secondButtonValue;
checkDueTo=checkDueToOld;
if(!isCheck) return false;
}
}
}
if(possiblePawnFourPosition!=null)
{
for(j=0;j<possiblePawnFourPosition.length;j++)
{
if(possiblePawnFourPosition[j][0]==-1) break;
if(checkPositions[i][0]==possiblePawnFourPosition[j][0] && checkPositions[i][1]==possiblePawnFourPosition[j][1])
{
byte value=chessBoardModel.board[pawnFourPosition[0]][pawnFourPosition[1]];
chessBoardModel.board[pawnFourPosition[0]][pawnFourPosition[1]]=0;
byte secondButtonValue=chessBoardModel.board[possiblePawnFourPosition[j][0]][possiblePawnFourPosition[j][1]];
checkDueToOld=checkDueTo;
if(whiteTurn)
{
chessBoardModel.board[possiblePawnFourPosition[j][0]][possiblePawnFourPosition[j][1]]=12;
isCheck=isCheckConditionDueToSameColouredPiece((byte)12,whiteTurn);
}
else
{
chessBoardModel.board[possiblePawnFourPosition[j][0]][possiblePawnFourPosition[j][1]]=20;
isCheck=isCheckConditionDueToSameColouredPiece((byte)20,whiteTurn);
}
chessBoardModel.board[pawnFourPosition[0]][pawnFourPosition[1]]=value;
chessBoardModel.board[possiblePawnFourPosition[j][0]][possiblePawnFourPosition[j][1]]=secondButtonValue;
checkDueTo=checkDueToOld;
if(!isCheck) return false;
}
}
}
if(possiblePawnFivePosition!=null)
{
for(j=0;j<possiblePawnFivePosition.length;j++)
{
if(possiblePawnFivePosition[j][0]==-1) break;
if(checkPositions[i][0]==possiblePawnFivePosition[j][0] && checkPositions[i][1]==possiblePawnFivePosition[j][1])
{
byte value=chessBoardModel.board[pawnFivePosition[0]][pawnFivePosition[1]];
chessBoardModel.board[pawnFivePosition[0]][pawnFivePosition[1]]=0;
byte secondButtonValue=chessBoardModel.board[possiblePawnFivePosition[j][0]][possiblePawnFivePosition[j][1]];
checkDueToOld=checkDueTo;
if(whiteTurn)
{
chessBoardModel.board[possiblePawnFivePosition[j][0]][possiblePawnFivePosition[j][1]]=13;
isCheck=isCheckConditionDueToSameColouredPiece((byte)13,whiteTurn);
}
else
{
chessBoardModel.board[possiblePawnFivePosition[j][0]][possiblePawnFivePosition[j][1]]=21;
isCheck=isCheckConditionDueToSameColouredPiece((byte)21,whiteTurn);
}
chessBoardModel.board[pawnFivePosition[0]][pawnFivePosition[1]]=value;
chessBoardModel.board[possiblePawnFivePosition[j][0]][possiblePawnFivePosition[j][1]]=secondButtonValue;
checkDueTo=checkDueToOld;
if(!isCheck) return false;
}
}
}
if(possiblePawnSixPosition!=null)
{
for(j=0;j<possiblePawnSixPosition.length;j++)
{
if(possiblePawnSixPosition[j][0]==-1) break;
if(checkPositions[i][0]==possiblePawnSixPosition[j][0] && checkPositions[i][1]==possiblePawnSixPosition[j][1])
{
byte value=chessBoardModel.board[pawnSixPosition[0]][pawnSixPosition[1]];
chessBoardModel.board[pawnSixPosition[0]][pawnSixPosition[1]]=0;
byte secondButtonValue=chessBoardModel.board[possiblePawnSixPosition[j][0]][possiblePawnSixPosition[j][1]];
checkDueToOld=checkDueTo;
if(whiteTurn)
{
chessBoardModel.board[possiblePawnSixPosition[j][0]][possiblePawnSixPosition[j][1]]=14;
isCheck=isCheckConditionDueToSameColouredPiece((byte)14,whiteTurn);
}
else
{
chessBoardModel.board[possiblePawnSixPosition[j][0]][possiblePawnSixPosition[j][1]]=22;
isCheck=isCheckConditionDueToSameColouredPiece((byte)22,whiteTurn);
}
chessBoardModel.board[pawnSixPosition[0]][pawnSixPosition[1]]=value;
chessBoardModel.board[possiblePawnSixPosition[j][0]][possiblePawnSixPosition[j][1]]=secondButtonValue;
checkDueTo=checkDueToOld;
if(!isCheck) return false;
}
}
}
if(possiblePawnSevenPosition!=null)
{
for(j=0;j<possiblePawnSevenPosition.length;j++)
{
if(possiblePawnSevenPosition[j][0]==-1) break;
if(checkPositions[i][0]==possiblePawnSevenPosition[j][0] && checkPositions[i][1]==possiblePawnSevenPosition[j][1])
{
//System.out.println("Chal sakte hain");
byte value=chessBoardModel.board[pawnSevenPosition[0]][pawnSevenPosition[1]];
chessBoardModel.board[pawnSevenPosition[0]][pawnSevenPosition[1]]=0;
byte secondButtonValue=chessBoardModel.board[possiblePawnSevenPosition[j][0]][possiblePawnSevenPosition[j][1]];
checkDueToOld=checkDueTo;
if(whiteTurn)
{
chessBoardModel.board[possiblePawnSevenPosition[j][0]][possiblePawnSevenPosition[j][1]]=15;
isCheck=isCheckConditionDueToSameColouredPiece((byte)15,whiteTurn);
}
else
{
chessBoardModel.board[possiblePawnSevenPosition[j][0]][possiblePawnSevenPosition[j][1]]=23;
isCheck=isCheckConditionDueToSameColouredPiece((byte)23,whiteTurn);
}
chessBoardModel.board[pawnSevenPosition[0]][pawnSevenPosition[1]]=value;
chessBoardModel.board[possiblePawnSevenPosition[j][0]][possiblePawnSevenPosition[j][1]]=secondButtonValue;
checkDueTo=checkDueToOld;
if(!isCheck) return false;
}
}
}
if(possiblePawnEightPosition!=null)
{
for(j=0;j<possiblePawnEightPosition.length;j++)
{
if(possiblePawnEightPosition[j][0]==-1) break;
if(checkPositions[i][0]==possiblePawnEightPosition[j][0] && checkPositions[i][1]==possiblePawnEightPosition[j][1])
{
byte value=chessBoardModel.board[pawnEightPosition[0]][pawnEightPosition[1]];
chessBoardModel.board[pawnEightPosition[0]][pawnEightPosition[1]]=0;
byte secondButtonValue=chessBoardModel.board[possiblePawnEightPosition[j][0]][possiblePawnEightPosition[j][1]];
checkDueToOld=checkDueTo;
if(whiteTurn)
{
chessBoardModel.board[possiblePawnEightPosition[j][0]][possiblePawnEightPosition[j][1]]=16;
isCheck=isCheckConditionDueToSameColouredPiece((byte)16,whiteTurn);
}
else
{
chessBoardModel.board[possiblePawnEightPosition[j][0]][possiblePawnEightPosition[j][1]]=24;
isCheck=isCheckConditionDueToSameColouredPiece((byte)24,whiteTurn);
}
chessBoardModel.board[pawnEightPosition[0]][pawnEightPosition[1]]=value;
chessBoardModel.board[possiblePawnEightPosition[j][0]][possiblePawnEightPosition[j][1]]=secondButtonValue;
checkDueTo=checkDueToOld;
if(!isCheck) return false;
}
}
}

}
return true;
}
public boolean isCheckConditionDueToSameColouredPiece(byte piece,boolean whiteTurn)
{
int i=0;
int j=0;
byte s;
int [] p;
if(piece>=17 && piece<=32)
{
for(i=0;i<8;i++)
{
for(j=0;j<8;j++)
{
p=new int[2];
s=chessBoardModel.board[i][j];
p[0]=i;
p[1]=j;
if(s==1 || s==8)
{
//System.out.println("`````````"+s+"~~"+p[0]+","+p[1]);
if(isCheckCondition(s,p[0],p[1],p[0],p[1],true,true)) return true;
}
else if(s==4)
{
//System.out.println("`````````"+s+"~~"+p[0]+","+p[1]);
if(isCheckCondition(s,p[0],p[1],p[0],p[1],true,true)) return true;
}
else if(s==3 || s==6)
{
//System.out.println("`````````"+s+"~~"+p[0]+","+p[1]);
if(isCheckCondition(s,p[0],p[1],p[0],p[1],true,true)) return true;
}
else if(s==2 || s==7)
{
//System.out.println("`````````"+s+"~~"+p[0]+","+p[1]);
if(isCheckCondition(s,p[0],p[1],p[0],p[1],true,true)) return true;
}
else if(s==5)
{
//System.out.println("`````````"+s+"~~"+p[0]+","+p[1]);
if(isCheckCondition(s,p[0],p[1],p[0],p[1],true,true)) return true;
}
else if(s>=9 && s<=16)
{
//System.out.println("`````````"+s+"~~"+p[0]+","+p[1]);
if(isCheckCondition(s,p[0],p[1],p[0],p[1],true,true)) return true;
}
}
}
}
else
{
for(i=0;i<8;i++)
{
for(j=0;j<8;j++)
{
p=new int[2];
s=chessBoardModel.board[i][j];
p[0]=i;
p[1]=j;
if(s==25 || s==32)
{
//System.out.println("`````````"+s+"~~"+p[0]+","+p[1]);
if(isCheckCondition(s,p[0],p[1],p[0],p[1],false,true)) return true;
}
else if(s==28)
{
//System.out.println("`````````"+s+"~~"+p[0]+","+p[1]);
if(isCheckCondition(s,p[0],p[1],p[0],p[1],false,true)) return true;
}
else if(s==27 || s==30)
{
//System.out.println("`````````"+s+"~~"+p[0]+","+p[1]);
if(isCheckCondition(s,p[0],p[1],p[0],p[1],false,true)) return true;
}
else if(s==26 || s==31)
{
//System.out.println("`````````"+s+"~~"+p[0]+","+p[1]);
if(isCheckCondition(s,p[0],p[1],p[0],p[1],false,true)) return true;
}
else if(s==29)
{
//System.out.println("`````````"+s+"~~"+p[0]+","+p[1]);
if(isCheckCondition(s,p[0],p[1],p[0],p[1],false,true)) return true;
}
if(s>=17 && s<=24)
{
//System.out.println("`````````"+s+"~~"+p[0]+","+p[1]);
if(isCheckCondition(s,p[0],p[1],p[0],p[1],false,true)) return true;
}
}
}
}

return false;
}
public boolean isCheckCondition(byte piece,int firstPositionRow,int firstPositionCol,int secondPositionRow,int secondPositionCol,boolean whiteTurn,boolean calledFromInside)
{
int [] firstPosition=new int[2];
firstPosition[0]=firstPositionRow;
firstPosition[1]=firstPositionCol;
int [] secondPosition=new int[2];
secondPosition[0]=secondPositionRow;
secondPosition[1]=secondPositionCol;
boolean b=iCC(piece,firstPosition,secondPosition,whiteTurn,calledFromInside);
//System.out.println("~=~++~+~+`=+~+~Returning : "+b);
return b;
}
public boolean iCC(byte piece,int firstPosition[],int []secondPosition,boolean whiteTurn,boolean calledFromInside)
{
//System.out.println("2.*******"+piece+"~~"+firstPosition[0]+","+firstPosition[1]+" "+secondPosition[0]+","+secondPosition[1]+","+calledFromInside);
int [] kingPosition=new int[2];
byte s;
int [] p=null;
for(int i=0;i<8;i++)
{
for(int j=0;j<8;j++)
{
s=chessBoardModel.board[i][j];
if(whiteTurn && s==29)
{
kingPosition[0]=i;
kingPosition[1]=j;
break;
}
else if(!whiteTurn && s==5) 
{
kingPosition[0]=i;
kingPosition[1]=j;
break;
}
}
}
int j=0;
int i=0;
boolean isAnyThreat=false;
if(piece==3 || piece==6 || piece==27 || piece==30)
{
int [][] validPositions=bishopValidator.getValidPositions(firstPosition,secondPosition);
for(i=0;i<validPositions.length;i++)
{
//System.out.println("~~~"+validPositions[i][0]+" "+validPositions[i][1]);
if(validPositions[i][0]==kingPosition[0] && validPositions[i][1]==kingPosition[1])
{
isAnyThreat=true;
break;
}
}
if(!isAnyThreat)
{
if(!calledFromInside)
{
if(whiteTurn)
{
for(i=0;i<8;i++)
{
for(j=0;j<8;j++)
{
p=new int[2];
p[0]=i;
p[1]=j;
s=chessBoardModel.board[i][j];
if(s==1 || s==8)
{
if(iCC(s,p,p,whiteTurn,true))
{
checkDueTo=s;
return true;
}
}
else if(s==4)
{
if(iCC(s,p,p,whiteTurn,true))
{
checkDueTo=s;
return true;
}
}
}
}
}
else
{
for(i=0;i<8;i++)
{
for(j=0;j<8;j++)
{
p=new int[2];
p[0]=i;
p[1]=j;
s=chessBoardModel.board[i][j];
if(s==25 || s==32)
{
if(iCC(s,p,p,whiteTurn,true))
{
checkDueTo=s;
return true;
}
}
else if(s==28)
{
if(iCC(s,p,p,whiteTurn,true))
{
checkDueTo=s;
return true;
}
}
}
}
}
}
return false;//Abhi incomplete yaha pe dusre pieces se indirect check ho sakta hain
}
if(secondPosition[0]<kingPosition[0])
{
if(secondPosition[1]<kingPosition[1])
{
for(i=secondPosition[0]+1,j=secondPosition[1]+1;i<kingPosition[0];i++)
{
if(chessBoardModel.board[i][j]!=0) return false;
if(j<kingPosition[1]) j++;
}
}
else
{
for(i=secondPosition[0]+1,j=secondPosition[1]-1;i<kingPosition[0];i++)
{
//System.out.println(piecesMap.get(pieces[i][j]));
if(chessBoardModel.board[i][j]!=0) return false;
if(j>kingPosition[1]) j--;
}
}
}
else
{
if(secondPosition[1]<kingPosition[1])
{
for(i=secondPosition[0]-1,j=secondPosition[1]+1;i>kingPosition[0];i--)
{
if(chessBoardModel.board[i][j]!=0) return false;
if(j<kingPosition[1]) j++;
}
}
else
{
for(i=secondPosition[0]-1,j=secondPosition[1]-1;i>kingPosition[0];i--)
{
if(chessBoardModel.board[i][j]!=0) return false;
if(j>kingPosition[1]) j--;
}
}
}
checkDueTo=piece;
return true;
}
else if(piece==1 || piece==8 || piece==25 || piece==32)
{
int [][] validPositions=rookValidator.getValidPositions(firstPosition,secondPosition);
for(i=0;i<validPositions.length;i++)
{
//System.out.println("~~~"+validPositions[i][0]+" "+validPositions[i][1]);
if(validPositions[i][0]==kingPosition[0] && validPositions[i][1]==kingPosition[1])
{
isAnyThreat=true;
break;
}
}
if(!isAnyThreat)
{
if(!calledFromInside)
{
if(whiteTurn)
{
for(i=0;i<8;i++)
{
for(j=0;j<8;j++)
{
p=new int[2];
p[0]=i;
p[1]=j;
s=chessBoardModel.board[i][j];
if(s==4)
{
if(iCC(s,p,p,whiteTurn,true))
{
checkDueTo=s;
return true;
}
}
else if(s==3 || s==6)
{
if(iCC(s,p,p,whiteTurn,true))
{
checkDueTo=s;
return true;
}
}
}
}
}
else
{
for(i=0;i<8;i++)
{
for(j=0;j<8;j++)
{
p=new int[2];
p[0]=i;
p[1]=j;
s=chessBoardModel.board[i][j];
if(s==27 || s==30)
{
if(iCC(s,p,p,whiteTurn,true))
{
checkDueTo=s;
return true;
}
}
else if(s==28)
{
if(iCC(s,p,p,whiteTurn,true))
{
checkDueTo=s;
return true;
}
}
}
}
}
}

return false;//Abhi incomplete yaha pe dusre pieces se indirect check ho sakta hain
}
if(secondPosition[0]==kingPosition[0])
{
if(secondPosition[1]+1==kingPosition[1] || secondPosition[1]-1==kingPosition[1]) return true;
if(kingPosition[1]>secondPosition[1])
{
for(i=secondPosition[0],j=secondPosition[1]+1;j<kingPosition[1];j++)
{
if(chessBoardModel.board[i][j]!=0) return false;
}
}
else
{
for(i=secondPosition[0],j=secondPosition[1]-1;j>kingPosition[1];j--)
{
if(chessBoardModel.board[i][j]!=0) return false;
}
}
}
else if(secondPosition[1]==kingPosition[1])
{
if(secondPosition[0]+1==kingPosition[0] || secondPosition[0]-1==kingPosition[0]) return true;
if(kingPosition[0]>secondPosition[0])
{
for(i=secondPosition[0]+1,j=secondPosition[1];i<kingPosition[0];i++)
{
if(chessBoardModel.board[i][j]!=0) return false;
}
}
else
{
for(i=secondPosition[0]-1,j=secondPosition[1];i>kingPosition[0];i--)
{
if(chessBoardModel.board[i][j]!=0) return false;
}
}
}
}
else if(piece==2 || piece==7 || piece==26 || piece==31)
{
//System.out.println("Piece:"+piece+firstPosition[0]+","+firstPosition[1]+" "+secondPosition[0]+","+secondPosition[1]);
int [][] validPositions=knightValidator.getValidPositions(firstPosition,secondPosition);
isAnyThreat=false;
int x;
for(x=0;x<validPositions.length;x++)
{
if(validPositions[x][0]==kingPosition[0] && validPositions[x][1]==kingPosition[1])
{
isAnyThreat=true;
break;
}
}
if(!isAnyThreat)
{
if(!calledFromInside)
{
if(whiteTurn)
{
for(i=0;i<8;i++)
{
for(j=0;j<8;j++)
{
p=new int[2];
p[0]=i;
p[1]=j;
s=chessBoardModel.board[i][j];
if(s==1 || s==8)
{
if(iCC(s,p,p,whiteTurn,true))
{
checkDueTo=s;
return true;
}
}
else if(s==4)
{
if(iCC(s,p,p,whiteTurn,true))
{
checkDueTo=s;
return true;
}
}
else if(s==3 || s==6)
{
if(iCC(s,p,p,whiteTurn,true))
{
checkDueTo=s;
return true;
}
}
}
}
}
else
{
for(i=0;i<8;i++)
{
for(j=0;j<8;j++)
{
p=new int[2];
p[0]=i;
p[1]=j;
s=chessBoardModel.board[i][j];
if(s==25 || s==32)
{
if(iCC(s,p,p,whiteTurn,true))
{
checkDueTo=s;
return true;
}
}
else if(s==28)
{
if(iCC(s,p,p,whiteTurn,true))
{
checkDueTo=s;
return true;
}
}
else if(s==27 || s==30)
{
if(iCC(s,p,p,whiteTurn,true))
{
checkDueTo=s;
return true;
}
}
}
}
}
}
return false;
}
checkDueTo=piece;
return true;
}
else if(piece==4 || piece==28)
{
boolean bishopCheck=iCC((byte)3,firstPosition,secondPosition,whiteTurn,true);
boolean rookCheck=iCC((byte)1,firstPosition,secondPosition,whiteTurn,true);
if(!bishopCheck && !rookCheck) return false;
}
else if(piece==29)
{
for(i=0;i<8;i++)
{
for(j=0;j<8;j++)
{
p=new int[2];
p[0]=i;
p[1]=j;
s=chessBoardModel.board[i][j];
if(s==1 || s==8)
{
if(iCC(s,p,p,true,true))
{
checkDueTo=s;
return true;
}
}
else if(s==4)
{
if(iCC(s,p,p,true,true))
{
checkDueTo=s;
return true;
}
}
else if(s==3 || s==6)
{
if(iCC(s,p,p,true,true))
{
checkDueTo=s;
return true;
}
}
else if(s==2 || s==7)
{
if(iCC(s,p,p,true,true))
{
checkDueTo=s;
return true;
}
}
else if(s>=9 && s<=16)
{
if(iCC(s,p,p,true,true))
{
checkDueTo=s;
return true;
}
}
else if(s==5)
{
int [][] validPositions=kingValidator.getValidPositions(p,p,whiteTurn);
for(int k=0;k<validPositions.length;k++)
{
if(validPositions[k][0]==secondPosition[0] && validPositions[k][1]==secondPosition[1]) return true;
}
}
}
}
return false;
}
else if(piece==5)
{
for(i=0;i<8;i++)
{
for(j=0;j<8;j++)
{
p=new int[2];
p[0]=i;
p[1]=j;
s=chessBoardModel.board[i][j];
if(s==25 || s==32)
{
if(iCC(s,p,p,false,true))
{
checkDueTo=s;
return true;
}
}
else if(s==28)
{
if(iCC(s,p,p,false,true))
{
checkDueTo=s;
return true;
}
}
else if(s==27 || s==30)
{
if(iCC(s,p,p,false,true))
{
checkDueTo=s;
return true;
}
}
else if(s==26 || s==31)
{
if(iCC(s,p,p,false,true))
{
checkDueTo=s;
return true;
}
}
else if(s>=17 && s<=24)
{
if(iCC(s,p,p,false,true))
{
checkDueTo=s;
return true;
}
}
else if(s==29)
{
int [][] validPositions=kingValidator.getValidPositions(p,p,whiteTurn);
for(int k=0;k<validPositions.length;k++)
{
//System.out.println(validPositions[k][0]+" "+validPositions[k][1]+" "+secondPosition[0]+" "+secondPosition[1]);
if(validPositions[k][0]==secondPosition[0] && validPositions[k][1]==secondPosition[1]) return true;//done
}
}
}
}
return false;
}
else if((piece>=9 && piece<=16) || (piece>=17 && piece<=24))
{
int [][] validPositions=pawnValidator.getValidPositions(firstPosition,secondPosition,whiteTurn);
for(i=0;i<validPositions.length;i++)
{
//System.out.println("~~~"+validPositions[i][0]+" "+validPositions[i][1]);
if(validPositions[i][0]==kingPosition[0] && validPositions[i][1]==kingPosition[1])
{
//System.out.println("King in valid pos of pawn");
//System.out.println("Turn "+whiteTurn+secondPosition[0]+" "+secondPosition[1]+" "+kingPosition[0]+" "+kingPosition[1]);
if(!whiteTurn && (kingPosition[0]==secondPosition[0]-1 || kingPosition[0]==secondPosition[0]-2) && kingPosition[1]==secondPosition[1]) return false;
else if(whiteTurn && (kingPosition[0]==secondPosition[0]+1 || kingPosition[0]==secondPosition[0]+2) && kingPosition[1]==secondPosition[1]) return false;
isAnyThreat=true;
break;
}
}
if(!isAnyThreat)
{
if(whiteTurn)
{
for(i=0;i<8;i++)
{
for(j=0;j<8;j++)
{
p=new int[2];
p[0]=i;
p[1]=j;
s=chessBoardModel.board[i][j];
if(s==1 || s==8)
{
if(iCC(s,p,p,whiteTurn,true))
{
checkDueTo=s;
return true;
}
}
else if(s==4)
{
if(iCC(s,p,p,whiteTurn,true))
{
checkDueTo=s;
return true;
}
}
else if(s==3 || s==6)
{
if(iCC(s,p,p,whiteTurn,true))
{
checkDueTo=s;
return true;
}
}
}
}
}
else
{
for(i=0;i<8;i++)
{
for(j=0;j<8;j++)
{
p=new int[2];
p[0]=i;
p[1]=j;
s=chessBoardModel.board[i][j];
if(s==25 || s==32)
{
if(iCC(s,p,p,whiteTurn,true))
{
checkDueTo=s;
return true;
}
}
else if(s==28)
{
if(iCC(s,p,p,whiteTurn,true))
{
checkDueTo=s;
return true;
}
}
else if(s==27 || s==30)
{
if(iCC(s,p,p,whiteTurn,true))
{
checkDueTo=s;
return true;
}
}
}
}
}

return false;
}
}

checkDueTo=piece;
return true;
}
/*--------------Check Condition & Possible Moves Left Methods ends--------------*/
}
/*----------------------MOVES VALIDATION CLASS ENDS-------------------------*/

//invitationsList Table classes
class InvitationsListModel extends AbstractTableModel
{
private java.util.List<Message> messages;
private String title[]={"Members"," ","  "};
private JButton acceptButton;
private java.util.List<JButton> rejectButton;
InvitationsListModel()
{
messages=new LinkedList<>();
acceptButton=new JButton("Accept");
rejectButton=new LinkedList<>();
}
public int getRowCount()
{
return this.messages.size();
}
public String getColumnName(int index)
{
return this.title[index];
}
public Object getValueAt(int row,int column)
{
if(column==0) return this.messages.get(row).fromUsername;
else if(column==1) return acceptButton;
return rejectButton.get(row);
}
public int getColumnCount()
{
return this.title.length;
}
public boolean isCellEditable(int row,int col)
{
if(col==0) return false;
return true;
}
public Class getColumnClass(int c)
{
if(c==0) return String.class;
return JButton.class;
}
public void setValueAt(Object object,int row,int col)
{
//System.out.println(row+" "+col+" "+object);
String text=object.toString();
if(text.equals("Accept"))
{
String s=this.messages.get(row).fromUsername;
this.messages.remove(row);
//Something to be done
this.fireTableDataChanged();
int i=0;
while(this.messages.size()>0)
{
ChessUI.this.sendInvitation(this.messages.get(i).fromUsername,MESSAGE_TYPE.CHALLENGE_REJECTED);
this.messages.remove(i);
}
this.messages.clear();
this.fireTableDataChanged();
timer.stop();
timerToFetchMessages.stop();
isOpponentDisconnected.start();
ChessUI.this.sendInvitation(s,MESSAGE_TYPE.CHALLENGE_ACCEPTED);
ChessUI.this.p1.removeAll();
ChessUI.this.p2.removeAll();
ChessUI.this.p1.updateUI();
ChessUI.this.p2.updateUI();
ChessUI.this.container.removeAll();
ChessUI.this.repaint();
ChessUI.this.revalidate();
//System.out.println("~~~~~~~~~~~~~~~Setting up UI~~~~~~~~~~~~~");
movesValidation=new MovesValidation();
ChessUI.this.setChessBoard(s,ChessUI.this.username);
movesValidation.playedAMove=true;
canIPlay.start();
}
else if(text.equals("Reject"))
{
this.messages.remove(row);
if(this.messages.size()==0)
{
ChessUI.this.availableMembersListModel.setMembers(new LinkedList<String>());
timer.start();
}
//Something to be done
this.fireTableDataChanged();
ChessUI.this.sendInvitation(this.messages.get(row).fromUsername,MESSAGE_TYPE.CHALLENGE_REJECTED);
}
}
public void setMembers(java.util.List<Message> messages)
{
for(int i=0;i<messages.size();i++)
{
this.rejectButton.add(new JButton("Reject"));
this.messages.add(messages.get(i));
}
fireTableDataChanged();
}
public void memberDisconnected(java.util.List<Message> messages)
{
for(int i=0;i<messages.size();i++)
{
this.messages.remove(messages.get(i));
}
fireTableDataChanged();
}
}

class InvitationsListAcceptButtonRenderer implements TableCellRenderer
{
public Component getTableCellRendererComponent(JTable table,Object value,boolean a,boolean b,int r,int c)
{
return (JButton)value;
}
}
class InvitationsListAcceptButtonCellEditor extends DefaultCellEditor
{
private JButton button;
private boolean isClicked;
private int row,col;
InvitationsListAcceptButtonCellEditor()
{
super(new JCheckBox());
button=new JButton("Accept");
button.setOpaque(true);
button.addActionListener(new ActionListener(){
public void actionPerformed(ActionEvent ae)
{
fireEditingStopped();
}
});
}
public Component getTableCellEditorComponent(JTable table,Object value,boolean a,int row,int column)
{
this.row=row;
this.col=column;
button.setForeground(Color.black);
button.setBackground(UIManager.getColor("Button.background"));
button.setText("Accept");
isClicked=true;
return button;
}
public Object getCellEditorValue()
{
//System.out.println("getCellEditorValue got called");
//System.out.println("Button at cell : "+this.row+","+this.col+" got clicked");
return "Accept";
}
public boolean stopCellEditing()
{
isClicked=false;
return super.stopCellEditing();
}
public void fireEditingStopped()
{
super.fireEditingStopped();
}
}
//FOR REJECT BUTTON
class InvitationsListRejectButtonRenderer implements TableCellRenderer
{
public Component getTableCellRendererComponent(JTable table,Object value,boolean a,boolean b,int r,int c)
{
return (JButton)value;
}
}
class InvitationsListRejectButtonCellEditor extends DefaultCellEditor
{
private JButton button;
private boolean isClicked;
private int row,col;
InvitationsListRejectButtonCellEditor()
{
super(new JCheckBox());
button=new JButton("Reject");
button.setOpaque(true);
button.addActionListener(new ActionListener(){
public void actionPerformed(ActionEvent ae)
{
fireEditingStopped();
}
});
}
public Component getTableCellEditorComponent(JTable table,Object value,boolean a,int row,int column)
{
this.row=row;
this.col=column;
button.setForeground(Color.black);
button.setBackground(UIManager.getColor("Button.background"));
button.setText("Reject");
isClicked=true;
return button;
}
public Object getCellEditorValue()
{
//System.out.println("getCellEditorValue got called");
//System.out.println("Button at cell : "+this.row+","+this.col+" got clicked");
return "Reject";
}
public boolean stopCellEditing()
{
isClicked=false;
return super.stopCellEditing();
}
public void fireEditingStopped()
{
super.fireEditingStopped();
}
}

//InvitationsList Table Ends

//AvailableMembers List Table classes
class AvailableMembersListModel extends AbstractTableModel
{
private java.util.List<String> members;
private String title[]={"Members"," "};
private java.util.List<JButton> inviteButtons;
private boolean awaitingInvitationReply;
AvailableMembersListModel()
{
members=new LinkedList<>();
inviteButtons=new LinkedList<>();
awaitingInvitationReply=false;
}
public int getRowCount()
{
return this.members.size();
}
public String getColumnName(int index)
{
return this.title[index];
}
public Object getValueAt(int row,int column)
{
if(column==0) return this.members.get(row);
return this.inviteButtons.get(row);
}
public int getColumnCount()
{
return this.title.length;
}
public boolean isCellEditable(int row,int col)
{
if(col==1) return true;
return false;
}
public Class getColumnClass(int c)
{
if(c==0) return String.class;
return JButton.class;
}
public void setValueAt(Object object,int row,int col)
{
//System.out.println(row+" "+col+" "+object);
if(col==1)
{
JButton button=this.inviteButtons.get(row);
String text=(String)object;
button.setText(text);
//button.setEnabled(false);
if(text.equalsIgnoreCase("Invited"))
{
awaitingInvitationReply=true;
for(JButton inviteButton:inviteButtons) inviteButton.setEnabled(false);
this.fireTableDataChanged();
ChessUI.this.sendInvitation(this.members.get(row),MESSAGE_TYPE.CHALLENGE);
}
else if(text.equalsIgnoreCase("Invite"))
{
awaitingInvitationReply=false;
for(JButton inviteButton:inviteButtons) inviteButton.setEnabled(true);
this.fireTableDataChanged();
}
}
}
public void setMembers(java.util.List<String> members)
{
if(awaitingInvitationReply) return;
this.members=members;
this.inviteButtons.clear();
for(int i=0;i<this.members.size();i++) this.inviteButtons.add(new JButton("invite"));
fireTableDataChanged();
}
}

class AvailableMembersListButtonRenderer implements TableCellRenderer
{
public Component getTableCellRendererComponent(JTable table,Object value,boolean a,boolean b,int r,int c)
{
return (JButton)value;
}
}
class AvailableMembersListButtonCellEditor extends DefaultCellEditor
{
private JButton button;
private ActionListener actionListener;
private boolean isClicked;
private int row,col;
AvailableMembersListButtonCellEditor()
{
super(new JCheckBox());
this.actionListener=new ActionListener(){
public void actionPerformed(ActionEvent ae)
{
fireEditingStopped();
}
};
}
public Component getTableCellEditorComponent(JTable table,Object value,boolean a,int row,int column)
{
this.row=row;
this.col=column;
this.button=(JButton)availableMembersListModel.getValueAt(row,column);
this.button.removeActionListener(this.actionListener);
this.button.addActionListener(this.actionListener);
button.setForeground(Color.black);
button.setBackground(UIManager.getColor("Button.background"));
button.setText("Invite");
isClicked=true;
return button;
}
public Object getCellEditorValue()
{
//System.out.println("getCellEditorValue got called");
//System.out.println("Button at cell : "+this.row+","+this.col+" got clicked");
return "Invited";
}
public boolean stopCellEditing()
{
isClicked=false;
return super.stopCellEditing();
}
public void fireEditingStopped()
{
super.fireEditingStopped();
}
}
/*--------------------PIECE VALIDATOR CLASSES STARTS--------------------*/
class RookValidator extends Thread
{
int [] firstPosition;
int [] secondPosition;
boolean isWhite;
boolean isMoveValid;
public void setPositionAndTurn(int r1,int c1,int r2,int c2,boolean isWhite)
{
this.firstPosition=new int[2];
this.firstPosition[0]=r1;
this.firstPosition[1]=c1;
this.secondPosition=new int[2];
this.secondPosition[0]=r2;
this.secondPosition[1]=c2;
this.isWhite=isWhite;
}
public RookValidator()
{
}
public boolean isMoveValid()
{
return isMoveValid;
}
public int [][] getPossiblePositions(int [] position,boolean whiteTurn)
{				
if(position==null) return null;
int possiblePositions[][]=new int[14][2];
int i;
int j;
for(i=0;i<possiblePositions.length;i++)
{
possiblePositions[i][0]=-1;
possiblePositions[i][1]=-1;
}
int u=0;
int v=0;
i=position[0];
j=position[1];
int a;
int b=j+1;
boolean uptoPiece=false;
byte value=chessBoardModel.board[i][j];
while(b<8)
{
if(chessBoardModel.board[i][b]!=0)
{
if((whiteTurn && (chessBoardModel.board[i][b]>=1 && chessBoardModel.board[i][b]<=16)) || (!whiteTurn && (chessBoardModel.board[i][b]>=17 && chessBoardModel.board[i][b]<=32))) break;
else if((whiteTurn && chessBoardModel.board[i][b]==29) || (!whiteTurn && chessBoardModel.board[i][b]==5)) break;
else if((whiteTurn && chessBoardModel.board[i][b]>=17 && chessBoardModel.board[i][b]<=32) || (!whiteTurn && chessBoardModel.board[i][b]>=1 && chessBoardModel.board[i][b]<=16)) uptoPiece=true;
}
possiblePositions[u][0]=i;
possiblePositions[u][1]=b;
b++;
u++;
if(uptoPiece) break;
}
b=j-1;
uptoPiece=false;
while(b>-1)
{
if(chessBoardModel.board[i][b]!=0)
{
if((whiteTurn && (chessBoardModel.board[i][b]>=1 && chessBoardModel.board[i][b]<=16)) || (!whiteTurn && (chessBoardModel.board[i][b]>=17 && chessBoardModel.board[i][b]<=32))) break;
else if((whiteTurn && chessBoardModel.board[i][b]==29) || (!whiteTurn && chessBoardModel.board[i][b]==5)) break;
else if((whiteTurn && chessBoardModel.board[i][b]>=17 && chessBoardModel.board[i][b]<=32) || (!whiteTurn && chessBoardModel.board[i][b]>=1 && chessBoardModel.board[i][b]<=16)) uptoPiece=true;
}
possiblePositions[u][0]=i;
possiblePositions[u][1]=b;
b--;
u++;
if(uptoPiece) break;
}
uptoPiece=false;
a=i+1;
while(a<8)
{
if(chessBoardModel.board[a][j]!=0)
{
if((whiteTurn && (chessBoardModel.board[a][j]>=1 && chessBoardModel.board[a][j]<=16)) || (!whiteTurn && (chessBoardModel.board[a][j]>=17 && chessBoardModel.board[a][j]<=32))) break;
else if((whiteTurn && chessBoardModel.board[a][j]==29) || (!whiteTurn && chessBoardModel.board[a][j]==5)) break;
else if((whiteTurn && chessBoardModel.board[a][j]>=17 && chessBoardModel.board[a][j]<=32) || (!whiteTurn && chessBoardModel.board[a][j]>=1 && chessBoardModel.board[a][j]<=16)) uptoPiece=true;
}
possiblePositions[u][0]=a;
possiblePositions[u][1]=j;
a++;
u++;
if(uptoPiece) break;
}
uptoPiece=false;
a=i-1;
while(a>-1)
{
if(chessBoardModel.board[a][j]!=0)
{
if((whiteTurn && (chessBoardModel.board[a][j]>=1 && chessBoardModel.board[a][j]<=16)) || (!whiteTurn && (chessBoardModel.board[a][j]>=17 && chessBoardModel.board[a][j]<=32))) break;
else if((whiteTurn && chessBoardModel.board[a][j]==29) || (!whiteTurn && chessBoardModel.board[a][j]==5)) break;
else if((whiteTurn && chessBoardModel.board[a][j]>=17 && chessBoardModel.board[a][j]<=32) || (!whiteTurn && chessBoardModel.board[a][j]>=1 && chessBoardModel.board[a][j]<=16)) uptoPiece=true;
}
possiblePositions[u][0]=a;
possiblePositions[u][1]=j;
a--;
u++;
if(uptoPiece) break;
}
//for(i=0;i<possiblePositions.length;i++) System.out.println("Rook Position : "+possiblePositions[i][0]+" "+possiblePositions[i][1]);
return possiblePositions;
}
public int [][] getValidPositions(int []firstPosition,int[] secondPosition)
{
int validPositions[][]=new int [14][2];
int i;
int j;
for(i=0;i<validPositions.length;i++) for(j=0;j<validPositions[i].length;j++) validPositions[i][j]=-1;
int u=0;
int v=0;
i=secondPosition[0];
j=secondPosition[1];
int a;
int b=j+1;
while(b<8)
{
validPositions[u][0]=i;
validPositions[u][1]=b;
b++;
u++;
}
b=j-1;
while(b>-1)
{
validPositions[u][0]=i;
validPositions[u][1]=b;
b--;
u++;
}
a=i+1;
while(a<8)
{
validPositions[u][0]=a;
validPositions[u][1]=j;
a++;
u++;
}
a=i-1;
while(a>-1)
{
validPositions[u][0]=a;
validPositions[u][1]=j;
a--;
u++;
}
return validPositions;
}
public void run()
{
if(firstPosition[0]!=secondPosition[0] && firstPosition[1]!=secondPosition[1]) return;
int validPositions[][]=new int [14][2];
int i;
int j;
for(i=0;i<validPositions.length;i++) for(j=0;j<validPositions[i].length;j++) validPositions[i][j]=-1;
int u=0;
int v=0;
i=firstPosition[0];
j=firstPosition[1];
int a;
int b=j+1;
while(b<8)
{
validPositions[u][0]=i;
validPositions[u][1]=b;
b++;
u++;
}
b=j-1;
while(b>-1)
{
validPositions[u][0]=i;
validPositions[u][1]=b;
b--;
u++;
}
a=i+1;
while(a<8)
{
validPositions[u][0]=a;
validPositions[u][1]=j;
a++;
u++;
}
a=i-1;
while(a>-1)
{
validPositions[u][0]=a;
validPositions[u][1]=j;
a--;
u++;
}
boolean isPositionValid=false;
for(i=0;i<validPositions.length;i++)
{
j=0;
if(secondPosition[0]==validPositions[i][j] && secondPosition[1]==validPositions[i][j+1])
{
isPositionValid=true;
break;
}
}
if(!isPositionValid) return;
byte [] whitePieces={1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
byte [] blackPieces={17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32};
byte piece;
int [] p=new int[2];
int x,y;
u=firstPosition[0];
v=firstPosition[1];
//for(JButton button:pieces)
for(x=0;x<8;x++)
{
for(y=0;y<8;y++)
{
piece=chessBoardModel.board[x][y];
p[0]=x;
p[1]=y;
if(p[0]==secondPosition[0] && p[1]==secondPosition[1])
{
if(isWhite)
{
for(int k=0;k<whitePieces.length;k++) if(piece==whitePieces[k]) return;
}
else
{
for(int k=0;k<blackPieces.length;k++) if(piece==blackPieces[k]) return;
}
}
for(i=0;i<validPositions.length;i++) 
{
j=0;
if(secondPosition[0]==firstPosition[0])
{
if(secondPosition[1]>firstPosition[1])
{
if(p[0]==validPositions[i][j] && p[1]==validPositions[i][j+1] && p[0]==firstPosition[0] && p[1]>firstPosition[1] && p[1]<secondPosition[1])
{
for(int k=0;k<whitePieces.length;k++)
{
if(piece==whitePieces[k] || piece==blackPieces[k]) return;
/*if(isWhite && piece.equals(whitePieces[k]) ||) return;
else if(!isWhite && piece.equals(blackPieces[k])) return;*/
}
}
}
else
{
if(p[0]==validPositions[i][j] && p[1]==validPositions[i][j+1] && p[0]==firstPosition[0] && p[1]<firstPosition[1] && p[1]>secondPosition[1])
{
for(int k=0;k<whitePieces.length;k++)
{
if(piece==whitePieces[k] || piece==blackPieces[k]) return;
}
}
}
}
else if(secondPosition[1]==firstPosition[1])
{
if(secondPosition[1]>firstPosition[1])
{
if(p[0]==validPositions[i][j] && p[1]==validPositions[i][j+1] && p[1]==firstPosition[1] && p[0]>firstPosition[0] && p[0]<secondPosition[0])
{
for(int k=0;k<whitePieces.length;k++)
{
if(piece==whitePieces[k] || piece==blackPieces[k]) return;
/*if(isWhite && piece.equals(whitePieces[k]) ||) return;
else if(!isWhite && piece.equals(blackPieces[k])) return;*/
}
}
}
else
{
if(p[0]==validPositions[i][j] && p[1]==validPositions[i][j+1] && p[1]==firstPosition[1] && p[0]<firstPosition[0] && p[0]>secondPosition[0])
{
for(int k=0;k<whitePieces.length;k++)
{
if(piece==whitePieces[k] || piece==blackPieces[k]) return;
}
}
}
}
}
}
}
isMoveValid=true;
}
}
/*--------------------ROOK VALIDATOR CLASS ENDS--------------------*/
/*--------------------BISHOP VALIDATOR CLASS--------------------*/
class BishopValidator extends Thread
{
int [] firstPosition;
int [] secondPosition;
boolean isWhite;
boolean isMoveValid;
public void setPositionAndTurn(int r1,int c1,int r2,int c2,boolean isWhite)
{
this.firstPosition=new int[2];
this.firstPosition[0]=r1;
this.firstPosition[1]=c1;
this.secondPosition=new int[2];
this.secondPosition[0]=r2;
this.secondPosition[1]=c2;
this.isWhite=isWhite;
}
public BishopValidator()
{
}
public boolean isMoveValid()
{
return isMoveValid;
}
public int [][] getPossiblePositions(int [] position,boolean whiteTurn)
{
if(position==null) return null;
int possiblePositions[][]=new int[14][2];
int i=position[0];
int j=position[1];
int k;
int l;
int u,v;
for(u=0;u<possiblePositions.length;u++) for(v=0;v<possiblePositions[u].length;v++) possiblePositions[u][v]=-1;
u=0;
i++;
j++;
boolean uptoPiece=false;
if(j<8)
{
while(i<8)
{
if(chessBoardModel.board[i][j]!=0)
{
if((whiteTurn && (chessBoardModel.board[i][j]>=1 && chessBoardModel.board[i][j]<=16)) || (!whiteTurn && (chessBoardModel.board[i][j]>=17 && chessBoardModel.board[i][j]<=32))) break;
else if((whiteTurn && chessBoardModel.board[i][j]==29) || (!whiteTurn && chessBoardModel.board[i][j]==5)) break;
else if((whiteTurn && chessBoardModel.board[i][j]>=17 && chessBoardModel.board[i][j]<=32) || (!whiteTurn && chessBoardModel.board[i][j]>=1 && chessBoardModel.board[i][j]<=16)) uptoPiece=true;
}
possiblePositions[u][0]=i;
possiblePositions[u++][1]=j;
if(j==7) break;
if(uptoPiece) break;
j++;
i++;
}
}
i=position[0];
j=position[1];
i++;
j--;
uptoPiece=false;
if(j>-1)
{
while(i<8)
{
if(chessBoardModel.board[i][j]!=0)
{
if((whiteTurn && (chessBoardModel.board[i][j]>=1 && chessBoardModel.board[i][j]<=16)) || (!whiteTurn && (chessBoardModel.board[i][j]>=17 && chessBoardModel.board[i][j]<=32))) break;
else if((whiteTurn && chessBoardModel.board[i][j]==29) || (!whiteTurn && chessBoardModel.board[i][j]==5)) break;
else if((whiteTurn && chessBoardModel.board[i][j]>=17 && chessBoardModel.board[i][j]<=32) || (!whiteTurn && chessBoardModel.board[i][j]>=1 && chessBoardModel.board[i][j]<=16)) uptoPiece=true;
}
possiblePositions[u][0]=i;
possiblePositions[u++][1]=j;
if(j==0) break;
if(uptoPiece) break;
j--;
i++;
}
}
i=position[0];
j=position[1];
i--;
j++;
uptoPiece=false;
if(i>-1)
{
while(j<8)
{
if(chessBoardModel.board[i][j]!=0)
{
if((whiteTurn && (chessBoardModel.board[i][j]>=1 && chessBoardModel.board[i][j]<=16)) || (!whiteTurn && (chessBoardModel.board[i][j]>=17 && chessBoardModel.board[i][j]<=32))) break;
else if((whiteTurn && chessBoardModel.board[i][j]==29) || (!whiteTurn && chessBoardModel.board[i][j]==5)) break;
else if((whiteTurn && chessBoardModel.board[i][j]>=17 && chessBoardModel.board[i][j]<=32) || (!whiteTurn && chessBoardModel.board[i][j]>=1 && chessBoardModel.board[i][j]<=16)) uptoPiece=true;
}
possiblePositions[u][0]=i;
possiblePositions[u++][1]=j;
if(i==0) break;
if(uptoPiece) break;
i--;
j++;
}
}
i=position[0];
j=position[1];
i--;
j--;
uptoPiece=false;
if(i>-1)
{
while(j>-1)
{
if(chessBoardModel.board[i][j]!=0)
{
if((whiteTurn && (chessBoardModel.board[i][j]>=1 && chessBoardModel.board[i][j]<=16)) || (!whiteTurn && (chessBoardModel.board[i][j]>=17 && chessBoardModel.board[i][j]<=32))) break;
else if((whiteTurn && chessBoardModel.board[i][j]==29) || (!whiteTurn && chessBoardModel.board[i][j]==5)) break;
else if((whiteTurn && chessBoardModel.board[i][j]>=17 && chessBoardModel.board[i][j]<=32) || (!whiteTurn && chessBoardModel.board[i][j]>=1 && chessBoardModel.board[i][j]<=16)) uptoPiece=true;
}
possiblePositions[u][0]=i;
possiblePositions[u++][1]=j;
if(i==0) break;
if(uptoPiece) break;
i--;
j--;
}
}
//for(i=0;i<possiblePositions.length;i++) System.out.println("Bishop Position : "+possiblePositions[i][0]+" "+possiblePositions[i][1]);
return possiblePositions;
}
public int [][] getValidPositions(int []firstPosition,int []secondPosition)
{
int i=secondPosition[0];
int j=secondPosition[1];
int k;
int l;
int validPositions[][]=new int[14][2];
int u,v;
for(u=0;u<validPositions.length;u++) for(v=0;v<validPositions[u].length;v++) validPositions[u][v]=-1;
u=0;
i++;
j++;
if(j<8)
{
while(i<8)
{
validPositions[u][0]=i;
validPositions[u++][1]=j;
if(j==7) break;
j++;
i++;
}
}
i=secondPosition[0];
j=secondPosition[1];
i++;
j--;
if(j>-1)
{
while(i<8)
{
validPositions[u][0]=i;
validPositions[u++][1]=j;
if(j==0) break;
j--;
i++;
}
}
i=secondPosition[0];
j=secondPosition[1];
i--;
j++;
if(i>-1)
{
while(j<8)
{
validPositions[u][0]=i;
validPositions[u++][1]=j;
if(i==0) break;
i--;
j++;
}
}
i=secondPosition[0];
j=secondPosition[1];
i--;
j--;
if(i>-1)
{
while(j>-1)
{
validPositions[u][0]=i;
validPositions[u++][1]=j;
if(i==0) break;
i--;
j--;
}
}
return validPositions;
}
public void run()
{
int i=firstPosition[0];
int j=firstPosition[1];
int k=secondPosition[0];
int l=secondPosition[1];
int validPositions[][]=new int[14][2];
int u,v;
for(u=0;u<validPositions.length;u++) for(v=0;v<validPositions[u].length;v++) validPositions[u][v]=-1;
u=0;
boolean movingUp=false;
if(k>i && l>j)
{
movingUp=false;
while(i<8)
{
i++;
if(i==8) break;
v=0;
if(j<7)
{
j++;
validPositions[u][v++]=i;
validPositions[u][v]=j;
u++;
}
}
}
else if(k>i && l<j)
{
movingUp=false;
while(i<8)
{
i++;
if(i==8) break;
v=0;
if(j>0)
{
j--;
validPositions[u][v++]=i;
validPositions[u][v]=j;
u++;
}
}
}
else if(k<i && l>j)
{
movingUp=true;
while(i>0)
{
i--;
v=0;
if(j<7)
{
j++;
validPositions[u][v++]=i;
validPositions[u][v]=j;
u++;
}
}
}
else if(k<i && l<j)
{
movingUp=true;
while(i>0)
{
i--;
v=0;
if(j>0)
{
j--;
validPositions[u][v++]=i;
validPositions[u][v]=j;
u++;
}
}
}
else return;
byte piece;
int [] p=new int[2];
byte [] whitePieces={1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
byte [] blackPieces={17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32};
boolean isPositionValid=false;
for(i=0;i<validPositions.length;i++)
{
j=0;
if(validPositions[i][j]==secondPosition[0] && validPositions[i][j+1]==secondPosition[1])
{
isPositionValid=true;
break;
}
}
if(!isPositionValid) return;
int x,y;
for(x=0;x<8;x++)
{
for(y=0;y<8;y++)
{
piece=chessBoardModel.board[x][y];
p[0]=x;
p[0]=y;
for(i=0;i<validPositions.length;i++)
{
j=0;
if((!movingUp && p[0]<secondPosition[0]) || (movingUp && p[0]>secondPosition[0]))
{
if(validPositions[i][j]==p[0] && validPositions[i][j+1]==p[1] && p[0]!=secondPosition[0] && p[1]!=secondPosition[1] && p[0]!=firstPosition[0])
{
for(k=0;k<whitePieces.length;k++)
if(piece==whitePieces[k] || piece==blackPieces[k]) return;
}
}
if(p[0]==secondPosition[0] && p[1]==secondPosition[1])
{
if(isWhite)
{
for(k=0;k<whitePieces.length;k++)
if(piece==whitePieces[k]) return;
}
else
{
for(k=0;k<blackPieces.length;k++)
if(piece==blackPieces[k]) return;
}
} 
}
}
}
isMoveValid=true;
}
}
/*----------------BISHOP VALIDATOR CLASS ENDS---------------*/
/*--------------------KNIGHT VALIDATOR CLASS--------------------*/
class KnightValidator extends Thread
{
int [] firstPosition;
int [] secondPosition;
boolean isWhite;
boolean isMoveValid;
public void setPositionAndTurn(int r1,int c1,int r2,int c2,boolean isWhite)
{
this.firstPosition=new int[2];
this.firstPosition[0]=r1;
this.firstPosition[1]=c1;
this.secondPosition=new int[2];
this.secondPosition[0]=r2;
this.secondPosition[1]=c2;
this.isWhite=isWhite;
}
public KnightValidator()
{
}
public boolean isMoveValid()
{
return isMoveValid;
}
public int [][] getPossiblePositions(int [] position,boolean whiteTurn)
{
if(position==null) return null;
int possiblePositions[][]=new int[8][2];
int i;
int j;
for(i=0;i<possiblePositions.length;i++) for(j=0;j<possiblePositions[i].length;j++) possiblePositions[i][j]=-1;
int u=0;
int v=0;
i=position[0];
j=position[1];
if(j+2<8)
{
if(i+1<8)
{
if(chessBoardModel.board[i+1][j+2]==0 || (whiteTurn && chessBoardModel.board[i+1][j+2]>=17 && chessBoardModel.board[i+1][j+2]!=29) || (!whiteTurn && chessBoardModel.board[i+1][j+2]>=1 && chessBoardModel.board[i+1][j+2]<=16 && chessBoardModel.board[i+1][j+2]!=5))
{
possiblePositions[u][0]=i+1;
possiblePositions[u][1]=j+2;
u++;
}
}
if(i-1>-1)
{
if(chessBoardModel.board[i-1][j+2]==0 || (whiteTurn && chessBoardModel.board[i-1][j+2]>=17 && chessBoardModel.board[i-1][j+2]!=29) || (!whiteTurn && chessBoardModel.board[i-1][j+2]>=1 && chessBoardModel.board[i-1][j+2]<=16 && chessBoardModel.board[i-1][j+2]!=5))
{
possiblePositions[u][0]=i-1;
possiblePositions[u][1]=j+2;
u++;
}
}
}
if(j-2>-1)
{
if(i+1<8)
{
if(chessBoardModel.board[i+1][j-2]==0 || (whiteTurn && chessBoardModel.board[i+1][j-2]>=17 && chessBoardModel.board[i+1][j-2]!=29) || (!whiteTurn && chessBoardModel.board[i+1][j-2]>=1 && chessBoardModel.board[i+1][j-2]<=16 && chessBoardModel.board[i+1][j-2]!=5))
{
possiblePositions[u][0]=i+1;
possiblePositions[u][1]=j-2;
u++;
}
}
if(i-1>-1)
{
if(chessBoardModel.board[i-1][j-2]==0 || (whiteTurn && chessBoardModel.board[i-1][j-2]>=17 && chessBoardModel.board[i-1][j-2]!=29) || (!whiteTurn && chessBoardModel.board[i-1][j-2]>=1 && chessBoardModel.board[i-1][j-2]<=16 && chessBoardModel.board[i-1][j-2]!=5))
{
possiblePositions[u][0]=i-1;
possiblePositions[u][1]=j-2;
u++;
}
}
}
if(i+2<8)
{
if(j+1<8)
{
if(chessBoardModel.board[i+2][j+1]==0 || (whiteTurn && chessBoardModel.board[i+2][j+1]>=17 && chessBoardModel.board[i+2][j+1]!=29) || (!whiteTurn && chessBoardModel.board[i+2][j+1]>=1 && chessBoardModel.board[i+2][j+1]<=16 && chessBoardModel.board[i+2][j+1]!=5))
{
possiblePositions[u][0]=i+2;
possiblePositions[u][1]=j+1;
u++;
}
}
if(j-1>-1)
{
if(chessBoardModel.board[i+2][j-1]==0 || (whiteTurn && chessBoardModel.board[i+2][j-1]>=17 && chessBoardModel.board[i+2][j-1]!=29) || (!whiteTurn && chessBoardModel.board[i+2][j-1]>=1 && chessBoardModel.board[i+2][j-1]<=16 && chessBoardModel.board[i+2][j-1]!=5))
{
possiblePositions[u][0]=i+2;
possiblePositions[u][1]=j-1;
u++;
}
}
}
if(i-2>-1)
{
if(j+1<8)
{
if(chessBoardModel.board[i-2][j+1]==0 || (whiteTurn && chessBoardModel.board[i-2][j+1]>=17 && chessBoardModel.board[i-2][j+1]!=29) || (!whiteTurn && chessBoardModel.board[i-2][j+1]>=1 && chessBoardModel.board[i-2][j+1]<=16 && chessBoardModel.board[i-2][j+1]!=5))
{
possiblePositions[u][0]=i-2;
possiblePositions[u][1]=j+1;
u++;
}
}
if(j-1>-1)
{
if(chessBoardModel.board[i-2][j-1]==0 || (whiteTurn && chessBoardModel.board[i-2][j-1]>=17 && chessBoardModel.board[i-2][j-1]!=29) || (!whiteTurn && chessBoardModel.board[i-2][j-1]>=1 && chessBoardModel.board[i-2][j-1]<=16 && chessBoardModel.board[i-2][j-1]!=5))
{
possiblePositions[u][0]=i-2;
possiblePositions[u][1]=j-1;
u++;
}
}
}
//for(i=0;i<possiblePositions.length;i++) System.out.println("Knight Position : "+possiblePositions[i][0]+" "+possiblePositions[i][1]);
return possiblePositions;
}
public int [][] getValidPositions(int []firstPosition,int [] secondPosition)
{
int validPositions[][]=new int [8][2];
int i;
int j;
for(i=0;i<validPositions.length;i++) for(j=0;j<validPositions[i].length;j++) validPositions[i][j]=-1;
int u=0;
int v=0;
i=firstPosition[0];
j=firstPosition[1];
//System.out.println("~~~~~~~~~~~~~~"+i+", "+j);
if(j+2<8)
{
if(i+1<8)
{
validPositions[u][0]=i+1;
validPositions[u][1]=j+2;
u++;
}
if(i-1>-1)
{
validPositions[u][0]=i-1;
validPositions[u][1]=j+2;
u++;
}
}
if(j-2>-1)
{
if(i+1<8)
{
validPositions[u][0]=i+1;
validPositions[u][1]=j-2;
u++;
}
if(i-1>-1)
{
validPositions[u][0]=i-1;
validPositions[u][1]=j-2;
u++;
}
}
if(i+2<8)
{
if(j+1<8)
{
validPositions[u][0]=i+2;
validPositions[u][1]=j+1;
u++;
}
if(j-1>-1)
{
validPositions[u][0]=i+2;
validPositions[u][1]=j-1;
u++;
}
}
if(i-2>-1)
{
if(j+1<8)
{
validPositions[u][0]=i-2;
validPositions[u][1]=j+1;
u++;
}
if(j-1>-1)
{
validPositions[u][0]=i-2;
validPositions[u][1]=j-1;
u++;
}
}
return validPositions;
}
public void run()
{
int validPositions[][]=new int [8][2];
int i;
int j;
for(i=0;i<validPositions.length;i++) for(j=0;j<validPositions[i].length;j++) validPositions[i][j]=-1;
int u=0;
int v=0;
i=firstPosition[0];
j=firstPosition[1];
if(j+2<8)
{
if(i+1<8)
{
validPositions[u][v++]=i+1;
validPositions[u][v]=j+2;
u++;
}
if(i-1>-1)
{
v=0;
validPositions[u][v++]=i-1;
validPositions[u][v]=j+2;
u++;
}
}
v=0;
if(j-2>-1)
{
if(i+1<8)
{
validPositions[u][v++]=i+1;
validPositions[u][v]=j-2;
u++;
}
if(i-1>-1)
{
v=0;
validPositions[u][v++]=i-1;
validPositions[u][v]=j-2;
u++;
}
}
v=0;
if(i+2<8)
{
if(j+1<8)
{
validPositions[u][v++]=i+2;
validPositions[u][v]=j+1;
u++;
}
if(j-1>-1)
{
v=0;
validPositions[u][v++]=i+2;
validPositions[u][v]=j-1;
u++;
}
}
v=0;
if(i-2>-1)
{
if(j+1<8)
{
validPositions[u][v++]=i-2;
validPositions[u][v]=j+1;
u++;
}
if(j-1>-1)
{
v=0;
validPositions[u][v++]=i-2;
validPositions[u][v]=j-1;
u++;
}
}
boolean isPositionValid=false;
for(i=0;i<validPositions.length;i++)
{
j=0;
if(secondPosition[0]==validPositions[i][j] && secondPosition[1]==validPositions[i][j+1])
{
isPositionValid=true;
break;
}
}
if(!isPositionValid) return;
byte [] whitePieces={1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
byte [] blackPieces={17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32};
byte piece;
int x,y;
int [] p=new int[2];
for(x=0;x<8;x++)
{
for(y=0;y<8;y++)
{
piece=chessBoardModel.board[x][y];
p[0]=x;
p[1]=y;
for(i=0;i<validPositions.length;i++) 
{
j=0;
if(p[0]==validPositions[i][j] && p[1]==validPositions[i][j+1] && p[0]==secondPosition[0] && p[1]==secondPosition[1])
{
for(int k=0;k<whitePieces.length;k++)
{
if(isWhite && piece==whitePieces[k]) return;
if(!isWhite && piece==blackPieces[k]) return;
}
}
}
}
}
isMoveValid=true;
}
}
/*----------------KNIGHT VALIDATOR CLASS ENDS---------------*/
/*--------------------QUEEN VALIDATOR CLASS--------------------*/
class QueenValidator extends Thread
{
int [] firstPosition;
int [] secondPosition;
boolean isWhite;
boolean isMoveValid;
public void setPositionAndTurn(int r1,int c1,int r2,int c2,boolean isWhite)
{
this.firstPosition=new int[2];
this.firstPosition[0]=r1;
this.firstPosition[1]=c1;
this.secondPosition=new int[2];
this.secondPosition[0]=r2;
this.secondPosition[1]=c2;
this.isWhite=isWhite;
}
public QueenValidator()
{
}
public boolean isMoveValid()
{
return isMoveValid;
}
public int [][] getPossiblePositions(int [] position,boolean whiteTurn)
{
if(position==null) return null;
int [][] possiblePositionsOfRook=rookValidator.getPossiblePositions(position,whiteTurn);
int [][] possiblePositionsOfBishop=bishopValidator.getPossiblePositions(position,whiteTurn);
int [][] possiblePositions=new int [possiblePositionsOfRook.length+possiblePositionsOfBishop.length][2];
int i=0;
for(i=0;i<possiblePositionsOfRook.length;i++)
{
possiblePositions[i][0]=possiblePositionsOfRook[i][0];
possiblePositions[i][1]=possiblePositionsOfRook[i][1];
}
for(int j=0;i<possiblePositions.length;i++,j++)
{
possiblePositions[i][0]=possiblePositionsOfBishop[j][0];
possiblePositions[i][1]=possiblePositionsOfBishop[j][1];
}
//for(i=0;i<possiblePositions.length;i++) System.out.println("Queen Position : "+possiblePositions[i][0]+" "+possiblePositions[i][1]);
return possiblePositions;
}
public int [][] getValidPositions(int []firstPosition,int []secondPosition)
{
int [][] validPositionsOfRook=rookValidator.getValidPositions(firstPosition,secondPosition);
int [][] validPositionsOfBishop=bishopValidator.getValidPositions(firstPosition,secondPosition);
int [][] validPositions=new int [validPositionsOfRook.length+validPositionsOfBishop.length][2];
int i=0;
for(i=0;i<validPositionsOfRook.length;i++)
{
validPositions[i][0]=validPositionsOfRook[i][0];
validPositions[i][1]=validPositionsOfRook[i][1];
}
for(int j=0;i<validPositions.length;i++,j++)
{
validPositions[i][0]=validPositionsOfBishop[j][0];
validPositions[i][1]=validPositionsOfRook[j][1];
}
return validPositions;
}
public void run()
{
boolean isRookMoveValid;
boolean isBishopMoveValid;
RookValidator rookValidator=new RookValidator();
BishopValidator bishopValidator=new BishopValidator();
if(isWhite)
{
rookValidator.setPositionAndTurn(firstPosition[0],firstPosition[1],secondPosition[0],secondPosition[1],true);
rookValidator.start();
try
{
rookValidator.join();
}catch(Exception e)
{}
isRookMoveValid=rookValidator.isMoveValid();
bishopValidator.setPositionAndTurn(firstPosition[0],firstPosition[1],secondPosition[0],secondPosition[1],true);
bishopValidator.start();
try
{
bishopValidator.join();
}catch(Exception e)
{}
isBishopMoveValid=bishopValidator.isMoveValid();
}
else
{
rookValidator.setPositionAndTurn(firstPosition[0],firstPosition[1],secondPosition[0],secondPosition[1],false);
rookValidator.start();
try
{
rookValidator.join();
}catch(Exception e)
{}
isRookMoveValid=rookValidator.isMoveValid();
bishopValidator.setPositionAndTurn(firstPosition[0],firstPosition[1],secondPosition[0],secondPosition[1],false);
bishopValidator.start();
try
{
bishopValidator.join();
}catch(Exception e)
{}
isBishopMoveValid=bishopValidator.isMoveValid();
}
if(isRookMoveValid || isBishopMoveValid) isMoveValid=true;
}
}
/*----------------QUEEN VALIDATOR CLASS ENDS---------------*/
/*--------------------KING VALIDATOR CLASS--------------------*/
class KingValidator extends Thread
{
int [] firstPosition;
int [] secondPosition;
boolean isWhite;
boolean isMoveValid;
boolean isCastlingMoveWhite;
boolean isCastlingMoveBlack;
boolean whiteKingCastled;
boolean blackKingCastled;
public boolean isCastlingMove(boolean isWhite)
{
if(isWhite) return isCastlingMoveWhite;
return isCastlingMoveBlack;
}
public boolean isCastled(boolean isWhite)
{
if(isWhite) return whiteKingCastled;
return blackKingCastled;
}
public void setPositionAndTurn(int r1,int c1,int r2,int c2,boolean isWhite)
{
this.firstPosition=new int[2];
this.firstPosition[0]=r1;
this.firstPosition[1]=c1;
this.secondPosition=new int[2];
this.secondPosition[0]=r2;
this.secondPosition[1]=c2;
this.isWhite=isWhite;
}
public int [][] getPossiblePositions(int [] position,boolean whiteTurn)
{
if(position==null) return null;
int possiblePositions[][]=new int [8][2];
int i;
int j;
for(i=0;i<possiblePositions.length;i++) for(j=0;j<possiblePositions[i].length;j++) possiblePositions[i][j]=-1;
int u=0;
int v=0;
i=position[0];
j=position[1];
if(j+1<8)
{
if(chessBoardModel.board[i][j+1]!=0 && !((whiteTurn && chessBoardModel.board[i][j+1]>=17 && chessBoardModel.board[i][j+1]<=32) || (!whiteTurn && chessBoardModel.board[i][j+1]>=1 && chessBoardModel.board[i][j+1]<=16)));
else
{
possiblePositions[u][v++]=i;
possiblePositions[u][v]=j+1;
u++;
}
if(i+1<8)
{
if(chessBoardModel.board[i+1][j+1]!=0 && !((whiteTurn && chessBoardModel.board[i+1][j+1]>=17 && chessBoardModel.board[i+1][j+1]<=32) || (!whiteTurn && chessBoardModel.board[i+1][j+1]>=1 && chessBoardModel.board[i+1][j+1]<=16)));
else
{
v=0;
possiblePositions[u][v++]=i+1;
possiblePositions[u][v]=j+1;
u++;
}
}
if(i-1>-1)
{
if(chessBoardModel.board[i-1][j+1]!=0 && !((whiteTurn && chessBoardModel.board[i-1][j+1]>=17 && chessBoardModel.board[i-1][j+1]<=32) || (!whiteTurn && chessBoardModel.board[i-1][j+1]>=1 && chessBoardModel.board[i-1][j+1]<=16)));
else
{
v=0;
possiblePositions[u][v++]=i-1;
possiblePositions[u][v]=j+1;
u++;
}
}
}
v=0;
if(j-1>-1)
{
if(chessBoardModel.board[i][j-1]!=0 && !((whiteTurn && chessBoardModel.board[i][j-1]>=17 && chessBoardModel.board[i][j-1]<=32) || (!whiteTurn && chessBoardModel.board[i][j-1]>=1 && chessBoardModel.board[i][j-1]<=16)));
else
{
possiblePositions[u][v++]=i;
possiblePositions[u][v]=j-1;
u++;
}
if(i+1<8)
{
if(chessBoardModel.board[i+1][j-1]!=0 && !((whiteTurn && chessBoardModel.board[i+1][j-1]>=17 && chessBoardModel.board[i+1][j-1]<=32) || (!whiteTurn && chessBoardModel.board[i+1][j-1]>=1 && chessBoardModel.board[i+1][j-1]<=16)));
else
{
v=0;
possiblePositions[u][v++]=i+1;
possiblePositions[u][v]=j-1;
u++;
}
}
if(i-1>-1)
{
if(chessBoardModel.board[i-1][j-1]!=0 && !((whiteTurn && chessBoardModel.board[i-1][j-1]>=17 && chessBoardModel.board[i-1][j-1]<=32) || (!whiteTurn && chessBoardModel.board[i-1][j-1]>=1 && chessBoardModel.board[i-1][j-1]<=16)));
else
{
v=0;
possiblePositions[u][v++]=i-1;
possiblePositions[u][v]=j-1;
u++;
}
}
}
v=0;
if(i+1<8)
{
if(chessBoardModel.board[i+1][j]!=0 && !((whiteTurn && chessBoardModel.board[i+1][j]>=17 && chessBoardModel.board[i+1][j]<=32) || (!whiteTurn && chessBoardModel.board[i+1][j]>=1 && chessBoardModel.board[i+1][j]<=16)));
else
{
possiblePositions[u][v++]=i+1;
possiblePositions[u][v]=j;
u++;
}
}
v=0;
if(i-1>-1)
{
if(chessBoardModel.board[i-1][j]!=0 && !((whiteTurn && chessBoardModel.board[i-1][j]>=17 && chessBoardModel.board[i-1][j]<=32) || (!whiteTurn && chessBoardModel.board[i-1][j]>=1 && chessBoardModel.board[i-1][j]<=16)));
else
{
possiblePositions[u][v++]=i-1;
possiblePositions[u][v]=j;
u++;
}
}
//for(i=0;i<possiblePositions.length;i++) System.out.println("King Position : "+possiblePositions[i][0]+" "+possiblePositions[i][1]);
return possiblePositions;
}
public int [][] getValidPositions(int []firstPosition,int[] secondPosition,boolean whiteTurn)
{
int validPositions[][]=new int [8][2];
int i;
int j;
for(i=0;i<validPositions.length;i++) for(j=0;j<validPositions[i].length;j++) validPositions[i][j]=-1;
int u=0;
int v=0;
i=secondPosition[0];
j=secondPosition[1];

if(j+1<8)
{
validPositions[u][v++]=i;
validPositions[u][v]=j+1;
u++;
if(i+1<8)
{
v=0;
validPositions[u][v++]=i+1;
validPositions[u][v]=j+1;
u++;
}
if(i-1>-1)
{
v=0;
validPositions[u][v++]=i-1;
validPositions[u][v]=j+1;
u++;
}
}
v=0;
if(j-1>-1)
{
validPositions[u][v++]=i;
validPositions[u][v]=j-1;
u++;
if(i+1<8)
{
v=0;
validPositions[u][v++]=i+1;
validPositions[u][v]=j-1;
u++;
}
if(i-1>-1)
{
v=0;
validPositions[u][v++]=i-1;
validPositions[u][v]=j-1;
u++;
}
}
v=0;
if(i+1<8)
{
validPositions[u][v++]=i+1;
validPositions[u][v]=j;
u++;
}
v=0;
if(i-1>-1)
{
validPositions[u][v++]=i-1;
validPositions[u][v]=j;
u++;
}
return validPositions;
}
public KingValidator()
{
}
public boolean isMoveValid()
{
return isMoveValid;
}
public void run()
{
int validPositions[][]=new int [8][2];
int i;
int j;
for(i=0;i<validPositions.length;i++) for(j=0;j<validPositions[i].length;j++) validPositions[i][j]=-1;
int u=0;
int v=0;
i=firstPosition[0];
j=firstPosition[1];
if(!whiteKingCastled && i==0 && j==4)
{
if(secondPosition[0]==0 && (secondPosition[1]==1 || secondPosition[1]==2))
{
if(isWhite && chessBoardModel.board[0][0]==1)
{
validPositions[u][v++]=0;
validPositions[u][v]=1;
u++;
isCastlingMoveWhite=true;
}
}
else if(secondPosition[0]==0 && secondPosition[1]==6)
{
if(isWhite && chessBoardModel.board[0][7]==8)
{
validPositions[u][v++]=0;
validPositions[u][v]=6;
u++;
isCastlingMoveWhite=true;
}
}
}
if(!blackKingCastled && i==7 && j==4)
{
if(secondPosition[0]==7 && (secondPosition[1]==1 || secondPosition[1]==2))
{
if(!isWhite && chessBoardModel.board[7][0]==25)
{
validPositions[u][v++]=7;
validPositions[u][v]=1;
u++;
isCastlingMoveBlack=true;
}
}
else if(secondPosition[0]==7 && secondPosition[1]==6)
{
if(!isWhite &&  chessBoardModel.board[7][7]==32)
{
validPositions[u][v++]=7;
validPositions[u][v]=6;
u++;
isCastlingMoveBlack=true;
}
}
}
v=0;
if(j+1<8)
{
validPositions[u][v++]=i;
validPositions[u][v]=j+1;
u++;
if(i+1<8)
{
v=0;
validPositions[u][v++]=i+1;
validPositions[u][v]=j+1;
u++;
}
if(i-1>-1)
{
v=0;
validPositions[u][v++]=i-1;
validPositions[u][v]=j+1;
u++;
}
}
v=0;
if(j-1>-1)
{
validPositions[u][v++]=i;
validPositions[u][v]=j-1;
u++;
if(i+1<8)
{
v=0;
validPositions[u][v++]=i+1;
validPositions[u][v]=j-1;
u++;
}
if(i-1>-1)
{
v=0;
validPositions[u][v++]=i-1;
validPositions[u][v]=j-1;
u++;
}
}
v=0;
if(i+1<8)
{
validPositions[u][v++]=i+1;
validPositions[u][v]=j;
u++;
}
v=0;
if(i-1>-1)
{
validPositions[u][v++]=i-1;
validPositions[u][v]=j;
u++;
}
boolean isPositionValid=false;
for(i=0;i<validPositions.length;i++)
{
j=0;
if(secondPosition[0]==validPositions[i][j] && secondPosition[1]==validPositions[i][j+1])
{
isPositionValid=true;
break;
}
}
if(!isPositionValid) return;
byte [] whitePieces={1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
byte [] blackPieces={17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32};
byte piece;
int x,y;
int [] p=new int[2];
for(x=0;x<8;x++)
{
for(y=0;y<8;y++)
{
piece=chessBoardModel.board[x][y];
p[0]=x;
p[1]=y;
for(i=0;i<validPositions.length;i++) 
{
j=0;
if(p[0]==validPositions[i][j] && p[1]==validPositions[i][j+1] && p[0]==secondPosition[0] && p[1]==secondPosition[1])
{
for(int k=0;k<whitePieces.length;k++)
{
if(isWhite && piece==whitePieces[k]) return;
else if(!isWhite && piece==blackPieces[k]) return;
}
}
}
}
}
isMoveValid=true;
}
}
/*----------------KING VALIDATOR CLASS ENDS---------------*/
/*--------------------PAWN VALIDATOR CLASS--------------------*/
class PawnValidator extends Thread
{
int [] firstPosition;
int [] secondPosition;
boolean isWhite;
boolean isMoveValid;
public void setPositionAndTurn(int r1,int c1,int r2,int c2,boolean isWhite)
{
this.firstPosition=new int[2];
this.firstPosition[0]=r1;
this.firstPosition[1]=c1;
this.secondPosition=new int[2];
this.secondPosition[0]=r2;
this.secondPosition[1]=c2;
this.isWhite=isWhite;
}
public PawnValidator()
{
}
public boolean isMoveValid()
{
return isMoveValid;
}
public int [][] getPossiblePositions(int [] position,boolean whiteTurn)
{
if(position==null) return null;
int possiblePositions[][]=new int [4][2];
int i;
int j;
for(i=0;i<possiblePositions.length;i++) for(j=0;j<possiblePositions[i].length;j++) possiblePositions[i][j]=-1;
int u=0;
int v=0;
i=position[0];
j=position[1];
if(whiteTurn)
{
if(j+1<8)
{
if(i+1<8)
{
if(chessBoardModel.board[i+1][j+1]==0 || (chessBoardModel.board[i+1][j+1]!=0 && chessBoardModel.board[i+1][j+1]>=1 && chessBoardModel.board[i+1][j+1]<=16));
else if(chessBoardModel.board[i+1][j+1]>=17 && chessBoardModel.board[i+1][j+1]<=32 && chessBoardModel.board[i+1][j+1]!=29)
{
possiblePositions[u][0]=i+1;
possiblePositions[u][1]=j+1;
u++;
}
}
}
if(j-1>-1)
{
if(i+1<8)
{
if(chessBoardModel.board[i+1][j-1]==0 || (chessBoardModel.board[i+1][j-1]!=0 && chessBoardModel.board[i+1][j-1]>=1 && chessBoardModel.board[i+1][j-1]<=16));
else if(chessBoardModel.board[i+1][j-1]>=17 && chessBoardModel.board[i+1][j-1]<=32 && chessBoardModel.board[i+1][j-1]!=29)
{
possiblePositions[u][0]=i+1;
possiblePositions[u][1]=j-1;
u++;
}
}
}
if(i+1<8)
{
if(chessBoardModel.board[i+1][j]!=0);
else
{
possiblePositions[u][0]=i+1;
possiblePositions[u][1]=j;
u++;
}
}
}
else
{
if(j+1<8)
{
if(i-1>-1)
{
if(chessBoardModel.board[i-1][j+1]==0 || (chessBoardModel.board[i-1][j+1]!=0 && chessBoardModel.board[i-1][j+1]>=17 && chessBoardModel.board[i-1][j+1]<=32));
else if(chessBoardModel.board[i-1][j+1]>=1 && chessBoardModel.board[i-1][j+1]<=16 && chessBoardModel.board[i-1][j+1]!=5)
{
possiblePositions[u][0]=i-1;
possiblePositions[u][1]=j+1;
u++;
}
}
}
if(j-1>-1)
{
if(i-1>-1)
{
if(chessBoardModel.board[i-1][j-1]==0 || (chessBoardModel.board[i-1][j-1]!=0 && chessBoardModel.board[i-1][j-1]>=17 && chessBoardModel.board[i-1][j-1]<=32));
else if(chessBoardModel.board[i-1][j-1]>=1 && chessBoardModel.board[i-1][j-1]<=16 && chessBoardModel.board[i-1][j-1]!=5)
{
possiblePositions[u][0]=i-1;
possiblePositions[u][1]=j-1;
u++;
}
}
}
if(i-1>-1)
{
if(chessBoardModel.board[i-1][j]!=0);
else
{
possiblePositions[u][0]=i-1;
possiblePositions[u][1]=j;
u++;
}
}
}
if(whiteTurn && i==1)
{
if(chessBoardModel.board[i+2][j]!=0);
else
{
possiblePositions[u][0]=i+2;
possiblePositions[u][1]=j;
u++;
}
}
else if(!whiteTurn && i==6)
{
if(chessBoardModel.board[i-2][j]!=0);
else
{
possiblePositions[u][0]=i-2;
possiblePositions[u][1]=j;
u++;
}
}
//for(i=0;i<possiblePositions.length;i++) System.out.println("Pawn Position : "+possiblePositions[i][0]+" "+possiblePositions[i][1]);
return possiblePositions;
}
public int [][] getValidPositions(int []firstPosition,int []secondPosition,boolean isWhite)
{
int validPositions[][]=new int [4][2];
int i;
int j;
for(i=0;i<validPositions.length;i++) for(j=0;j<validPositions[i].length;j++) validPositions[i][j]=-1;
int u=0;
int v=0;
i=secondPosition[0];
j=secondPosition[1];
if(isWhite)
{
if(j+1<8)
{
if(i+1<8)
{
validPositions[u][0]=i+1;
validPositions[u][1]=j+1;
u++;
}
}
if(j-1>-1)
{
if(i+1<8)
{
v=0;
validPositions[u][0]=i+1;
validPositions[u][1]=j-1;
u++;
}
}
if(i+1<8)
{
validPositions[u][0]=i+1;
validPositions[u][1]=j;
u++;
}
}
else
{
if(j+1<8)
{
if(i-1>-1)
{
validPositions[u][0]=i-1;
validPositions[u][1]=j+1;
u++;
}
}
if(j-1>-1)
{
if(i-1>-1)
{
validPositions[u][0]=i-1;
validPositions[u][1]=j-1;
u++;
}
}
if(i-1>-1)
{
validPositions[u][0]=i-1;
validPositions[u][1]=j;
u++;
}
}
if(isWhite && i==1)
{
validPositions[u][0]=i+2;
validPositions[u][1]=j;
u++;
}
else if(!isWhite && i==6)
{
validPositions[u][0]=i-2;
validPositions[u][1]=j;
u++;
}
return validPositions;
}
public void run()
{
int validPositions[][]=new int [4][2];
int i;
int j;
for(i=0;i<validPositions.length;i++) for(j=0;j<validPositions[i].length;j++) validPositions[i][j]=-1;
int u=0;
int v=0;
i=firstPosition[0];
j=firstPosition[1];
if(isWhite)
{
if(j+1<8)
{
if(i+1<8)
{
validPositions[u][v++]=i+1;
validPositions[u][v]=j+1;
u++;
}
}
v=0;
if(j-1>-1)
{
if(i+1<8)
{
v=0;
validPositions[u][v++]=i+1;
validPositions[u][v]=j-1;
u++;
}
}
v=0;
if(i+1<8)
{
validPositions[u][v++]=i+1;
validPositions[u][v]=j;
u++;
}
}
else
{
if(j+1<8)
{
if(i-1>-1)
{
v=0;
validPositions[u][v++]=i-1;
validPositions[u][v]=j+1;
u++;
}
}
v=0;
if(j-1>-1)
{
if(i-1>-1)
{
v=0;
validPositions[u][v++]=i-1;
validPositions[u][v]=j-1;
u++;
}
}
v=0;
if(i-1>-1)
{
validPositions[u][v++]=i-1;
validPositions[u][v]=j;
u++;
}
}
if(isWhite && i==1)
{
v=0;
validPositions[u][v++]=i+2;
validPositions[u][v]=j;
u++;
}
else if(!isWhite && i==6)
{
v=0;
validPositions[u][v++]=i-2;
validPositions[u][v]=j;
u++;
}
boolean isPositionValid=false;
for(i=0;i<validPositions.length;i++)
{
j=0;
if(secondPosition[0]==validPositions[i][j] && secondPosition[1]==validPositions[i][j+1])
{
isPositionValid=true;
break;
}
}
if(!isPositionValid) return;
byte [] whitePieces={1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
byte [] blackPieces={17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32};
byte piece;
int [] p=new int[2];
u=firstPosition[0];
v=firstPosition[1];
int x,y;
for(x=0;x<8;x++)
{
for(y=0;y<8;y++)
{
piece=chessBoardModel.board[x][y];
p[0]=x;
p[1]=y;
for(i=0;i<validPositions.length;i++) 
{
j=0;
if(p[0]==validPositions[i][j] && p[1]==validPositions[i][j+1] && p[0]==secondPosition[0] && p[1]==secondPosition[1])
{
if(isWhite && p[0]==u+1 && p[1]==v+1)
{
if(piece==0)
{
if(chessBoardModel.board[u][v+1]>=17 && chessBoardModel.board[u][v+1]<=24 && u==3)
{
chessBoardModel.board[u][v+1]=0;
chessBoardModel.pieces[u][v+1].setIcon(null);
isMoveValid=true;
}
return;
}
}
else if(isWhite && p[0]==u+1 && p[1]==v-1)
{
if(piece==0)
{
if(chessBoardModel.board[u][v-1]>=17 && chessBoardModel.board[u][v-1]<=24 && u==3)
{
chessBoardModel.board[u][v-1]=0;
chessBoardModel.pieces[u][v-1].setIcon(null);
isMoveValid=true;
}
return;
}
}
else if(!isWhite && p[0]==u-1 && p[1]==v+1)
{
if(piece==0)
{
if(chessBoardModel.board[u][v+1]>=9 && chessBoardModel.board[u][v+1]<=16 && u==4)
{
chessBoardModel.board[u][v+1]=0;
chessBoardModel.pieces[u][v+1].setIcon(null);
isMoveValid=true;
}
return;
}
}
else if(!isWhite && p[0]==u-1 && p[1]==v-1)
{
if(piece==0)
{
if(chessBoardModel.board[u][v+1]>=9 && chessBoardModel.board[u][v+1]<=16 && u==4)
{
chessBoardModel.board[u][v-1]=0;
chessBoardModel.pieces[u][v-1].setIcon(null);
isMoveValid=true;

}
return;
}
}
for(int k=0;k<whitePieces.length;k++)
{
if(isWhite && piece==whitePieces[k]) return;
if(!isWhite && piece==blackPieces[k]) return;
}
}
}
}
}
isMoveValid=true;
}
}
/*--------------------PIECE VALIDATOR CLASSES ENDS--------------------*/
}//outer class ends