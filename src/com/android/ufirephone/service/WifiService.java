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


//开启WIFI，获取本机的网络信息(网络硬件操作)
public class WifiService extends Service
{
	static boolean checkflag 					= false;				//本机信息检测线程开启标志
	static boolean wififlag 					= true;					//WIFI开启标志
	static public InetAddress localInetAddress 	= null;
	static public byte[] localIpBytes	 		= null;
	static public String localIp 				= null;
	
	private WifiManager wifiManager = null;								//WIFI管理
	private ServiceBinder sBinder = new ServiceBinder();
	
	
	@Override
	public void onCreate() 
	{
		super.onCreate();
		
		wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		if(!checkflag)
		{
			new CheckNetworkConnectivity().start();									//侦测网络状态，获取IP地址
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
	
	
	
	public class ServiceBinder extends Binder									//用于外部获取服务
	{
		public WifiService getService()
		{
			return WifiService.this;
		}
	}
	
	public boolean GetNetStat()													//获取WIFI开启状态
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
	
	public boolean TurnOnWifi()													//开启WIFI
	{
		if (!wifiManager.isWifiEnabled()) 
		{
			wifiManager.setWifiEnabled(true);
			wififlag = true;
		}
		return true;
	}
	
	public boolean TurnOffWifi()												//关闭WIFI
	{
		if (wifiManager.isWifiEnabled()) 
		{
			wifiManager.setWifiEnabled(false);
			wififlag = false;
		}
		return true;		
	}
	
	public InetAddress GetInetAddress() 										//获取InetAddress
	{

		return localInetAddress;
	}
	
	public String GetStringAddress()											//获取String地址
	{
		
		return localIp;
	}
	
	private class CheckNetworkConnectivity extends Thread							//检测网络连接状态,获得本机IP地址 
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
	
	

	void Iamhere(String str)													//标记语言
	{
		Log.d("DEBUGTAG", "Welcom:Wifi Service " + str);
		//System.out.println("Welcom:Wifi Service " + str);
		//Toast.makeText(getApplicationContext(),"Welcom:Wifi Service " + str, Toast.LENGTH_SHORT).show();
	}
	


}
