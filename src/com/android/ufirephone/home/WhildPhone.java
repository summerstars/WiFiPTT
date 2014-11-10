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
	private SensorManager mSensorManager;												// ���������Sensor������	
	private SweepRadar sw = null;
	private boolean sweepradarrun = false;												//�����״��߳�
	
	private LocationManager lm;															//λ�ù�����
	private MediaButtonReceiver mediaButtonReceiver = null;								//�߿ع㲥������
	
	private Person me = new Person();													//��¼������Ϣ
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wildphone); 

		MainActivity.whildphone = this;

		getMyInfomation();																//��ȡ������Ϣ
		MuticalAudioStart();															//�����鲥����
		MuticalConlProtStart();															//�����鲥���ݽ���
		QuickTalkBtnListen();															//ͨ����ť����
		MediaBtnListen();																//�߿ذ�ť����

		sw = (SweepRadar) findViewById(R.id.RadarView);
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);				// ��ȡ����Ĵ������������

		MuticalFileStart();																//�ಥ�ļ�����	
		InitGPS();																		//��ʼ��GPS
		StartRadar();
		
	}
	@Override
	protected void onResume() 
	{
		super.onResume();
		
		mSensorManager.registerListener(this,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
				SensorManager.SENSOR_DELAY_GAME);										// Ϊϵͳ�ķ��򴫸���ע�������
	}	
	@Override
	protected void onPause() 
	{
		super.onPause();
		
		mSensorManager.unregisterListener(this);										// ȡ��ע��
	}
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) 
	{

	}	
	@Override
	public void onSensorChanged(SensorEvent event) 
	{	
		if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) 							// ����ˮƽ����������
		{
			float degree = event.values[0];												// ��ȡ��Z��ת���ĽǶ�		
			sw.SetCompassDegree(degree);
		}
	}  
	@Override
    protected void onDestroy() 
    {
    	super.onDestroy();  
        mAudioManager.unregisterMediaButtonEventReceiver(mb); 							//ȡ��ע�� 
        MainActivity.mutilconlprotP.MultiConlProt(false);
        StopRadar();
    }
	@Override
	public boolean onKeyDown(int keyCode , KeyEvent event)
	{
		
        switch(keyCode)
        {  
            case KeyEvent.KEYCODE_HEADSETHOOK:								//���Ż���ͣ
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:							//���Ż���ͣ
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
            case KeyEvent.KEYCODE_HEADSETHOOK:								//���Ż���ͣ
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:							//���Ż���ͣ
            	MainActivity.mutilTalkP.MultiAudioToTalk(false);
            	
            	break;
        }
		
		return true;
	}
	
	private void getMyInfomation()														//������ѵ������Ϣ
	{
		me.personId = (int)System.currentTimeMillis();
		me.personHeadIconId = 2;
		me.personNickeName = "HANSE";
		me.ipAddress = "192.168.1.1";
	}
	private void MuticalAudioStart()													//�����鲥����
	{
		if( MainActivity.mutilTalkP == null)											//���鲥�ر������¿���
		{
			WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
			MainActivity.mutilTalkP = new MulticComm(wifiManager,"255.255.255.255",9081);       			
		}
		MainActivity.isUserMuticalTalk = true;
		MainActivity.mutilTalkP.MultiMask((byte)1, (byte)1);
		MainActivity.mutilTalkP.MultiSetID(1);
		MainActivity.mutilTalkP.MultiAudioStart();
	}
	private void MuticalConlProtStart()													//�����鲥ͨ��
	{
		if( MainActivity.mutilconlprotP == null)										//���鲥�ر������¿���
		{
			WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
			MainActivity.mutilconlprotP = new MulticComm(wifiManager,"255.255.255.255",8090);       			
		}
		MainActivity.mutilconlprotP.MultiMask((byte)1, (byte)1);
		MainActivity.mutilconlprotP.MultiSetID(1);
		
		MainActivity.mutilconlprotP.SetInfomation(me);
		MainActivity.mutilconlprotP.MultiConlProt(true);
	}
	private void MuticalFileStart()														//�����鲥�ļ�����
	{
		if( MainActivity.mutilFileP == null)											//���鲥�ر������¿���
		{
			WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
			MainActivity.mutilFileP = new MulticComm(wifiManager,"255.255.255.255",9083);       			
		}
		MainActivity.mutilFileP.MultiMask((byte)1, (byte)1);
		MainActivity.mutilFileP.MultiSetID(1);
		
		MainActivity.mutilFileP.MultiSendFileStart("/sdcard/wrildufirephone/1.png",200);
		MainActivity.mutilFileP.MultiReceiveFileStart(1, 22479, "/sdcard/wrildufirephone/2.png");
	}
	private void MediaBtnListen()														//�߿ذ�ť����(��onKeyDown/Up�д����������㲥������ʵ��û����)
	{
		mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        mb = new ComponentName(getPackageName(),MediaButtonReceiver.class.getName());
        mAudioManager.registerMediaButtonEventReceiver(mb);								//ע�������
        //unregisterReceiver(mediaButtonReceiver); 
	}
	private void QuickTalkBtnListen()													//ͨ����ť����
	{
		Button QuickTalk_Btn = (Button)findViewById(R.id.Talk_btn);						
		OnTouchListener imageButtonTouchListener = null;								//ͨ����ť����
		imageButtonTouchListener = new OnTouchListener() 								
		{
			public boolean onTouch(View v, MotionEvent event) 
			{
				switch (event.getAction()) 
				{
			
					case MotionEvent.ACTION_DOWN:										//��ס�¼�������ִ�д��������
					{
						Iamhere("QuickTalk_Btn DOWN ...");
						MainActivity.mutilTalkP.MultiAudioToTalk(true);
						UpDataSweepRadar();
						
						break;
					}
					case MotionEvent.ACTION_MOVE:										//�ƶ��¼�������ִ�д��������
					{
						
						break;
					}
					case MotionEvent.ACTION_UP:											//�ɿ��¼�������ִ�д��������
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
	private void InitGPS()																//��ʼ����λ
	{
		 lm=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
	        
	        
		 if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER))						//�ж�GPS�Ƿ���������
		 {
			 Toast.makeText(this, "�뿪��GPS����...", Toast.LENGTH_SHORT).show();
			 Iamhere("GPS is not open!");
			 Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS); 		//���ؿ���GPS�������ý���  
			 startActivityForResult(intent,0); 

			 return;
		 }
		 Criteria criteria = new Criteria();
		 criteria.setAccuracy(Criteria.ACCURACY_FINE);									//Criteria.ACCURACY_COARSE�Ƚϴ��ԣ�Criteria.ACCURACY_FINE��ȽϾ�ϸ     
		 criteria.setSpeedRequired(false);												//�����Ƿ�Ҫ���ٶ�
		 criteria.setCostAllowed(false);												//�����Ƿ�������Ӫ���շ�
		 criteria.setBearingRequired(false);											//�����Ƿ���Ҫ��λ��Ϣ
		 criteria.setAltitudeRequired(false);											//�����Ƿ���Ҫ������Ϣ
		 criteria.setPowerRequirement(Criteria.POWER_LOW);								//���öԵ�Դ������  
		 String bestProvider = lm.getBestProvider(criteria, true);						//Ϊ��ȡ����λ����Ϣʱ���ò�ѯ����	        
		 Location location= lm.getLastKnownLocation(bestProvider); 						//��ȡλ����Ϣ   
		 UpDateGps(location);
		 lm.addGpsStatusListener(														//����״̬
				 new GpsStatus.Listener() 
				 {
			        public void onGpsStatusChanged(int event) 
			        {
			            switch (event) 
			            {
				            
				            case GpsStatus.GPS_EVENT_FIRST_FIX:								//��һ�ζ�λ
				                Iamhere("��һ�ζ�λ");
				                break;
				           
				            case GpsStatus.GPS_EVENT_SATELLITE_STATUS: 						//����״̬�ı�
				            	Iamhere("����״̬�ı�");
				                
				                GpsStatus gpsStatus=lm.getGpsStatus(null);					//��ȡ��ǰ״̬
				                
				                int maxSatellites = gpsStatus.getMaxSatellites();			//��ȡ���ǿ�����Ĭ�����ֵ
				                
				                Iterator<GpsSatellite> iters = gpsStatus.getSatellites().iterator();//����һ�������������������� 
				                int count = 0;     
				                while (iters.hasNext() && count <= maxSatellites) 
				                {     
				                    GpsSatellite s = iters.next();     
				                    count++;     
				                }   
				                Iamhere("��������"+count+"������");
				                break;
				            
				            case GpsStatus.GPS_EVENT_STARTED:								//��λ����
				            	Iamhere("��λ����");
				                break;
				            
				            case GpsStatus.GPS_EVENT_STOPPED:								//��λ����
				            	Iamhere("��λ����");
				                break;
			            }
			        };
		    });	
		 
		 lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5*1000, 1, 			//���ü�����ʱ��Ϊ�������Сλ�Ʊ仯	
				 new LocationListener() 
		 		{
		        	public void onLocationChanged(Location location) 
		        	{
		        		UpDateGps(location);
			            Iamhere("ʱ�䣺"+location.getTime()); 
			            Iamhere("���ȣ�"+location.getLongitude()); 
			            Iamhere("γ�ȣ�"+location.getLatitude()); 
			            Iamhere("���Σ�"+location.getAltitude()); 
		        	}
		        	public void onStatusChanged(String provider, int status, Bundle extras) 
		        	{
		            switch (status) 
		            {
			            case LocationProvider.AVAILABLE:								//GPS״̬Ϊ�ɼ�ʱ
			            	Iamhere("��ǰGPS״̬Ϊ�ɼ�״̬");
			                break;
			            
			            case LocationProvider.OUT_OF_SERVICE:							//GPS״̬Ϊ��������ʱ
			            	Iamhere("��ǰGPS״̬Ϊ��������״̬");
			                break;
			            
			            case LocationProvider.TEMPORARILY_UNAVAILABLE:					//GPS״̬Ϊ��ͣ����ʱ
			            	Iamhere("��ǰGPS״̬Ϊ��ͣ����״̬");
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
    private void UpDateGps(Location location)											//λ�ø��µ��ú���
    {
        if(location!=null)
        {
        	me.longitude = location.getLongitude();
        	me.latitude  = location.getLatitude();
        	MainActivity.mutilconlprotP.SetInfomation(me);								//ˢ�±���λ����Ϣ       	
    		sw.SetCompassSelfNode(me);													//���״������ñ���	
        }
        else
        {
        	
        }
    }
    private void UpDataSweepRadar()
    {
		int size;
		Person pr = new Person();
		size = MainActivity.mutilconlprotP.GetPersonListSize();							//��ȡ�û��б��С
		for(int i=0;i<size;i++)
		{
			MainActivity.mutilconlprotP.GetPersonByNum(i, pr);							//����Ż�ȡ�û���Ϣ
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
	class UpDataRadar extends Thread													//��ʱˢ���û��߳�
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
    public void onBackPressed()															//˫�����˳����� 
    {
    	if (mBackKeyPressedTimes == 0) 
    	{
    		Toast.makeText(this, "�ٰ�һ���˳����� ", Toast.LENGTH_SHORT).show();
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

	public void Iamhere(String s)														//���б�Ƿ���
	{
		Log.d(TAG, s);
		//Toast.makeText(this, "Welcome" + "���:" + s, Toast.LENGTH_SHORT).show();
	}		
}

