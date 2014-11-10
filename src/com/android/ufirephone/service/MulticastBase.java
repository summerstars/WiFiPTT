package com.android.ufirephone.service;


import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import android.net.wifi.WifiManager;
import android.util.Log;
import java.util.Arrays;
import com.android.ufirephone.util.Constant;
import android.app.Activity;

//�շ�������
public class MulticastBase
{
	private static String TAG = "ePTT[MulticastBase]";
	private WifiManager wifimanager 		= null;
	private WifiManager.MulticastLock lock;
	private MulticastSocket multicastSocket = null;
	private String groupadds 				= Constant.MULTICAST_IP;					//�鲥��ַ
	private int groupport 					= Constant.PPORT;							//�鲥�˿�	
	private static final int BUFSIZE 		= 2048;
	private byte[] recvBuffer			 	= new byte[BUFSIZE];	
	private InetAddress destAddress 		= null;
	
	
	public MulticastBase()
	{
		
		
	}	
	public MulticastBase(WifiManager wifiManager,String adds,int port)					//���캯��
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
			if(!destAddress.isMulticastAddress())										//���õ�ַ�Ƿ��Ƕಥ��ַ 
				 Iamhere("destAddress is not MulticastAddress!");
			multicastSocket = new MulticastSocket(port);
			multicastSocket.joinGroup(InetAddress.getByName(groupadds));
			multicastSocket.setTimeToLive(4);											//64�鲥��������
			multicastSocket.setLoopbackMode(true);										//�������Լ��İ�
			Iamhere("MulticastHelper build ..."); 
			
		}
		catch (Exception e)
		{
			SocketException();
			e.printStackTrace();
		}			
	}
	public void SocketException()														//�쳣����
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
	
	public void MulticastSend(byte[] buf,int length)									//�ಥ��������
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
	public int MulticastReceive(byte[] buf,int reqlength)								//�ಥ��������
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

	private void Iamhere(String s)														//��Ƿ���
	{
		Log.d(TAG, "Multicast: " + s);
		//Toast.makeText(this, "Welcome", "���:" + "MuticalCommunication " + s, Toast.LENGTH_SHORT).show();
	}

}