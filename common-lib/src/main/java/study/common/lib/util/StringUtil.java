package study.common.lib.util;

import java.util.Random;
import java.util.regex.Pattern;

/**
 * 문자열 처리 공통 유틸리티
 */
public class StringUtil {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^01[0-9]-?[0-9]{4}-?[0-9]{4}$");

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final Random RANDOM = new Random();

    /**
     * 문자열이 비어있는지 확인 (null, 공백 포함)
     *
     * @param str
     * @return
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * 문자열이 비어있지 않은지 확인
     *
     * @param str
     * @return
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 문자열 길이 검증 (min <= length <= max)
     *
     * @param str
     * @param min
     * @param max
     * @return
     */
    public static boolean isValidLength(String str, int min, int max) {
        if (isEmpty(str)) {
            return false;
        }
        int length = str.trim()
                .length();
        return length >= min && length <= max;
    }

    /**
     * 이메일 형식 검증
     *
     * @param email
     * @return
     */
    public static boolean isValidEmail(String email) {
        if (isEmpty(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email)
                .matches();
    }

    /**
     * 전화번호 형식 검증 (한국 휴대폰 번호)
     *
     * @param phone
     * @return
     */
    public static boolean isValidPhoneNumber(String phone) {
        if (isEmpty(phone)) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.replaceAll("-", ""))
                .matches();
    }

    /**
     * 이메일 마스킹 (a***@gmail.com)
     *
     * @param email
     * @return
     */
    public static String maskEmail(String email) {
        if (isEmpty(email) || !isValidEmail(email)) {
            return email;
        }

        int atIndex = email.indexOf("@");
        if (atIndex <= 1) {
            return email;
        }

        String localPart = email.substring(0, atIndex);
        String domainPart = email.substring(atIndex);

        // 첫 글자 + *** + 도메인
        return localPart.charAt(0) + "***" + domainPart;
    }

    /**
     * 전화번호 마스킹 (010-****-1234)
     *
     * @param phone
     * @return
     */
    public static String maskPhoneNumber(String phone) {
        if (isEmpty(phone)) {
            return phone;
        }

        String cleanPhone = phone.replaceAll("-", "");
        if (cleanPhone.length() != 11) {
            return phone;
        }

        return cleanPhone.substring(0, 3) + "-****-" + cleanPhone.substring(7);
    }

    /**
     * 이름 마스킹 (김**)
     *
     * @param name
     * @return
     */
    public static String maskName(String name) {
        if (isEmpty(name)) {
            return name;
        }

        if (name.length() <= 1) {
            return name;
        }

        return name.charAt(0) + "*".repeat(name.length() - 1);
    }

    /**
     * 랜덤 문자열 생성 (영문 + 숫자)
     *
     * @param length
     * @return
     */
    public static String generateRandomCode(int length) {
        if (length < 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    /**
     * 문자열 자르기 (길이 초과 시 ... 추가)
     *
     * @param str
     * @param maxLength
     * @return
     */
    public static String truncate(String str, int maxLength) {
        if (isEmpty(str) || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }

    /**
     * 한글 이름 검증 (2~4자, 한글만)
     *
     * @param name
     * @return
     */
    public static boolean isValidKoreanName(String name) {
        if (isEmpty(name)) {
            return false;
        }
        return name.matches("^[가-힣]{2,4}$");
    }

    /**
     * 비밀번호 강도 검증 (8자 이상, 영문+숫자+특수문자)
     *
     * @param password
     * @return
     */
    public static boolean isValidPassword(String password) {
        if (isEmpty(password) || password.length() < 8) {
            return false;
        }

        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");

        return hasLetter && hasDigit && hasSpecial;
    }

    /**
     * null을 빈 문자열로 변환
     *
     * @param str
     * @return
     */
    public static String nullToEmpty(String str) {
        return str == null ? "" : str;
    }

    /**
     * 빈 문자열을 null로 변환
     *
     * @param str
     * @return
     */
    public static String emptyToNull(String str) {
        return isEmpty(str) ? null : str;
    }
}
