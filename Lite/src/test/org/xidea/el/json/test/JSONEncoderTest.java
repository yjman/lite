package org.xidea.el.json.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.xidea.el.json.JSONEncoder;

public class JSONEncoderTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testEncodeObject() throws IOException {
		assertEquals("{}", JSONEncoder.encode(new Object()));
		StringBuilder out = new StringBuilder();
		new JSONEncoder(null,false,10).encode(new Object(),out);
		assertEquals("{\"class\":\"java.lang.Object\"}", out.toString());
	}

}
