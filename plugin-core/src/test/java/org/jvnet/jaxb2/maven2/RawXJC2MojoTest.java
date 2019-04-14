package org.jvnet.jaxb2.maven2;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.Assert.assertEquals;

public class RawXJC2MojoTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private File file;

    @Before
    public void createJarFile() throws Exception {
        file = temporaryFolder.newFile("my.jar");
        try (JarOutputStream out = new JarOutputStream(new FileOutputStream(file))) {
            out.putNextEntry(new JarEntry("dir/"));
            out.closeEntry();
            out.putNextEntry(new JarEntry("dir/nested.xjb"));
            out.write("nested binding".getBytes(StandardCharsets.UTF_8));
            out.closeEntry();
            out.putNextEntry(new JarEntry("root.xjb"));
            out.write("root binding".getBytes(StandardCharsets.UTF_8));
            out.closeEntry();
        }
    }

    @Test
    public void collectsBindingUrisFromArtifact() throws Exception {
        List<URI> bindings = new ArrayList<>();

        RawXJC2Mojo.collectBindingUrisFromArtifact(file, bindings);

        assertEquals(2, bindings.size());
        assertEquals(URI.create("jar:" + file.toURI() + "!/dir/nested.xjb"), bindings.get(0));
        assertEquals(URI.create("jar:" + file.toURI() + "!/root.xjb"), bindings.get(1));
        assertEquals("nested binding", readContent(bindings.get(0)));
        assertEquals("root binding", readContent(bindings.get(1)));
    }

    private String readContent(URI uri) throws Exception {
        try (InputStream in = uri.toURL().openConnection().getInputStream()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }
            return out.toString(StandardCharsets.UTF_8.name());
        }
    }
}