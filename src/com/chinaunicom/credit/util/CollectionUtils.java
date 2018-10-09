package com.chinaunicom.credit.util;

import java.util.Collection;

public class CollectionUtils {

	public static boolean isEmpty(Collection<?> list) {
		return null == list || list.isEmpty();
	}
	
	public static boolean isNotEmpty(Collection<?> list) {
		return null != list && !list.isEmpty();
	}
}
