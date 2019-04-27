package tech.ascs.icity.iform.api.service;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tech.ascs.icity.iform.api.model.FileUploadModel;
import tech.ascs.icity.iform.api.model.FileUploadType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/file")
public interface FileUploadService {

	/**
	 * 文件上传
	 *
	 * @param request 文件
	 * @return
	 */
	@ApiOperation(value = "文件上传", notes = "文件上传", position = 0)
	@ApiImplicitParams({
		@ApiImplicitParam(paramType = "query", name = "file", value = "文件", required = true)
	})
	@PostMapping(value="/upload")
	FileUploadModel fileUpload(HttpServletRequest request) ;


	/**
	 * 多文件上传
	 *
	 * @param request 多文件
	 * @return
	 */
	@ApiOperation(value = "文件批量上传", notes = "文件批量上传", position = 0)
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "query", name = "file", value = "多个文件", required = true)
	})
	@PostMapping(value="/batch/upload")
	List<FileUploadModel> batchFileUpload(HttpServletRequest request) ;


	@ApiOperation("下载功能")
	@GetMapping("/download")
	String downloadTemplate(HttpServletResponse response, HttpServletRequest request);


	@ApiOperation("解析excel文件")
	@PostMapping("/parseExcel")
	List<Map<String, Object>> parseExcel(HttpServletRequest request);

}
