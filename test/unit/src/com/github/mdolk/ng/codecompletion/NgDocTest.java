package com.github.mdolk.ng.codecompletion;

import com.github.mdolk.ng.netbeans.codecompletion.NgDoc;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class NgDocTest {
	private static final String DOC =
		"/**\n" +
		" * @workInProgress\n" +
		" * @ngdoc directive\n" +
		" * @name angular.directive.ng:hide\n" +
		" *\n" +
		" * @description\n" +
		" * The `ng:hide` and `ng:show` directives hide or show a portion\n" +
		" * of the HTML conditionally.\n" +
		" *\n" +
		" * @element ANY\n" +
		" * @param {expression} expression If the {@link guide/dev_guide.expressions expression} truthy then\n" +
		" *     the element is shown or hidden respectively.\n" +
		" * @param {expression} foo some other cool expression.\n" +
		" *\n" +
		" * @batman is the {@link very/cool coolest} hero\n" +
		" *     ever!\n" +
		" */";

	@Test
	public void testIsPresent() {
		NgDoc ngDoc = new NgDoc(DOC);
		assertTrue(ngDoc.isPresent("@workInProgress"));
		assertFalse(ngDoc.isPresent("@notPresent"));
	}

	@Test
	public void testGetAttribute() {
		NgDoc ngDoc = new NgDoc(DOC);
		assertEquals("directive", ngDoc.getAttribute("@ngdoc"));
	}

	@Test
	public void testGetMultiLineAttribute() {
		NgDoc ngDoc = new NgDoc(DOC);
		String description = ngDoc.getMultilineAttribute("@description");
		assertTrue(description.startsWith("The `ng:hide` and"));
		assertTrue(description.endsWith("HTML conditionally.\n\n"));
		String batman = ngDoc.getMultilineAttribute("@batman");
		assertEquals("is the {@link very/cool coolest} hero\n    ever!\n", batman);
	}

	@Test
	public void testGetMultiLineAttributes() {
		NgDoc ngDoc = new NgDoc(DOC);
		List<String> params = ngDoc.getMultilineAttributes("@param");
		assertEquals(2, params.size());
		assertEquals("{expression} foo some other cool expression.\n\n", params.get(1));
	}

	@Test
	public void testStripLeadingWhitespaceAndAsterisks() {
		NgDoc ngDoc = new NgDoc(
			"/**\n" +
			" * foo\n" +
			" *\n" +
			" * bar\n" +
			" * baz\n" +
			"       boz\n" +
			"       biz\n" +
			" */");
		assertEquals("foo\n\nbar\nbaz\n      boz\n      biz\n", ngDoc.getText());
	}
}
