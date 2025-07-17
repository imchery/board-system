package study.common.lib.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * 날짜/시간 처리 공통 유틸리티
 */
public class DateUtil {

    // 자주 사용하는 포맷터들
    public static final DateTimeFormatter YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter YYYY_MM_DD_HH_MM_SS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");
    public static final DateTimeFormatter HH_MM_SS = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static final DateTimeFormatter KOREAN_DATE = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");
    public static final DateTimeFormatter KOREAN_DATETIME = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분");

    /**
     * 현재 날짜/시간 문자열 반환
     */
    public static String now() {
        return LocalDateTime.now()
                .format(YYYY_MM_DD_HH_MM_SS);
    }

    /**
     * 현재 날짜 문자열 반환
     */
    public static String today() {
        return LocalDate.now()
                .format(YYYY_MM_DD);
    }

    /**
     * 현재 시간 문자열 반환
     */
    public static String currentTime() {
        return LocalTime.now()
                .format(HH_MM_SS);
    }

    /**
     * LocalDateTime을 지정된 포맷으로 변환
     */
    public static String formatDateTime(LocalDateTime dateTime, DateTimeFormatter formatter) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(formatter);
    }

    /**
     * LocalDate를 지정된 포맷으로 변환
     */
    public static String formatDate(LocalDate date, DateTimeFormatter formatter) {
        if (date == null) {
            return "";
        }
        return date.format(formatter);
    }

    /**
     * 한국어 날짜 포맷 (2025년 1월 17일)
     */
    public static String toKoreanDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(KOREAN_DATE);
    }

    /**
     * 한국어 날짜시간 포맷 (2025년 1월 17일 14시 30분)
     */
    public static String toKoreanDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(KOREAN_DATETIME);
    }

    /**
     * 상대 시간 표시 (방금 전, 5분 전, 1시간 전, 1일 전)
     */
    public static String getRelativeTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }

        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(dateTime, now);

        if (minutes < 1) {
            return "방금 전";
        } else if (minutes < 60) {
            return minutes + "분 전";
        }

        long hours = ChronoUnit.HOURS.between(dateTime, now);
        if (hours < 24) {
            return hours + "시간 전";
        }

        long days = ChronoUnit.DAYS.between(dateTime, now);
        if (days < 7) {
            return days + "일 전";
        }

        long weeks = ChronoUnit.WEEKS.between(dateTime, now);
        if (weeks < 4) {
            return weeks + "주 전";
        }

        long months = ChronoUnit.MONTHS.between(dateTime, now);
        if (months < 12) {
            return months + "개월 전";
        }

        long years = ChronoUnit.YEARS.between(dateTime, now);
        return years + "년 전";
    }

    /**
     * 두 날짜 사이의 일수 계산
     */
    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * 날짜가 특정 범위 내에 있는지 확인
     */
    public static boolean isDateBetween(LocalDate date, LocalDate startDate, LocalDate endDate) {
        if (date == null || startDate == null || endDate == null) {
            return false;
        }
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * 오늘인지 확인
     */
    public static boolean isToday(LocalDate date) {
        if (date == null) {
            return false;
        }
        return date.equals(LocalDate.now());
    }

    /**
     * 어제인지 확인
     */
    public static boolean isYesterday(LocalDate date) {
        if (date == null) {
            return false;
        }
        return date.equals(LocalDate.now()
                .minusDays(1));
    }

    /**
     * 이번 주인지 확인
     */
    public static boolean isThisWeek(LocalDate date) {
        if (date == null) {
            return false;
        }
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY);
        return isDateBetween(date, startOfWeek, endOfWeek);
    }

    /**
     * 이번 달인지 확인
     */
    public static boolean isThisMonth(LocalDate date) {
        if (date == null) {
            return false;
        }
        LocalDate today = LocalDate.now();
        return date.getYear() == today.getYear() && date.getMonth() == today.getMonth();
    }

    /**
     * 나이 계산 (만 나이)
     */
    public static int calculateAge(LocalDate birthDate) {
        if (birthDate == null) {
            return 0;
        }
        return Period.between(birthDate, LocalDate.now())
                .getYears();
    }

    /**
     * 평일인지 확인 (월~금)
     */
    public static boolean isWeekday(LocalDate date) {
        if (date == null) {
            return false;
        }
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }

    /**
     * 주말인지 확인 (토, 일)
     */
    public static boolean isWeekend(LocalDate date) {
        return !isWeekday(date);
    }

    /**
     * 월의 첫 번째 날
     */
    public static LocalDate getFirstDayOfMonth(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.withDayOfMonth(1);
    }

    /**
     * 월의 마지막 날
     */
    public static LocalDate getLastDayOfMonth(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.withDayOfMonth(date.lengthOfMonth());
    }
}
