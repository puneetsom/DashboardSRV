package com.amdocs.dashboard.excel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Comparator;

import java.util.List;


public class ExcelUtil {
	public static void copyStream(InputStream in, OutputStream out)
			throws IOException {
		byte[] chunk = new byte[1024];
		int count;
		while ((count = in.read(chunk)) >= 0) {
			out.write(chunk, 0, count);
			out.flush();
		}
	}

	@SuppressWarnings("unchecked")
	public static void sortList(List<String> list) {
		if (list == null)
			return;
		StringAscComparator comparator = new StringAscComparator();
		Collections.sort(list, comparator);
	}
}

@SuppressWarnings("rawtypes")
class StringAscComparator implements Comparator {
	public int compare(Object arg0, Object arg1) {
		return arg0.toString().compareTo(arg1.toString());
	}
}
