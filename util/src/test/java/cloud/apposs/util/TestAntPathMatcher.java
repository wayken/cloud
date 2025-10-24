package cloud.apposs.util;

import org.junit.Assert;
import org.junit.Test;

public class TestAntPathMatcher {
    private AntPathMatcher pathMatcher = new AntPathMatcher();

    @Test
    // test exact matching
    public void testExactMatching() {
        Assert.assertTrue(pathMatcher.match("test", "test"));
        Assert.assertTrue(pathMatcher.match("/test", "/test"));
        Assert.assertTrue(pathMatcher.match("http://example.org", "http://example.org")); // SPR-14141
        Assert.assertFalse(pathMatcher.match("/test.jpg", "test.jpg"));
        Assert.assertFalse(pathMatcher.match("test", "/test"));
        Assert.assertFalse(pathMatcher.match("/test", "test"));
    }

    @Test
    // test matching with ?'s
    public void testExactMatchingWithQM() {
        Assert.assertTrue(pathMatcher.match("t?st", "test"));
        Assert.assertTrue(pathMatcher.match("??st", "test"));
        Assert.assertTrue(pathMatcher.match("tes?", "test"));
        Assert.assertTrue(pathMatcher.match("te??", "test"));
        Assert.assertTrue(pathMatcher.match("?es?", "test"));
        Assert.assertFalse(pathMatcher.match("tes?", "tes"));
        Assert.assertFalse(pathMatcher.match("tes?", "testt"));
        Assert.assertFalse(pathMatcher.match("tes?", "tsst"));
    }

    @Test
    // test matching with *'s
    public void testExactMatchingWithSM() {
        Assert.assertTrue(pathMatcher.match("*", "test"));
        Assert.assertTrue(pathMatcher.match("test*", "test"));
        Assert.assertTrue(pathMatcher.match("test*", "testTest"));
        Assert.assertTrue(pathMatcher.match("test/*", "test/Test"));
        Assert.assertTrue(pathMatcher.match("test/*", "test/t"));
        Assert.assertTrue(pathMatcher.match("test/*", "test/"));
        Assert.assertTrue(pathMatcher.match("*test*", "AnothertestTest"));
        Assert.assertTrue(pathMatcher.match("*test", "Anothertest"));
        Assert.assertTrue(pathMatcher.match("*.*", "test."));
        Assert.assertTrue(pathMatcher.match("*.*", "test.test"));
        Assert.assertTrue(pathMatcher.match("*.*", "test.test.test"));
        Assert.assertTrue(pathMatcher.match("*.test.com", "aa.test.com"));
        Assert.assertTrue(pathMatcher.match("test*aaa", "testblaaaa"));
        Assert.assertFalse(pathMatcher.match("test*", "tst"));
        Assert.assertFalse(pathMatcher.match("test*", "tsttest"));
        Assert.assertFalse(pathMatcher.match("test*", "test/"));
        Assert.assertFalse(pathMatcher.match("test*", "test/t"));
        Assert.assertFalse(pathMatcher.match("test/*", "test"));
        Assert.assertFalse(pathMatcher.match("*test*", "tsttst"));
        Assert.assertFalse(pathMatcher.match("*test", "tsttst"));
        Assert.assertFalse(pathMatcher.match("*.*", "tsttst"));
        Assert.assertFalse(pathMatcher.match("test*aaa", "test"));
        Assert.assertFalse(pathMatcher.match("test*aaa", "testblaaab"));
    }

    @Test
    // test matching with ?'s and /'s
    public void testExactMatchingWithQDM() {
        Assert.assertTrue(pathMatcher.match("/?", "/a"));
        Assert.assertTrue(pathMatcher.match("/?/a", "/a/a"));
        Assert.assertTrue(pathMatcher.match("/a/?", "/a/b"));
        Assert.assertTrue(pathMatcher.match("/??/a", "/aa/a"));
        Assert.assertTrue(pathMatcher.match("/a/??", "/a/bb"));
        Assert.assertTrue(pathMatcher.match("/?", "/a"));
    }

    @Test
    // test matching with **'s
    public void testExactMatchingWithDSM() {
        Assert.assertTrue(pathMatcher.match("/**", "/testing/testing"));
        Assert.assertTrue(pathMatcher.match("/*/**", "/testing/testing"));
        Assert.assertTrue(pathMatcher.match("/**/*", "/testing/testing"));
        Assert.assertTrue(pathMatcher.match("/bla/**/bla", "/bla/testing/testing/bla"));
        Assert.assertTrue(pathMatcher.match("/bla/**/bla", "/bla/testing/testing/bla/bla"));
        Assert.assertTrue(pathMatcher.match("/**/test", "/bla/bla/test"));
        Assert.assertTrue(pathMatcher.match("/bla/**/**/bla", "/bla/bla/bla/bla/bla/bla"));
        Assert.assertTrue(pathMatcher.match("/bla*bla/test", "/blaXXXbla/test"));
        Assert.assertTrue(pathMatcher.match("/*bla/test", "/XXXbla/test"));
        Assert.assertFalse(pathMatcher.match("/bla*bla/test", "/blaXXXbl/test"));
        Assert.assertFalse(pathMatcher.match("/*bla/test", "XXXblab/test"));
        Assert.assertFalse(pathMatcher.match("/*bla/test", "XXXbl/test"));

        Assert.assertFalse(pathMatcher.match("/????", "/bala/bla"));
        Assert.assertFalse(pathMatcher.match("/**/*bla", "/bla/bla/bla/bbb"));

        Assert.assertTrue(pathMatcher.match("/*bla*/**/bla/**", "/XXXblaXXXX/testing/testing/bla/testing/testing/"));
        Assert.assertTrue(pathMatcher.match("/*bla*/**/bla/*", "/XXXblaXXXX/testing/testing/bla/testing"));
        Assert.assertTrue(pathMatcher.match("/*bla*/**/bla/**", "/XXXblaXXXX/testing/testing/bla/testing/testing"));
        Assert.assertTrue(pathMatcher.match("/*bla*/**/bla/**", "/XXXblaXXXX/testing/testing/bla/testing/testing.jpg"));

        Assert.assertTrue(pathMatcher.match("*bla*/**/bla/**", "XXXblaXXXX/testing/testing/bla/testing/testing/"));
        Assert.assertTrue(pathMatcher.match("*bla*/**/bla/*", "XXXblaXXXX/testing/testing/bla/testing"));
        Assert.assertTrue(pathMatcher.match("*bla*/**/bla/**", "XXXblaXXXX/testing/testing/bla/testing/testing"));
        Assert.assertFalse(pathMatcher.match("*bla*/**/bla/*", "XXXblaXXXX/testing/testing/bla/testing/testing"));

        Assert.assertFalse(pathMatcher.match("/x/x/**/bla", "/x/x/x/"));

        Assert.assertTrue(pathMatcher.match("/foo/bar/**", "/foo/bar")) ;

        Assert.assertTrue(pathMatcher.match("", ""));

        Assert.assertTrue(pathMatcher.match("/{bla}.*", "/testing.html"));
    }
}
