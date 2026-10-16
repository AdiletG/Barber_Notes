package kg.barbernotes.barbernotes.common.util;

import lombok.Data;

@Data
public final class PhoneNormalizer {
    private PhoneNormalizer() {}

    public static String normalizer(String raw){
        if(raw == null) return null;

        String digits = raw.replaceAll("\\D", "");

        if(digits.length() == 10 && digits.startsWith("0")){
            return "996" +  digits.substring(1);
        }

        if(digits.length() == 9){
            return "996" + digits;
        }

        return digits;
    }
}