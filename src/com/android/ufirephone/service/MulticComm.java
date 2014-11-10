/*
 * 初始化:
 * 		MulticComm(WifiManager wifiManager,String adds,int port)		配置网络参数(ip、端口)
 * 		void MultiMask(byte channel,byte point)							设置掩码(频道、频点)
 * 		void MultiSetID(int uid)										设置用户ID
 * 		void SetInfomation(Person p)									设置本机的相关信息
 * 语音：
 * 		int MultiAudioGetID()											获取当前语音用户ID
 * 		void MultiAudioStart()											开启语音线程
 * 		void MultiAudioStop()											关闭语音线程
 * 		void MultiAudioToTalk(boolean stat)								收听/发送语音状态切换
 * 		int MultiAudioRePlay( int userid, int num)						回放某用户的某次录音(返回录音文件总数量)
 * 		boolean MultiAudioStopPlay()									停止回放
 * 		语音数据保存在 ./wrildufirephone/Audio/"用户ID"/wav/0+。wav
 * 
 * 消息：
 * 		void MultiMsgSend(String msg)									发送消息(240字节)
 * 		boolean MultiMsgReceive(byte[] buf)								接收消息(id/msg)
 * 协议收发处理：
 * 		void MultiConlProt(boolean stat)								开/关启协议解析线程，为接收方法提供数据
 * 
 * 	用户注册信息交换:
 * 		int GetPersonListSize()											获取用户列表大小
 * 		boolean GetPersonByNum(int num ,Person person)					获取num号用户，Person返回，并函数返回总用户数
 * 		boolean GetPersonByID(int id ,Person p)							获取id号用户，Person返回
 * 
 * 	透传/指令：
 * 		void MultiDataSend(byte[] buf,int reqlength)					发送透传数据
 * 		boolean MultiDataReceive(byte[] buf)							接收透传数据(id/data)
 * 		
 * 
 * 小文件传输:
 * 		boolean MultiSendFileStart(String filepath ,int delay)						小文件广播发送(需要自定义发送文件名及文件大小),delay用于延时防丢包
 * 		boolean MultiReceiveFileStart(int userid, long filesize, String filepath)	接收某人的某文件
 * 		void MultiTransferFileStop(FILEOPTYPE type)									停止文件操作
 * 		int MultiTransferFileCheck(FILEOPTYPE type)									检测文件收发进度
 * 		enum FILEOPTYPE {SEND,RECEIVE}
 *  
 * 		
 * 接口使用说明：
 * 			1)本接口文件需基类 MulticastBase 支持
 * 			2)初始化对象 MulticComm(WifiManager wifiManager,String adds,int port)
 * 			3)设置掩码 MultiMask(byte channel,byte point)
 * 			4)设置本机ID MultiSetID(int uid)
 * 			5)根据用途可选择初始化为语音接口或协议传输接口
 * 				(1)语音接口 
 * 						开启语音线程  MultiAudioStart()
 * 						在程序中根据需要调用 MultiAudioToTalk(boolean stat)
 * 						在通讯过程中需要获取当前语音用户ID时调用 	MultiAudioGetID()
 * 						结束是调用 MultiAudioStop()
 * 				(2)协议传输接口
 * 						设置本机用户信息 SetInfomation(Person p)
 * 						开启协议处理线程  MultiConlProt()
 * 						可调用 MultiMsgSend(String msg)，MultiMsgReceive(byte[] buf) 收发消息(最长240BYTE)
 * 						可调用 MultiDataSend(byte[] buf,int reqlength)  MultiDataReceive(byte[] buf) 发送透传数据(最长240BYTE)
 *						可调用GetPersonListSize()获取用户列表大小
 * 						可调用GetPersonByNum(int num ,Person person)获取列表中第num号用户信息
 * 						可调用GetPersonByID(int id ,Person p)通过id号在列表中查找用户并返回，Person返回
 * 				(3)消息传输接口
 * 						请参见(2)
 * 				(4)小文件传输接口
 * 						在开始利用API传输文件前，用户需要通过其他接口获取文件名及文件大小，这些是文件传输API所需的传入参数
 * 						开启文件传输MultiSendFileStart(String filepath ,int delay),filepath为文件绝对路径，delay为发送主动延时，防止发送太快造成丢包
 * 						接收某id/掩码的文件数据MultiReceiveFileStart(int userid, long filesize, String filepath)
 * 						文件传输过程中可通过MultiTransferFileCheck(FILEOPTYPE type)	检测文件收发进度，type为枚举型 enum FILEOPTYPE {SEND,RECEIVE}
 * 						有时可能需要中止文件的传输，可通过MultiTransferFileStop(FILEOPTYPE type)
*/
package com.android.ufirephone.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.util.Log;
import android.net.wifi.WifiManager;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.android.ufirephone.service.MulticastBase;

import xmu.swordbearer.audio.AudioCodec;

import com.android.ufirephone.util.*;

import java.io.File;  
import java.io.FileInputStream;  
import java.io.FileNotFoundException;  
import java.io.FileOutputStream;  
import java.io.IOException;

import android.os.Environment;
import android.content.SharedPreferences;


//利用比特流形成底层服务(兼容协议、带频道频点的多播语音/文字/透传)
public class MulticComm extends MulticastBase
{
	private static String TAG = "ePTT[MulticComm]";
	private boolean userisToTalk 		= false;						//在通话标记用于终止线程 
	private MultiAudioSpeek speek 		= null;
	private MultiAudioListen listen 	= null;
	private boolean multiaudioflag		= false;						//多播语音线程开启标志
	private boolean parsepackflag		= true;							//分析数据报线程开启标志
	private Person me 					= new Person();					//本机信息结构体
	private int lastAudioId				= 0;							//上一次用户ID
	private static int remoteAudioId	= 0;							//当前语音远端用户ID
	private static Map<Integer,Person> childrenMap 	= new HashMap<Integer,Person>();	//当前在线用户
	private static ArrayList<Integer> personKeys 	= new ArrayList<Integer>();			//当前在线用户id

	private byte[] regBuffer 			= new byte[Constant.bufferSize];//本机网络注册交互指令
	private byte[] msgSendBuffer 		= new byte[Constant.bufferSize];//信息交互栈
	private byte[] userCmdBuffer 		= new byte[Constant.bufferSize];//用户指令栈
	private int sbufferSize 			= 0;							//音频缓冲区大小	
	private byte[] recvBuffer 			= new byte[Constant.bufferSize];//数据报接收缓冲区
	private final static int NETPACKSIZE 	= 244;						//网络层包大小
	private final static int USERIDSIZE		= 4;						//用户ID长度
	private final static int PACKBUFSIZE 	= 128;						//网络层数据报缓冲区到校
	private class DataPackSocket										//网络层数据报原始包
	{
		public byte[] pack = new byte[NETPACKSIZE];						//原始数据段
		
		public DataPackSocket()
		{
			Arrays.fill(pack, (byte)0);
		}
		public void DataPackSetData(byte[] data,int start)				//拷贝数据
		{
			System.arraycopy(data, start, pack, 0, NETPACKSIZE);
		}
	}
	private DataPackSocket[] msgPackBuf		= new DataPackSocket[PACKBUFSIZE];
	private DataPackSocket[] cmdPackBuf		= new DataPackSocket[PACKBUFSIZE];
	private int msgPackHead 				= 0;						//消息栈顶指针
	private int cmdPackHead					= 0;						//指令栈顶指针
	private static CommunicationBridge com 	= null;						//协议处理线程
	private static UpdateMe upme 			= null;						//更新用户信息线程
	private static CheckUserOnline chkuser 	= null;						//检测用户在线线程
	
	public enum FILEOPTYPE {SEND,RECEIVE};								//文件操作类别 
	private MultiSendFile mfs 				= null;						//文件发送线程
	private MultiReceiveFile mfr 			= null;						//文件接收线程
	private int curfile 					= 0;						//当前用户录音文件位置
	private boolean replayflag 				= false;					//回放标志
	private MediaPlayer mPlayer 			= null;						//录音回放
	private HandleFile filehandle			= new HandleFile();			//文件操作
	private static final String AudioName 	= "/sdcard/wrildufirephone/Audio/";			//录音文件保存位置
	

	public MulticComm(WifiManager wifiManager,String adds,int port)						//构造函数
	{
		super(wifiManager,adds,port);
		Iamhere("MulticComm building...");
		initCmdBuffer();																//初始化各寄存器
		for(int i = 0 ;i < PACKBUFSIZE ; i++)											//初始化消息缓冲区
		{
			msgPackBuf[i] = new DataPackSocket();
			cmdPackBuf[i] = new DataPackSocket();
		}
	}
	public void MultiMask(byte channel,byte point)										//设置信道掩码
	{												
		me.channel			= channel;													//A ~ Z   (26) 													
		me.point			= point;													//0 ~ 255 (256)
		
		//用户指令
		userCmdBuffer[250] 	= channel;													//指令频道
		userCmdBuffer[251] 	= point;													//指令频点
		//通讯消息
		msgSendBuffer[250] 	= channel;																										
		msgSendBuffer[251] 	= point;
		//注册信息
		regBuffer[250] 		= channel;																										
		regBuffer[251]		= point;	
		
	}	
	public void MultiSetID(int uid)														//设置本机ID(初始化各指令缓冲区)
	{
		//userId 		=	uid;														//设置本机ID
		me.personId 	= 	uid;														//设置本机ID
		byte[] id 		=	int2ByteArray(uid);
		byte[] userid 	= 	{ 0, 0, 0, 0 };
		System.arraycopy(id, 0, userid, 0, 4);											//获取ID
		System.arraycopy(userid, 0, msgSendBuffer, 6, 4);								//设置消息缓存区用户ID
		System.arraycopy(userid, 0, userCmdBuffer, 6, 4);								//设置数据透传区用户ID
		System.arraycopy(userid, 0, regBuffer, 6, 4);
	}
    public void SetInfomation(Person p)													//设置本机的相关信息
    {
		me.CopyFrom(p);																	//拷贝信息	
		
    	//更新用户注册缓冲数据
    	System.arraycopy(int2ByteArray(me.personId), 0, regBuffer, 6, 4);
    	System.arraycopy(int2ByteArray(me.personHeadIconId), 0, regBuffer, 10, 4);
    	ByteArrayReSet(regBuffer,14,44);												//把原来的昵称内容清空
    	byte[] nickeNameBytes = me.personNickeName.getBytes();
    	System.arraycopy(nickeNameBytes, 0, regBuffer, 14, nickeNameBytes.length);

    	int ip = IpString2Int(me.ipAddress);
    	byte[] localIpBytes = int2ByteArray(ip);
    	System.arraycopy(localIpBytes,0,regBuffer,44,4);
    	byte[] longitude = putDouble(me.longitude);										//获取经度
    	System.arraycopy(longitude, 0, regBuffer, 48, 8);
    	byte[] latitude = putDouble(me.latitude);										//获取纬度
    	System.arraycopy(latitude, 0, regBuffer, 56, 8);
	
    }
    
	public int MultiAudioRePlay( int userid, int num)									//回放某用户的某次录音
	{
		if(!replayflag)																	//防止多个播放
		{  
	        try
	        {
	        	String wavpath = AudioName + "/" + userid + "/" + "wav/";
	        	String filepath = wavpath + num + ".wav";
            	File dirFile = new File(wavpath);
            	File[] files = dirFile.listFiles();
            	int curfile = files.length;		        	
	        	File wav = new File(filepath);
	        	if(!wav.exists())														//若文件不存在则返回语音文件数
	        	{
	        		Iamhere("The wav file is not exist!");
	        		
	        		return curfile;										
	        	}
	        	mPlayer = new MediaPlayer();											//录音播放
	        	mPlayer.setDataSource(filepath);
	            mPlayer.prepare();
	            replayflag = true;
	            Iamhere("Playing " + AudioName + "/" + userid + "/" + "wav/" + num + ".wav");
	            mPlayer.start();
	            replayflag = false;
	            
	            return curfile;
	        }
	        catch(IOException e)
	        {  
	            Iamhere("播放失败");  
	        }
		}
        
		return Integer.MAX_VALUE;
	}
	public boolean MultiAudioClearUp()													//清除所有音频文件缓存
	{
		filehandle.deleteDirectory(AudioName);
		return true;
	}
	public boolean MultiAudioStopPlay()													//停止回放
	{
		mPlayer.release();  
        mPlayer = null;
        replayflag = false;
        
        return true;
	}
	public int MultiAudioGetID()														//获取用户ID
	{
		
		return remoteAudioId;
	}
	public void MultiAudioToTalk(boolean stat)											//通话开关方法
	{
		if(true == stat)
		{
			userisToTalk = true;
		}
		else
		{
			userisToTalk = false;
		}
	}
	public void MultiAudioStart()														//开启组播语音
	{
		Iamhere("MultiAudioStart running ...");
		
		multiaudioflag = true;
		MultiAudioClearUp();
		speek = new MultiAudioSpeek();
		listen = new MultiAudioListen();
		speek.start();
		listen.start();		
	}
	public void MultiAudioStop()														//关闭组播语音
	{
		Iamhere("MultiAudioStop running ...");
		
		try
		{
			multiaudioflag = false;
			speek.stop();
			listen.interrupt();
			SocketException();															//关闭套接字
		}
		catch (Exception e)
		{
			SocketException();
			e.printStackTrace();
		}		
	}
	//多播发送语音(数据报前导:掩码/用户ID/语音数据)
	private class MultiAudioSpeek extends Thread										//组播语音通话线程
	{
		private int rbufferSize = 0;
		private AudioRecord recorder = null;
		byte[] audio = new byte[640];													//录音缓冲区
		int getlength = 0;																//录音长度
		
		byte[] encodeBuf = new byte[640];												//发送缓冲区
		int encodeSize = 0;																//编码长度
		
		public MultiAudioSpeek()
		{
			Iamhere("MultiAudioSpeek building ...");
			try
			{	
				rbufferSize = AudioRecord.getMinBufferSize(8000,AudioFormat.CHANNEL_CONFIGURATION_MONO,AudioFormat.ENCODING_PCM_16BIT);	//获得录音缓冲区大小
				recorder  = new AudioRecord(MediaRecorder.AudioSource.MIC,8000,AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT,rbufferSize*10);//获得录音机对象				
					
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		public void Talk()
		{
			AudioCodec.audio_codec_init(20);											//初始化编码器
			recorder.startRecording();	
			while(multiaudioflag  && !this.isInterrupted())
			{
				try
				{
					encodeBuf[0]=me.channel;
					encodeBuf[1]=me.point;
					
					byte[] id 		=	int2ByteArray(me.personId);
					byte[] userid 	= 	{ 0, 0, 0, 0 };
					System.arraycopy(id, 0, userid, 0, 4);								//获取ID
					System.arraycopy(userid, 0, encodeBuf, 2, 4);						//附加上用户ID
					
					getlength  = recorder.read(audio,0,640);							//从MIC读取音频数据
					encodeSize = AudioCodec.audio_encode(audio, 0, getlength, encodeBuf, 6);//编码
					if(userisToTalk && getlength >=0)
					{
						MulticastSend(encodeBuf,encodeSize);							//编码发送76B
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			recorder.stop();
			recorder.release();
		}
    	public void run()
    	{
    		Iamhere("MultiAudioSpeek running ...");
    		Talk();
    	}
	}
	private class MultiAudioListen extends Thread										//组播语音收听线程
	{
		//private int sbufferSize = 0;
		private byte[] remoteAudioid 		= { 0, 0, 0, 0 };							//远程用户ID
		private AudioTrack player = null;
		byte[] revbuf = new byte[640];													//网络接收缓冲区(抓包分析一包632B)
		int getlength = 0;																//获取网络包长度
		
		private byte[] decodeBuf = new byte[1024];										//解码缓冲区-
		private int decodeSize = 0;														//解码后长度-

		private FileOutputStream fos = null;
		
		private long startime 	= 0;													//开始计时时间
		private long curtime	= 0;													//当前时间
		private boolean overtimeflag = true;											//超时标志(已经执行过超时程序)
		private final int AUDIO_OVERTIME = 6000;										//语音写入文件超时
		
		
		public void Listen()															//监听播放线程
		{
			AudioCodec.audio_codec_init(20);											//初始化编码器(30编码杂音15退出)		
			player.play();		
			
			while(multiaudioflag)
			{
				try
				{
					getlength = MulticastReceive(revbuf,revbuf.length);					//获取网络数据										
					if(me.channel == revbuf[0] && me.point == revbuf[1] && !userisToTalk && getlength >0)
					{
						//Iamhere("Playing ...");
						overtimeflag = false;
						startime = System.currentTimeMillis();							//设置收到语音数据时间						
						System.arraycopy(revbuf, 2, remoteAudioid, 0, 4);				//获取远程用户ID
						remoteAudioId = byteArray2Int(remoteAudioid);					//获取ID
						decodeSize = AudioCodec.audio_decode(revbuf, 6, getlength, decodeBuf, 0);
						player.write(decodeBuf, 0, decodeSize);							//编码播放
						StoreAudio();													//保存录音
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		
			try																			//析构处理 
			{  
				player.stop();															//停止播放
	            fos.close();															//关闭写入流  
	        } 
			catch (IOException e) 
			{  
	            e.printStackTrace();  
	        } 
			
		}
		public MultiAudioListen()														//初始化播放器/创建文件夹
		{
			try
			{	
				sbufferSize = android.media.AudioTrack.getMinBufferSize(8000,AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);							  //获得音频缓冲区大小
				player = new AudioTrack(AudioManager.STREAM_MUSIC,8000,AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT,sbufferSize*10,AudioTrack.MODE_STREAM);//获得音轨对象
				player.setStereoVolume(1.0f, 1.0f);
				CheckDir(AudioName);													//建立录音存储目录
							
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}				
		}
		private boolean CheckDir(String AudioName)										//检测用户录音目录若不存在则建立(若存在返回true,不存在返回false并创建)
		{
			File dir = new File(AudioName);
			
			if(!isExternalStorageReadable() && !isExternalStorageReadable())			//判断SD卡是否可读写
			{
				Iamhere("SD CARD ERR ....");
				return false;
			}
	        if (!dir.exists())															//文件夹建立 
	        {
	        	Iamhere("MAKE DIR :"+ AudioName + "....");  
	        	dir.mkdirs(); 
	        	return false;
	        }
	        
	        return true;
		}
		private void StoreAudio()														//存储用户录音
		{
			if( lastAudioId != remoteAudioId )											//若用户切换
			{
				try
				{
		            if(lastAudioId != 0)												//0为初始ID
		            {
		            	Iamhere("Write  AudioFile to " + lastAudioId + "/" + curfile + ".wav ....");
		            	
		            	fos.flush();fos.close();										//将之前用户数据压入
		            																	//输出之前用户数据文件
		            	filehandle.copyWaveFile(AudioName + "/tmp/" + lastAudioId + "/" + curfile + ".tmp",
		            							AudioName + "/wav/" + lastAudioId + "/" + curfile + ".wav",sbufferSize);
		            	
		            }
		            
		            String userpath =  AudioName + remoteAudioId + "/";					//更新路径
		            String wavpath = userpath + "wav/";
		            String tmpath = userpath + "tmp/";
	            	CheckDir(tmpath);													//是否存在当前用户文件夹，没有则创建
	            	CheckDir(wavpath);
	            	
	            	File dirFile = new File(tmpath);
	            	File[] files = dirFile.listFiles();
	            	curfile = files.length;												//获取当前音频文件数            	
					File file = new File(tmpath + curfile + ".tmp"); 
		            //if( file.exists() )	{ file.delete(); }							//刷新文件(仅保留最新)  
					fos = new FileOutputStream(file); 									//建立一个可存取字节的文

		            lastAudioId = remoteAudioId;
				}
				catch (Exception e) 
				{  
		            e.printStackTrace();  
		        } 
			}
			
			try
			{
				fos.write(decodeBuf, 0, decodeSize);									//将语音写入tmp文件
			}
			catch (IOException e) 
			{  
	            e.printStackTrace();  
	        } 
		}
		private class MultiAudioOverTime extends TimerTask								//定时执行检测任务(若一段时间没有收到用户语音)
		{
			public void run()
			{
				curtime = System.currentTimeMillis();									//后去当前时间
				if( (curtime - startime > AUDIO_OVERTIME) && !overtimeflag)				//若文件写入超时
				{
	            	Iamhere("Write to AudioFile " + remoteAudioId + ".wav ....");
	            	overtimeflag = true;
	            	//输出之前用户数据文件						
	            	filehandle.copyWaveFile(AudioName  + remoteAudioId + "/tmp/"+ curfile + ".tmp",
							AudioName  + remoteAudioId + "/wav/"+ curfile + ".wav",sbufferSize);
				}				
			}
		}
		public void run() 
		{
			Iamhere("MultiAudioListen Thread running ...");
			
			Timer timer=new Timer();													
			timer.schedule(new MultiAudioOverTime(),500,AUDIO_OVERTIME/2);				//定时检测接收情况
			Listen();

		}
	}

	//数据透传及消息发送方法
	public void MultiDataSend(byte[] cmd,int reqlength)									//透传发送数据
	{
		int length = reqlength >= 240 ? 240:reqlength;			

		ByteArrayReSet(userCmdBuffer, 10, 250);											//清空发送数据组包
		System.arraycopy(cmd, 0, userCmdBuffer, 10, length);
		//校验字段暂未添加
		MulticastSend(userCmdBuffer,Constant.bufferSize);
		
		Iamhere("MultiDataSend " + " ...");
		
	}
	public void MultiMsgSend(String msg)												//发送消息
	{
		byte[] msgbyte = msg.getBytes();												//获取消息bytes
		int length = msgbyte.length >= 240 ? 240:msgbyte.length;			

		ByteArrayReSet(msgSendBuffer, 10, 250);											//清空发送数据组包
		System.arraycopy(msgbyte, 0, msgSendBuffer, 10, length);
		//校验字段暂未添加
		MulticastSend(msgSendBuffer,Constant.bufferSize);
		
		Iamhere("MultiMsgSend " + msg + " ...");
		
	}
	public boolean MultiDataReceive(byte[] buf)											//透传接收数据
	{
		if(cmdPackHead - 1 >= 0)
		{
			cmdPackHead --;
			Arrays.fill(buf, (byte)0);
			System.arraycopy(cmdPackBuf[cmdPackHead].pack, 0, buf, 0, NETPACKSIZE);	

			return true;
		}
		else
		{	
			return false;
		}
	}
	public boolean MultiMsgReceive(byte[] buf)											//接收消息 
	{
		if(msgPackHead - 1 >= 0)														//栈顶指针总指向当前可插入位置
		{
			msgPackHead --;
			Arrays.fill(buf, (byte)0);
			System.arraycopy(msgPackBuf[msgPackHead].pack, 0, buf, 0, NETPACKSIZE);	
			
			return true;
		}
		else
		{	
			return false;
		}
	}
	
	//CommunicationBridge线程接收数据报并解析，将解析后的数据缓存到相应数组中，并发送相应广播，该函数为外部访问接口
	public void MultiConlProt(boolean stat)												//开启协议解析线程
	{
		if(stat == true)
		{
			parsepackflag = true;
			com = new CommunicationBridge();											//开启协议解析线程
			upme = new UpdateMe();														
			chkuser = new CheckUserOnline();
			
			com.start();
			upme.start();																//开启定时注册线程
			chkuser.start();															//开启检测用户在线线程
			
		}
		else
		{
			parsepackflag = false;
			com.interrupt();
			upme.interrupt();
			chkuser.interrupt();
		}
		
	}
	public int GetPersonListSize()														//获取用户列表大小
	{
		
		return personKeys.size();
	}
	public boolean GetPersonByNum(int num ,Person person)								//获取num号用户，Person返回，并函数返回总用户数
	{
		int size, personid;
		Person mapuser = new Person();
		
		size = personKeys.size();
		if(num > size)
		{
			Iamhere("personKeys overflow!");
			
			return false;
		}
		else
		{
			personid = personKeys.get(num);												//获取num号用户id
			mapuser = childrenMap.get(personid);										//从MAP中获取personid用户		
			person.CopyFrom(mapuser);													//数据拷贝用于返回
			
			return true;
		}
	}
	public boolean GetPersonByID(int id ,Person p)										//获取id号用户，Person返回
	{
		Person user = new Person();
		
		if(personKeys.equals(id))
		{
			user = childrenMap.get(id);
			p.CopyFrom(user);
			
			return true;
		}
		else
		{
			
			return false;
		}		
		
	}
	private class CommunicationBridge extends Thread									//协议分析处理模块
	{
		
		@Override
		public void run() 																//打开组播端口，准备组播通讯
		{
			super.run();
			while( parsepackflag && !this.isInterrupted())
			{
				//不完整数据报直接丢弃
				if(Constant.bufferSize == MulticastReceive(recvBuffer,Constant.bufferSize))
				{
					byte[] head = Constant.pkgHead;
					if( recvBuffer[0] == head[0]            &&
						recvBuffer[1] == head[1]            &&
						recvBuffer[2] == head[2]            &&							//检测包头
						recvBuffer[250] == me.channel     	&&
						recvBuffer[251] == me.point										//检测掩码
																						//检测校验和暂未加入
					                                     		)
					{
						Iamhere("CommunicationBridge Receive Mask Packet ...");
						parsePackage(recvBuffer);
						
					}
				}
			}
			
		}
		private void parsePackage(byte[] pkg) 											//解析接收到的数据包
		{
			int CMD 	= pkg[3];														//命令字
			int cmdType = pkg[4];														//命令类型
			int oprCmd 	= pkg[5];														//操作命令

			//获得用户ID号
			byte[] uId = new byte[4];													//数据报中获取的ID
			System.arraycopy(pkg, 6, uId, 0, 4);
			int remoteId = byteArray2Int(uId);											//转换为int型ID
			
			switch (CMD) 
			{
				case Constant.CMD80:
				switch (cmdType) 
				{			
					case Constant.CMD_TYPE1:	
						if(remoteId != me.personId)										//如果该信息不是自己发出则给对方发送回应包,并把对方加入用户列表
						{
							Iamhere("remoteID:"+remoteId + "/me.personId:" + me.personId);
							if(!personKeys.contains(remoteId))							//若用户没有注册过
							{
								regBuffer[4] = Constant.CMD_TYPE2;
								MulticastSend(regBuffer,Constant.bufferSize);			//发送应答包
							}
							updatePerson(remoteId, pkg);								//刷新用户列表
						}
						
						break;
						
						case Constant.CMD_TYPE2:
							if(remoteId != me.personId)									//用户响应的注册(屏蔽本机注册数据包)
							{
								updatePerson(remoteId, pkg);
							}
							
						break;
						
						case Constant.CMD_TYPE3:
							childrenMap.remove(remoteId);
							personKeys.remove(Integer.valueOf(remoteId));
						break;
				}
				break;
				
				case Constant.CMD81:
					Iamhere("CommunicationBridge UserMsg() ...");
					UserMsg(cmdType,oprCmd);
				break;
			
				case Constant.CMD84:													//84命令，指令交换
					Iamhere("CommunicationBridge UserCMD() ...");
					UserCMD(cmdType,oprCmd);
				break;
				
				default:
				break;
			}
		}
		public void UserMsg(int cmdType,int oprCmd)										//注册消息处理函数
		{
			switch (cmdType) 
			{
				case Constant.CMD_TYPE1:
					switch(oprCmd)
					{
						case Constant.OPR_CMD1:											//接收到消息
							if(msgPackHead < PACKBUFSIZE)								//若满则不接收新数据
							{
								msgPackBuf[msgPackHead].DataPackSetData(recvBuffer, 6);
								msgPackHead ++;
							}
							break;
						
						case Constant.OPR_CMD2:	
		
							break;
					}
					break;
				
				case Constant.CMD_TYPE2:

					break;
				
				case Constant.CMD_TYPE3:

					break;
			}			
		}
		public void UserCMD(int cmdType,int oprCmd)										//指令处理函数
		{
			switch(cmdType)
			{
				case Constant.CMD_TYPE1:
					switch(oprCmd)
					{
						case Constant.OPR_CMD1:											//接收到指令
							if(cmdPackHead < PACKBUFSIZE)								//若满则不接收新数据
							{
								cmdPackBuf[cmdPackHead].DataPackSetData(recvBuffer, 6);
								cmdPackHead ++;
							}
							break;
						
						case Constant.OPR_CMD2:	
		
							break;
					}
					
					break;
					
					
				case Constant.CMD_TYPE2:
					
					break;
			}			
		}
		private void updatePerson(int userId,byte[] pkg)								//更新或加用户信息到用户列表中
		{
			Person person = new Person();
			getPerson(pkg,person);
			childrenMap.put(userId, person);
			if(!personKeys.contains(Integer.valueOf(userId)))
			{
				Iamhere("personKeys add " + userId);
				personKeys.add(Integer.valueOf(userId));
			}
		}
		private void getPerson(byte[] pkg,Person person)								//分析数据包并获取一个用户信息
		{
			
			byte[] personIdBytes = new byte[4];
			byte[] iconIdBytes = new byte[4];
			byte[] nickeNameBytes = new byte[30];
			byte[] personIpBytes = new byte[4];
			
			System.arraycopy(pkg, 6, personIdBytes, 0, 4);
			System.arraycopy(pkg, 10, iconIdBytes, 0, 4);
			System.arraycopy(pkg, 14, nickeNameBytes, 0, 30);
			System.arraycopy(pkg, 44, personIpBytes, 0, 4);
			
			person.personId 		= byteArray2Int(personIdBytes);
			person.personHeadIconId = byteArray2Int(iconIdBytes);
			person.personNickeName	= (new String(nickeNameBytes)).trim();
			person.ipAddress 		= Constant.intToIp(byteArray2Int(personIpBytes));
			person.timeStamp 		= System.currentTimeMillis();
			
	    	byte[] longitude = new byte[8];												//获取经度
	    	byte[] latitude = new byte[8];												//获取纬度
	    	System.arraycopy(pkg, 48,longitude, 0, 8);
	    	System.arraycopy(pkg, 56, latitude, 0, 8);
	    	person.longitude = getDouble(longitude);
	    	person.latitude = getDouble(latitude);
		}
	}
	private class UpdateMe extends Thread												//修改心跳时间，增加实时性,5s
	{
		@Override
		public void run() 
		{
			while(parsepackflag && !this.isInterrupted())
			{
				try
				{
					regBuffer[4] = Constant.CMD_TYPE1;									//恢复成注册请求标志，向网络中注册自己
					MulticastSend(regBuffer,Constant.bufferSize);						//发送应答包
					sleep(5000);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	private class CheckUserOnline extends Thread										//检测用户是否在线，如果超过15说明用户已离线，秒则从列表中清除该用户
	{
		@Override
		public void run() 
		{
			super.run();
			while(parsepackflag && !this.isInterrupted())
			{
				if(childrenMap.size()>0)
				{
					Set<Integer> keys = childrenMap.keySet();
					for (Integer key : keys) 
					{
						if(System.currentTimeMillis()-childrenMap.get(key).timeStamp>15000)
						{
							childrenMap.remove(key);
							personKeys.remove(Integer.valueOf(key));
						}
					}
				}
				try 
				{
					sleep(5000);
				} 
				catch (InterruptedException e) 
				{
					e.printStackTrace();
				}
			}
		}
	}

	//多播文件收发
	@SuppressWarnings("deprecation")
	public void MultiTransferFileStop(FILEOPTYPE type)									//停止文件操作
	{
		if(type == FILEOPTYPE.SEND)
		{
			//mfs.stop();
			mfs.interrupt();
		}
		else
		{
			//mfr.stop();
			mfr.interrupt();
		}
	}
	public int MultiTransferFileCheck(FILEOPTYPE type)									//检测文件收发进度
	{
		if(type == FILEOPTYPE.SEND)
		{
			return mfs.Progress();
		}
		else
		{
			
			return mfr.Progress();
		}
	}
	public boolean MultiSendFileStart( String filepath, int delay)						//小文件发送
	{
		if(mfs != null)
		{
			//mfs.stop();
			mfs.interrupt();
			mfs = null;
		}
		mfs = new MultiSendFile( filepath, delay);
		mfs.start();	
		
		return true;
	}
	public boolean MultiReceiveFileStart(int userid, long filesize, String filepath)	//接收某人的某文件
	{
		if(mfr != null)
		{
			mfr.interrupt();
			//mfr.stop();
			mfr = null;
		}
		mfr = new MultiReceiveFile(userid , filesize , filepath);
		mfr.start();
		
		return true;
	}
	private class MultiSendFile extends Thread											//发送文件线程
	{
		private String filepath = null;
		private int delay = 100;														//发送延时避免丢包
		private long filelength = 0;													//文件总大小
		private long sendlength = 0;													//文件已发送大小
		
 		public MultiSendFile(String filepath , int delay)								//发送指定文件
		{
			this.filepath = filepath;
			this.delay = delay;
		}
 		public int Progress()
 		{
 			return (int)( ( (float)sendlength / (float)filelength ) * 100 );
 		}
		public void run()
		{
			byte[] sendBuf = new byte[518];												//发送缓冲区
			byte[] fileBuf = new byte[512];												//文件发送缓存区
			int getlength = 0;															//接收到文件段长度

			Arrays.fill(sendBuf , (byte)0);												//清空数据缓冲区
			Arrays.fill(fileBuf , (byte)0);
			
			try
			{
				File file = new File(filepath);
				FileInputStream fis = new FileInputStream(file);						//获取文件流
				filelength = fis.available();
				
				sendBuf[0]=me.channel;														//添加频道、频点
				sendBuf[1]=me.point;
				
				byte[] id 		=	int2ByteArray(me.personId);
				byte[] userid 	= 	{ 0, 0, 0, 0 };
				System.arraycopy(id, 0, userid, 0, 4);									//获取ID
				System.arraycopy(userid, 0, sendBuf, 2, 4);								//附加上用户ID	

				while( fis.read(fileBuf) != -1 )										//直到文件结尾
				{
					
					getlength  = fileBuf.length;										//读取文件的长度
					sendlength += getlength;											//已发送文件长度
					System.arraycopy( fileBuf, 0, sendBuf, 6, getlength);
					MulticastSend(sendBuf,getlength + 6);
					sleep(delay);
				}
				fis.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}			
		}
	}
	private class MultiReceiveFile extends Thread										//接收并保存文件
	{
		private int userid;
		private long filesize;
		private String filepath = null;
		private byte[] remoteAudioid 		= { 0, 0, 0, 0 };							//远程用户ID
		private int remoteAudioId;														//远程用户ID
		private long filelength = 0;													//文件总大小
		private long revlength = 0;														//文件已接收大小		
		
		public MultiReceiveFile(int userid, long filesize, String filepath)				//接收某人的某文件							
		{
			this.userid = userid;
			filelength = filesize;
			this.filesize = filesize;
			this.filepath = filepath;
		}
		public int Progress()
		{
			return (int)( ( (float)revlength / (float)filelength ) * 100 );
		}
		public void run()
		{
			byte[] revbuf = new byte[518];												//接收缓冲区
			int getlength = 0;															//文件长度
			
			try
			{
				File file = new File(filepath);
				FileOutputStream fis = new FileOutputStream(file);						//获取文件
			
				while( filesize > 0)
				{
					try
					{
						getlength = MulticastReceive( revbuf, revbuf.length) - 6;		//获取网络数据
						System.arraycopy(revbuf, 2, remoteAudioid, 0, 4);				//获取远程用户ID
						remoteAudioId = byteArray2Int(remoteAudioid);					//获取ID	
						if(me.channel == revbuf[0] && me.point == revbuf[1] && 			//过滤数据(频率/频点/ID)
									userid == remoteAudioId  && getlength > 0)
						{
							filesize = filesize - getlength;
							revlength += getlength;
							fis.write(revbuf, 6, getlength);							//存储文件流
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}				
				
				fis.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}	
			
		}
	}

	//辅助函数
	private void initCmdBuffer()														//初始化各发送缓存区的基本数据段
	{
		//初始化用户注册指令缓存
		Arrays.fill(regBuffer, (byte)0);
		System.arraycopy(Constant.pkgHead, 0, regBuffer, 0, 3);
		regBuffer[3] = Constant.CMD80;
		regBuffer[4] = Constant.CMD_TYPE1;
		regBuffer[5] = Constant.OPR_CMD1;
		
		//初始化信息缓存
		Arrays.fill(msgSendBuffer, (byte)0);
		System.arraycopy(Constant.pkgHead, 0, msgSendBuffer, 0, 3);
		msgSendBuffer[3] = Constant.CMD81;
		msgSendBuffer[4] = Constant.CMD_TYPE1;
		msgSendBuffer[5] = Constant.OPR_CMD1;

		//初始化指令缓存
		Arrays.fill(userCmdBuffer, (byte)0);
		System.arraycopy(Constant.pkgHead, 0, userCmdBuffer, 0, 3);
		userCmdBuffer[3] = Constant.CMD84;
		userCmdBuffer[4] = Constant.CMD_TYPE1;
		userCmdBuffer[5] = Constant.OPR_CMD1;
	}
	public boolean isExternalStorageWritable() 											//查询外部存储是否可写
	{
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) 
	    {
	        return true;
	    }
	    return false;
	}
	public boolean isExternalStorageReadable() 											//查询外部存储是否可读
	{
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state) ||
	        Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) 
	    {
	        return true;
	    }
	    return false;
	}
	public File getAlbumStorageDir(String albumName)									//后去绝对路径
	{
	    // Get the directory for the user's public pictures directory. 
	    File file = new File(Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_PICTURES), albumName);
	    if (!file.mkdirs()) 
	    {
	        Iamhere("Directory not created");
	    }
	    return file;
	}
	
	//通用工具类方法
	public static String IpInt2String(int a)											//int型ip转string
	{  
		StringBuffer sb=new StringBuffer();  
		int b=(a>>0)&0xff;  
		sb.append(b+".");  
		b=(a>>8)&0xff;  
		sb.append(b+".");  
		b=(a>>16)&0xff;  
		sb.append(b+".");  
		b=(a>>24)&0xff;  
		sb.append(b);
		
		return sb.toString();  
	} 
    public static int IpString2Int(String hostname) 									//把String类型的Ip地址转换成int类型
    {    
	     InetAddress inetAddress;    
	     try 
	     {        
	    	 inetAddress = InetAddress.getByName(hostname);    
	     } 
	     catch (UnknownHostException e)
	     {        
	    	 return -1;    
	     }     
	     byte[] addrBytes;   
	     int addr;   
	     addrBytes = inetAddress.getAddress();    
	     addr = ((addrBytes[3] & 0xff) << 24)| ((addrBytes[2] & 0xff)<< 16)| ((addrBytes[1] & 0xff) << 8)| (addrBytes[0] & 0xff);
	     
	    return addr; 
    }
	private static void ByteArrayReSet(byte[] buf,int start , int end)					//数组指定段重置(包含start 不包含 end)
	{
		int count = 0;
		for(count = start; count < end ; count++)
		{
			buf[count] = 0;
		}
		
	}
	public static byte[] putDouble( double x) 											//double 转 byte
	{  
        byte[] b = new byte[8];  
        long l = Double.doubleToLongBits(x);  
        for (int i = 0; i < 8; i++) 
        {  
            b[i] = new Long(l).byteValue();  
            l = l >> 8;  
        }
        return b;
    }
    public static double getDouble(byte[] b) 											//byte 转 double
    {  
        long m;  
        m = b[0];  
        m &= 0xff;  
        m |= ((long) b[1] << 8);  
        m &= 0xffff;  
        m |= ((long) b[2] << 16);  
        m &= 0xffffff;  
        m |= ((long) b[3] << 24);  
        m &= 0xffffffffl;  
        m |= ((long) b[4] << 32);  
        m &= 0xffffffffffl;  
        m |= ((long) b[5] << 40);  
        m &= 0xffffffffffffl;  
        m |= ((long) b[6] << 48);  
        m &= 0xffffffffffffffl;  
        m |= ((long) b[7] << 56);  
        return Double.longBitsToDouble(m);  
    }
	private static byte[] int2ByteArray(int value) 										//int转byte
	{
		byte[] targets = new byte[4];

		targets[0] = (byte) (value & 0xff);												// 最低位 
		targets[1] = (byte) ((value >> 8) & 0xff);										// 次低位 
		targets[2] = (byte) ((value >> 16) & 0xff);										// 次高位 
		targets[3] = (byte) (value >>> 24);												// 最高位,无符号右移
		
		
		return targets; 
	}
	public static final int byteArrayToInt(byte[] b) 
	{
		return (b[0] << 24) + ((b[1] & 0xFF) << 16) + ((b[2] & 0xFF) << 8)
				+ (b[3] & 0xFF);
	}
	private static final int byteArray2Int(byte[] b) 									//byte转int
	{
		int targets = (b[0] & 0xff) | ((b[1] << 8) & 0xff00) | ((b[2] << 24) >>> 8) | (b[3] << 24); 
		
		return targets; 
	}
	public void Iamhere(String s)														//标记方法
	{
		Log.d(TAG, "MulticComm: " + s);		
	}
	
}
