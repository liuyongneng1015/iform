package tech.ascs.icity.iform.api.service;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import tech.ascs.icity.iform.api.model.SynLog;

@RequestMapping("/synLog")
@Api(description = "动态表同步接口")
public interface SynLogService {
	


	    @ApiOperation("新增同步记录")
		@PostMapping
		public void add(@RequestBody SynLog synLog);

}
