package org.lwt.recvwebsocket.test;

public class Test01 {
	public static void main(String[] args) {
		String fileName = "test.123.zip";
		String nameExcludeExt = fileName.substring(0, fileName.indexOf("."+"zip"));
		System.out.println(nameExcludeExt);
	}
}
