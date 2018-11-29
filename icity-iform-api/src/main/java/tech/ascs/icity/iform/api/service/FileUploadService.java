package tech.ascs.icity.iform.api.service;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tech.ascs.icity.iform.api.model.FormInstance;
import tech.ascs.icity.model.IdEntity;
import tech.ascs.icity.model.Page;

import java.util.List;
import java.util.Map;

@RequestMapping("/file-upload")
public interface FileUploadService {

	/**
	 * 文件上传
	 *
	 * @param file 文件
	 * @return
	 */
	@ApiOperation(value = "文件上传", notes = "文件上传", position = 0)
	@GetMapping("/{listId}")
	@ApiImplicitParams({
		@ApiImplicitParam(paramType = "query", name = "file", value = "文件", required = false)
	})
	@RequestMapping(value="/uploadimg", method = RequestMethod.POST)
	String fileUpload(@RequestParam("file") MultipartFile file) ;


}
