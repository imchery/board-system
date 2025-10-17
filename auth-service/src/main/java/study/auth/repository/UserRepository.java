package study.auth.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import study.auth.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    /**
     * username 으로 사용자 찾기
     * 로그인, 중복 체크에 사용
     *
     * @param username 사용자명
     * @return User 객체
     */
    Optional<User> findByUsername(String username);

    /**
     * email로 사용자 찾기
     * 중복 체크, 비밀번호 찾기에 사용
     *
     * @param email 이메일주소
     * @return User 객체
     */
    Optional<User> findByEmail(String email);

    /**
     * username 존재 여부 확인
     * 회원가입 시 중복 체크
     *
     * @param username 사용자명
     * @return 중복여부
     */
    boolean existsByUsername(String username);

    /**
     * email 존재 여부 확인
     * 회원가입 시 중복 체크
     *
     * @param email 이메일주소
     * @return 중복여부
     */
    boolean existsByEmail(String email);
}
