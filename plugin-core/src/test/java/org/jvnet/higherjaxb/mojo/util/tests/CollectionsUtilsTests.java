package org.jvnet.higherjaxb.mojo.util.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Comparator;

import org.junit.jupiter.api.Test;
import org.jvnet.higherjaxb.mojo.util.CollectionUtils;
import org.jvnet.higherjaxb.mojo.util.CollectionUtils.Function;

public class CollectionsUtilsTests {

	@Test
	public void correctlyCompares() {

		final Function<String, String> identity = new Function<String, String>() {
			@Override
			public String eval(String argument) {
				return argument;
			}
		};
		final Comparator<String> gt = CollectionUtils
				.<String> gtWithNullAsGreatest();
		final Comparator<String> lt = CollectionUtils
				.<String> ltWithNullAsSmallest();
		assertEquals("b", CollectionUtils.bestValue(
				Arrays.<String> asList("a", "b"), identity, gt));
		assertEquals("a", CollectionUtils.bestValue(
				Arrays.<String> asList("a", "b"), identity, lt));
		assertEquals(
				null,
				CollectionUtils.bestValue(
						Arrays.<String> asList("a", null, "b"), identity, gt));
		assertEquals(
				null,
				CollectionUtils.bestValue(
						Arrays.<String> asList("a", null, "b"), identity, lt));
	}
}
