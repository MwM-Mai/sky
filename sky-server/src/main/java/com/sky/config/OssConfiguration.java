package com.sky.config;

import com.sky.properties.AliOssProperties;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class OssConfiguration {

  // AliOssProperties 参数注入
  @Bean
  @ConditionalOnMissingBean // 保证整个ioc容器只有一个AliOssUtil对象
  public AliOssUtil aliOssUtil(AliOssProperties aliOssProperties) {
	return new AliOssUtil(aliOssProperties.getEndpoint(),
			aliOssProperties.getAccessKeyId(),
			aliOssProperties.getAccessKeySecret(),
			aliOssProperties.getBucketName());
  }
}
