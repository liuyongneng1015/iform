package tech.ascs.icity.iform.controller;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tech.ascs.icity.iform.IFormException;
import tech.ascs.icity.iform.api.service.FileUploadService;
import tech.ascs.icity.iform.service.UploadService;

@Api(tags = "文件上传服务", description = "文件上传服务")
@RestController
public class FileUploadController implements FileUploadService {

	@Autowired
	private UploadService uploadService;


	@Override
	public String fileUpload(MultipartFile file) {
		try {
			return uploadService.uploadOneFileReturnUrl(file);
		} catch (Exception e) {
			throw new IFormException("上传文件失败" + e.getMessage());
		}
	}
}
