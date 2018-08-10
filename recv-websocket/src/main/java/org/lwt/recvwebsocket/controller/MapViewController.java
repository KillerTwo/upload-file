package org.lwt.recvwebsocket.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class MapViewController {
	@GetMapping("/test")
	public String test() {
		return "upload/download";
	}
	
	/**
	 * 返回上传文件的页面
	 * 
	 * @return
	 */
	@RequestMapping(value = "/selectfile", method = RequestMethod.GET)
	public String mapUploadView() {
		return "upload/upload";
	}
}
