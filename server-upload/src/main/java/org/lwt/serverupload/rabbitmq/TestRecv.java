package org.lwt.serverupload.rabbitmq;

public class TestRecv {
	public static void main(String[] args) {
		String path = "C:\\Users\\Administrator\\Documents\\test\\recv\\";
		UploadFileImpl uploadimpl = new UploadFileImpl();
		try {
			uploadimpl.receiver(path);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
