package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component //托管给 Spring 容器没有 @Component，Spring 就不会扫描这个类，也就无法实现自动化的配置注入和对象依赖管理。
@ConfigurationProperties(prefix = "sky.alioss")
//告诉 Spring：“请把配置文件中特定前缀（如 sky.alioss）下的值拿过来
@Data
//创建一个 Bean，将配置文件中的属性值注入到这个 Bean 中
public class AliOssProperties {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

}
