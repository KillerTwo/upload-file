package org.lwt.serverupload.rabbitmq;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
/**
 * 	��ʱ����
 * 	������ʹ�ط�ʱ�䣬
 * 	�����ָ��ʱ���ڻ�û�н��յ��������ط�����
 * 	�����ʱ���ڽ��յ�������ֹͣ��ʱ��
 * 	@author Administrator
 *
 */
public class CountDown {
	private int limitSec;
	private int curSec;
	private boolean responseFlag;
	public CountDown(int limitSec, boolean responseFlag) throws InterruptedException{
		this.limitSec = limitSec;
		this.curSec = limitSec;
		this.responseFlag = responseFlag;
		//System.out.println("count down from "+limitSec+" s ");
		if(!responseFlag) {																			// ����ڽ����ʱ��֮ǰ��û���յ���Ӧ�򲻽���ʱ�������򲻾��˼�ʱ
			Timer timer = new Timer();
			timer.schedule(new TimerTask(){
				public void run(){
					//System.out.println("Time remians "+ --curSec +" s");
					if(responseFlag) {
						return ;
					}
				}
			},0,1000);
			TimeUnit.SECONDS.sleep(limitSec);
			timer.cancel();
		}
	}

}
