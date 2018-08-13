package org.lwt.recvwebsocket.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.websocket.Session;

import org.apache.commons.io.IOUtils;
import org.lwt.recvwebsocket.websocket.WebSocketServerEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value="/file")
public class DownloadController {

	@Autowired
	private WebSocketServerEndpoint webSocketServerEndpoint;
	/**
	 *  	下载文件
	 * 	@param id
	 * 	@param request
	 * 	@param response
	 * 	@throws Exception
	 */
	@GetMapping(value="/download/{fileName}/{ext}")
	public void download(@PathVariable String fileName, @PathVariable String ext, 
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		System.out.println(fileName);
		System.out.println(ext);
		String path = ResourceUtils.getURL("").getPath() + "recvtemp";
		path = path.substring(1, path.length());
		File recvTemp = new File(path);
		if(!recvTemp.exists()) {
			//recvTemp.mkdirs();
			//return "";
			System.out.println("文件不存在。。。");
		}
		System.out.println(path);
		try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(new File(path+"\\"+fileName+"."+ext)));
				BufferedOutputStream outputStream = new BufferedOutputStream(response.getOutputStream());) {
			response.setContentType("application/x-download");
			//response.setContentType("multipart/form-data;charset=UTF-8");
			response.setHeader("Content-Disposition", "attachment;fileName="+  fileName+"."+ext +";filename*=utf-8''"+URLEncoder.encode(fileName+"."+ext,"UTF-8"));
			//response.addHeader("Content-Disposition", "attachment;filename=test.txt");
			IOUtils.copy(inputStream, outputStream);
			outputStream.flush();
			
			System.out.println("文件下载完成。。。");
		}
		if(new File(path+"\\"+fileName+"."+ext).exists()) {
			if(new File(path+"\\"+fileName+"."+ext).delete()) {
				System.out.println("文件已经被下载，删除临时文件成功。。。");
			}
		}

	}
	@RequestMapping(value="/sessiontest")
	public void test(HttpSession httpSession) {
		String userId = "userId";
		webSocketServerEndpoint.sendMessage(userId, "hello world.");
	}
	
}
