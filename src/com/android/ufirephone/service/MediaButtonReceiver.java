package com.android.ufirephone.service;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;



import com.android.ufirephone.home.WhildPhone;
import com.android.ufirephone.home.MainActivity;

public class MediaButtonReceiver extends BroadcastReceiver 
{  
	
	private static String TAG = "ePTT[MediaButtonReceiver]";
	private static boolean mutilTalkFlag = false;								//组播开启标志
	
	
    @Override  
    public void onReceive(Context context, Intent intent) 
    {
        String intentAction = intent.getAction(); 								// 获得Action  
        KeyEvent keyEvent = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

        if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) 
        {
              
            int keyCode = keyEvent.getKeyCode();								// 获得按键字节码               
            int keyAction = keyEvent.getAction();								// 按下 / 松开 按钮           
            long eventTime = keyEvent.getEventTime()-keyEvent.getDownTime();	// 获得事件的时间
            boolean isLongPress = (eventTime>1000);
            

            if(keyAction == KeyEvent.ACTION_UP)
            {
            	if(mutilTalkFlag == false)
            	{
            		//MainActivity.whildphone.Iamhere("KEY PLAY IS PUSH");
            		//MainActivity.mutilTalkP.MultiAudioToTalk(true);
            		mutilTalkFlag = true;
            	}
            	else
            	{
                	//MainActivity.whildphone.Iamhere("KEY RES");
                	//MainActivity.mutilTalkP.MultiAudioToTalk(false);
                	mutilTalkFlag = false;
            	}
            	
            	return;
            }

            switch(keyCode)
            {  
	            case KeyEvent.KEYCODE_HEADSETHOOK:								//播放或暂停
	            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:							//播放或暂停
	                if(isLongPress)
	                {
                	
	                }
	                else
	                {
	                	
	                }
	                break;
	                
	            case KeyEvent.KEYCODE_MEDIA_NEXT:								//下一首键   
	            	
	                break;  
	                  
	              
	            case KeyEvent.KEYCODE_MEDIA_PREVIOUS: 							//上一曲键

	            	 break;
	            	 
	            default:
	            	
	            	break;
            }
        }  
          
    }
    
}  