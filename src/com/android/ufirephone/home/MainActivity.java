package com.android.ufirephone.home;

import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;
import android.app.TabActivity;
import com.android.ufirephone.service.*;


@SuppressWarnings("deprecation")
public class MainActivity extends TabActivity
{
	static public boolean mainactivitypause = false;						//主activity启动标志
	
	static public MulticComm mutilTalkP	= null;								//组播语音通信线程 
	static public MulticComm mutilconlprotP	= null;							//组播数据通信线程 
	static public MulticComm mutilFileP		= null;							//组播文件传输
	static public boolean isUserMuticalTalk = false;						//是否开始组播通信
	static public WhildPhone whildphone = null;								//户外对讲acticity
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		
	}
	
	public void Iamhere(String s)
	{
		Log.d("Welcome", "标记:" + s);
		Toast.makeText(this, "Welcome" + "标记:" + s, Toast.LENGTH_SHORT).show();
	}
	
}


