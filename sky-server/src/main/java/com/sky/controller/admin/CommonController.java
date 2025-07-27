package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * @author 张立业
 * @version 1.0
 */
@Slf4j
@RequestMapping("/admin/common")
@Controller
@Api(tags = "文件上传")
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;
    //使用阿里云oss上传
    @PostMapping("/upload")
    @ResponseBody
    @ApiOperation("文件上传")
    public Result upload(MultipartFile file) {
        //获取原始文件名
        String originalFilename = file.getOriginalFilename();
        log.info("文件上传，原始文件名：{}", originalFilename);
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String url = null;
        try {
            //1.调用AliOssUtil工具类的upload文件上传方法
            String objectName = UUID.randomUUID().toString() + suffix;
            url = aliOssUtil.upload(file.getBytes(), objectName);
        } catch (IOException e) {
            log.info("文件上传失败！！！{}", e.getMessage());
            return Result.error("文件上传失败");
        }

        //2.返回图片路径结果
        return Result.success(url);
    }
}
