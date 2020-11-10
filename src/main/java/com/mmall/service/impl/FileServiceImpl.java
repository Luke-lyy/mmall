package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.mmall.service.IFileService;
import com.mmall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
@Service("iFileService")
public class FileServiceImpl implements IFileService {

    private Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    public String upload(MultipartFile file,String path){
        //获取文件名，如abc.jpg
        String filename = file.getOriginalFilename();
        //获取扩展名jpg
        String fileExtensionName = filename.substring(filename.lastIndexOf(".")+1);
        //給文件设置名称，如li.jpg
        String uploadFilename = UUID.randomUUID().toString()+"."+fileExtensionName;
        logger.info("文件开始上传，上传的文件名:{},上传的路径:{},新的文件名:{}",filename,path,uploadFilename);

        File fileDir = new File(path);
        if (!fileDir.exists()){
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }
        File targetFile = new File(path,uploadFilename);

        try {
            file.transferTo(targetFile);
            //文件上传成功
            FTPUtil.uploadFile(Lists.newArrayList(targetFile));
            //将targetFile上传到我们的FTP服务器上

            targetFile.delete();
            //传完之后，删除upload文件夹下面的文件

        } catch (IOException e) {
            logger.error("上传文件异常",e);
            return null;
        }
        return targetFile.getName();
    }
}
