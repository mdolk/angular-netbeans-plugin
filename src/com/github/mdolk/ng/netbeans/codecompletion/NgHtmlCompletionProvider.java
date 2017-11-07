package com.github.mdolk.ng.netbeans.codecompletion;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.filesystems.FileObject;
import org.openide.util.Utilities;

@MimeRegistration(mimeType="text/html", service=CompletionProvider.class)
public class NgHtmlCompletionProvider implements CompletionProvider {

	private final NgDocCache ngDocCache = new NgDocCache();

	@Override
	public CompletionTask createTask(int queryType, JTextComponent component) {
		if (queryType != CompletionProvider.COMPLETION_QUERY_TYPE)
			return null;
		return new AsyncCompletionTask(new AsyncCompletionQuery() {
			@Override
			protected void query(CompletionResultSet resultSet, Document document, int caretOffset) {
				NgHtmlCompletionContext ctx = new NgHtmlCompletionContext((StyledDocument)document, caretOffset);
				if (ctx.isInAttributeValue()) {
					resultSet.finish();
					return;
				}
				String filter = ctx.getFilter();
				Set<String> added = new HashSet<>();
				for (NgDoc ngDoc : getNgDocs()) {
					String type = ngDoc.getAttribute("@ngdoc");
					String name = ngDoc.getAttribute("@name");
					String allowedElement = ngDoc.getAttribute("@element");
					if ("directive".equals(type) && ctx.isInMatchingTag(allowedElement)) {
						name = stripPrefix(name); // strip "angular.directive." from name
						if (!added.contains(name) && name.startsWith(filter)) {
							resultSet.addItem(new NgHtmlCompletionItem(name, ctx, ngDoc));
							added.add(name);
						}
					}
					if ("widget".equals(type)) {
						name = stripPrefix(name); //strip "angular.widget." from name
						// attribute widgets
						if (!added.contains(name) && name.startsWith("@"+filter) && ctx.isInMatchingTag(allowedElement)) {
							resultSet.addItem(new NgHtmlCompletionItem(name.substring(1), ctx, ngDoc));
							added.add(name);
						}
						// element widgets
						if (!added.contains(name) && !name.startsWith("@") && ctx.isOpeningTag() && name.startsWith(filter)) {
							resultSet.addItem(new NgHtmlCompletionItem(name, ctx, ngDoc));
							added.add(name);
						}
					}
				}
				resultSet.finish();
			}

			private String stripPrefix(String name) {
				int lastDotIndex = name.lastIndexOf(".");
				if (lastDotIndex == -1) {
					return name;
				} else {
					return name.substring(lastDotIndex+1);
				}
			}
		}, component);
	}

	private List<NgDoc> getNgDocs() {
		// which file is currently beeing edited?
		FileObject editFile = Utilities.actionsGlobalContext().lookup(FileObject.class);
		// in which project?
		Project owner = FileOwnerQuery.getOwner(editFile);
		return ngDocCache.getNgDocs(owner);
	}

	@Override
	public int getAutoQueryTypes(JTextComponent component, String typedText) {
		return 0; // code completion box will not appear unless user explicitly asks
	}


}
