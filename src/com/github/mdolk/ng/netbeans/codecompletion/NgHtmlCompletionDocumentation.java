package com.github.mdolk.ng.netbeans.codecompletion;

import com.petebevin.markdown.MarkdownProcessor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.Action;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.openide.util.Exceptions;

public class NgHtmlCompletionDocumentation implements CompletionDocumentation {
	private final NgHtmlCompletionItem item;

	public NgHtmlCompletionDocumentation(NgHtmlCompletionItem item) {
		this.item = item;
	}

	@Override
	public String getText() {
		NgDoc ngDoc = item.getNgDoc();
		StringBuilder sb = new StringBuilder();
		appendWorkInProgress(sb, ngDoc);
		appendTitle(sb, ngDoc);
		appendDescription(sb, ngDoc);
		appendParameters(sb, ngDoc);
		appendExample(sb, ngDoc);
		return sb.toString();
	}

	private static void appendWorkInProgress(StringBuilder sb, NgDoc ngDoc) {
		if (ngDoc.isPresent("@workInProgress")) {
			sb.append("<b>Work in Progress!</b><br>");
			sb.append("<i>This documentation is currently being revised. ");
			sb.append("It might be incomplete or contain inaccuracies.</i>");
		}
	}

	private static void appendTitle(StringBuilder sb, NgDoc ngDoc) {
		sb.append("<h1>").append(ngDoc.getAttribute("@name")).append("</h1>");
	}

	private static void appendDescription(StringBuilder sb, NgDoc ngDoc) {
		sb.append("<h2>Description</h2>");
		sb.append(format(ngDoc.getMultilineAttribute("@description")));
	}

	private static void appendParameters(StringBuilder sb, NgDoc ngDoc) {
		List<String> params = ngDoc.getMultilineAttributes("@param");
		if (!params.isEmpty()) {
			sb.append("<h2>Parameters</h2>");
			sb.append("<table>");
			for (String param : params) {
				String[] split = param.split(" ", 3);
				String type = split[0];
				String name = split[1];
				String desc = split[2];
				sb.append("<tr valign=top>");
				sb.append("<td>&bull;</td>");
				sb.append("<td><tt>").append(name).append("</tt></td>");
				sb.append("<td>&mdash;</td>");
				sb.append("<td><tt>").append(type).append("</tt></td>");
				sb.append("<td>&mdash;</td>");
				sb.append("<td>").append(format(desc)).append("</td>");
				sb.append("</tr>");
			}
			sb.append("</table>");
		}
	}

	private static void appendExample(StringBuilder sb, NgDoc ngDoc) {
		List<String> examples = ngDoc.getMultilineAttributes("@example");
		for(String example : examples) {
			sb.append("<h2>Example</h2>");
			int indexOfDocExample = example.indexOf("<doc:example>");
			if (indexOfDocExample == -1) {
				// no <doc:example>
				sb.append(example);
			} else {
				// has <doc:example>
				sb.append(format(example.substring(0, indexOfDocExample)));  // description
				Pattern pattern = Pattern.compile("<doc:source.*?>(.*?)</doc:source>", Pattern.DOTALL);
				Matcher matcher = pattern.matcher(example);
				if (matcher.find()) {
					sb.append("<pre>");
					sb.append(matcher.group(1).replaceAll("<", "&lt;").replaceAll(">", "&gt;"));
					sb.append("</pre>");
				}
			}
		}
	}

	private static String format(String text) {
		// Escape HTML tags in pre blocks
		Scanner scanner = new Scanner(text).useDelimiter("</?pre>");
		boolean isPre = false;
		StringBuilder sb = new StringBuilder();
		while (scanner.hasNext()) {
			String group = scanner.next();
			if (isPre) {
				sb
				.append("<pre>")
				.append(group.replaceAll("<", "&lt;").replaceAll(">", "&gt;"))
				.append("</pre>");
			} else {
				sb.append(group);
			}
			isPre = !isPre;
		}
		text = sb.toString();

		// Minor tweaks
		text = text.replaceAll("\\{@link\\s+([^\\s\\}]+)\\s*([^\\}]*?)\\s*}", "_$2_"); // display links in italic
		text = text.replaceAll("<angular/>", "<tt>&lt;angular/&gt;</tt>");

		// Markdown format the rest
		MarkdownProcessor mp = new MarkdownProcessor();
		return mp.markdown(text);
	}

	@Override
	public URL getURL() {
		String name = item.getNgDoc().getAttribute("@name");
		if (name.startsWith("angular.")) {
			try {
				// TODO: Netbeans does not like the hash bang URLs, rewrites and spams log :-/
				return new URL("http://docs.angularjs.org/#!/api/" + name);
			} catch (MalformedURLException ex) {
				Exceptions.printStackTrace(ex);
				return null;
			}
		}
		return null;
	}

	@Override
	public CompletionDocumentation resolveLink(String string) {
		return null;
	}

	@Override
	public Action getGotoSourceAction() {
		return null;
	}
}