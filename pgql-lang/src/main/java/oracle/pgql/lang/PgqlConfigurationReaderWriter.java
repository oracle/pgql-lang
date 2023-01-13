package oracle.pgql.lang;

import java.io.IOException;
import java.io.Reader;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.config.AConfigurationReaderWriter;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.language.LanguageVersion;

import com.fasterxml.jackson.core.JsonFactory;
import com.google.inject.ConfigurationException;
import com.virtlink.commons.configuration2.jackson.JacksonConfiguration;

public class PgqlConfigurationReaderWriter extends AConfigurationReaderWriter {

  @Override
  protected JacksonConfiguration createNew(HierarchicalConfiguration<ImmutableNode> sourceConfiguration) {
    return new JacksonConfiguration(new JsonFactory()) {};
  }

  public JacksonConfiguration create(HierarchicalConfiguration<ImmutableNode> source, FileObject rootFolder) {
    return createNew(source);
  }

  @Override
  public HierarchicalConfiguration<ImmutableNode> read(Reader reader, FileObject rootFolder)
      throws IOException, ConfigurationException {
      JacksonConfiguration config = create(null, rootFolder);
      config.setProperty("name", "pgql-lang");
      config.setProperty("id", new LanguageIdentifier("oracle.pg", "pgqllang", new LanguageVersion(0,0,0,"SNAPSHOT")));
      return config;
  }
}
