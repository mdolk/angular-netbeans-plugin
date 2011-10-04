package com.github.mdolk.ng.netbeans.codecompletion;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;

public class NgHtmlCompletionItem implements CompletionItem {
	private static final ImageIcon fieldIcon = new ImageIcon(
		ImageUtilities.loadImage("com/github/mdolk/ng/netbeans/favicon.png")
	);
	private static final Color fieldColor = Color.decode("0x0000B2");

	private final String name;
	private final NgHtmlCompletionContext context;
	private final NgDoc ngDoc;

	public NgHtmlCompletionItem(String name, NgHtmlCompletionContext context, NgDoc ngDoc) {
		this.name = name;
		this.context = context;
		this.ngDoc = ngDoc;
	}

	@Override
	public int getPreferredWidth(Graphics g, Font font) {
		String type = ngDoc.getAttribute("@ngdoc");
		String s = context.isOpeningTag() ? "&lt;" + name + "&gt;" : name;
		return CompletionUtilities.getPreferredWidth(s, type, g, font);
	}

	@Override
	public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
		String type = ngDoc.getAttribute("@ngdoc");
		String s =  context.isOpeningTag() ? "&lt;" + name + "&gt;" : name;
		CompletionUtilities.renderHtml(fieldIcon, s, "<i>"+type+"</i>", g, defaultFont, (selected ? Color.white : fieldColor), width, height, selected);
	}

	@Override
	public CharSequence getSortText() {
		return name;
	}

	@Override
	public CharSequence getInsertPrefix() {
		return name;
	}

	@Override
	public void defaultAction(JTextComponent component) {
		StyledDocument doc = (StyledDocument)component.getDocument();
		try {
			boolean isOpeningTag = context.isOpeningTag(); // Need to check this before messing with the document!
			int startOffset = context.getFilterStartOffset();
			int caretOffset = context.getCaretOffset();
			doc.remove(startOffset, caretOffset-startOffset);
			if (isOpeningTag) {
				doc.insertString(startOffset, name, null);
			} else {
				String insert = name + "=\"\"";
				doc.insertString(startOffset, insert, null);
				component.setCaretPosition(startOffset + insert.length() - 1);
			}
			Completion.get().hideAll();
		} catch (BadLocationException ex) {
			Exceptions.printStackTrace(ex);
		}
	}
	
	@Override
	public CompletionTask createDocumentationTask() {
		return new AsyncCompletionTask(new AsyncCompletionQuery() {
			@Override
			protected void query(CompletionResultSet crs, Document document, int i) {
				crs.setDocumentation(new NgHtmlCompletionDocumentation(NgHtmlCompletionItem.this));
				crs.finish();
			}
		});
	}

	@Override
	public CompletionTask createToolTipTask() {
		return null;
	}


	@Override
	public int getSortPriority() {
		return 0;
	}

	@Override
	public boolean instantSubstitution(JTextComponent jtc) {
		return false;
	}

	@Override
	public void processKeyEvent(KeyEvent ke) { }

	public NgDoc getNgDoc() {
		return ngDoc;
	}
}
