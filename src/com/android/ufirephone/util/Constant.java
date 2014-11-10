package com.android.ufirephone.util;


public class Constant 
{
	public static final int bufferSize 		= 256;
	public static final int msgLength 		= 180;
	public static final int fileNameLength 	= 90;
	public static final int readBufferSize 	= 4096;							//ÎÄ¼þ¶ÁÐ´»º´æ
	public static final byte[] pkgHead 		= "CBT".getBytes();
	public static final int CMD80 			= 80;
	public static final int CMD81 			= 81;
	public static final int CMD82 			= 82;
	public static final int CMD83 			= 83;
	public static final int CMD84 			= 84;	
	public static final int CMD_TYPE1 		= 1;
	public static final int CMD_TYPE2 		= 2;
	public static final int CMD_TYPE3 		= 3;
	public static final int OPR_CMD1 		= 1;
	public static final int OPR_CMD2 		= 2;
	public static final int OPR_CMD3 		= 3;
	public static final int OPR_CMD4 		= 4;
	public static final int OPR_CMD5 		= 5;
	public static final int OPR_CMD6 		= 6;
	public static final int OPR_CMD10 		= 10;
	
	public static final String SERVER_IP	= "172.16.78.138";//"172.16.78.167";
	public static final String BROADCAST_IP = "255.255.255.255";
	public static final String MULTICAST_IP = "234.5.6.1";
	public static final int SERVER_PORT		= 5760;
	public static final int MAUDIO_PORT 	= 5761;
	public static final int PPORT 			= 8090;
	public static final int PAUDIO_PORT 	= 9080;
	
	
	public static String intToIp(int i) 													//INT to IP×ª»»
	{   
		String ip = ( (i >> 24) & 0xFF) +"."+((i >> 16 ) & 0xFF)+"."+((i >> 8 ) & 0xFF)+"."+(i & 0xFF );
		
		return ip;
	}
}
