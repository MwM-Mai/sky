package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * 通用接口
 */
@RestController
@RequestMapping("/admin/common")
@Slf4j
@Api(tags = "通用接口")
public class CommonController {

  @Autowired
  private AliOssUtil aliOssUtil;

  /**
   * 文件上传
   * @param file
   * @return
   */
  @PostMapping("/upload")
  @ApiOperation("图片上传")
  public Result<String> upload(MultipartFile file) {
	log.info("文件上传; {}", file);

	try {
	  String fileName = file.getOriginalFilename();
	  assert fileName != null;
	  String extension = fileName.substring(fileName.lastIndexOf("."));
	  String name = UUID.randomUUID().toString() + extension;
	  String filePath = aliOssUtil.upload(file.getBytes(), name);

	  return Result.success(filePath);
	} catch (Exception e) {
	  log.info("文件上传失败: {}", e.getMessage());
	  return Result.error(MessageConstant.UPLOAD_FAILED);
	}
  }
}
