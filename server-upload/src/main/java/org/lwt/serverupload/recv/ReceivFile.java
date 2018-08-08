package org.lwt.serverupload.recv;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.lwt.serverupload.rabbitmq.UploadFileImpl;
import org.lwt.serverupload.tools.EncryptUtil;
import org.lwt.serverupload.tools.FileUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping(value = "/upload")
public class ReceivFile {
	/**
	 * 返回上传文件的页面
	 * 
	 * @return
	 */
	@RequestMapping(value = "/selectfile", method = RequestMethod.GET)
	public String mapUploadView() {
		return "upload/upload";
	}

	@RequestMapping(value = "/rest", method = RequestMethod.GET)
	@ResponseBody
	public String test() {
		return "upload";
	}

	/**
	 * 保存文件
	 * 
	 * @param file
	 */
	@RequestMapping(value = "/chunk", method = RequestMethod.POST)
	@ResponseBody
	public String recvFile(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
		String contentType = file.getContentType();
		String fileName = file.getOriginalFilename();
		boolean success = false;
		// 创建一个临时目录，用来存放上传的文件
		String path = "";
		try {
			path = ResourceUtils.getURL("").getPath() + "customtemppath";
			path = path.substring(1, path.length());
			System.out.println(path);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		File tempFile = new File(path);
		if (!tempFile.exists()) {
			tempFile.mkdirs();
			System.out.println("创建文件成功。");
		}
		System.out.println(path + "\\" + fileName);
		/*
		 * try { // 将文件存储到本地 file.transferTo(new File(path+"\\"+fileName)); } catch
		 * (IllegalStateException e1) { e1.printStackTrace(); } catch (IOException e1) {
		 * e1.printStackTrace(); }
		 */
		try (BufferedInputStream inputStream = new BufferedInputStream(file.getInputStream());
				RandomAccessFile randomFile = new RandomAccessFile(path + "\\" + fileName, "rw")) {
			randomFile.seek(randomFile.length());
			byte[] buf = new byte[1024];
			int length = 0;
			while ((length = inputStream.read(buf)) != -1) {
				randomFile.write(buf, 0, length);
				System.out.println(length);
				System.err.println("文件的字节数组：" + buf);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			UploadFileImpl uploadimpl = new UploadFileImpl();
			success = uploadimpl.sendData(path + "\\" + fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		/*
		 * try { byte[] bytes = file.getBytes(); String encodeStr =
		 * EncryptUtil.encodeByBase64(bytes);
		 * System.err.println("加密后的文件字符串："+encodeStr); uploadimpl.sendData(encodeStr,
		 * fileName, "C:\\Users\\Administrator\\Documents\\test\\recv\\"); } catch
		 * (Exception e) { e.printStackTrace(); }
		 */
		System.out.println(contentType + "--文件名--" + fileName);
		if (success) {
			System.out.println("上传文件成功。");
			return "上传文件成功。";
		} else {
			System.out.println("上传文件失败。");
			return "上传文件失败。";
		}

	}
	/**
	 * 下载文件
	 * @param id
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@GetMapping(value="{id}")
	public void download(@PathVariable String id, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		System.out.println(id);
		String path = ResourceUtils.getURL("").getPath() + "customtemppath";
		path = path.substring(1, path.length());
		String fileName = "text.txt";
		try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(new File(path+"\\"+"test01.txt")));
				BufferedOutputStream outputStream = new BufferedOutputStream(response.getOutputStream());) {
			response.setContentType("application/x-download");
			//response.setContentType("multipart/form-data;charset=UTF-8");
			response.setHeader("Content-Disposition", "attachment;fileName="+  fileName +";filename*=utf-8''"+URLEncoder.encode(fileName,"UTF-8"));
			//response.addHeader("Content-Disposition", "attachment;filename=test.txt");
			IOUtils.copy(inputStream, outputStream);
			outputStream.flush();
		}

	}

}
