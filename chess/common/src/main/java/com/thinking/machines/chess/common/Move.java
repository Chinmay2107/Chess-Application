package com.thinking.machines.chess.common;
public class Move implements java.io.Serializable
{
public byte player;
public byte piece;
public byte fromX,fromY;
public byte toX,toY;
public boolean isLastMove; //Check Mate
}