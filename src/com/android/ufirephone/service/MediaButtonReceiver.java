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
	private static boolean mutilTalkFlag = false;								//�鲥������־
	
	
    @Override  
    public void onReceive(Context context, Intent intent) 
    {
        String intentAction = intent.getAction(); 								// ���Action  
        KeyEvent keyEvent = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

        if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) 
        {
              
            int keyCode = keyEvent.getKeyCode();								// ��ð����ֽ���               
            int keyAction = keyEvent.getAction();								// ���� / �ɿ� ��ť           
            long eventTime = keyEvent.getEventTime()-keyEvent.getDownTime();	// ����¼���ʱ��
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
	            case KeyEvent.KEYCODE_HEADSETHOOK:								//���Ż���ͣ
	            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:							//���Ż���ͣ
	                if(isLongPress)
	                {
                	
	                }
	                else
	                {
	                	
	                }
	                break;
	                
	            case KeyEvent.KEYCODE_MEDIA_NEXT:								//��һ�׼�   
	            	
	                break;  
	                  
	              
	            case KeyEvent.KEYCODE_MEDIA_PREVIOUS: 							//��һ����

	            	 break;
	            	 
	            default:
	            	
	            	break;
            }
        }  
          
    }
    
}  