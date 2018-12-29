package tech.ascs.icity.iform.controller;

import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tech.ascs.icity.iform.IFormException;
import tech.ascs.icity.iform.api.model.FileUploadModel;
import tech.ascs.icity.iform.api.service.FileUploadService;
import tech.ascs.icity.iform.service.UploadService;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Api(tags = "文件上传服务", description = "文件上传服务")
@RestController
public class FileUploadController implements FileUploadService {

	@Autowired
	private UploadService uploadService;

	public FileUploadModel fileUpload(HttpServletRequest request) {
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
}
