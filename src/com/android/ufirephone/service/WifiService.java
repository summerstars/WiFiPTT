package com.android.ufirephone.service;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Binder;
import android.os.IBinder;
import android.content.ContextWrapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.preference.PreferenceManager;
import android.widget.Toast;
import java.io.UnsupportedEncodingException;
import java.net.DatagramSocket;
import java.net.SocketException;
import android.util.Log;
import android.os.Looper;
import android.database.Cursor;
import java.io.ByteArrayInputStream;  
import java.io.ByteArrayOutputStream;


import android.content.ContentResolver;


//����WIFI����ȡ������������Ϣ(����Ӳ������)
public class WifiService extends Service
{
	static boolean checkflag 					= false;				//������Ϣ����߳̿�����־
	static boolean wififlag 					= true;					//WIFI������־
	static public InetAddress localInetAddress 	= null;
	static public byte[] localIpBytes	 		= null;
	static public String localIp 				= null;
	
	private WifiManager wifiManager = null;								//WIFI����
	private ServiceBinder sBinder = new ServiceBinder();
	
	
	@Override
	public void onCreate() 
	{
		super.onCreate();
		
		wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		if(!checkflag)
		{
			new CheckNetworkConnectivity().start();									//�������״̬����ȡIP��ַ
			checkflag = true;
		}
		Iamhere("Wifi Service Created...");
	}
	@Override
	public IBinder onBind(Intent arg0) 
	{
		Iamhere("Service onBinded...");
		return sBinder;
	}
	
	
	
	public class ServiceBinder extends Binder									//�����ⲿ��ȡ����
	{
		public WifiService getService()
		{
			return WifiService.this;
		}
	}
	
	public boolean GetNetStat()													//��ȡWIFI����״̬
	{
		if(wifiManager.isWifiEnabled())
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public boolean TurnOnWifi()													//����WIFI
	{
		if (!wifiManager.isWifiEnabled()) 
		{
			wifiManager.setWifiEnabled(true);
			wififlag = true;
		}
		return true;
	}
	
	public boolean TurnOffWifi()												//�ر�WIFI
	{
		if (wifiManager.isWifiEnabled()) 
		{
			wifiManager.setWifiEnabled(false);
			wififlag = false;
		}
		return true;		
	}
	
	public InetAddress GetInetAddress() 										//��ȡInetAddress
	{

		return localInetAddress;
	}
	
	public String GetStringAddress()											//��ȡString��ַ
	{
		
		return localIp;
	}
	
	private class CheckNetworkConnectivity extends Thread							//�����������״̬,��ñ���IP��ַ 
	{
		public void run() 
		{
			try 
			{
				if (!wifiManager.isWifiEnabled() && wififlag) 
				{
					Iamhere("Wifi Service Turn Wifi on...");
					wifiManager.setWifiEnabled(true);
				}	
				for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) 
				{
					NetworkInterface intf = en.nextElement();
					for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) 
					{
						InetAddress inetAddress = enumIpAddr.nextElement();
						if (!inetAddress.isLoopbackAddress()) 
						{
							localInetAddress = inetAddress;
							localIp = inetAddress.getHostAddress().toString();
							localIpBytes = inetAddress.getAddress();
							Iamhere("Wifi Service Get ip is "+localIp+" ...");
						}
					}
				}
					
			} 
			catch (Exception ex) 
			{
				ex.printStackTrace();
			}
		};
	};
	
	

	void Iamhere(String str)													//�������
	{
		Log.d("DEBUGTAG", "Welcom:Wifi Service " + str);
		//System.out.println("Welcom:Wifi Service " + str);
		//Toast.makeText(getApplicationContext(),"Welcom:Wifi Service " + str, Toast.LENGTH_SHORT).show();
	}
	


}
