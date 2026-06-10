package com.banking.Banking.Entity;

import com.banking.Banking.validation.CustomNotFoundException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.List;

@Getter
@RequiredArgsConstructor
public enum MobileOperator {
    MTS("МТС", List.of("910", "911", "912", "913", "914", "915", "916", "917", "918", "919", "980", "981", "982", "983", "984", "985", "986", "987", "988", "989")),
    MEGAFON("Мегафон", List.of("920", "921", "922", "923", "924", "925", "926", "927", "928", "929", "999")),
    BEELINE("Билайн", List.of("903", "905", "906", "909", "960", "961", "962", "963", "964", "965", "966", "967", "968", "969")),
    TELE2("Теле2", List.of("950", "951", "952", "953", "958", "991", "992", "993", "994", "995", "996", "997"));

    private final String operatorName;
    private final List<String> operatorCode;

    public static MobileOperator getOperator(String rawPhone) {
        if (rawPhone == null)
            throw new CustomNotFoundException("Неизвестный мобильный оператор", "phone");

        String phoneDigits = rawPhone.replaceAll("\\D+", "");
        if (phoneDigits.length() == 11 && (phoneDigits.startsWith("7") || phoneDigits.startsWith("8"))) {
            String defCode = phoneDigits.substring(1, 4);
            for (MobileOperator operator : values()) {
                if (operator.getOperatorCode().contains(defCode)) {
                    return operator;
                }
            }
        }
        throw new CustomNotFoundException("Неизвестный мобильный оператор", "phone");
    }
}