package java.nio.file.attribute;

import java.time.Instant;

public class FileTimeProvider implements FileTime.Provider {
  public FileTimeProvider() {
    FileTime.setProvider(this);
  }

  @Override
  public Object toInstant(FileTime fileTime) {
    return Instant.ofEpochMilli(fileTime.toMillis());
  }

  @Override
  public FileTime fromInstant(Object instant) {
    return FileTime.fromMillis(((Instant) instant).toEpochMilli());
  }
}
