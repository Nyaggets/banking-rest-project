package com.banking.Banking.Service;

import com.banking.Banking.Entity.MobileOperatorEnum;
import com.banking.Banking.validation.CustomNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class PhoneService {
    public String normalizePhone(String phone) {
        if (phone == null || phone.isBlank())
            return null;

        String phoneDigits = phone.replaceAll("[^\\d]", "");
        if (phoneDigits.length() == 11 && phoneDigits.startsWith("7"))
            phoneDigits = "8" + phoneDigits.substring(1);
        if (phoneDigits.length() == 10)
            return "8" + phoneDigits;

        return phoneDigits;
    }

    public MobileOperatorEnum getOperatorOrThrow(String rawPhone) {
        String phoneDigits = normalizePhone(rawPhone);
        if (phoneDigits == null || phoneDigits.length() != 11 || !(phoneDigits.startsWith("7") || phoneDigits.startsWith("8")))
            throw new CustomNotFoundException("Неизвестный мобильный оператор", "phone");

        String operatorCode = phoneDigits.substring(1, 4);
        for (MobileOperatorEnum operator : MobileOperatorEnum.values()) {
            if (operator.getOperatorCodes().contains(operatorCode)) {
                return operator;
            }
        }
        throw new CustomNotFoundException("Неизвестный мобильный оператор", "phone");
    }
}
