package org.jvnet.higherjaxb.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AbstractHigherjaxbBaseMojoTest {

	@TempDir
	private File temporaryFolder;
    private File testJarFile;

    @BeforeEach
    public void createJarFile() throws Exception {
        testJarFile = new File(temporaryFolder, "test.jar");
        try (JarOutputStream out = new JarOutputStream(new FileOutputStream(testJarFile))) {
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
        
        final AbstractHigherjaxbBaseMojo<Void> mojo = new AbstractHigherjaxbBaseMojo<Void>()
        {
			@Override
			protected CoreOptionsFactory<Void> getOptionsFactory()
			{
				throw new UnsupportedOperationException();
			}
			
			@Override
			public void doExecute(Void options) throws MojoExecutionException
			{
				throw new UnsupportedOperationException();
			}

			@Override
			protected String getJaxbNamespaceURI()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			protected String[] getXmlSchemaNames(Class<?> packageInfoClass)
			{
				throw new UnsupportedOperationException();
			}

			@Override
			protected String getEpisodePackageName()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			protected String[] getXmlNamespaceNames(Class<?> packageInfoClass)
			{
				throw new UnsupportedOperationException();
			}
		}; 

        mojo.collectBindingURIsFromArtifact(testJarFile, bindings);

        assertEquals(2, bindings.size());
        assertEquals(URI.create("jar:" + testJarFile.toURI() + "!/dir/nested.xjb"), bindings.get(0));
        assertEquals(URI.create("jar:" + testJarFile.toURI() + "!/root.xjb"), bindings.get(1));
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