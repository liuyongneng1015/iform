package tech.ascs.icity.iform.controller;

import io.swagger.annotations.Api;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import sun.net.www.http.HttpCaptureInputStream;
import tech.ascs.icity.iform.IFormException;
import tech.ascs.icity.iform.api.model.FileUploadModel;
import tech.ascs.icity.iform.api.service.FileUploadService;
import tech.ascs.icity.iform.service.UploadService;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Api(tags = "文件上传服务", description = "文件上传服务")
@RestController
public class FileUploadController implements FileUploadService {

	private Logger log = LoggerFactory.getLogger(FileUploadController.class);

	@Autowired
	private UploadService uploadService;

	public FileUploadModel fileUpload(HttpServletRequest request) {
		log.error("fileUpload in ");
		MultipartFile file = ((MultipartHttpServletRequest)request).getFile("file");
		String fileSizeLimit = request.getParameter("fileSizeLimit");
		Integer size = null;
		if(StringUtils.isNoneBlank(fileSizeLimit)){
			size = Integer.parseInt(fileSizeLimit);
		}
		try {
			return uploadService.uploadOneFileReturnUrl(size, file);
		} catch (Exception e) {
			throw new IFormException("上传文件失败" + e.getMessage());
		}
	}

	public List<FileUploadModel> batchFileUpload(HttpServletRequest request) {
		List<MultipartFile> files =((MultipartHttpServletRequest)request).getFiles("file");
		String fileSizeLimit = request.getParameter("fileSizeLimit");
		Integer size = null;
		if(StringUtils.isNoneBlank(fileSizeLimit)){
			size = Integer.parseInt(fileSizeLimit);
		}
		List<FileUploadModel> list = new ArrayList<>();
		if(files != null && files.size() > 0) {
			for (MultipartFile file : files){
				try {
					list.add(uploadService.uploadOneFileReturnUrl(size, file));
				} catch (Exception e) {
					throw new IFormException("上传文件失败" + e.getMessage());
				}
			}
		}
		return list;
	}



	public String downloadTemplate(HttpServletResponse response, HttpServletRequest request) {
		String fileUrl = request.getParameter("url");
		try {
			if (fileUrl != null) {
				//设置文件路径
				URL url = new URL(fileUrl);
				InputStream inputStream = null;
				if (url != null) {
					inputStream = url.openStream();
					response.setContentType("application/force-download");// 设置强制下载不打开
					response.addHeader("Content-Disposition", "attachment;fileName=" + fileUrl.substring(fileUrl.lastIndexOf("/")+1));// 设置文件名
					byte[] buffer = new byte[2048];
					BufferedInputStream bis = null;
					try {
						bis = new BufferedInputStream(inputStream);
						OutputStream os = response.getOutputStream();
						int i = bis.read(buffer);
						while (i != -1) {
							os.write(buffer, 0, i);
							i = bis.read(buffer);
						}
						return "下载成功";
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						if (bis != null) {
							IOUtils.closeQuietly(bis);
						}
						if (inputStream != null) {
							IOUtils.closeQuietly(inputStream);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "下载失败";
	}
}
