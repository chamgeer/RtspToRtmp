package com.chamgeer.rtsptortmp;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author chamgeer
 * @Date 2021/9/14
 */
@Component
@ConfigurationProperties(prefix="video")
@ToString
public class InitailConfig {

    @Getter
    @Setter
    private String keepalive;//保活时长（分钟）
    @Getter
    @Setter
    private String push_ip;//推送地址
    @Getter
    @Setter
    private String push_port;//推送端口



}
