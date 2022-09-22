package com.h.controller;

import com.h.common.Code;
import com.h.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.UUID;

/**
 * 文件上传下载处理
 */
@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    @Value("${Takeout.filePath.dishPhotoPath}")
    private String dishPhotoPath;

    /**
     * 文件上传，客服端上传的流转存磁盘
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {
        log.info("upload    name:{}    temp：{}", file.getOriginalFilename(), file.toString());
        //从文件名取后缀
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        //新文件名，以防后续同名覆盖；之前上传的文件没删除，被攻击？
        String fileName =UUID.randomUUID() + suffix;


        File dir = new File(dishPhotoPath);
        if(! dir.exists()){
            dir.mkdirs();//路径不存在则创建
        }
        try {//转存到磁盘
            file.transferTo(new File(dishPhotoPath + fileName));
            log.info("磁盘存入新文件：{}",dishPhotoPath + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //将新文件名响应给客户端，并由客服端请求此文件，以回显；并且文件名作为表的photo字段
        return R.success(Code.UPLOAD_OK, fileName);
    }

    /**
     * 文件下载，从磁盘响应到客服端
     * @param name
     * @param response
     * @return
     */
    @GetMapping("download")
    public /*R<String>*/void download(String name, HttpServletResponse response){
        //设置了返回值，前端图片不显示
        try {
            //从磁盘读取对应名文件对象
            FileInputStream inputStream = new FileInputStream(dishPhotoPath + name);
            log.info("请求文件：{}   路径：{}",name,dishPhotoPath + name);

            //设置响应内容格式为image/JPEG，
            response.setContentType("image/jpeg");
            ServletOutputStream outputStream = response.getOutputStream();

            //输入流读取，字节输出流写给客服端
            int len = 0;
            byte[] bytes = new byte[1024];
            while (-1 != (len = inputStream.read(bytes))){
                outputStream.write(bytes,0,len);
                outputStream.flush();
            }

            //return R.success(Code.DOWNLOAD_OK,"DOWNLOAD_OK");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //return R.error(Code.DOWNLOAD_ERR,"DOWNLOAD_ERR");
    }
}
