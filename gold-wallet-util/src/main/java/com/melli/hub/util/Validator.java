package com.melli.hub.util;

import com.melli.hub.util.date.DateUtils;
import org.apache.http.util.TextUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StringUtils;

import java.math.BigInteger;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validator {

    private static final Logger log = LogManager.getLogger(Validator.class);
    private final static Pattern pattern = Pattern.compile("^989\\d{9}$");
    private static final Pattern IPV4_PATTERN = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
    private static final Pattern PERSIAN_BIRTHDATE_PATTERN = Pattern.compile("^[1-4]\\d{3}((0[1-6]((3[0-1])|([1-2][0-9])|(0[1-9])))|((1[0-2]|(0[7-9]))(30|([1-2][0-9])|(0[1-9]))))$");
    private final static int CIF_LENGTH = 10;
    private final static int ACC_LENGTH = 13;
    private final static int IBAN_LENGTH = 26;
    private final static String COUNTRY_CODE = "IR";
    private final static String SAMAN_IBAN = "56";
    private static String TEJARAT = "627353";
    private static String KOSAR = "505801";

    public static boolean checkPhoneNumber(String mobile) {
        Matcher matcher = pattern.matcher(mobile);
        return matcher.matches();
    }

    public static boolean isValidPurchaseTime() {
        return LocalTime.now().isAfter(LocalTime.parse("00:15:00")) && LocalTime.now().isBefore(LocalTime.parse("23:40:00"));
    }

    public static boolean isNull(String data) {
        return !StringUtils.hasText(data);
    }

    public static String cleanPhoneNumber(String mobile) {
        if (isNull(mobile)) {
            return mobile;
        }
        String removeExtraCharacter = mobile.replaceAll("\\D", "");

        if (removeExtraCharacter.startsWith("09")) {
            return removeExtraCharacter.replaceAll("^09", "9");
        }

        if (removeExtraCharacter.startsWith("989")) {
            return removeExtraCharacter.replaceAll("^989", "9");
        }

        return removeExtraCharacter;
    }

    public static boolean isValidCif(String cif) {

        if (org.apache.commons.lang.StringUtils.isBlank(cif)) {
            return false;
        }

        if (!cif.matches("^[0-9]{2,10}")) {
            return false;
        }

        if (cif.length() != CIF_LENGTH) {
            return false;
        }

        if (cif.equals("0000000000")) {
            return false;
        }

        return true;
    }

    public static boolean isValidNationalCode(String nationalCode) {

        if (org.apache.commons.lang.StringUtils.isBlank(nationalCode)) {
            return false;
        }

        String nationalId = normalNationalCode(nationalCode);

        if (nationalId.equalsIgnoreCase("1111111111") || nationalId.equalsIgnoreCase("2222222222") || nationalId.equalsIgnoreCase("3333333333") || nationalId.equalsIgnoreCase("4444444444") || nationalId.equalsIgnoreCase("5555555555")
            || nationalId.equalsIgnoreCase("6666666666") || nationalId.equalsIgnoreCase("7777777777") || nationalId.equalsIgnoreCase("8888888888") || nationalId.equalsIgnoreCase("9999999999") || nationalId.equalsIgnoreCase("0000000000")) {
            return false;
        }

        List<String> nationalIdList = new ArrayList(Arrays.asList(nationalId.split("")));
        String lastChar = nationalIdList.get(9);
        nationalIdList.remove(9);

        int i = 10;
        int sum = 0;

        for (String value : nationalIdList) {
            sum += Integer.valueOf(value) * i;
            i--;
        }

        int mod = sum % 11;

        if (mod >= 2) {
            mod = 11 - mod;
        }

        return mod == Integer.parseInt(lastChar);

    }

    public static String normalCif(String cif) {
        String normalCif = cleanBody(cif);
        return padWithZero(normalCif, CIF_LENGTH);
    }

    public static String normalNationalCode(String nationalCode) {
        String normalNationalCode = cleanBody(nationalCode);
        return padWithZero(normalNationalCode, CIF_LENGTH);
    }

    public static String padWithZero(String command, int length) {

        String padMessage = command;

        while (padMessage.length() < length) {
            padMessage = "0" + padMessage;
        }
        return padMessage;
    }

    public static String cleanBody(String body) {

        String cleanBody = "";
        if (!StringUtils.hasText(body)) {
            return cleanBody;
        }
        cleanBody = PersianUtils.fromPersianNumeric(body);
        return cleanBody.replaceAll("\\D", "");
    }

    public static String normalMobile(String body) {

        String cleanBody = "";
        if (!StringUtils.hasText(body)) {
            return cleanBody;
        }
        cleanBody = PersianUtils.fromPersianNumeric(body);
        return cleanBody.replaceAll("\\D", "").replaceAll("^09", "9");
    }


    //valid birthDate 13650126
    public static boolean isValidBirthDate(String birthDate) {

        if (!StringUtils.hasText(birthDate)) {
            log.error("birthdate is null!!!");
            return false;
        }

        if (birthDate.length() != 8) {
            log.error("birthdate ({}) length is not equal 8 character!!", birthDate);
            return false;
        }

        Matcher matcher = PERSIAN_BIRTHDATE_PATTERN.matcher(birthDate);

        if (!matcher.matches()) {
            log.error("format birthdate ({}) is not valid!!", birthDate);
            return false;
        }
        return true;

    }

    public static String cleanStringBody(String body) {

        if (!StringUtils.hasText(body)) {
            return "";
        }

        return body.replaceAll("\\w", "");
    }

    public static String normalAccount(String account) {

        String normalAccount = cleanBody(account);
        return padWithZero(normalAccount, ACC_LENGTH);
    }

    public static boolean isValidAccount(String account) {

        if (org.apache.commons.lang.StringUtils.isBlank(account)) {
            return false;
        }

        if (!org.apache.commons.lang.StringUtils.isNumeric(account)) {
            return false;
        }

        if (account.length() != ACC_LENGTH) {
            return false;
        }
        if (account.equals("0000000000000")) {
            return false;
        }

        int sum = 0;

        boolean alternate = false;
        for (int i = account.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(account.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }

    public static String normalInstallment(String account) {

        return cleanBody(account);
    }


    public static String formatPersianDate(String date) {

        if (date.length() == 6) {
            return date.substring(0, 2) + "-" + date.substring(2, 4) + "-" + date.substring(4, 6);
        }
        if (date.length() == 8) {
            return date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6, 8);
        }
        return date;
    }

    public static String formatPersianTime(String time) {
        if (time.length() == 6) {
            return time.substring(0, 2) + ":" + time.substring(2, 4) + ":" + time.substring(4, 6);
        }
        return time;
    }

    public static boolean isValidateLat(float lat) {

        if (lat < -90 || lat > 90) {
            return false;
        }
        return true;
    }

    public static boolean isValidateLng(float lng) {
        if (lng < -180 || lng > 180) {
            return false;
        }
        return true;
    }

    public static String cleanBodyNoneUtf(String body) {

        StringBuilder sb = new StringBuilder();

        for (char c : body.toCharArray()) {
            if ((int) c > 0x6f9) {
                sb.append("-");
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    /**
     * validation amount in input pishkhan
     *
     * @param amount
     * @return
     */
    public static boolean isValidAmount(String amount) {

        if (!isNumeric(amount)) {
            return false;
        }

        return Long.parseLong(amount) > 0;
    }

    public static boolean isValidRequestType(String requestType) {

        if (!isNumeric(requestType)) {
            return false;
        }

        return Long.parseLong(requestType) >= 0 && Long.parseLong(requestType) <= 10;
    }

    public static boolean isValidEmail(String email) {
        String emailPattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        return !TextUtils.isEmpty(email) && email.matches(emailPattern);
    }

    public static boolean isValidMobile(String phone) {
        String phonePattern = "^(9)\\d{9}$";
        return !TextUtils.isEmpty(phone) && phone.matches(phonePattern);
    }

    public static boolean isValidPin(String pin) {
        return !TextUtils.isEmpty(pin) && pin.length() > 3 && isNumeric(pin);
    }

    public static boolean isValidUsername(String username) {
        return !TextUtils.isEmpty(username) && username.length() > 3;
    }

    public static boolean isValidPassword(String password) {
        return !TextUtils.isEmpty(password) && password.length() > 4;
    }


    public static boolean isValidName(String name) {
        return !TextUtils.isEmpty(name) && name.length() > 2;
    }

    public static boolean isValidToken(String token) {

        if (!TextUtils.isEmpty(token)) {
            return token.length() > 5;
        }
        return false;
    }

    public static boolean isValidTerminalType(String terminalType) {
        if (TextUtils.isEmpty(terminalType)) {
            return false;
        }

        return terminalType.length() <= 4;
    }

    public static boolean isValidCashBack(String cashBack) {

        if (!TextUtils.isEmpty(cashBack)) {
            return (cashBack.length() == 1 && Integer.parseInt(cashBack) < 3);
        }
        return false;
    }

    public static boolean isValidCashPercent(String cashPercent) {
        return isValidPercent(cashPercent);
    }

    public static boolean isValidId(String id) {
        return !TextUtils.isEmpty(id) && isNumeric(id);
    }

    public static boolean isValidTraceId(String traceId) {
        return !TextUtils.isEmpty(traceId) && traceId.length() > 0 && isNumeric(traceId);
    }

    public static boolean isValidUId(String traceId) {
        return !TextUtils.isEmpty(traceId);
    }

    public static boolean isValidCard(String number) {
        if (!isNumeric(number)) {
            return false;
        }
        if (number.length() != 16) {
            return false;
        }

        if (number.substring(0, 6).equalsIgnoreCase(TEJARAT) || number.substring(0, 6).equalsIgnoreCase(KOSAR)) {
            return true;
        }
        int sum = 0;

        boolean alternate = false;
        for (int i = number.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(number.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }

        return (sum % 10 == 0);
    }

    public static boolean isValidBooleanNumber(String value) {
        return !isNull(value) && (value.equalsIgnoreCase("0") || value.equalsIgnoreCase("1"));
    }

    public static boolean isValidStatus(String status) {
        return isNumeric(status) && Integer.parseInt(status) < 3;
    }


    public static boolean isValidSecretKey(String secretKey) {
        return !TextUtils.isEmpty(secretKey) && secretKey.length() == 16;
    }

    public static boolean isNumeric(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        char[] chars = str.toCharArray();
        for (char c : chars) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    public static String findSeason(Date currentDate) {

        String monthStr = DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, currentDate, "YYYYMM", false);

        return switch (monthStr.substring(4, 6)) {
            case "01", "02", "03" -> monthStr.substring(0, 4) + "1";
            case "04", "05", "06" -> monthStr.substring(0, 4) + "2";
            case "07", "08", "09" -> monthStr.substring(0, 4) + "3";
            case "10", "11", "12" -> monthStr.substring(0, 4) + "4";
            default -> "0";
        };

    }

    public static boolean isValidateIP(final String ip) {
        return !Validator.isNull(ip);
		/*if (Validator.isNull(ip)) {
			return false;
		}
		return IPV4_PATTERN.matcher(ip).matches();*/
    }

    public static boolean isValidateIPChannel(final String ip) {
        return !Validator.isNull(ip);
    }

    public static boolean isValidType(String type) {

        if (!isNumeric(type)) {
            return false;
        }

        return Integer.parseInt(type) >= 0 || Integer.parseInt(type) <= 99;

    }


    public static boolean isValidPercent(String value) {
        try {
            Float.parseFloat(value);
            return true;
        } catch (NumberFormatException e) {
            //not float
            return false;
        }
    }


    public static boolean isValidAccountType(String type) {

        if (!isNumeric(type)) {
            return false;
        }

        if (type.length() > 2) {
            return false;
        }

        return Integer.parseInt(type) >= 0 || Integer.parseInt(type) <= 99;

    }

    public static boolean isValidGroupId(String groupId) {
        return isNumeric(groupId);
    }

    private static int charCode(char c) {
        return ((int) (c) - (int) 'A') + 10;
    }

    public static boolean isValidIban(String shba) {

        if (!StringUtils.hasText(shba)) {
            return false;
        }

        if (!shba.startsWith("IR")) {
            return false;
        }

        if (shba.length() != IBAN_LENGTH) {
            return false;
        }

        String converted = shba.substring(4) + charCode(COUNTRY_CODE.charAt(0)) + charCode(COUNTRY_CODE.charAt(1)) + "00";
        BigInteger value = new BigInteger(converted);
        BigInteger mod = value.mod(new BigInteger("97"));
        int mod2 = 98 - mod.intValue();

        String ret = String.valueOf(mod2);
        if (ret.length() == 1) {
            ret = "0" + ret;
        }
        return ret.equalsIgnoreCase(shba.substring(2, 4));
    }

    public static boolean isIbanSamanBank(String shaba) {

        String iban = shaba.substring(5, 7);
        return iban.equalsIgnoreCase(SAMAN_IBAN);
    }

    public static int validatePageSize(int pageSize) {
        return Math.max(pageSize, 1);
    }

    public static int validatePageNumber(int pageNumber) {
        return Math.max(pageNumber, 0);
    }


    public static void main(String[] args) {
//		System.out.println("args = " + normalMobile("09126146558"));
//		System.out.println("args = " + normalMobile("9126146558"));
        System.out.println(isValidBirthDate("13650130"));
        System.out.println(isValidBirthDate("1365126"));
        System.out.println(isValidBirthDate("13651132"));
        System.out.println(isValidBirthDate("13651330"));
    }

}
