package study.content.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * MongoDB 설정
 * - Auditing 활성화: @CreatedDate, @LastModifiedDate 자동 관리
 */
@Configuration
@EnableMongoAuditing // MongoDB Auditing 기능 활성화
public class MongoConfig {
}
