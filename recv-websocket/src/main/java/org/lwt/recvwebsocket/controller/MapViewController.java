package org.lwt.recvwebsocket.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MapViewController {
	@GetMapping("/test")
	public String test() {
		return "upload/upload";
	}
}
