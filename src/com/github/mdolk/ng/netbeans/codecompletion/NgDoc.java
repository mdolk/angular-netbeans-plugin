package com.github.mdolk.ng.netbeans.codecompletion;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NgDoc {
	private final String text;

	public NgDoc(String text) {
		if (!text.startsWith("/**"))
			throw new IllegalArgumentException("Comment must start with '/**'");
		if (!text.endsWith("*/"))
			throw new IllegalArgumentException("Comment must end with '*/'");
		this.text = stripLeadingWhitespaceAndAsterisks(text.substring(3, text.length()-2));
	}

	private static String stripLeadingWhitespaceAndAsterisks(String text) {
		Pattern pattern = Pattern.compile("(?:\\s*\\*)?[\\s&&[^\\r\\n]]?(.*\\n)");
		Matcher matcher = pattern.matcher(text);
		StringBuilder sb = new StringBuilder();
		while (matcher.find()) {
			sb.append(matcher.group(1));
		}
		return sb.toString();
	}

	public boolean isPresent(String attribute) {
		return text.contains(attribute);
	}

	public String getAttribute(String attribute) {
		attribute += " ";
		int start = text.lastIndexOf(attribute);
		if (start == -1) {
			return null;
		}
		start += attribute.length();
		int end = text.indexOf("\n", start);
		return text.substring(start, end).trim();
	}

	public String getMultilineAttribute(String attribute) {
		List<String> all = getMultilineAttributes(attribute);
		if (all.isEmpty()) {
			return null;
		}
		return all.get(0);
	}

	public List<String> getMultilineAttributes(String attribute) {
		// Match until next @ but ignore @ if in {}, e.g. can return "I have a {@link a/y link} embedded!"
		Pattern pattern = Pattern.compile(toRegExp(attribute)+"\\s((?:[^\\{@]*(?:\\{[^\\}]*\\})?)*)");
		Matcher matcher = pattern.matcher(text);
		List<String> result = new ArrayList<String>();
		while (matcher.find()) {
			result.add( matcher.group(1) );
		}
		return result;
	}

	private static String toRegExp(String s) {
		final String[] ZEROS = new String[] {"0000","000","00","0", ""};
		StringBuilder sb = new StringBuilder();
		for (char c : s.toCharArray()) {
			String hexString = Integer.toHexString((int)c);
			sb.append("\\u").append(ZEROS[hexString.length()]).append(hexString);
		}
		return sb.toString();
	}

	public String getText() {
		return text;
	}
}
