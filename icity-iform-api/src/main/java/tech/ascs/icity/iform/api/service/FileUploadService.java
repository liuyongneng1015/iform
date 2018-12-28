package tech.ascs.icity.iform.api.service;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tech.ascs.icity.iform.api.model.FileUploadModel;

import java.util.List;


@RestController
@RequestMapping("/file")
public interface FileUploadService {

	/**
	 * 文件上传
	 *
	 * @param file 文件
	 * @return
	 */
	@ApiOperation(value = "文件上传", notes = "文件上传", position = 0)
	@ApiImplicitParams({
		@ApiImplicitParam(paramType = "query", name = "file", value = "文件", required = true)
	})
	@PostMapping(value="/upload")
	FileUploadModel fileUpload(@RequestParam("file") MultipartFile file) ;


	/**
	 * 多文件上传
	 *
	 * @param files 多文件
	 * @return
	 */
	@ApiOperation(value = "文件上传", notes = "文件上传", position = 0)
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "query", name = "files", value = "多个文件", required = true)
	})
	@PostMapping(value="/batch/upload")
	List<FileUploadModel> fileUpload(@RequestParam("files") MultipartFile[] files) ;
}
