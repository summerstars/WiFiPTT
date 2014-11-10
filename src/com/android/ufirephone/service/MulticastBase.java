package com.android.ufirephone.service;


import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import android.net.wifi.WifiManager;
import android.util.Log;
import java.util.Arrays;
import com.android.ufirephone.util.Constant;
import android.app.Activity;

//收发比特流
public class MulticastBase
{
	private static String TAG = "ePTT[MulticastBase]";
	private WifiManager wifimanager 		= null;
	private WifiManager.MulticastLock lock;
	private MulticastSocket multicastSocket = null;
	private String groupadds 				= Constant.MULTICAST_IP;					//组播地址
	private int groupport 					= Constant.PPORT;							//组播端口	
	private static final int BUFSIZE 		= 2048;
	private byte[] recvBuffer			 	= new byte[BUFSIZE];	
	private InetAddress destAddress 		= null;
	
	
	public MulticastBase()
	{
		
		
	}	
	public MulticastBase(WifiManager wifiManager,String adds,int port)					//构造函数
	{
		Iamhere("MulticastHelper building ...");
		
		if(adds != null)
		{
			groupadds = adds;
		}
		if(port != 0)
		{
			groupport = port;
		}
		
		this.wifimanager = wifiManager;
		try
		{
			lock= wifimanager.createMulticastLock("UDPwifi");
			if (!wifimanager.isWifiEnabled())
				wifimanager.setWifiEnabled(true);
			destAddress = InetAddress.getByName(groupadds);				
			if(!destAddress.isMulticastAddress())										//检测该地址是否是多播地址 
				 Iamhere("destAddress is not MulticastAddress!");
			multicastSocket = new MulticastSocket(port);
			multicastSocket.joinGroup(InetAddress.getByName(groupadds));
			multicastSocket.setTimeToLive(4);											//64组播到本地区
			multicastSocket.setLoopbackMode(true);										//不接受自己的包
			Iamhere("MulticastHelper build ..."); 
			
		}
		catch (Exception e)
		{
			SocketException();
			e.printStackTrace();
		}			
	}
	public void SocketException()														//异常处理
	{
		Iamhere("MulticastHelper exception ...");
		
		try 
		{
			if(null!=multicastSocket && !multicastSocket.isClosed())
			{
				multicastSocket.leaveGroup(InetAddress.getByName(groupadds));
				multicastSocket.close();
			}
		} catch (Exception e1) 
		{
			Iamhere("MulticastHelper exception ERR...");
			e1.printStackTrace();
		}		
	}
	
	public void MulticastSend(byte[] buf,int length)									//多播发送数据
	{
		Iamhere("MulticastHelper sending ...");
		try
		{
			if(null!=multicastSocket && !multicastSocket.isClosed())
			{
				DatagramPacket packet;
				packet = new DatagramPacket(buf, length, destAddress,groupport); 
				multicastSocket.send(packet);
			}
		}
		catch (Exception e)
		{
			SocketException();
			e.printStackTrace();
		}			
	}
	public int MulticastReceive(byte[] buf,int reqlength)								//多播接收数据
	{
		Iamhere("MulticastHelper receiveing ...");
		try
		{
			lock.acquire();
			Arrays.fill(recvBuffer,(byte)0);
			DatagramPacket rdp = new DatagramPacket(recvBuffer, recvBuffer.length);
        	multicastSocket.receive(rdp);
        	int getlength = 0;
        	if(reqlength <= rdp.getLength())
        	{
        		getlength = reqlength;
        		System.arraycopy(recvBuffer, 0, buf, 0, getlength);
        	}
        	else
        	{
        		getlength = rdp.getLength();
        		System.arraycopy(recvBuffer, 0, buf, 0, getlength);
        	}
        	
        	String strMsg=new String(rdp.getData()).toString().trim();
            Iamhere(rdp.getAddress().getHostAddress().toString() + ":" +strMsg );
            lock.release();
            
            return getlength;
		}
		catch (Exception e)
		{
			SocketException();
			e.printStackTrace();
			return -1;
		}		
	}

	private void Iamhere(String s)														//标记方法
	{
		Log.d(TAG, "Multicast: " + s);
		//Toast.makeText(this, "Welcome", "标记:" + "MuticalCommunication " + s, Toast.LENGTH_SHORT).show();
	}

}