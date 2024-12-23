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

@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
@Slf4j
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;

    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file) {      //这里的形参名称必要要和前端的请求参数名称保持一致才能接受到前端传过来的文件
        log.info("文件上传:{}", file);
        //为了防止文件重名而发生覆盖，我们需要对原始文件进行重命名
        try {
            String originalFilename = file.getOriginalFilename();

            //截取原始文件名后缀
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

            //构造新文件名称
            String objectName = UUID.randomUUID() + extension;

            //得到文件请求路径
            String filePath = aliOssUtil.upload(file.getBytes(), objectName);

            return Result.success(filePath);
        } catch (IOException e) {
            log.error("文件上传失败",e);
        }

        return Result.error(MessageConstant.UPLOAD_FAILED);
    }

}
