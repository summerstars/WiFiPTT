package com.android.ufirephone.util;

import java.io.Serializable;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.DisplayMetrics;

public class Person implements Serializable
{
	private static final long serialVersionUID 	= 1L;
	public int personId 						= 0;
	public int personHeadIconId 				= 0;
	public int groupId 							= 0;
	public String personNickeName 				= null;
	public String ipAddress 					= null;
	public String loginTime 					= null;
	public long timeStamp 						= 0;
	public int  range							= 500;                					//ÏÔÊ¾·¶Î§°ë¾¶
	public double longitude						= 0;
	public double latitude						= 0;
	public byte  direction 						= 0;
	public boolean changed 						= false;
	
	public byte channel 						= 0;									//A ~ Z   (26) 
	public byte point							= 0;
	
	public Person(int personId,int personHeadIconId,String personNickeName,String ipAddress,String loginTime)
	{
		this.personId = personId;
		this.personHeadIconId = personHeadIconId;
		this.personNickeName = personNickeName;
		this.ipAddress = ipAddress;
		this.loginTime = loginTime;
	}
	public Person(){}

	public void CopyFrom(Person p)														//¿½±´×ÔPerson
	{
    	this.personId 			= p.personId;
    	this.personHeadIconId 	= p.personHeadIconId;
    	this.groupId			= p.groupId;
    	this.personNickeName 	= p.personNickeName;
    	this.ipAddress 			= p.ipAddress;
    	this.loginTime 			= p.loginTime;	
    	this.timeStamp			= p.timeStamp;
    	this.groupId			= p.groupId;
    	this.range				= p.range;
    	this.longitude			= p.longitude;
    	this.latitude			= p.latitude;
    	this.direction			= p.direction;
    	
		
	}
	public void CopyTo(Person p)														//¿½±´µ½Person
	{
    	p.personId 			= this.personId;
    	p.personHeadIconId 	= this.personHeadIconId;
    	p.groupId			= this.groupId;
    	p.personNickeName 	= this.personNickeName;
    	p.ipAddress 		= this.ipAddress;
    	p.loginTime 		= this.loginTime;
    	p.groupId			= this.groupId;
    	p.range				= this.range;
    	p.longitude			= this.longitude;
    	p.latitude			= this.latitude;
    	p.direction			= this.direction;
		
	}
}
