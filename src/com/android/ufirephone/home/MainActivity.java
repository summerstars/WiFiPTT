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
	static public boolean mainactivitypause = false;						//��activity������־
	
	static public MulticComm mutilTalkP	= null;								//�鲥����ͨ���߳� 
	static public MulticComm mutilconlprotP	= null;							//�鲥����ͨ���߳� 
	static public MulticComm mutilFileP		= null;							//�鲥�ļ�����
	static public boolean isUserMuticalTalk = false;						//�Ƿ�ʼ�鲥ͨ��
	static public WhildPhone whildphone = null;								//����Խ�acticity
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		
	}
	
	public void Iamhere(String s)
	{
		Log.d("Welcome", "���:" + s);
		Toast.makeText(this, "Welcome" + "���:" + s, Toast.LENGTH_SHORT).show();
	}
	
}


