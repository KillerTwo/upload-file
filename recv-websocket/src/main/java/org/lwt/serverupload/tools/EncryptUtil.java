package org.lwt.serverupload.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
import sun.nio.ch.FileChannelImpl;
/**
 * 	MD5��base64������
 * 	@author Administrator
 *
 */
public class EncryptUtil {
	private static final char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f'};
	private static MessageDigest mMessageDigest = null;
	public static void main(String[] args) {
		
		//System.out.println(getMsg());
	/*	String data = "[115.0, 97.0, 103.0, 101.0, 46.0, 103.0, 101.0, 116.0, 66.0, 121.0, 116.0, 101.0, 115.0, 40.0, 41.0, 41.0, 59.0, 42.0, 47.0, 13.0, 10.0, 9.0, 9.0, 83.0, 121.0, 115.0, 116.0, 101.0, 109.0, 46.0, 111.0, 117.0, 116.0, 46.0, 112.0, 114.0, 105.0, 110.0, 116.0, 108.0, 110.0, 40.0, 34.0, 32.0, 91.0, 112.0, 114.0, 111.0, 100.0, 117.0, 99.0, 101.0, 114.0, 93.0, 32.0, 83.0, 101.0, 110.0, 116.0, 32.0, 39.0, 34.0, 32.0, 43.0, 32.0, 109.0, 101.0, 115.0, 115.0, 97.0, 103.0, 101.0, 66.0, 111.0, 100.0, 121.0, 66.0, 121.0, 116.0, 101.0, 115.0, 46.0, 116.0, 111.0, 83.0, 116.0, 114.0, 105.0, 110.0, 103.0, 40.0, 41.0, 32.0, 43.0, 32.0, 34.0, 39.0, 34.0, 41.0, 59.0, 13.0, 10.0, 9.0, 9.0, 13.0, 10.0, 9.0, 9.0, 99.0, 104.0, 97.0, 110.0, 110.0, 101.0, 108.0, 46.0, 99.0, 108.0, 111.0, 115.0, 101.0, 40.0, 41.0, 59.0, 13.0, 10.0, 9.0, 9.0, 99.0, 111.0, 110.0, 110.0, 101.0, 99.0, 116.0, 105.0, 111.0, 110.0, 46.0, 99.0, 108.0, 111.0, 115.0, 101.0, 40.0, 41.0, 59.0, 13.0, 10.0, 9.0, 125.0, 13.0, 10.0, 9.0, 13.0, 10.0, 9.0, 13.0, 10.0, 9.0, 13.0, 10.0, 9.0, 13.0, 10.0, 125.0, 13.0, 10.0, 35.0, 115.0, 112.0, 114.0, 105.0, 110.0, 103.0, 46.0, 114.0, 97.0, 98.0, 98.0, 105.0, 116.0, 109.0, 113.0, 46.0, 104.0, 111.0, 115.0, 116.0, 61.0, 49.0, 57.0, 50.0, 46.0, 49.0, 54.0, 56.0, 46.0, 49.0, 46.0, 51.0, 13.0, 10.0, 35.0, 115.0, 112.0, 114.0, 105.0, 110.0, 103.0, 46.0, 114.0, 97.0, 98.0, 98.0, 105.0, 116.0, 109.0, 113.0, 46.0, 112.0, 111.0, 114.0, 116.0, 61.0, 53.0, 54.0, 55.0, 50.0, 13.0, 10.0, 35.0, 115.0, 112.0, 114.0, 105.0, 110.0, 103.0, 46.0, 114.0, 97.0, 98.0, 98.0, 105.0, 116.0, 109.0, 113.0, 46.0, 117.0, 115.0, 101.0, 114.0, 110.0, 97.0, 109.0, 101.0, 61.0, 97.0, 108.0, 105.0, 99.0, 101.0, 13.0, 10.0, 35.0, 115.0, 112.0, 114.0, 105.0, 110.0, 103.0, 46.0, 114.0, 97.0, 98.0, 98.0, 105.0, 116.0, 109.0, 113.0, 46.0, 112.0, 97.0, 115.0, 115.0, 119.0, 111.0, 114.0, 100.0, 61.0, 49.0, 50.0, 51.0, 52.0, 53.0, 54.0, 13.0, 10.0, 13.0, 10.0, 115.0, 112.0, 114.0, 105.0, 110.0, 103.0, 46.0, 114.0, 97.0, 98.0, 98.0, 105.0, 116.0, 109.0, 113.0, 46.0, 104.0, 111.0, 115.0, 116.0, 61.0, 49.0, 48.0, 46.0, 49.0, 48.0, 46.0, 49.0, 48.0, 46.0, 49.0, 52.0, 13.0, 10.0, 115.0, 112.0, 114.0, 105.0, 110.0, 103.0, 46.0, 114.0, 97.0, 98.0, 98.0, 105.0, 116.0, 109.0, 113.0, 46.0, 118.0, 105.0, 114.0, 116.0, 117.0, 97.0, 108.0, 45.0, 104.0, 111.0, 115.0, 116.0, 61.0, 121.0, 100.0, 107.0, 112.0, 98.0, 109.0, 112.0, 13.0, 10.0, 115.0, 112.0, 114.0, 105.0, 110.0, 103.0, 46.0, 114.0, 97.0, 98.0, 98.0, 105.0, 116.0, 109.0, 113.0, 46.0, 112.0, 111.0, 114.0, 116.0, 61.0, 53.0, 54.0, 55.0, 50.0, 13.0, 10.0, 115.0, 112.0, 114.0, 105.0, 110.0, 103.0, 46.0, 114.0, 97.0, 98.0, 98.0, 105.0, 116.0, 109.0, 113.0, 46.0, 117.0, 115.0, 101.0, 114.0, 110.0, 97.0, 109.0, 101.0, 61.0, 121.0, 100.0, 117.0, 115.0, 101.0, 114.0, 13.0, 10.0, 115.0, 112.0, 114.0, 105.0, 110.0, 103.0, 46.0, 114.0, 97.0, 98.0, 98.0, 105.0, 116.0, 109.0, 113.0, 46.0, 112.0, 97.0, 115.0, 115.0, 119.0, 111.0, 114.0, 100.0, 61.0, 121.0, 100.0, 64.0, 117.0, 115.0, 101.0, 114.0, 13.0, 10.0, 35.0, 101.0, 120.0, 99.0, 104.0, 97.0, 110.0, 103.0, 101.0, 32.0, 110.0, 97.0, 109.0, 101.0, 13.0, 10.0, 109.0, 113.0, 46.0, 99.0, 111.0, 110.0, 102.0, 105.0, 103.0, 46.0, 101.0, 120.0, 99.0, 104.0, 97.0, 110.0, 103.0, 101.0, 61.0, 108.0, 111.0, 103.0, 46.0, 100.0, 105.0, 114.0, 101.0, 99.0, 116.0, 13.0, 10.0, 35.0, 113.0, 117.0, 101.0, 117.0, 101.0, 32.0, 110.0, 97.0, 109.0, 101.0, 13.0, 10.0, 109.0, 113.0, 46.0, 99.0, 111.0, 110.0, 102.0, 105.0, 103.0, 46.0, 113.0, 117.0, 101.0, 117.0, 101.0, 46.0, 105.0, 110.0, 102.0, 111.0, 61.0, 108.0, 111.0, 103.0, 46.0, 105.0, 110.0, 102.0, 111.0, 13.0, 10.0, 35.0, 114.0, 111.0, 117.0, 116.0, 105.0, 110.0, 103.0, 32.0, 107.0, 101.0, 121.0, 13.0, 10.0, 109.0, 113.0, 46.0, 99.0, 111.0, 110.0, 102.0, 105.0, 103.0, 46.0, 113.0, 117.0, 101.0, 117.0, 101.0, 46.0, 105.0, 110.0, 102.0, 111.0, 46.0, 114.0, 111.0, 117.0, 116.0, 105.0, 110.0, 103.0, 46.0, 107.0, 101.0, 121.0, 61.0, 108.0, 111.0, 103.0, 46.0, 105.0, 110.0, 102.0, 111.0, 46.0, 114.0, 111.0, 117.0, 116.0, 105.0, 110.0, 103.0, 46.0, 107.0, 101.0, 121.0, 13.0, 10.0, 13.0, 10.0, 109.0, 113.0, 46.0, 99.0, 111.0, 110.0, 102.0, 105.0, 103.0, 46.0, 113.0, 117.0, 101.0, 117.0, 101.0, 46.0, 101.0, 114.0, 114.0, 111.0, 114.0, 61.0, 108.0, 111.0, 103.0, 46.0, 101.0, 114.0, 114.0, 111.0, 114.0, 13.0, 10.0, 109.0, 113.0, 46.0, 99.0, 111.0, 110.0, 102.0, 105.0, 103.0, 46.0, 113.0, 117.0, 101.0, 117.0, 101.0, 46.0, 114.0, 111.0, 117.0, 116.0, 105.0, 110.0, 103.0, 46.0, 107.0, 101.0, 121.0, 61.0, 108.0, 111.0, 103.0, 46.0, 101.0, 114.0, 114.0, 111.0, 114.0, 46.0, 114.0, 111.0, 117.0, 116.0, 105.0, 110.0, 103.0, 46.0, 107.0, 101.0, 121.0, 13.0, 10.0, 13.0, 10.0, 13.0, 10.0, 13.0, 10.0, 13.0, 10.0, 99.0, 108.0, 97.0, 114.0, 101.0, 40.0, 81.0, 85.0, 69.0, 85.0, 69.0, 95.0, 78.0, 65.0, 77.0, 69.0, 44.0, 32.0, 102.0, 97.0, 108.0, 115.0, 101.0, 44.0, 32.0, 102.0, 97.0, 108.0, 115.0, 101.0, 44.0, 32.0, 102.0, 97.0, 108.0, 115.0, 101.0, 44.0, 32.0, 110.0, 117.0, 108.0, 108.0, 41.0, 59.0, 13.0, 10.0, 9.0, 9.0, 83.0, 121.0, 115.0, 116.0, 101.0, 109.0, 46.0, 111.0, 117.0, 116.0, 46.0, 112.0, 114.0, 105.0, 110.0, 116.0, 108.0, 110.0, 40.0, 34.0, -74.0, -45.0, -63.0, -48.0, 34.0, 43.0, 115.0, 117.0, 99.0, 99.0, 101.0, 115.0, 115.0, 46.0, 103.0, 101.0, 116.0, 81.0, 117.0, 101.0, 117.0, 101.0, 40.0, 41.0, 41.0, 59.0, 13.0, 10.0, 9.0, 9.0, 83.0, 121.0, 115.0, 116.0, 101.0, 109.0, 46.0, 111.0, 117.0, 116.0, 46.0, 112.0, 114.0, 105.0, 110.0, 116.0, 108.0, 110.0, 40.0, 34.0, -65.0, -51.0, -69.0, -89.0, -74.0, -53.0, -54.0, -3.0, -63.0, -65.0, 34.0, 43.0, 115.0, 117.0, 99.0, 99.0, 101.0, 115.0, 115.0, 46.0, 103.0, 101.0, 116.0, 67.0, 111.0, 110.0, 115.0, 117.0, 109.0, 101.0, 114.0, 67.0, 111.0, 117.0, 110.0, 116.0, 40.0, 41.0, 41.0, 59.0, 13.0, 10.0, 9.0, 9.0, 83.0, 121.0, 115.0, 116.0, 101.0, 109.0, 46.0, 111.0, 117.0, 116.0, 46.0, 112.0, 114.0, 105.0, 110.0, 116.0, 108.0, 110.0, 40.0, 34.0, -49.0, -5.0, -49.0, -94.0, -54.0, -3.0, 34.0, 43.0, 115.0, 117.0, 99.0, 99.0, 101.0, 115.0, 115.0, 46.0, 103.0, 101.0, 116.0, 77.0, 101.0, 115.0, 115.0, 97.0, 103.0, 101.0, 67.0, 111.0, 117.0, 110.0, 116.0, 40.0, 41.0, 41.0, 59.0, 13.0, 10.0, 9.0, 9.0, 83.0, 116.0, 114.0, 105.0, 110.0, 103.0, 32.0, 109.0, 101.0, 115.0, 115.0, 97.0, 103.0, 101.0, 32.0, 61.0, 32.0, 34.0, 72.0, 101.0, 108.0, 108.0, 111.0, 32.0, 87.0, 111.0, 114.0, 108.0, 100.0, 33.0, 34.0, 59.0, 13.0, 10.0, 9.0, 9.0, 99.0, 104.0, 97.0, 110.0, 110.0, 101.0, 108.0, 46.0, 98.0, 97.0, 115.0, 105.0, 99.0, 80.0, 117.0, 98.0, 108.0, 105.0, 115.0, 104.0, 40.0, 34.0, 34.0, 44.0, 32.0, 81.0, 85.0, 69.0, 85.0, 69.0, 95.0, 78.0, 65.0, 77.0, 69.0, 44.0, 32.0, 110.0, 117.0, 108.0, 108.0, 44.0, 32.0, 109.0, 101.0, 115.0]";
        data = data.replaceAll("[\\[\\]]", "");
        System.out.println(data);
        byte[] bytes = data.getBytes();
        System.out.println(bytes);*/
		
		String s = "hello world";
		String decodeStr = encodeByBase64(s);
		System.out.println("���ܺ�"+decodeStr);
		
		String sourceStr = decodeByBase64(decodeStr);
		System.out.println("���ܺ�"+sourceStr);
		
	}
	
	public static String getMsg() {
		
				// ���ݰ�
				Map<String, Object> pkMap = new HashMap<>();
				
				String md5 = getStringMD5("hello world...MD5");
				pkMap.put("user", "");
				pkMap.put("packcount", "");
				pkMap.put("packno", "");
				pkMap.put("curpacksize", "");
				pkMap.put("flag", "");
				pkMap.put("md5", md5);
				pkMap.put("ext", "");
				pkMap.put("allmd5", "");
				pkMap.put("data", "hello world...MD5");
				
				//Gson gson = new Gson();
				
				/*String pkMsg = gson.toJson(pkMap);*/
				String pkMsg = JsonUtil.getJsonFromMap(pkMap);
				System.out.println(pkMsg);
				System.out.println(getStringMD5(pkMsg));
		
		return pkMsg;
	}
	
	/**
	 * ��ȡ�ֽ������md5ֵ
	 * @param bytes
	 * @return String md5
	 * @throws Exception
	 */
	public static String getMD5String(byte[] bytes) {
		MessageDigest messagedigest = null;
		try {
			messagedigest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			
			e.printStackTrace();
		}
        messagedigest.update(bytes);  
        return bufferToHex(messagedigest.digest());  
	}
	 private static String bufferToHex(byte bytes[]) {
	     return bufferToHex(bytes, 0, bytes.length);  
	 }
	 private static String bufferToHex(byte bytes[], int m, int n) {
	     StringBuffer stringbuffer = new StringBuffer(2 * n);  
	     int k = m + n;  
	     for (int l = m; l < k; l++) {  
	             char c0 = hexDigits[(bytes[l] & 0xf0) >> 4];
	             char c1 = hexDigits[bytes[l] & 0xf];
	             stringbuffer.append(c0);  
	             stringbuffer.append(c1);  
	     }  
	     return stringbuffer.toString();  
	 }
	 
	public static String getStringMD5(String s) {
        MessageDigest mdInst;
        try {
            																// ���MD5ժҪ�㷨�� MessageDigest ����
            mdInst = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
 
        byte[] btInput = s.getBytes();
        																	// ʹ��ָ�����ֽڸ���ժҪ
        mdInst.update(btInput);
        																	// �������
        byte[] md = mdInst.digest();
        																	// ������ת����ʮ�����Ƶ��ַ�����ʽ
        int length = md.length;
        char str[] = new char[length * 2];
        int k = 0;
        for (byte b : md) {
            str[k++] = hexDigits[b >>> 4 & 0xf];
            str[k++] = hexDigits[b & 0xf];
        }
        return new String(str);
    }

	public static String getFileMD5(File file) {
		 
        FileInputStream in = null;
        FileChannel ch = null;
        ByteBuffer buf = null;
        try {
            in = new FileInputStream(file);
            ch = in.getChannel();
            long size = file.length();
            buf = ch.map(FileChannel.MapMode.READ_ONLY, 0, size);
            String md5 = MD5(buf);
            return md5;
        } catch (FileNotFoundException e) {
        	//e.printStackTrace();
            return "";
        } catch (IOException e) {
        	//e.printStackTrace();
            return "";
        } finally {
        	
            try {
            																// �ֶ�unmap,������ch.mapʱ�����ֶ�unmap����ɾ�����ļ�
				Method m = FileChannelImpl.class.getDeclaredMethod("unmap",
						MappedByteBuffer.class);  							//���ݷ������Ͳ��������б��ȡ�÷����ķ���
				m.setAccessible(true);  
				m.invoke(FileChannelImpl.class, buf);						// ������ö���Ͳ���������methodָ���Ķ���
			} catch (Exception e1) {
				e1.printStackTrace();
			}  
        	if(ch != null) {
            	try {
					ch.close();
					//System.out.println("ch close");
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
            if (in != null) {
                try {
                    in.close();
                    														//System.out.println("in close");
                } catch (IOException e) {
                    														// �ر��������Ĵ���һ�㶼���Ժ���
                	e.printStackTrace();
                }
            }
          
        }
        
    }

	/**
	 * �Զ����ȡmd5ֵ
	 * @param file
	 * @return ���ܺ���ַ���
	 */
	public static String getFileMD5String(List<byte[]> bytes) {
		
		 try {
		        mMessageDigest = MessageDigest.getInstance("MD5");
		    } catch (NoSuchAlgorithmException e) {
		        
		        e.printStackTrace();
		}

		
        try {
        	
            for (byte[] bs : bytes) {
            	mMessageDigest.update(bs);
			}
            return new BigInteger(1, mMessageDigest.digest()).toString(16);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
	}
	
	
	/**
     * 	����MD5У��
     * 	@param buffer
     * 	@return �մ�������޷���� MessageDigestʵ��
     */
    
    private static String MD5(ByteBuffer buffer) {
    	
        String s = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(buffer);
            byte tmp[] = md.digest(); 										// MD5 �ļ�������һ�� 128 λ�ĳ�������
            																// ���ֽڱ�ʾ���� 16 ���ֽ�
            char str[] = new char[16 * 2]; 									// ÿ���ֽ��� 16 ���Ʊ�ʾ�Ļ���ʹ�������ַ���
            																// ���Ա�ʾ�� 16 ������Ҫ 32 ���ַ�
            int k = 0; 														// ��ʾת������ж�Ӧ���ַ�λ��
            for (int i = 0; i < 16; i++) { 									// �ӵ�һ���ֽڿ�ʼ���� MD5 ��ÿһ���ֽ�
                															// ת���� 16 �����ַ���ת��
                byte byte0 = tmp[i]; 										// ȡ�� i ���ֽ�
                str[k++] = hexDigits[byte0 >>> 4 & 0xf]; 					// ȡ�ֽ��и� 4 λ������ת��, >>>,
                															// �߼����ƣ�������λһ������
                str[k++] = hexDigits[byte0 & 0xf]; 							// ȡ�ֽ��е� 4 λ������ת��
            }
            s = new String(str); 											// ����Ľ��ת��Ϊ�ַ���
 
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }finally {
        	
        }
        return s;
    }
    /**
     * 	ʹ��base64���ļ�����
     * 	@param s	Ҫ���ܵ��ַ���
     * 	@return	���ܺ���ַ���
     */
	public static String encodeByBase64(String s) {
		byte[] bytes = s.getBytes();
		return new BASE64Encoder().encode(bytes);
	}
	/**
	 * 
	 * 	ʹ��base64����
	 * 
	 * 	@param decodeStr	���ܺ���ַ���
	 * 	@return	���ܺ���ַ���
	 */
	public static String decodeByBase64(String decodeStr) {
		byte[] bytes;
		try {
			bytes = new BASE64Decoder().decodeBuffer(decodeStr);
			return new String(bytes,"utf-8");
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		return null;
	}
	
	 /**
     * 	ʹ��base64���ļ�����
     * 	@param s	Ҫ���ܵ��ַ���
     * 	@return	���ܺ���ַ���
     */
	public static String encodeByBase64(byte[] bytes) {
		
		return Base64.getEncoder().encodeToString(bytes);
	}
	
	/**
	 * 
	 * 	ʹ��base64����
	 * 
	 * 	@param decodeStr	���ܺ���ַ���
	 * 	@return	���ܺ���ַ���
	 */
	public static byte[] decodeByteByBase64(String decodeStr) {
		
		return Base64.getDecoder().decode(decodeStr); 
	}
	
}
