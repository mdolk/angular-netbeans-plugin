package com.github.mdolk.ng.netbeans.codecompletion;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
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
			sb.append("<ul>");
			for (String param : params) {
				String[] split = param.split(" ", 3);
				String type = split[0];
				String name = split[1];
				String desc = split[2];
				sb.append("<li>")
				.append("<tt>").append(name).append("</tt>").append(" - ")
				.append("<tt>").append(type).append("</tt>").append(" : ")
				.append(format(desc))
				.append("</li>");
			}
			sb.append("</ul>");
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
					sb.append("<hr><pre>");
					sb.append(matcher.group(1).replaceAll("<", "&lt;").replaceAll(">", "&gt;"));
					sb.append("</pre><hr>");
				}
			}
		}
	}

	private static String format(String ngDocMarkup) {
		// TODO : Format bullet lists
		StringBuilder sb = new StringBuilder();
		String[] paragraphs = ngDocMarkup.split("\\n[ \\t]*\\n");
		for (String paragraph : paragraphs) {
			sb.append("<p>");
			sb.append(paragraph
				.replaceAll("<", "&lt;")
				.replaceAll(">", "&gt;")
				.replaceAll("`(.*?)`", "<tt>$1</tt>") // monospace font
				.replaceAll("\\{@link (\\S)+([^\\}]+)\\}", "<i>$2</i>") // display links in italic
				.replaceAll("(?s)&lt;pre&gt;(.*?)&lt;/pre&gt;", "<pre>$1</pre>") // keep <pre> tags
				.replaceAll("(?:^|\\n)# (.*)\\n", "<h3>$1</h3>") // # headers
			);
			sb.append("</p>");
		}
		return sb.toString();
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
