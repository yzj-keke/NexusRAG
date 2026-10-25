package com.nexusrag;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * NexusRAG 后端服务启动类。
 *
 * @author yangzijie
 * @date 2026/10/26
 */
@SpringBootApplication
public class NexusRagApplication {

    /**
     * 启动 NexusRAG 后端服务。
     *
     * @param args 应用启动参数
     * @author yangzijie
     * @date 2026/10/26
     */
    public static void main(String[] args) {
        SpringApplication.run(NexusRagApplication.class, args);
    }
}

