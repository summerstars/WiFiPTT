/*�ļ�������
 * 
 * 
 */
package com.android.ufirephone.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class HandleFile 
{
	private boolean flag;
	private static String matches = "[A-Za-z]:\\\\[^:?\"><*]*";	
	//sPath.matches(matches) �����ķ���ֵ�ж��Ƿ���ȷ 
	
	public boolean deleteDirectory(String sPath) 
	{
	    if (!sPath.endsWith(File.separator)) 											//���sPath�����ļ��ָ�����β���Զ�����ļ��ָ���  
	    {  
	        sPath = sPath + File.separator;  
	    }  
	    File dirFile = new File(sPath);  
	    
	    if (!dirFile.exists() || !dirFile.isDirectory()) 								//���dir��Ӧ���ļ������ڣ����߲���һ��Ŀ¼�����˳�  
	    {  
	        return false;  
	    }  
	    flag = true;  
	    //ɾ���ļ����µ������ļ�(������Ŀ¼)  
	    File[] files = dirFile.listFiles();  
	    for (int i = 0; i < files.length; i++) 
	    {  
	         
	        if (files[i].isFile()) 														//ɾ�����ļ� 
	        {  
	            flag = deleteFile(files[i].getAbsolutePath());  
	            if (!flag) break;  
	        }  
	        else 																		//ɾ����Ŀ¼ 
	        {  
	            flag = deleteDirectory(files[i].getAbsolutePath());  
	            if (!flag) break;  
	        }  
	    }  
	    if (!flag) 
	    {
	    	return false;  
	    }
	     
	    if (dirFile.delete()) 															//ɾ����ǰĿ¼ 
	    {  
	        return true;  
	    } 
	    else 
	    {  
	        return false;  
	    }  
	}
	public boolean deleteFile(String sPath) 
	{
	    File file = new File(sPath); 	    
	    if (file.isFile() && file.exists()) 											// ·��Ϊ�ļ��Ҳ�Ϊ�������ɾ��  
	    {  
	        file.delete();  
	        return true;  
	    }  
	    return false;  
	}
	public boolean DeleteFolder(String sPath) 
	{
	    File file = new File(sPath);  
	     
	    if (!file.exists())																// �ж�Ŀ¼���ļ��Ƿ����  
	    { 
	        return false;  
	    } 
	    else 
	    {
	        if (file.isFile()) 															// Ϊ�ļ�ʱ����ɾ���ļ�����
	        {    
	            return deleteFile(sPath);  
	        } 
	        else 																		// ΪĿ¼ʱ����ɾ��Ŀ¼����
	        {    
	            return deleteDirectory(sPath);  
	        }  
	    }  
	}	
	public void copyWaveFile(String inFilename, String outFilename,int sbufferSize) 	//���������ļ�תΪ��Ƶ�ļ�
	{  
        FileInputStream in = null;  
        FileOutputStream out = null;  
        long totalAudioLen = 0;  
        long totalDataLen = totalAudioLen + 36;  
        long longSampleRate = 8000;  
        int channels = 1;  
        long byteRate = 16 * longSampleRate * channels / 8;  
        byte[] data = new byte[sbufferSize];  
        try 
        {  
            in = new FileInputStream(inFilename);  
            out = new FileOutputStream(outFilename);  
            totalAudioLen = in.getChannel().size();  
            totalDataLen = totalAudioLen + 36;  
            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,  
                    longSampleRate, channels, byteRate);  
            while (in.read(data) != -1) 
            {  
                out.write(data);  
            }  
            in.close();  
            out.close();  
        } 
        catch (FileNotFoundException e) 
        {  
            e.printStackTrace();  
        } 
        catch (IOException e) 
        {  
            e.printStackTrace();  
        }  
    } 
	
	private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,			//�����Ƶ�ļ�ͷ 
									long totalDataLen, long longSampleRate, int channels, long byteRate)  throws IOException 
    {  
        byte[] header = new byte[44];  
        header[0] = 'R'; 																// RIFF/WAVE header  
        header[1] = 'I';  
        header[2] = 'F';  
        header[3] = 'F';  
        header[4] = (byte) (totalDataLen & 0xff);  
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);  
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);  
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);  
        header[8] = 'W';  
        header[9] = 'A';  
        header[10] = 'V';  
        header[11] = 'E';  
        header[12] = 'f'; 																// 'fmt ' chunk  
        header[13] = 'm';  
        header[14] = 't';  
        header[15] = ' ';  
        header[16] = 16; 																// 4 bytes: size of 'fmt ' chunk  
        header[17] = 0;  
        header[18] = 0;  
        header[19] = 0;  
        header[20] = 1; 																// format = 1  
        header[21] = 0;  
        header[22] = (byte) channels;  
        header[23] = 0;  
        header[24] = (byte) (longSampleRate & 0xff);  
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);  
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);  
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);  
        header[28] = (byte) (byteRate & 0xff);  
        header[29] = (byte) ((byteRate >> 8) & 0xff);  
        header[30] = (byte) ((byteRate >> 16) & 0xff);  
        header[31] = (byte) ((byteRate >> 24) & 0xff);  
        header[32] = (byte) (2 * 16 / 8); 												// block align  
        header[33] = 0;  
        header[34] = 16; 																// bits per sample  
        header[35] = 0;  
        header[36] = 'd';  
        header[37] = 'a';  
        header[38] = 't';  
        header[39] = 'a';  
        header[40] = (byte) (totalAudioLen & 0xff);  
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);  
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);  
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);  
        out.write(header, 0, 44);  
    }
}
