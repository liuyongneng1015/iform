package tech.ascs.icity.iform.controller;

import com.googlecode.genericdao.search.Filter;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tech.ascs.icity.iform.IFormException;
import tech.ascs.icity.iform.api.model.*;
import tech.ascs.icity.iform.api.service.FileUploadService;
import tech.ascs.icity.iform.model.ColumnModelEntity;
import tech.ascs.icity.iform.model.DataModelEntity;
import tech.ascs.icity.iform.model.IndexModelEntity;
import tech.ascs.icity.iform.service.ColumnModelService;
import tech.ascs.icity.iform.service.DataModelService;
import tech.ascs.icity.iform.service.UploadService;
import tech.ascs.icity.jpa.dao.Query;
import tech.ascs.icity.jpa.tools.DTOTools;
import tech.ascs.icity.model.IdEntity;
import tech.ascs.icity.model.Page;
import tech.ascs.icity.utils.BeanUtils;

import java.util.ArrayList;
import java.util.List;

@Api(tags = "文件上传服务", description = "文件上传服务")
@RestController
public class FileUploadController implements FileUploadService {

	@Autowired
	private UploadService uploadService;


	@Override
	public String fileUpload(MultipartFile file) {
		try {
			return uploadService.uploadOneFileReturnUrl(file, false);
		} catch (Exception e) {
			throw new IFormException("上传文件失败" + e.getMessage());
		}
	}
}
