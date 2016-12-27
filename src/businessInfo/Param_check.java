package businessInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import dbpool.Query;

public class Param_check {
	private boolean leap_year(int year) {
		if (year % 100 == 0) {
			if (year % 400 == 0) {
				return true;
			} else {
				return false;
			}
		} else {
			if (year % 4 == 0) {
				return true;
			}
			return false;
		}

	}// 判断闰年

	public boolean time_check(String time, int length) {// length代表时间字符串应该该有的长度(8或者10)
		if (length != 8 && length != 10)
			return false;
		if (time == null || time.length() != length)
			return false;
		for (int i = 0; i < time.length(); ++i) {
			if (time.charAt(i) != '0' && time.charAt(i) != '1' && time.charAt(i) != '2' && time.charAt(i) != '3'
					&& time.charAt(i) != '4' && time.charAt(i) != '5' && time.charAt(i) != '6' && time.charAt(i) != '7'
					&& time.charAt(i) != '8' && time.charAt(i) != '9') {
				return false;
			}
		}
		if (length == 8) {
			// 20160314
			int year = Integer.parseInt(time.substring(0, 4));
			int month = Integer.parseInt(time.substring(4, 6));
			int day = Integer.parseInt(time.substring(6));
			if (year < 1970)
				return false;
			if (month > 12 || month < 1)
				return false;
			boolean leapYear = leap_year(year);
			if (month == 2) {
				if (leapYear) {
					if (day < 1 || day > 29)
						return false;
				} else {
					if (day < 1 || day > 28)
						return false;
				}
			} else
				if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) {
				if (day < 1 || day > 31)
					return false;
			} else {
				if (day < 1 || day > 30)
					return false;
			}

		}
		if (length == 10) {
			int year = Integer.parseInt(time.substring(0, 4));
			int month = Integer.parseInt(time.substring(4, 6));
			int day = Integer.parseInt(time.substring(6, 8));
			int hour = Integer.parseInt(time.substring(8));
			if (year < 1970)
				return false;
			if (month > 12 || month < 1)
				return false;
			boolean leapYear = leap_year(year);
			if (month == 2) {
				if (leapYear) {
					if (day < 1 || day > 29)
						return false;
				} else {
					if (day < 1 || day > 28)
						return false;
				}
			} else
				if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) {
				if (day < 1 || day > 31)
					return false;
			} else {
				if (day < 1 || day > 30)
					return false;
			}
			if (hour < 0 || hour > 24)
				return false;
		}
		return true;
	}

	public ArrayList<Integer> hourString_check(String hourString) {// 检查hourString的合法性，同时返回hours列表
		String[] hoursStr = hourString.split("\\|");
		ArrayList<Integer> hours = new ArrayList<Integer>();
		for (int i = 0; i < hoursStr.length; ++i) {
			try {
				hours.add(Integer.parseInt(hoursStr[i]));
			} catch (Exception e) {// 如果出现非数字，报错
				return null;
			}
		} // 判断是否都是数字
		if (hours.isEmpty() || hours.get(0) < 0 || hours.get(hours.size() - 1) >= 24) {
			return null;
		} // 判断数字范围
		for (int i = 1; i < hours.size(); ++i) {
			if (hours.get(i) <= hours.get(i - 1)) {
				return null;
			}
		} // 判断数字是否递增
		return hours;
	}

	public boolean UID_check(String UID) {
		if (UID == null || UID.length() == 0) {
			return false;
		}
		for (int i = 0; i < UID.length(); ++i) {
			if (UID.charAt(i) != '0' && UID.charAt(i) != '1' && UID.charAt(i) != '2' && UID.charAt(i) != '3'
					&& UID.charAt(i) != '4' && UID.charAt(i) != '5' && UID.charAt(i) != '6' && UID.charAt(i) != '7'
					&& UID.charAt(i) != '8' && UID.charAt(i) != '9') {
				return false;
			}
		}
		List<Map<String, Object>> r = new Query().select("*").table("mm_user").where("user_id")
				.equal(Integer.parseInt(UID)).fetchAll();
		if (r.isEmpty())
			return false;

		return true;
	}

	public boolean boxID_check(String boxID) {
		if (boxID == null || boxID.length() == 0 || boxID.length() != 12) {
			return false;
		}
		return true;
	}

	public boolean boxIDs_check(String boxIDs) {
		if (boxIDs == null || boxIDs.length() == 0) {
			return false;
		}
		return true;
	}
}
