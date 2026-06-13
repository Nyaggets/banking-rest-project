package com.banking.Banking.Entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.List;

@Getter
@RequiredArgsConstructor
public enum MobileOperatorEnum {
    MTS("МТС", List.of("910", "911", "912", "913", "914", "915", "916", "917", "918", "919", "980", "981", "982", "983", "984", "985", "986", "987", "988", "989")),
    MEGAFON("МегаФон", List.of("920", "921", "922", "923", "924", "925", "926", "927", "928", "929", "902")),
    BEELINE("Билайн", List.of("903", "905", "906", "909", "960", "961", "962", "963", "964", "965", "966", "967", "968", "969")),
    TELE2("Теле2", List.of("950", "951", "952", "953", "958", "991", "992", "993", "994", "995", "996", "997"));

    private final String operatorName;
    private final List<String> operatorCodes;
}