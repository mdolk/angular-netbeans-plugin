package com.github.mdolk.ng.netbeans.codecompletion;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.filesystems.FileObject;

public class NgUtils {

	public static List<FileObject> findByMimeType(FileObject root, String mime) {
		if (!root.isFolder()) {
			throw new IllegalArgumentException("Not a folder");
		}
		List<FileObject> result = new ArrayList<FileObject>();
		Enumeration<? extends FileObject> children = root.getChildren(true);
		while (children.hasMoreElements()) {
			FileObject file = children.nextElement();
			if (mime.equals(file.getMIMEType())) {
				result.add(file);
			}
		}
		return result;
	}

	/**
	 * Find all groups staring with '/&ast;&ast;' and ending with &ast;/
	 */
	public static List<String> getDocComments(String s) {
		Pattern pattern = Pattern.compile("/\\*\\*.*?\\*/", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(s);
		List<String> result = new ArrayList<String>();
		while (matcher.find()) {
			result.add(matcher.group());
		}
		return result;
	}
}
