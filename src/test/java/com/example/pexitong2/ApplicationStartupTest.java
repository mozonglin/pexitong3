package com.example.pexitong2;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class ApplicationStartupTest {
    
    @Test
    public void contextLoads() {
        // 这个测试只是验证Spring上下文能够正常加载
        // 如果有Bean重复定义等问题，这里会失败
    }
}




