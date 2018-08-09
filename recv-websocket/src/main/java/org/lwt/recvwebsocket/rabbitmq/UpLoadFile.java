package org.lwt.recvwebsocket.rabbitmq;

import java.io.File;

import javax.jws.WebParam;
import javax.jws.WebService;

@WebService
public interface UpLoadFile {
	/**
	 * 	�ϴ��ļ�����
	 * 	@param file ���ļ����ֽ�������ܺ���ַ���
	 * 	@param fileName �ļ�����������չ����
	 *  @param tempPath ��ʱ�ļ�Ŀ¼
	 *  @return boolean ���ͳɹ�����true������ʧ�ܷ���false
	 */
	/*boolean sendData(@WebParam(name="file") String file,
			@WebParam(name="fileName")String fileName, 
			@WebParam(name="tempPath") String tempPath) throws Exception;*/
	boolean sendData(@WebParam(name="file") String file) throws Exception;
	
	void receiver(@WebParam(name="file") String file) throws Exception;
	
	/*void recv(String path, String fileName) throws Exception;*/
}
