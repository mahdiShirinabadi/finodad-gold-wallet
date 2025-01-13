package com.melli.hub.util;

import org.apache.commons.lang.StringUtils;

public class PersianUtils {
	public static String toPersianNumeric(String s) {

		return toPersianNumeric(s, false);
	}

	public static String toPersianNumeric(String s, boolean commaFormat) {

		String sWithOutComma = s;

		if (commaFormat) {
			sWithOutComma = addComa(s);
		}

		return StringUtils.replaceChars(sWithOutComma, "0123456789", "\u06f0\u06f1\u06f2\u06f3\u06f4\u06f5\u06f6\u06f7\u06f8\u06f9");
	}

	public static String fromPersianNumeric(String s) {

		String englishNumbers1 = StringUtils.replaceChars(s, "\u06f0\u06f1\u06f2\u06f3\u06f4\u06f5\u06f6\u06f7\u06f8\u06f9", "0123456789");
		return StringUtils.replaceChars(englishNumbers1, "\u0660\u0661\u0662\u0663\u0664\u0665\u0666\u0667\u0668\u0669", "0123456789");
	}

	protected static String addComa(String str) {
		if (StringUtils.isBlank(str)) {
			return "";
		}
		String t = "";
		if ("+".equals(str.substring(0, 1)) || "-".equals(str.substring(0, 1))) {
			t = str.substring(0, 1);
			str = str.substring(1);
		}

		StringBuffer temp = new StringBuffer(str);
		int index = str.length() - 3;
		while (index > 0) {
			temp.insert(index, ",");
			index = index - 3;
		}
		return t + temp.toString();
	}

	public static String addDash(String str) {
		if (StringUtils.isBlank(str)) {
			return "";
		}
		String t = "";

		StringBuffer temp = new StringBuffer(str);
		int index = str.length() - 4;
		while (index > 0) {
			temp.insert(index, "-");
			index = index - 4;
		}
		return t + temp.toString();
	}
}
