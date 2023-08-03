package com.atguigu.srb.oss.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.CannedAccessControlList;
import com.atguigu.srb.oss.service.FileService;
import com.atguigu.srb.oss.utils.OssProperties;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {


    @Override
    public String upload(InputStream inputStream, String module, String fileName) {
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(
                OssProperties.ENDPOINT,
                OssProperties.KEY_ID,
                OssProperties.KEY_SECRET);

        //判断BUCKET_NAME是否存在
        if (!ossClient.doesBucketExist(OssProperties.BUCKET_NAME)){
            ossClient.createBucket(OssProperties.BUCKET_NAME);
            ossClient.setBucketAcl(OssProperties.BUCKET_NAME, CannedAccessControlList.PublicRead);
        }

        // 上传文件流
        // 文件目录结构 "avatar/2023/7/27/uuid.jpg"
        String timeFolder = new DateTime().toString("/yyyy/MM/dd/");
        //文件名替换
        fileName = UUID.randomUUID() + fileName.substring(fileName.lastIndexOf("."));
        String key = module + timeFolder + fileName;
        ossClient.putObject(OssProperties.BUCKET_NAME, key, inputStream) ;

        // 关闭OSSClient
        ossClient.shutdown();
        //返回文件的url地址
        return "https://" + OssProperties.BUCKET_NAME + "." + OssProperties.ENDPOINT + "/" + key;
    }

    @Override
    public void removeFile(String url) {
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(
                OssProperties.ENDPOINT,
                OssProperties.KEY_ID,
                OssProperties.KEY_SECRET);

        String host = "https://" + OssProperties.BUCKET_NAME + "." + OssProperties.ENDPOINT + "/";
        String objectName = url.substring(host.length());

        //删除文件
        ossClient.deleteObject(OssProperties.BUCKET_NAME, objectName);
        // 关闭OSSClient
        ossClient.shutdown();
    }

}
