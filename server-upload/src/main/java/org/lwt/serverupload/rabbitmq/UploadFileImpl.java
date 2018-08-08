package org.lwt.serverupload.rabbitmq;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.TimeoutException;

import javax.jws.WebService;

import org.apache.commons.codec.digest.DigestUtils;
import org.lwt.serverupload.tools.EncryptUtil;
import org.lwt.serverupload.tools.FileUtils;
import org.lwt.serverupload.tools.JsonUtil;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.AMQP.Basic.Return;
import com.rabbitmq.client.AMQP.BasicProperties;

/**
 *  	��װ���ݴ���ͽ��շ�����
 * @author Administrator
 *
 */
@WebService(serviceName="UpLoadFile")
@SuppressWarnings("deprecation")
public class UploadFileImpl implements UpLoadFile {
	
	
	/*private String username = "yduser";										// �û���
	private String password = "yd@user";									// ����
	private String vhost = "ydkpbmp";										// ��������
	private String ip = "10.10.10.14";		*/							// ����ip
	private String username = "alice";										// �û���
	private String password = "123456";									// ����
	private String vhost = "vhost_01";										// ��������
	private String ip = "192.168.1.3";
	private int port = 5672;												// ���ʶ˿�
	private Connection connection = null;									// tcp����
	private Channel channel = null;											// �ŵ�
	private String callbackQueueName = null;								// ��Ӧ������
	private boolean responseFlag = false;									// �Ƿ���Ҫ�ط������Ϊtrue����Ҫ�ط������Ϊfalse����Ҫ�ط���
	private File sourceFile = null;											// ���ϴ��ļ���File����Դ�ļ�File
	private boolean openFileFlag = true;									// ����ǽ��յ���һ�����򴴽�һ���ļ��洢���յ�������
    private File targetFile = null;											// Ŀ���ļ�	File
    private int recvPackCount = 0;											// ���ڼ������յ������ݰ�����
    private boolean isReSend = false;										// �жϽ��յ��������Ƿ�Ϊ�ط�����
    private static boolean firstRecv = false;								// ���Ϊtrue��ʾ�������ݽ���
    private static int sendTime = 0;										// �������ݵĴ���
    private List<byte[]> recvList = 
    		new ArrayList<>();												// ������Ž��յ��������ֽ������list
    private List<Map<String, Object>> recvListMap = 
    		new ArrayList<>();												// ������Ž��յ�����ʱ���Ѿ�ͨ����֤�����ݣ�����packNum,packSize,byte[]����ѭ�������ݴ浽�ļ���
    private String filenameTemp = null;
	public UploadFileImpl() {
		Connection connection = getConnection(ip, 
				port, username, password, vhost);							// ������������������
		try {
			this.channel = connection.createChannel();						//����ŵ�
			this.callbackQueueName = channel.queueDeclare().
					getQueue();												// ���ûص�����
		} catch (IOException e) {
			e.printStackTrace();
		}						
	}
	
	public UploadFileImpl(String ip, int port, 
			String username, String password, String vhost) {
		super();
		this.ip = ip;
		this.port = port;
		this.username = username;
		this.password = password;
		this.vhost = vhost;
	}
	
	
	/********************************************************/
	@Override
	public boolean sendData(String path) throws Exception{
		sourceFile = new File(path);
		connection = getConnection(ip, port, username, password, vhost);	// ������������������
		channel = connection.createChannel();								//����ŵ�
		callbackQueueName = channel.queueDeclare().getQueue();				// ���ûص�����
		
		/**
		 * 	������Ӧ��Ϣ�� ���趨��ʱ���ڽ��յ���Ӧ�����ǳɹ����յ���Ӧ��
		 * 	responseFlag��־����Ϊtrue
		 * 	��ʾ����Ҫ�ط����ݣ��ڽ��յ�ʧ�ܵ���Ӧ��responseFlag��
		 * 	־����Ϊfalse��ʾ��Ҫ���·���һ������
		 */
		try {
			UploadFileImpl.this.channel.basicConsume(callbackQueueName, 
					new DefaultConsumer(channel) {
				@Override
				public void handleDelivery(String consumerTag, 
						Envelope envelope, BasicProperties properties,
						byte[] body)
						throws IOException {
					System.err.println("接收到响应");
					String response = new String(body, "utf-8");
					Map<String, Object> resMap = JsonUtil.
							getMapFromJson(response);
					if("0".equals(resMap.get("status"))) {					// ������ص�״̬��Ϊ0����ճɹ�
						System.out.println("发送成功");
						responseFlag = true;
						sendTime = 0;
					}
					System.out.println(response);
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		/****************************************/
		sendTask();
		int temp = 0;	// 用来防阻进入无限循环
		System.out.println("sendTime "+sendTime+
				" || responseFlag "+responseFlag);							// ������ɵ�һ������(�ط�����С��3����û���յ���Ӧ�����ѭ��)
		while(sendTime <= 3 && !responseFlag) {
			System.out.println("进入while循环 ");
			temp++;
			if(temp > 3) {
				return false;
			}
			if(firstRecv) {
				
				new CountDown(10, responseFlag);							// ��ʱ10����
				System.out.println("判断是否需要重发数据");
																			// �ڸ�����ʱ���ڽ��յ���Ӧ
				if(responseFlag) {
					firstRecv = false;
				}else {														// û���ٸ�����ʱ���ڽ��յ���Ӧ
					firstRecv = true;
				}
				if(firstRecv) {
					System.err.println("重发数据第  "+sendTime+" 次");
					sendTask();
				}else {
					
					System.out.println("不需要重发");
					return true;
				}
			}
		}
		return false;
	}
	
	public void sendTask() {
		try {
			String exchangeName = "myexchanges05";							//����������
			channel.exchangeDeclare(exchangeName, "direct", true);
			String routingKey = "myroutingkey05";							//����routing-key
			List<byte[]> byteList = FileUtils.splitDemo(sourceFile);		//���ļ���֣�ÿ��Ϊ1024�ֽڣ�
			toSend(byteList, sourceFile, channel, exchangeName, 
					routingKey, callbackQueueName, null);					// ��������		
			System.out.println("发送数据结束");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/*******************************************************/
	@Override
	public void receiver(String path) throws Exception {
		/*byte[] tempByte = EncryptUtil.decodeByteByBase64(path);
		targetFile = FileUtils.bytes2File(tempByte,"C:\\RabbitMqTemp\\", fileName);*/
		recvPackCount = 0;
		Connection connection = getConnection(ip, port, 
				username, password, vhost);									// ������������������
	    Channel channel = connection.createChannel();						
		String exchangeName = "myexchanges05";								
		channel.exchangeDeclare(exchangeName, "direct", true);				//����������
		String queueName = channel.queueDeclare().getQueue();				//��������							
		String routingKey = "myroutingkey05";								//����routing-key
        channel.queueBind(queueName, exchangeName, routingKey);				//�󶨶��У�ͨ���� routingKey �����кͽ�����������
        /**
         * 	������Ϣ
         */
		
        boolean autoAck = false;											// ����Ϊ�ֶ�ȷ��
        String consumerTag = "";
        
        channel.basicConsume(queueName, autoAck, consumerTag,
        		new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag,
                                       Envelope envelope,
                                       AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                BasicProperties props = properties;
                BasicProperties replyProps = new BasicProperties()
                		.builder().build();
                System.err.println("接收到响应");
                if(isReSend) {												 // �ж��Ƿ�Ϊ�ط����ݣ�������ط����ѱ����ļ�ɾ���������ط�����
                	recvList.clear();
                	recvListMap.clear();
                	if(targetFile != null && targetFile.exists()) {
                		if(targetFile.delete()) {
                			System.out.println("删除已存在文件"
                					+ "准备重新接收数据");
                		}
                	}
                	isReSend = false;
                }
                recvPackCount++;	                                        // ���յ��İ���������1			
                String bodyStr = new String(body,"utf-8");					//���յ�����Ϣ
                //System.out.println("���ն˵�json�ַ�����"+bodyStr);
                Map<String,Object> map = JsonUtil.
                		getMapFromJson(bodyStr);							// ���յ�����Դ����Ϊmap����													
                if(openFileFlag) {											// ����ǵ�һ�ν����ļ��򴴽�Ŀ���ļ�
                	
                	try {
                		
                		File temp = new File(path);
                		if(!temp.exists()) {
                			temp.mkdirs();
                			System.err.println("创建文件成功");
                		}
						targetFile = new File(path+map.
								get("fileName")+"."+map.get("ext"));			// �����ļ����·��
						if(!targetFile.exists()) {
							targetFile.createNewFile();
							System.out.println("创建文件成功1");
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
                	openFileFlag = false;
                }
                byte[] bytes = EncryptUtil
                		.decodeByteByBase64((String)map.get("data"));		// �����յ����������ݽ���Ϊ�ֽ����飬������뵽�ļ���
                String recMd5 = "";												
                try {
                	recMd5 = EncryptUtil.getMD5String(bytes);				// ��ý��յ����ֽ������md5ֵ
				} catch (Exception e) {
					e.printStackTrace();
				}
                String response = "is ok...";								// ��Ӧ����Ϣ
                System.err.println("验证MD5值");
                if(map.get("md5").equals(recMd5)) {
                	System.err.println("MD5验证通过");
                	recvList.add(bytes);									// ��֤ͨ�����ֽ�������ӵ�recvList��
                	Map<String, Object> writeMap = new HashMap<>();
                	writeMap.put("data", recvList.size()-1);				// ���recvList�ж������ݵ�������
                	int packNum = 0;
                	long packSize = 0;
                	try {
						packNum = (int) map.get("packnum");					// ��ǰ�������
						packSize = new Long((int) map.get("packSize"));		// ÿ�����Ĵ�С
						writeMap.put("packNum", packNum);
						writeMap.put("packSize", packSize);
						recvListMap.add(writeMap);
					} catch (Exception e) {
						System.out.println("。。。");
						e.printStackTrace();
					}					
	                try {
						channel.basicAck(envelope.getDeliveryTag(), 
								false);										//������Ϣȷ����Ϣ
					} catch (Exception e1) {
						e1.printStackTrace();
					}
                }
                BigDecimal size = null;						
				try {
					size = (BigDecimal)map.get("packcount");				// ���Ͷ˷��͵İ���������
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				if(recvPackCount != size.intValue() && 						// ����Ѿ����յ����һ������ ������������һ�����ʾ�����ˣ����Ѿ�����ļ�ɾ��
						(int)map.get("flag") == 2) {
					isReSend = true;										// ���ط���־����Ϊtrue,��ʾ�ڴ�֮���ٽ��յ�������Ϊ�ط�����
                	recvPackCount = 0;										// �����յ��İ���������Ϊ0
					if(targetFile != null && targetFile.exists()) {
						if(targetFile.delete()) {
							System.out.println("删除文件成功");
						}
					}
                	System.out.println("丢包");
                }
                if(recvPackCount == size.intValue()) {						// ������յ������ݰ������ͷ��͵�������ͬ���ʾû�ж���
                	isReSend = true;										// ���ط���־����Ϊtrue,��ʾ�ڴ�֮���ٽ��յ�������Ϊ�ط�����
                	recvPackCount = 0;										// �����յ��İ���������Ϊ0
                	System.out.println("文件MD5验证通过");
                	String fileMD5 = 
                			EncryptUtil.getFileMD5String(recvList);			// ��ȡrecvList���ֽ������MD5ֵ�����ļ���md5ֵ��
                	System.out.println("allMD5== "+map.get("allMD5"));
                	System.out.println("fileMD5== "+fileMD5);
                	if(map.get("allMD5").equals(fileMD5)) {					//��������ļ�md5У��ͨ���򷵻ؽ��ճɹ�����Ӧ��
                		System.err.println("all md5验证。。。");
                    	Map<String, Object> responseMap = 
                    			new HashMap<>();
                    	responseMap.put("packid", map.get("packid"));
                    	responseMap.put("status", "0");
                    	response = JsonUtil.getJsonFromMap(responseMap);
                    	// �ڷ�����Ӧǰ˯15000����
                    	/*try {
							Thread.sleep(15000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}*/
                    	/**	���յ����еİ����ٷ���һ����Ӧ
                    	 *	 �õ�replyQueue������Ϊrouting key��������Ϣ
                         * 
                         * **/
                    	try {
                    		/*System.out.println("验证文件MD5");
                    		byte[] recvBytes = new byte[(int) targetFile.length()];
                    		recvBytes = FileUtils.toByteArray(targetFile);
                    		String decodeStr = EncryptUtil.encodeByBase64(recvBytes);	// ���ļ��ֽ��������base64���ܺ���ַ���
*/							System.out.println("返回响应。。。");
                    		channel.basicPublish("", 
									props.getReplyTo(), replyProps, 
									response.getBytes("UTF-8"));
						} catch (Exception e) {
							e.printStackTrace();
						}finally {
							try(RandomAccessFile randomFile = 
									new RandomAccessFile(targetFile, "rw");){	// �ڴ�д���ļ�
								for (Map<String, Object> m : recvListMap) {
									FileUtils.writeToFile(randomFile, 
											recvList.get((int)m.get("data")), 
											(int)m.get("packNum"), 
											(long)m.get("packSize"));
								}
							}catch(Exception e) {
								e.printStackTrace();
							}
						}
                	}else {
                		try {
                			if(targetFile.delete()) {
                				System.out.println("删除文件成功");				// ��������ļ���md5ֵ��֤��ͨ�����򽫸��ļ�ɾ���󷵻ش�����Ӧ
                			}
						} catch (Exception e) {
							e.printStackTrace();
						}
                	}
                }
            }
        });
        System.out.println("ִ�д���������");
	}
	
	/**
	 * 	�������ݵ�rabbitMQ
	 * 	@param byteList	�ļ��İ��ֽ�����
	 * 	@param file		�ļ�file
	 * 	@param channel	RabbitMQ�ŵ�
	 * 	@param exchangeName	RabbitMQ ��������
	 * 	@param routingKey	RabbitMQ ·�ɼ�
	 * 	@return long			����һ�����а����������ʱ��ʱ��
	 */
	private static long toSend(List<byte[]> byteList, File file,
			Channel channel, String exchangeName, 
			String routingKey, String callbackQueueName,
			QueueingConsumer consumer){
		//String fileMD5 = EncryptUtil.getFileMD5(file);					//��ȡ���ϴ��ļ���MD5
		System.out.println("开始发送数据。。。");
		String fileMD5 = "";
		FileInputStream in = null;
		try {
			in = new FileInputStream(file);
			fileMD5 = DigestUtils.md5Hex(in);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		String fileName = file.getName();
		for(int i = 0; i < byteList.size(); i++) {							// �ֿ�����ÿһ���ֵ�����
			String data = getFilePack(byteList.get(i), 
					fileMD5, byteList.size(), i, fileName);
			try {
				call(data,callbackQueueName, channel,
						consumer, exchangeName, routingKey);				// ��������
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		firstRecv = true;													// ��ʾ�����Ѿ����ͳɹ�
		sendTime++;															// ���ʹ�����1
		System.out.println("for循环发送数据结束");
		return System.currentTimeMillis();									// ���һ�����ݰ�������ɺ󷵻�һ��������ɵ�ʱ��
	}
	
	/**
	 * 	 ��������
	 * 
	 * 	@param message	���͵�����
	 * 	@param replyQueueName	�ص�����
	 * 	@param channel		�ŵ�
	 * 	@param consumer		
	 * 	QueueingConsumer consumer = new QueueingConsumer(channel);
	 * 	@return				��Ӧ��Ϣ
	 * 	@throws Exception	
	 */
	private static void call(String message, 
			String replyQueueName, Channel channel, 
			QueueingConsumer consumer,
			String exchangeName, String routingkey) 
					throws Exception {     
        BasicProperties props = new BasicProperties							//��װreplyQueue���ԣ��ص����У�����������Ӧ��
                                    .Builder()
                                    .replyTo(replyQueueName)
                                    .build();
        channel.basicPublish(exchangeName, 
        		routingkey, props, message.getBytes("utf-8"));				 //������Ϣ��������֮ǰ��װ�õ�����replyTo=��Ӧ���ص�������
    }
	
	/**
	 * 	��ȡ����json�ַ���
	 * 	@param bytes		ÿ���ֶε��ֽ�����
	 * 	@param fileMD5	�����ļ���MD5ֵ
	 * 	@param count		�����ļ��İ���
	 * 	@param pkSerial	����ţ���ǰ�ǵڼ�������
	 * 	@return	String	����json�ַ���
	 */
	private static String getFilePack(byte[] bytes, 
			String fileMD5, double count, int pkSerial,
			String fileName){
		Map<String, Object> map = new HashMap<>();
		String md5 = null;
		try {
			md5 = EncryptUtil.getMD5String(bytes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String[] fileNames = fileName.split("\\.");
		map.put("fileName", fileNames[0]);
		map.put("ext", fileNames[1]);
		map.put("date", System.currentTimeMillis()+1);
		map.put("packid", fileMD5);											// ���ļ���md5ֵ��Ϊ����idֵ
		map.put("packcount", count);										//�����ϴ������������ֳɶ��ٸ�С��
		if(pkSerial==0) {
			map.put("flag", 0);
		}else if(pkSerial == count-1) {
			map.put("flag", 2);
		}else {
			map.put("flag", 1);
		}
		map.put("md5", md5);												// ��ǰ�����ݵ�md5
		map.put("packnum", pkSerial);										// ��ǰ�����
		map.put("date", System.currentTimeMillis());						// ���Ͱ���ʱ��
		try {
			map.put("data", EncryptUtil.encodeByBase64(bytes));
		} catch (Exception e) {
			e.printStackTrace();
		}
		map.put("packSize", new Long(1024));
		map.put("allMD5", fileMD5);
		String data = JsonUtil.getJsonFromMap(map);
		return data;
	}
	/**
	 * 
	 *	 ��ȡһ������
	 * 
	 * 	@param host	����ip
	 * 	@param port	���Ӷ˿�
	 * 	@param userName	�����û���
	 * 	@param password	��������
	 * 	@param vhost		��������
	 * 	@return			Connection����������
	 */
	private static Connection getConnection(String host, 
			int port, String userName, String password, 
			String vhost) {
		ConnectionFactory factory = new ConnectionFactory();				//�������ӹ���
		Connection connection = null;
		factory.setUsername(userName);										// �����û�������
		factory.setPassword(password);
		factory.setVirtualHost(vhost);
		factory.setHost(host);												// ����rabbitMq��������ַ
		factory.setPort(port);
		try {
			connection = factory.newConnection();							// ������������������
			return connection;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
		return null;
	}
}
