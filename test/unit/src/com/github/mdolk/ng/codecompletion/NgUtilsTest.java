package com.github.mdolk.ng.codecompletion;

import com.github.mdolk.ng.netbeans.codecompletion.NgUtils;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class NgUtilsTest {
	@Test
	public void testGetDocs() {
		List<String> docs = NgUtils.getDocComments("xxx /**\nfoo */ yyy /** bar\n */ zzz");
		assertEquals(2, docs.size());
		assertEquals("/**\nfoo */", docs.get(0));
		assertEquals("/** bar\n */", docs.get(1));
	}
}
