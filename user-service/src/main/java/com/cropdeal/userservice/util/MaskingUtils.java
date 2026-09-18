package com.cropdeal.userservice.util;

public class MaskingUtils {

    public static String maskBankAccount(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return "XXXX";
        }
        String last4 = accountNumber.substring(accountNumber.length() - 4);
        return "XXXXXX" + last4;
    }
}
