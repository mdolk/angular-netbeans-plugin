package com.github.mdolk.ng.netbeans.codecompletion;

import java.util.Arrays;
import java.util.HashSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.openide.util.Exceptions;

public class NgHtmlCompletionContext {
	private final StyledDocument doc;
	private final int caretOffset;
	private final String filter;

	public NgHtmlCompletionContext(StyledDocument doc, int caretOffset) {
		this.doc = doc;
		this.caretOffset = caretOffset;
		this.filter = findFilter();
	}

	public int getCaretOffset() {
		return caretOffset;
	}

	public int getFilterStartOffset() {
		return caretOffset - filter.length();
	}

	public String getFilter() {
		return filter;
	}

	private String getInTagName() {
		int offset = lookLeftUntil('<','>');
		try {
			if (offset == -1 || charAt(offset) == '>') {
				return null; // not between tag brackets
			}
			int tagNameStart = offset + 1;
			int tagNameEnd = tagNameStart;
			while (!Character.isWhitespace(charAt(++tagNameEnd))) {}
			return doc.getText(tagNameStart, tagNameEnd - tagNameStart);
		} catch (BadLocationException ex) {
			Exceptions.printStackTrace(ex);
			return null;
		}
	}

	private int lookLeftUntil(Character... lookFor) {
		Character[] l = lookFor;
		HashSet<Character> set = new HashSet<Character>(Arrays.asList(l));
		int offset = caretOffset - 1;
		try {
			while (offset >= 0 && !set.contains(charAt(offset))) {
				offset--;
			}
			return offset;
		} catch (BadLocationException ex) {
			Exceptions.printStackTrace(ex);
			return -1;
		}
	}

	public boolean isOpeningTag() {
		int offset = lookLeftUntil('<', '>', ' ', '\t', '\n');
		try {
			if (offset != -1 && charAt(offset) == '<') {
				return true;
			}
			return false;
		} catch (BadLocationException ex) {
			Exceptions.printStackTrace(ex);
			return false;
		}
	}

	public boolean isInMatchingTag(String allowedElement) {
		if (isOpeningTag()) {
			return false;
		}
		String inTagName = getInTagName();
		if (inTagName == null) {
			return false;	// not inside a tag brackets
		}
		if (allowedElement == null || "ANY".equalsIgnoreCase(allowedElement) || inTagName.equalsIgnoreCase(allowedElement)) {
			return true;
		}
		return false;
	}

	public boolean isInAttributeValue() {
		if (!isInMatchingTag("ANY")) {
			return false;
		}
		try {
			int offset = lookLeftUntil('"','<');
			if (charAt(offset) == '<') {
				return false;
			}
			return charAt(offset - 1) == '=';
		} catch (BadLocationException ex) {
			Exceptions.printStackTrace(ex);
			return false;
		}
	}

	private String findFilter() {
		int offset = caretOffset - 1;
		if (offset < 0) {
			return "";
		}
		try {
			char c = charAt(offset);
			while (offset > 0 && !Character.isWhitespace(c) && c != '<' && c != '>') {
				offset--;
				c = charAt(offset);
			}
			return doc.getText(offset + 1, caretOffset - offset - 1);
		} catch (BadLocationException ex) {
			Exceptions.printStackTrace(ex);
			return "";
		}
	}

	private char charAt(int offset) throws BadLocationException {
		return doc.getText(offset, 1).charAt(0);
	}
}