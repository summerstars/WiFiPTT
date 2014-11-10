/*
 * ��ʼ��:
 * 		MulticComm(WifiManager wifiManager,String adds,int port)		�����������(ip���˿�)
 * 		void MultiMask(byte channel,byte point)							��������(Ƶ����Ƶ��)
 * 		void MultiSetID(int uid)										�����û�ID
 * 		void SetInfomation(Person p)									���ñ����������Ϣ
 * ������
 * 		int MultiAudioGetID()											��ȡ��ǰ�����û�ID
 * 		void MultiAudioStart()											���������߳�
 * 		void MultiAudioStop()											�ر������߳�
 * 		void MultiAudioToTalk(boolean stat)								����/��������״̬�л�
 * 		int MultiAudioRePlay( int userid, int num)						�ط�ĳ�û���ĳ��¼��(����¼���ļ�������)
 * 		boolean MultiAudioStopPlay()									ֹͣ�ط�
 * 		�������ݱ����� ./wrildufirephone/Audio/"�û�ID"/wav/0+��wav
 * 
 * ��Ϣ��
 * 		void MultiMsgSend(String msg)									������Ϣ(240�ֽ�)
 * 		boolean MultiMsgReceive(byte[] buf)								������Ϣ(id/msg)
 * Э���շ�����
 * 		void MultiConlProt(boolean stat)								��/����Э������̣߳�Ϊ���շ����ṩ����
 * 
 * 	�û�ע����Ϣ����:
 * 		int GetPersonListSize()											��ȡ�û��б��С
 * 		boolean GetPersonByNum(int num ,Person person)					��ȡnum���û���Person���أ��������������û���
 * 		boolean GetPersonByID(int id ,Person p)							��ȡid���û���Person����
 * 
 * 	͸��/ָ�
 * 		void MultiDataSend(byte[] buf,int reqlength)					����͸������
 * 		boolean MultiDataReceive(byte[] buf)							����͸������(id/data)
 * 		
 * 
 * С�ļ�����:
 * 		boolean MultiSendFileStart(String filepath ,int delay)						С�ļ��㲥����(��Ҫ�Զ��巢���ļ������ļ���С),delay������ʱ������
 * 		boolean MultiReceiveFileStart(int userid, long filesize, String filepath)	����ĳ�˵�ĳ�ļ�
 * 		void MultiTransferFileStop(FILEOPTYPE type)									ֹͣ�ļ�����
 * 		int MultiTransferFileCheck(FILEOPTYPE type)									����ļ��շ�����
 * 		enum FILEOPTYPE {SEND,RECEIVE}
 *  
 * 		
 * �ӿ�ʹ��˵����
 * 			1)���ӿ��ļ������ MulticastBase ֧��
 * 			2)��ʼ������ MulticComm(WifiManager wifiManager,String adds,int port)
 * 			3)�������� MultiMask(byte channel,byte point)
 * 			4)���ñ���ID MultiSetID(int uid)
 * 			5)������;��ѡ���ʼ��Ϊ�����ӿڻ�Э�鴫��ӿ�
 * 				(1)�����ӿ� 
 * 						���������߳�  MultiAudioStart()
 * 						�ڳ����и�����Ҫ���� MultiAudioToTalk(boolean stat)
 * 						��ͨѶ��������Ҫ��ȡ��ǰ�����û�IDʱ���� 	MultiAudioGetID()
 * 						�����ǵ��� MultiAudioStop()
 * 				(2)Э�鴫��ӿ�
 * 						���ñ����û���Ϣ SetInfomation(Person p)
 * 						����Э�鴦���߳�  MultiConlProt()
 * 						�ɵ��� MultiMsgSend(String msg)��MultiMsgReceive(byte[] buf) �շ���Ϣ(�240BYTE)
 * 						�ɵ��� MultiDataSend(byte[] buf,int reqlength)  MultiDataReceive(byte[] buf) ����͸������(�240BYTE)
 *						�ɵ���GetPersonListSize()��ȡ�û��б��С
 * 						�ɵ���GetPersonByNum(int num ,Person person)��ȡ�б��е�num���û���Ϣ
 * 						�ɵ���GetPersonByID(int id ,Person p)ͨ��id�����б��в����û������أ�Person����
 * 				(3)��Ϣ����ӿ�
 * 						��μ�(2)
 * 				(4)С�ļ�����ӿ�
 * 						�ڿ�ʼ����API�����ļ�ǰ���û���Ҫͨ�������ӿڻ�ȡ�ļ������ļ���С����Щ���ļ�����API����Ĵ������
 * 						�����ļ�����MultiSendFileStart(String filepath ,int delay),filepathΪ�ļ�����·����delayΪ����������ʱ����ֹ����̫����ɶ���
 * 						����ĳid/������ļ�����MultiReceiveFileStart(int userid, long filesize, String filepath)
 * 						�ļ���������п�ͨ��MultiTransferFileCheck(FILEOPTYPE type)	����ļ��շ����ȣ�typeΪö���� enum FILEOPTYPE {SEND,RECEIVE}
 * 						��ʱ������Ҫ��ֹ�ļ��Ĵ��䣬��ͨ��MultiTransferFileStop(FILEOPTYPE type)
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


//���ñ������γɵײ����(����Э�顢��Ƶ��Ƶ��Ķಥ����/����/͸��)
public class MulticComm extends MulticastBase
{
	private static String TAG = "ePTT[MulticComm]";
	private boolean userisToTalk 		= false;						//��ͨ�����������ֹ�߳� 
	private MultiAudioSpeek speek 		= null;
	private MultiAudioListen listen 	= null;
	private boolean multiaudioflag		= false;						//�ಥ�����߳̿�����־
	private boolean parsepackflag		= true;							//�������ݱ��߳̿�����־
	private Person me 					= new Person();					//������Ϣ�ṹ��
	private int lastAudioId				= 0;							//��һ���û�ID
	private static int remoteAudioId	= 0;							//��ǰ����Զ���û�ID
	private static Map<Integer,Person> childrenMap 	= new HashMap<Integer,Person>();	//��ǰ�����û�
	private static ArrayList<Integer> personKeys 	= new ArrayList<Integer>();			//��ǰ�����û�id

	private byte[] regBuffer 			= new byte[Constant.bufferSize];//��������ע�ύ��ָ��
	private byte[] msgSendBuffer 		= new byte[Constant.bufferSize];//��Ϣ����ջ
	private byte[] userCmdBuffer 		= new byte[Constant.bufferSize];//�û�ָ��ջ
	private int sbufferSize 			= 0;							//��Ƶ��������С	
	private byte[] recvBuffer 			= new byte[Constant.bufferSize];//���ݱ����ջ�����
	private final static int NETPACKSIZE 	= 244;						//��������С
	private final static int USERIDSIZE		= 4;						//�û�ID����
	private final static int PACKBUFSIZE 	= 128;						//��������ݱ���������У
	private class DataPackSocket										//��������ݱ�ԭʼ��
	{
		public byte[] pack = new byte[NETPACKSIZE];						//ԭʼ���ݶ�
		
		public DataPackSocket()
		{
			Arrays.fill(pack, (byte)0);
		}
		public void DataPackSetData(byte[] data,int start)				//��������
		{
			System.arraycopy(data, start, pack, 0, NETPACKSIZE);
		}
	}
	private DataPackSocket[] msgPackBuf		= new DataPackSocket[PACKBUFSIZE];
	private DataPackSocket[] cmdPackBuf		= new DataPackSocket[PACKBUFSIZE];
	private int msgPackHead 				= 0;						//��Ϣջ��ָ��
	private int cmdPackHead					= 0;						//ָ��ջ��ָ��
	private static CommunicationBridge com 	= null;						//Э�鴦���߳�
	private static UpdateMe upme 			= null;						//�����û���Ϣ�߳�
	private static CheckUserOnline chkuser 	= null;						//����û������߳�
	
	public enum FILEOPTYPE {SEND,RECEIVE};								//�ļ�������� 
	private MultiSendFile mfs 				= null;						//�ļ������߳�
	private MultiReceiveFile mfr 			= null;						//�ļ������߳�
	private int curfile 					= 0;						//��ǰ�û�¼���ļ�λ��
	private boolean replayflag 				= false;					//�طű�־
	private MediaPlayer mPlayer 			= null;						//¼���ط�
	private HandleFile filehandle			= new HandleFile();			//�ļ�����
	private static final String AudioName 	= "/sdcard/wrildufirephone/Audio/";			//¼���ļ�����λ��
	

	public MulticComm(WifiManager wifiManager,String adds,int port)						//���캯��
	{
		super(wifiManager,adds,port);
		Iamhere("MulticComm building...");
		initCmdBuffer();																//��ʼ�����Ĵ���
		for(int i = 0 ;i < PACKBUFSIZE ; i++)											//��ʼ����Ϣ������
		{
			msgPackBuf[i] = new DataPackSocket();
			cmdPackBuf[i] = new DataPackSocket();
		}
	}
	public void MultiMask(byte channel,byte point)										//�����ŵ�����
	{												
		me.channel			= channel;													//A ~ Z   (26) 													
		me.point			= point;													//0 ~ 255 (256)
		
		//�û�ָ��
		userCmdBuffer[250] 	= channel;													//ָ��Ƶ��
		userCmdBuffer[251] 	= point;													//ָ��Ƶ��
		//ͨѶ��Ϣ
		msgSendBuffer[250] 	= channel;																										
		msgSendBuffer[251] 	= point;
		//ע����Ϣ
		regBuffer[250] 		= channel;																										
		regBuffer[251]		= point;	
		
	}	
	public void MultiSetID(int uid)														//���ñ���ID(��ʼ����ָ�����)
	{
		//userId 		=	uid;														//���ñ���ID
		me.personId 	= 	uid;														//���ñ���ID
		byte[] id 		=	int2ByteArray(uid);
		byte[] userid 	= 	{ 0, 0, 0, 0 };
		System.arraycopy(id, 0, userid, 0, 4);											//��ȡID
		System.arraycopy(userid, 0, msgSendBuffer, 6, 4);								//������Ϣ�������û�ID
		System.arraycopy(userid, 0, userCmdBuffer, 6, 4);								//��������͸�����û�ID
		System.arraycopy(userid, 0, regBuffer, 6, 4);
	}
    public void SetInfomation(Person p)													//���ñ����������Ϣ
    {
		me.CopyFrom(p);																	//������Ϣ	
		
    	//�����û�ע�Ỻ������
    	System.arraycopy(int2ByteArray(me.personId), 0, regBuffer, 6, 4);
    	System.arraycopy(int2ByteArray(me.personHeadIconId), 0, regBuffer, 10, 4);
    	ByteArrayReSet(regBuffer,14,44);												//��ԭ�����ǳ��������
    	byte[] nickeNameBytes = me.personNickeName.getBytes();
    	System.arraycopy(nickeNameBytes, 0, regBuffer, 14, nickeNameBytes.length);

    	int ip = IpString2Int(me.ipAddress);
    	byte[] localIpBytes = int2ByteArray(ip);
    	System.arraycopy(localIpBytes,0,regBuffer,44,4);
    	byte[] longitude = putDouble(me.longitude);										//��ȡ����
    	System.arraycopy(longitude, 0, regBuffer, 48, 8);
    	byte[] latitude = putDouble(me.latitude);										//��ȡγ��
    	System.arraycopy(latitude, 0, regBuffer, 56, 8);
	
    }
    
	public int MultiAudioRePlay( int userid, int num)									//�ط�ĳ�û���ĳ��¼��
	{
		if(!replayflag)																	//��ֹ�������
		{  
	        try
	        {
	        	String wavpath = AudioName + "/" + userid + "/" + "wav/";
	        	String filepath = wavpath + num + ".wav";
            	File dirFile = new File(wavpath);
            	File[] files = dirFile.listFiles();
            	int curfile = files.length;		        	
	        	File wav = new File(filepath);
	        	if(!wav.exists())														//���ļ��������򷵻������ļ���
	        	{
	        		Iamhere("The wav file is not exist!");
	        		
	        		return curfile;										
	        	}
	        	mPlayer = new MediaPlayer();											//¼������
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
	            Iamhere("����ʧ��");  
	        }
		}
        
		return Integer.MAX_VALUE;
	}
	public boolean MultiAudioClearUp()													//���������Ƶ�ļ�����
	{
		filehandle.deleteDirectory(AudioName);
		return true;
	}
	public boolean MultiAudioStopPlay()													//ֹͣ�ط�
	{
		mPlayer.release();  
        mPlayer = null;
        replayflag = false;
        
        return true;
	}
	public int MultiAudioGetID()														//��ȡ�û�ID
	{
		
		return remoteAudioId;
	}
	public void MultiAudioToTalk(boolean stat)											//ͨ�����ط���
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
	public void MultiAudioStart()														//�����鲥����
	{
		Iamhere("MultiAudioStart running ...");
		
		multiaudioflag = true;
		MultiAudioClearUp();
		speek = new MultiAudioSpeek();
		listen = new MultiAudioListen();
		speek.start();
		listen.start();		
	}
	public void MultiAudioStop()														//�ر��鲥����
	{
		Iamhere("MultiAudioStop running ...");
		
		try
		{
			multiaudioflag = false;
			speek.stop();
			listen.interrupt();
			SocketException();															//�ر��׽���
		}
		catch (Exception e)
		{
			SocketException();
			e.printStackTrace();
		}		
	}
	//�ಥ��������(���ݱ�ǰ��:����/�û�ID/��������)
	private class MultiAudioSpeek extends Thread										//�鲥����ͨ���߳�
	{
		private int rbufferSize = 0;
		private AudioRecord recorder = null;
		byte[] audio = new byte[640];													//¼��������
		int getlength = 0;																//¼������
		
		byte[] encodeBuf = new byte[640];												//���ͻ�����
		int encodeSize = 0;																//���볤��
		
		public MultiAudioSpeek()
		{
			Iamhere("MultiAudioSpeek building ...");
			try
			{	
				rbufferSize = AudioRecord.getMinBufferSize(8000,AudioFormat.CHANNEL_CONFIGURATION_MONO,AudioFormat.ENCODING_PCM_16BIT);	//���¼����������С
				recorder  = new AudioRecord(MediaRecorder.AudioSource.MIC,8000,AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT,rbufferSize*10);//���¼��������				
					
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		public void Talk()
		{
			AudioCodec.audio_codec_init(20);											//��ʼ��������
			recorder.startRecording();	
			while(multiaudioflag  && !this.isInterrupted())
			{
				try
				{
					encodeBuf[0]=me.channel;
					encodeBuf[1]=me.point;
					
					byte[] id 		=	int2ByteArray(me.personId);
					byte[] userid 	= 	{ 0, 0, 0, 0 };
					System.arraycopy(id, 0, userid, 0, 4);								//��ȡID
					System.arraycopy(userid, 0, encodeBuf, 2, 4);						//�������û�ID
					
					getlength  = recorder.read(audio,0,640);							//��MIC��ȡ��Ƶ����
					encodeSize = AudioCodec.audio_encode(audio, 0, getlength, encodeBuf, 6);//����
					if(userisToTalk && getlength >=0)
					{
						MulticastSend(encodeBuf,encodeSize);							//���뷢��76B
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
	private class MultiAudioListen extends Thread										//�鲥���������߳�
	{
		//private int sbufferSize = 0;
		private byte[] remoteAudioid 		= { 0, 0, 0, 0 };							//Զ���û�ID
		private AudioTrack player = null;
		byte[] revbuf = new byte[640];													//������ջ�����(ץ������һ��632B)
		int getlength = 0;																//��ȡ���������
		
		private byte[] decodeBuf = new byte[1024];										//���뻺����-
		private int decodeSize = 0;														//����󳤶�-

		private FileOutputStream fos = null;
		
		private long startime 	= 0;													//��ʼ��ʱʱ��
		private long curtime	= 0;													//��ǰʱ��
		private boolean overtimeflag = true;											//��ʱ��־(�Ѿ�ִ�й���ʱ����)
		private final int AUDIO_OVERTIME = 6000;										//����д���ļ���ʱ
		
		
		public void Listen()															//���������߳�
		{
			AudioCodec.audio_codec_init(20);											//��ʼ��������(30��������15�˳�)		
			player.play();		
			
			while(multiaudioflag)
			{
				try
				{
					getlength = MulticastReceive(revbuf,revbuf.length);					//��ȡ��������										
					if(me.channel == revbuf[0] && me.point == revbuf[1] && !userisToTalk && getlength >0)
					{
						//Iamhere("Playing ...");
						overtimeflag = false;
						startime = System.currentTimeMillis();							//�����յ���������ʱ��						
						System.arraycopy(revbuf, 2, remoteAudioid, 0, 4);				//��ȡԶ���û�ID
						remoteAudioId = byteArray2Int(remoteAudioid);					//��ȡID
						decodeSize = AudioCodec.audio_decode(revbuf, 6, getlength, decodeBuf, 0);
						player.write(decodeBuf, 0, decodeSize);							//���벥��
						StoreAudio();													//����¼��
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		
			try																			//�������� 
			{  
				player.stop();															//ֹͣ����
	            fos.close();															//�ر�д����  
	        } 
			catch (IOException e) 
			{  
	            e.printStackTrace();  
	        } 
			
		}
		public MultiAudioListen()														//��ʼ��������/�����ļ���
		{
			try
			{	
				sbufferSize = android.media.AudioTrack.getMinBufferSize(8000,AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);							  //�����Ƶ��������С
				player = new AudioTrack(AudioManager.STREAM_MUSIC,8000,AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT,sbufferSize*10,AudioTrack.MODE_STREAM);//����������
				player.setStereoVolume(1.0f, 1.0f);
				CheckDir(AudioName);													//����¼���洢Ŀ¼
							
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}				
		}
		private boolean CheckDir(String AudioName)										//����û�¼��Ŀ¼������������(�����ڷ���true,�����ڷ���false������)
		{
			File dir = new File(AudioName);
			
			if(!isExternalStorageReadable() && !isExternalStorageReadable())			//�ж�SD���Ƿ�ɶ�д
			{
				Iamhere("SD CARD ERR ....");
				return false;
			}
	        if (!dir.exists())															//�ļ��н��� 
	        {
	        	Iamhere("MAKE DIR :"+ AudioName + "....");  
	        	dir.mkdirs(); 
	        	return false;
	        }
	        
	        return true;
		}
		private void StoreAudio()														//�洢�û�¼��
		{
			if( lastAudioId != remoteAudioId )											//���û��л�
			{
				try
				{
		            if(lastAudioId != 0)												//0Ϊ��ʼID
		            {
		            	Iamhere("Write  AudioFile to " + lastAudioId + "/" + curfile + ".wav ....");
		            	
		            	fos.flush();fos.close();										//��֮ǰ�û�����ѹ��
		            																	//���֮ǰ�û������ļ�
		            	filehandle.copyWaveFile(AudioName + "/tmp/" + lastAudioId + "/" + curfile + ".tmp",
		            							AudioName + "/wav/" + lastAudioId + "/" + curfile + ".wav",sbufferSize);
		            	
		            }
		            
		            String userpath =  AudioName + remoteAudioId + "/";					//����·��
		            String wavpath = userpath + "wav/";
		            String tmpath = userpath + "tmp/";
	            	CheckDir(tmpath);													//�Ƿ���ڵ�ǰ�û��ļ��У�û���򴴽�
	            	CheckDir(wavpath);
	            	
	            	File dirFile = new File(tmpath);
	            	File[] files = dirFile.listFiles();
	            	curfile = files.length;												//��ȡ��ǰ��Ƶ�ļ���            	
					File file = new File(tmpath + curfile + ".tmp"); 
		            //if( file.exists() )	{ file.delete(); }							//ˢ���ļ�(����������)  
					fos = new FileOutputStream(file); 									//����һ���ɴ�ȡ�ֽڵ���

		            lastAudioId = remoteAudioId;
				}
				catch (Exception e) 
				{  
		            e.printStackTrace();  
		        } 
			}
			
			try
			{
				fos.write(decodeBuf, 0, decodeSize);									//������д��tmp�ļ�
			}
			catch (IOException e) 
			{  
	            e.printStackTrace();  
	        } 
		}
		private class MultiAudioOverTime extends TimerTask								//��ʱִ�м������(��һ��ʱ��û���յ��û�����)
		{
			public void run()
			{
				curtime = System.currentTimeMillis();									//��ȥ��ǰʱ��
				if( (curtime - startime > AUDIO_OVERTIME) && !overtimeflag)				//���ļ�д�볬ʱ
				{
	            	Iamhere("Write to AudioFile " + remoteAudioId + ".wav ....");
	            	overtimeflag = true;
	            	//���֮ǰ�û������ļ�						
	            	filehandle.copyWaveFile(AudioName  + remoteAudioId + "/tmp/"+ curfile + ".tmp",
							AudioName  + remoteAudioId + "/wav/"+ curfile + ".wav",sbufferSize);
				}				
			}
		}
		public void run() 
		{
			Iamhere("MultiAudioListen Thread running ...");
			
			Timer timer=new Timer();													
			timer.schedule(new MultiAudioOverTime(),500,AUDIO_OVERTIME/2);				//��ʱ���������
			Listen();

		}
	}

	//����͸������Ϣ���ͷ���
	public void MultiDataSend(byte[] cmd,int reqlength)									//͸����������
	{
		int length = reqlength >= 240 ? 240:reqlength;			

		ByteArrayReSet(userCmdBuffer, 10, 250);											//��շ����������
		System.arraycopy(cmd, 0, userCmdBuffer, 10, length);
		//У���ֶ���δ���
		MulticastSend(userCmdBuffer,Constant.bufferSize);
		
		Iamhere("MultiDataSend " + " ...");
		
	}
	public void MultiMsgSend(String msg)												//������Ϣ
	{
		byte[] msgbyte = msg.getBytes();												//��ȡ��Ϣbytes
		int length = msgbyte.length >= 240 ? 240:msgbyte.length;			

		ByteArrayReSet(msgSendBuffer, 10, 250);											//��շ����������
		System.arraycopy(msgbyte, 0, msgSendBuffer, 10, length);
		//У���ֶ���δ���
		MulticastSend(msgSendBuffer,Constant.bufferSize);
		
		Iamhere("MultiMsgSend " + msg + " ...");
		
	}
	public boolean MultiDataReceive(byte[] buf)											//͸����������
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
	public boolean MultiMsgReceive(byte[] buf)											//������Ϣ 
	{
		if(msgPackHead - 1 >= 0)														//ջ��ָ����ָ��ǰ�ɲ���λ��
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
	
	//CommunicationBridge�߳̽������ݱ���������������������ݻ��浽��Ӧ�����У���������Ӧ�㲥���ú���Ϊ�ⲿ���ʽӿ�
	public void MultiConlProt(boolean stat)												//����Э������߳�
	{
		if(stat == true)
		{
			parsepackflag = true;
			com = new CommunicationBridge();											//����Э������߳�
			upme = new UpdateMe();														
			chkuser = new CheckUserOnline();
			
			com.start();
			upme.start();																//������ʱע���߳�
			chkuser.start();															//��������û������߳�
			
		}
		else
		{
			parsepackflag = false;
			com.interrupt();
			upme.interrupt();
			chkuser.interrupt();
		}
		
	}
	public int GetPersonListSize()														//��ȡ�û��б��С
	{
		
		return personKeys.size();
	}
	public boolean GetPersonByNum(int num ,Person person)								//��ȡnum���û���Person���أ��������������û���
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
			personid = personKeys.get(num);												//��ȡnum���û�id
			mapuser = childrenMap.get(personid);										//��MAP�л�ȡpersonid�û�		
			person.CopyFrom(mapuser);													//���ݿ������ڷ���
			
			return true;
		}
	}
	public boolean GetPersonByID(int id ,Person p)										//��ȡid���û���Person����
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
	private class CommunicationBridge extends Thread									//Э���������ģ��
	{
		
		@Override
		public void run() 																//���鲥�˿ڣ�׼���鲥ͨѶ
		{
			super.run();
			while( parsepackflag && !this.isInterrupted())
			{
				//���������ݱ�ֱ�Ӷ���
				if(Constant.bufferSize == MulticastReceive(recvBuffer,Constant.bufferSize))
				{
					byte[] head = Constant.pkgHead;
					if( recvBuffer[0] == head[0]            &&
						recvBuffer[1] == head[1]            &&
						recvBuffer[2] == head[2]            &&							//����ͷ
						recvBuffer[250] == me.channel     	&&
						recvBuffer[251] == me.point										//�������
																						//���У�����δ����
					                                     		)
					{
						Iamhere("CommunicationBridge Receive Mask Packet ...");
						parsePackage(recvBuffer);
						
					}
				}
			}
			
		}
		private void parsePackage(byte[] pkg) 											//�������յ������ݰ�
		{
			int CMD 	= pkg[3];														//������
			int cmdType = pkg[4];														//��������
			int oprCmd 	= pkg[5];														//��������

			//����û�ID��
			byte[] uId = new byte[4];													//���ݱ��л�ȡ��ID
			System.arraycopy(pkg, 6, uId, 0, 4);
			int remoteId = byteArray2Int(uId);											//ת��Ϊint��ID
			
			switch (CMD) 
			{
				case Constant.CMD80:
				switch (cmdType) 
				{			
					case Constant.CMD_TYPE1:	
						if(remoteId != me.personId)										//�������Ϣ�����Լ���������Է����ͻ�Ӧ��,���ѶԷ������û��б�
						{
							Iamhere("remoteID:"+remoteId + "/me.personId:" + me.personId);
							if(!personKeys.contains(remoteId))							//���û�û��ע���
							{
								regBuffer[4] = Constant.CMD_TYPE2;
								MulticastSend(regBuffer,Constant.bufferSize);			//����Ӧ���
							}
							updatePerson(remoteId, pkg);								//ˢ���û��б�
						}
						
						break;
						
						case Constant.CMD_TYPE2:
							if(remoteId != me.personId)									//�û���Ӧ��ע��(���α���ע�����ݰ�)
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
			
				case Constant.CMD84:													//84���ָ���
					Iamhere("CommunicationBridge UserCMD() ...");
					UserCMD(cmdType,oprCmd);
				break;
				
				default:
				break;
			}
		}
		public void UserMsg(int cmdType,int oprCmd)										//ע����Ϣ������
		{
			switch (cmdType) 
			{
				case Constant.CMD_TYPE1:
					switch(oprCmd)
					{
						case Constant.OPR_CMD1:											//���յ���Ϣ
							if(msgPackHead < PACKBUFSIZE)								//�����򲻽���������
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
		public void UserCMD(int cmdType,int oprCmd)										//ָ�����
		{
			switch(cmdType)
			{
				case Constant.CMD_TYPE1:
					switch(oprCmd)
					{
						case Constant.OPR_CMD1:											//���յ�ָ��
							if(cmdPackHead < PACKBUFSIZE)								//�����򲻽���������
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
		private void updatePerson(int userId,byte[] pkg)								//���»���û���Ϣ���û��б���
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
		private void getPerson(byte[] pkg,Person person)								//�������ݰ�����ȡһ���û���Ϣ
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
			
	    	byte[] longitude = new byte[8];												//��ȡ����
	    	byte[] latitude = new byte[8];												//��ȡγ��
	    	System.arraycopy(pkg, 48,longitude, 0, 8);
	    	System.arraycopy(pkg, 56, latitude, 0, 8);
	    	person.longitude = getDouble(longitude);
	    	person.latitude = getDouble(latitude);
		}
	}
	private class UpdateMe extends Thread												//�޸�����ʱ�䣬����ʵʱ��,5s
	{
		@Override
		public void run() 
		{
			while(parsepackflag && !this.isInterrupted())
			{
				try
				{
					regBuffer[4] = Constant.CMD_TYPE1;									//�ָ���ע�������־����������ע���Լ�
					MulticastSend(regBuffer,Constant.bufferSize);						//����Ӧ���
					sleep(5000);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	private class CheckUserOnline extends Thread										//����û��Ƿ����ߣ��������15˵���û������ߣ�������б���������û�
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

	//�ಥ�ļ��շ�
	@SuppressWarnings("deprecation")
	public void MultiTransferFileStop(FILEOPTYPE type)									//ֹͣ�ļ�����
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
	public int MultiTransferFileCheck(FILEOPTYPE type)									//����ļ��շ�����
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
	public boolean MultiSendFileStart( String filepath, int delay)						//С�ļ�����
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
	public boolean MultiReceiveFileStart(int userid, long filesize, String filepath)	//����ĳ�˵�ĳ�ļ�
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
	private class MultiSendFile extends Thread											//�����ļ��߳�
	{
		private String filepath = null;
		private int delay = 100;														//������ʱ���ⶪ��
		private long filelength = 0;													//�ļ��ܴ�С
		private long sendlength = 0;													//�ļ��ѷ��ʹ�С
		
 		public MultiSendFile(String filepath , int delay)								//����ָ���ļ�
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
			byte[] sendBuf = new byte[518];												//���ͻ�����
			byte[] fileBuf = new byte[512];												//�ļ����ͻ�����
			int getlength = 0;															//���յ��ļ��γ���

			Arrays.fill(sendBuf , (byte)0);												//������ݻ�����
			Arrays.fill(fileBuf , (byte)0);
			
			try
			{
				File file = new File(filepath);
				FileInputStream fis = new FileInputStream(file);						//��ȡ�ļ���
				filelength = fis.available();
				
				sendBuf[0]=me.channel;														//���Ƶ����Ƶ��
				sendBuf[1]=me.point;
				
				byte[] id 		=	int2ByteArray(me.personId);
				byte[] userid 	= 	{ 0, 0, 0, 0 };
				System.arraycopy(id, 0, userid, 0, 4);									//��ȡID
				System.arraycopy(userid, 0, sendBuf, 2, 4);								//�������û�ID	

				while( fis.read(fileBuf) != -1 )										//ֱ���ļ���β
				{
					
					getlength  = fileBuf.length;										//��ȡ�ļ��ĳ���
					sendlength += getlength;											//�ѷ����ļ�����
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
	private class MultiReceiveFile extends Thread										//���ղ������ļ�
	{
		private int userid;
		private long filesize;
		private String filepath = null;
		private byte[] remoteAudioid 		= { 0, 0, 0, 0 };							//Զ���û�ID
		private int remoteAudioId;														//Զ���û�ID
		private long filelength = 0;													//�ļ��ܴ�С
		private long revlength = 0;														//�ļ��ѽ��մ�С		
		
		public MultiReceiveFile(int userid, long filesize, String filepath)				//����ĳ�˵�ĳ�ļ�							
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
			byte[] revbuf = new byte[518];												//���ջ�����
			int getlength = 0;															//�ļ�����
			
			try
			{
				File file = new File(filepath);
				FileOutputStream fis = new FileOutputStream(file);						//��ȡ�ļ�
			
				while( filesize > 0)
				{
					try
					{
						getlength = MulticastReceive( revbuf, revbuf.length) - 6;		//��ȡ��������
						System.arraycopy(revbuf, 2, remoteAudioid, 0, 4);				//��ȡԶ���û�ID
						remoteAudioId = byteArray2Int(remoteAudioid);					//��ȡID	
						if(me.channel == revbuf[0] && me.point == revbuf[1] && 			//��������(Ƶ��/Ƶ��/ID)
									userid == remoteAudioId  && getlength > 0)
						{
							filesize = filesize - getlength;
							revlength += getlength;
							fis.write(revbuf, 6, getlength);							//�洢�ļ���
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

	//��������
	private void initCmdBuffer()														//��ʼ�������ͻ������Ļ������ݶ�
	{
		//��ʼ���û�ע��ָ���
		Arrays.fill(regBuffer, (byte)0);
		System.arraycopy(Constant.pkgHead, 0, regBuffer, 0, 3);
		regBuffer[3] = Constant.CMD80;
		regBuffer[4] = Constant.CMD_TYPE1;
		regBuffer[5] = Constant.OPR_CMD1;
		
		//��ʼ����Ϣ����
		Arrays.fill(msgSendBuffer, (byte)0);
		System.arraycopy(Constant.pkgHead, 0, msgSendBuffer, 0, 3);
		msgSendBuffer[3] = Constant.CMD81;
		msgSendBuffer[4] = Constant.CMD_TYPE1;
		msgSendBuffer[5] = Constant.OPR_CMD1;

		//��ʼ��ָ���
		Arrays.fill(userCmdBuffer, (byte)0);
		System.arraycopy(Constant.pkgHead, 0, userCmdBuffer, 0, 3);
		userCmdBuffer[3] = Constant.CMD84;
		userCmdBuffer[4] = Constant.CMD_TYPE1;
		userCmdBuffer[5] = Constant.OPR_CMD1;
	}
	public boolean isExternalStorageWritable() 											//��ѯ�ⲿ�洢�Ƿ��д
	{
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) 
	    {
	        return true;
	    }
	    return false;
	}
	public boolean isExternalStorageReadable() 											//��ѯ�ⲿ�洢�Ƿ�ɶ�
	{
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state) ||
	        Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) 
	    {
	        return true;
	    }
	    return false;
	}
	public File getAlbumStorageDir(String albumName)									//��ȥ����·��
	{
	    // Get the directory for the user's public pictures directory. 
	    File file = new File(Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_PICTURES), albumName);
	    if (!file.mkdirs()) 
	    {
	        Iamhere("Directory not created");
	    }
	    return file;
	}
	
	//ͨ�ù����෽��
	public static String IpInt2String(int a)											//int��ipתstring
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
    public static int IpString2Int(String hostname) 									//��String���͵�Ip��ַת����int����
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
	private static void ByteArrayReSet(byte[] buf,int start , int end)					//����ָ��������(����start ������ end)
	{
		int count = 0;
		for(count = start; count < end ; count++)
		{
			buf[count] = 0;
		}
		
	}
	public static byte[] putDouble( double x) 											//double ת byte
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
    public static double getDouble(byte[] b) 											//byte ת double
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
	private static byte[] int2ByteArray(int value) 										//intתbyte
	{
		byte[] targets = new byte[4];

		targets[0] = (byte) (value & 0xff);												// ���λ 
		targets[1] = (byte) ((value >> 8) & 0xff);										// �ε�λ 
		targets[2] = (byte) ((value >> 16) & 0xff);										// �θ�λ 
		targets[3] = (byte) (value >>> 24);												// ���λ,�޷�������
		
		
		return targets; 
	}
	public static final int byteArrayToInt(byte[] b) 
	{
		return (b[0] << 24) + ((b[1] & 0xFF) << 16) + ((b[2] & 0xFF) << 8)
				+ (b[3] & 0xFF);
	}
	private static final int byteArray2Int(byte[] b) 									//byteתint
	{
		int targets = (b[0] & 0xff) | ((b[1] << 8) & 0xff00) | ((b[2] << 24) >>> 8) | (b[3] << 24); 
		
		return targets; 
	}
	public void Iamhere(String s)														//��Ƿ���
	{
		Log.d(TAG, "MulticComm: " + s);		
	}
	
}
