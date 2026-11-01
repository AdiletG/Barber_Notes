package kg.barbernotes.barbernotes.common.util;


import kg.barbernotes.barbernotes.common.enums.ErrorCode;
import kg.barbernotes.barbernotes.common.exceptions.BusinessRuleViolationException;

public final class PhoneNormalizer {
    public final static String PHONE_RULES =
            "^(996\\d{9}|0\\d{9}|\\d{9})$";
    public final static String PHONE_ERROR_TEXT =
            "Номер телефона должен состоять только из цифр: 12 с кодом 996, или 10 с ведущим 0, или 9 без кода";

    private PhoneNormalizer() {}

    public static String mask(String phone) {
        if (phone == null || phone.isEmpty()) return null;

        int number = Math.max(0, phone.length() - 4);

        return "*".repeat(number)
                + phone.substring(number);
    }

    public static String normalize(String raw){
        if(raw == null) return null;

        String digits = raw.replaceAll("\\D", "");

        if(digits.length() == 10 && digits.startsWith("0")){
            return "996" +  digits.substring(1);
        }

        if(digits.length() == 9){
            return "996" + digits;
        }

        if (!digits.matches("^996\\d{9}$")) {
            throw new BusinessRuleViolationException(
                    ErrorCode.INVALID_INPUT,
                    "Номер телефона должен иметь формат 996XXXXXXXXX"
            );
        }


        return digits;
    }
}