package com.zyd.ecmall;

import com.zyd.ecmall.dto.MemberCreateRequest;
import com.zyd.ecmall.dto.MemberUpdateRequest;
import com.zyd.ecmall.entity.Member;
import com.zyd.ecmall.exception.DuplicateEmailException;
import com.zyd.ecmall.mapper.MemberMapper;
import com.zyd.ecmall.service.MemberService;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;

class MemberProfileTest {
    @Test
    void editingProfilePreservesPasswordUnlessExplicitlyChanged() throws Exception {
        var source = new UnpooledDataSource("org.h2.Driver",
                "jdbc:h2:mem:profile;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        try (var connection = source.getConnection(); var statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA ec_mall");
            statement.execute("""
                    CREATE TABLE ec_mall.members (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255), email VARCHAR(255) UNIQUE,
                        age INT, password_hash VARCHAR(255) NOT NULL,
                        role VARCHAR(10) DEFAULT 'USER', created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)
                    """);
        }
        Configuration configuration = new Configuration(new Environment("test", new JdbcTransactionFactory(), source));
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.addMapper(MemberMapper.class);
        try (var sql = new SqlSessionFactoryBuilder().build(configuration).openSession(true)) {
            MemberMapper mapper = sql.getMapper(MemberMapper.class);
            MemberService service = new MemberService(mapper, new BCryptPasswordEncoder());
            MemberCreateRequest registration = new MemberCreateRequest();
            registration.setName("山田");
            registration.setEmail("first@example.com");
            registration.setAge(20);
            registration.setPassword("pass1234");
            Member created = service.createMember(registration);
            String oldHash = mapper.selectByEmail("first@example.com").getPasswordHash();

            MemberUpdateRequest edit = new MemberUpdateRequest();
            edit.setName("山田太郎");
            edit.setEmail("renamed@example.com");
            service.updateMember(created.getId(), edit);
            assertEquals(oldHash, mapper.selectByEmail("renamed@example.com").getPasswordHash());
            assertEquals(created.getId(), service.login("renamed@example.com", "pass1234").getId());

            edit.setPassword("updated123");
            service.updateMember(created.getId(), edit);
            assertNotEquals(oldHash, mapper.selectByEmail("renamed@example.com").getPasswordHash());
            assertEquals(created.getId(), service.login("renamed@example.com", "updated123").getId());

            registration.setEmail("second@example.com");
            service.createMember(registration);
            edit.setEmail("second@example.com");
            assertThrows(DuplicateEmailException.class, () -> service.updateMember(created.getId(), edit));
        }
    }
}
