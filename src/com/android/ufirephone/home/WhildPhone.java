package com.android.ufirephone.home;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import com.android.ufirephone.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.media.AudioManager;
import android.content.ComponentName;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


















import com.android.ufirephone.service.MediaButtonReceiver;
import com.android.ufirephone.home.MainActivity;
import com.android.ufirephone.service.*;
import com.android.ufirephone.util.*;


public class WhildPhone  extends Activity implements SensorEventListener
{
	
	private static String TAG = "ePTT[WhildPhone]";
    public static AudioManager mAudioManager = null;
    public static ComponentName mb = null;
	private SensorManager mSensorManager;												// 定义真机的Sensor管理器	
	private SweepRadar sw = null;
	private boolean sweepradarrun = false;												//开启雷达线程
	
	private LocationManager lm;															//位置管理器
	private MediaButtonReceiver mediaButtonReceiver = null;								//线控广播接收器
	
	private Person me = new Person();													//记录本机信息
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wildphone); 

		MainActivity.whildphone = this;

		getMyInfomation();																//获取本机信息
		MuticalAudioStart();															//开启组播语音
		MuticalConlProtStart();															//开启组播数据交互
		QuickTalkBtnListen();															//通话按钮监听
		MediaBtnListen();																//线控按钮监听

		sw = (SweepRadar) findViewById(R.id.RadarView);
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);				// 获取真机的传感器管理服务

		MuticalFileStart();																//多播文件传输	
		InitGPS();																		//初始化GPS
		StartRadar();
		
	}
	@Override
	protected void onResume() 
	{
		super.onResume();
		
		mSensorManager.registerListener(this,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
				SensorManager.SENSOR_DELAY_GAME);										// 为系统的方向传感器注册监听器
	}	
	@Override
	protected void onPause() 
	{
		super.onPause();
		
		mSensorManager.unregisterListener(this);										// 取消注册
	}
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) 
	{

	}	
	@Override
	public void onSensorChanged(SensorEvent event) 
	{	
		if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) 							// 触发水平传感器类型
		{
			float degree = event.values[0];												// 获取绕Z轴转过的角度		
			sw.SetCompassDegree(degree);
		}
	}  
	@Override
    protected void onDestroy() 
    {
    	super.onDestroy();  
        mAudioManager.unregisterMediaButtonEventReceiver(mb); 							//取消注册 
        MainActivity.mutilconlprotP.MultiConlProt(false);
        StopRadar();
    }
	@Override
	public boolean onKeyDown(int keyCode , KeyEvent event)
	{
		
        switch(keyCode)
        {  
            case KeyEvent.KEYCODE_HEADSETHOOK:								//播放或暂停
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:							//播放或暂停
        		MainActivity.mutilTalkP.MultiAudioToTalk(true);
            	
            	break;
            case KeyEvent.KEYCODE_BACK:
            	onBackPressed();
            	break;
        }
		
		return true;
	}
	@Override
	public boolean onKeyUp(int keyCode , KeyEvent event)
	{
		
        switch(keyCode)
        {  
            case KeyEvent.KEYCODE_HEADSETHOOK:								//播放或暂停
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:							//播放或暂停
            	MainActivity.mutilTalkP.MultiAudioToTalk(false);
            	
            	break;
        }
		
		return true;
	}
	
	private void getMyInfomation()														//获得自已的相关信息
	{
		me.personId = (int)System.currentTimeMillis();
		me.personHeadIconId = 2;
		me.personNickeName = "HANSE";
		me.ipAddress = "192.168.1.1";
	}
	private void MuticalAudioStart()													//开启组播语音
	{
		if( MainActivity.mutilTalkP == null)											//若组播关闭则重新开启
		{
			WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
			MainActivity.mutilTalkP = new MulticComm(wifiManager,"255.255.255.255",9081);       			
		}
		MainActivity.isUserMuticalTalk = true;
		MainActivity.mutilTalkP.MultiMask((byte)1, (byte)1);
		MainActivity.mutilTalkP.MultiSetID(1);
		MainActivity.mutilTalkP.MultiAudioStart();
	}
	private void MuticalConlProtStart()													//开启组播通信
	{
		if( MainActivity.mutilconlprotP == null)										//若组播关闭则重新开启
		{
			WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
			MainActivity.mutilconlprotP = new MulticComm(wifiManager,"255.255.255.255",8090);       			
		}
		MainActivity.mutilconlprotP.MultiMask((byte)1, (byte)1);
		MainActivity.mutilconlprotP.MultiSetID(1);
		
		MainActivity.mutilconlprotP.SetInfomation(me);
		MainActivity.mutilconlprotP.MultiConlProt(true);
	}
	private void MuticalFileStart()														//开启组播文件传输
	{
		if( MainActivity.mutilFileP == null)											//若组播关闭则重新开启
		{
			WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
			MainActivity.mutilFileP = new MulticComm(wifiManager,"255.255.255.255",9083);       			
		}
		MainActivity.mutilFileP.MultiMask((byte)1, (byte)1);
		MainActivity.mutilFileP.MultiSetID(1);
		
		MainActivity.mutilFileP.MultiSendFileStart("/sdcard/wrildufirephone/1.png",200);
		MainActivity.mutilFileP.MultiReceiveFileStart(1, 22479, "/sdcard/wrildufirephone/2.png");
	}
	private void MediaBtnListen()														//线控按钮监听(在onKeyDown/Up中处理语音，广播接收器实际没用上)
	{
		mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        mb = new ComponentName(getPackageName(),MediaButtonReceiver.class.getName());
        mAudioManager.registerMediaButtonEventReceiver(mb);								//注册监听器
        //unregisterReceiver(mediaButtonReceiver); 
	}
	private void QuickTalkBtnListen()													//通话按钮监听
	{
		Button QuickTalk_Btn = (Button)findViewById(R.id.Talk_btn);						
		OnTouchListener imageButtonTouchListener = null;								//通话按钮监听
		imageButtonTouchListener = new OnTouchListener() 								
		{
			public boolean onTouch(View v, MotionEvent event) 
			{
				switch (event.getAction()) 
				{
			
					case MotionEvent.ACTION_DOWN:										//按住事件发生后执行代码的区域
					{
						Iamhere("QuickTalk_Btn DOWN ...");
						MainActivity.mutilTalkP.MultiAudioToTalk(true);
						UpDataSweepRadar();
						
						break;
					}
					case MotionEvent.ACTION_MOVE:										//移动事件发生后执行代码的区域
					{
						
						break;
					}
					case MotionEvent.ACTION_UP:											//松开事件发生后执行代码的区域
					{
						Iamhere("QuickTalk_Btn UP ...");
						MainActivity.mutilTalkP.MultiAudioToTalk(false);
						
						break;
					}
					default:

						break;
				}
				return false;
			}
		};
		QuickTalk_Btn.setOnTouchListener(imageButtonTouchListener);		
	}	
	private void InitGPS()																//初始化定位
	{
		 lm=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
	        
	        
		 if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER))						//判断GPS是否正常启动
		 {
			 Toast.makeText(this, "请开启GPS导航...", Toast.LENGTH_SHORT).show();
			 Iamhere("GPS is not open!");
			 Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS); 		//返回开启GPS导航设置界面  
			 startActivityForResult(intent,0); 

			 return;
		 }
		 Criteria criteria = new Criteria();
		 criteria.setAccuracy(Criteria.ACCURACY_FINE);									//Criteria.ACCURACY_COARSE比较粗略，Criteria.ACCURACY_FINE则比较精细     
		 criteria.setSpeedRequired(false);												//设置是否要求速度
		 criteria.setCostAllowed(false);												//设置是否允许运营商收费
		 criteria.setBearingRequired(false);											//设置是否需要方位信息
		 criteria.setAltitudeRequired(false);											//设置是否需要海拔信息
		 criteria.setPowerRequirement(Criteria.POWER_LOW);								//设置对电源的需求  
		 String bestProvider = lm.getBestProvider(criteria, true);						//为获取地理位置信息时设置查询条件	        
		 Location location= lm.getLastKnownLocation(bestProvider); 						//获取位置信息   
		 UpDateGps(location);
		 lm.addGpsStatusListener(														//监听状态
				 new GpsStatus.Listener() 
				 {
			        public void onGpsStatusChanged(int event) 
			        {
			            switch (event) 
			            {
				            
				            case GpsStatus.GPS_EVENT_FIRST_FIX:								//第一次定位
				                Iamhere("第一次定位");
				                break;
				           
				            case GpsStatus.GPS_EVENT_SATELLITE_STATUS: 						//卫星状态改变
				            	Iamhere("卫星状态改变");
				                
				                GpsStatus gpsStatus=lm.getGpsStatus(null);					//获取当前状态
				                
				                int maxSatellites = gpsStatus.getMaxSatellites();			//获取卫星颗数的默认最大值
				                
				                Iterator<GpsSatellite> iters = gpsStatus.getSatellites().iterator();//创建一个迭代器保存所有卫星 
				                int count = 0;     
				                while (iters.hasNext() && count <= maxSatellites) 
				                {     
				                    GpsSatellite s = iters.next();     
				                    count++;     
				                }   
				                Iamhere("搜索到："+count+"颗卫星");
				                break;
				            
				            case GpsStatus.GPS_EVENT_STARTED:								//定位启动
				            	Iamhere("定位启动");
				                break;
				            
				            case GpsStatus.GPS_EVENT_STOPPED:								//定位结束
				            	Iamhere("定位结束");
				                break;
			            }
			        };
		    });	
		 
		 lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5*1000, 1, 			//设置监听器时间为间隔、最小位移变化	
				 new LocationListener() 
		 		{
		        	public void onLocationChanged(Location location) 
		        	{
		        		UpDateGps(location);
			            Iamhere("时间："+location.getTime()); 
			            Iamhere("经度："+location.getLongitude()); 
			            Iamhere("纬度："+location.getLatitude()); 
			            Iamhere("海拔："+location.getAltitude()); 
		        	}
		        	public void onStatusChanged(String provider, int status, Bundle extras) 
		        	{
		            switch (status) 
		            {
			            case LocationProvider.AVAILABLE:								//GPS状态为可见时
			            	Iamhere("当前GPS状态为可见状态");
			                break;
			            
			            case LocationProvider.OUT_OF_SERVICE:							//GPS状态为服务区外时
			            	Iamhere("当前GPS状态为服务区外状态");
			                break;
			            
			            case LocationProvider.TEMPORARILY_UNAVAILABLE:					//GPS状态为暂停服务时
			            	Iamhere("当前GPS状态为暂停服务状态");
			                break;
		            }
		        }
		        public void onProviderEnabled(String provider) 
		        {
		            Location location=lm.getLastKnownLocation(provider);
		            UpDateGps(location);
		        }
		        public void onProviderDisabled(String provider) 
		        {
		        	UpDateGps(null);
		        }
		    });
	}
    private void UpDateGps(Location location)											//位置更新调用函数
    {
        if(location!=null)
        {
        	me.longitude = location.getLongitude();
        	me.latitude  = location.getLatitude();
        	MainActivity.mutilconlprotP.SetInfomation(me);								//刷新本机位置信息       	
    		sw.SetCompassSelfNode(me);													//在雷达中设置本机	
        }
        else
        {
        	
        }
    }
    private void UpDataSweepRadar()
    {
		int size;
		Person pr = new Person();
		size = MainActivity.mutilconlprotP.GetPersonListSize();							//获取用户列表大小
		for(int i=0;i<size;i++)
		{
			MainActivity.mutilconlprotP.GetPersonByNum(i, pr);							//按序号获取用户信息
			sw.AddCompassNode(pr);
		}    	
    }
    private void StartRadar()
    {
    	UpDataRadar ur = new UpDataRadar();
    	sweepradarrun = true;
    	ur.start();
    }
    private void StopRadar()
    {
    	sweepradarrun = false;
    }
	class UpDataRadar extends Thread													//定时刷新用户线程
	{
		@Override
		public void run()
		{
			while(sweepradarrun)
			{
	            try 
	            {
					UpDataSweepRadar();
					sleep(500);
	            } 
				catch(Exception e)
				{
					e.printStackTrace();
				}

			}
		}
	}
	private int mBackKeyPressedTimes = 0;
    public void onBackPressed()															//双返回退出方法 
    {
    	if (mBackKeyPressedTimes == 0) 
    	{
    		Toast.makeText(this, "再按一次退出程序 ", Toast.LENGTH_SHORT).show();
    		mBackKeyPressedTimes = 1;
    		new Thread() 
    		{
    			@Override
    			public void run() 
    			{
    				try 
    				{
    					Thread.sleep(2000);
    				} 
    				catch (InterruptedException e) 
    				{
    					e.printStackTrace();
    				} 
    				finally 
    				{
    					mBackKeyPressedTimes = 0;
    				}
    			}
    		}.start();
    		return;
    	}
    	else
    	{
    		onDestroy();
    		this.finish();
    	}
    	super.onBackPressed();
    }

	public void Iamhere(String s)														//运行标记方法
	{
		Log.d(TAG, s);
		//Toast.makeText(this, "Welcome" + "标记:" + s, Toast.LENGTH_SHORT).show();
	}		
}

