package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util;

import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Stream;

public class ClassPathUtils {

    private static Logger logger_ = LoggerFactory.getLogger(ClassPathUtils.class);
    private static ConcurrentMap<String, Object> locks = new ConcurrentHashMap<>();

    public ClassPathUtils() {

    }

    public static Properties getPropertiesFromClasspath(String propFileName)
            throws IOException {
        return getPropertiesFromClasspath(propFileName, null);
    }

    /**
     * Reads the specified properties file from the classpath. Returns null if it does not exist.
     *
     * @param propFileName
     * @param defaultProperties
     * @return The specified properties, null if they do not exist.
     * @throws IOException
     */
    public static Properties getPropertiesFromClasspath(String propFileName, Properties defaultProperties) throws IOException {
        Properties props = new Properties(defaultProperties);
        InputStream inputStream = ClassPathUtils.class.getClassLoader()
                .getResourceAsStream(propFileName);

        if (inputStream == null && Files.exists(Paths.get(propFileName))) {
            inputStream = new FileInputStream(new File(propFileName));
        }

        if (inputStream == null) {
            return null;
        } else {
            props.load(new InputStreamReader(inputStream, "UTF-8"));
            inputStream.close();
            return props;
        }
    }

    /**
     * Checks if the specified resource exists on the classPath.
     *
     * @param fileName
     * @return
     */
    public static boolean checkResource(String fileName) {
        InputStream inputStream = ClassPathUtils.class.getClassLoader()
                .getResourceAsStream(fileName);
        try {
            if (inputStream == null && new FileInputStream(new File(fileName)) == null) {
                return false;
            } else {
                return true;
            }
        } catch (FileNotFoundException e) {
            return false;
        }
    }


    public static File getFileFromClassPath(String fileName) throws URISyntaxException, FileNotFoundException {
        URL url = ClassPathUtils.class.getClassLoader()
                .getResource(fileName);
        if (url == null) {
            throw new FileNotFoundException("property file '" + fileName
                    + "' not found in the classpath");
        }
        return new File(url.toURI());
    }

    public static Path getPathFromClassPath(String path) throws URISyntaxException, FileNotFoundException {
        URL url = ClassPathUtils.class.getClassLoader()
            .getResource(path);

        if (url == null) {
            throw new FileNotFoundException("Path '" + path
                + "' not found in the classpath");
        }

        return Paths.get(url.toURI());
    }

    public static InputStream getInputStream(String fileName) {
        return ClassPathUtils.class.getClassLoader()
                .getResourceAsStream(fileName);
    }

    public static List<String> getContent(String fileName) throws IOException {
        List<String> content = new ArrayList<String>();
        InputStream inputStream = ClassPathUtils.class.getClassLoader()
                .getResourceAsStream(fileName);

        if (inputStream == null && Files.exists(Paths.get(fileName))) {
            inputStream = new FileInputStream(new File(fileName));
        }

        if (inputStream == null) {
            throw new FileNotFoundException("property file '" + fileName
                    + "' not found in the classpath");
        }

        BufferedReader bufReader = FileUtils.getBufferedUTF8Reader(inputStream);

        String line;
        while (true) {
            line = bufReader.readLine();
            if (line == "" || line == null) break;
            content.add(line);
        }

        bufReader.close();
        return content;
    }

    public static BufferedReader getBufferedReader(String fileName) throws FileNotFoundException {
      InputStream inputStream = ClassPathUtils.class.getClassLoader()
          .getResourceAsStream(fileName);

      if (inputStream == null && Files.exists(Paths.get(fileName))) {
        inputStream = new FileInputStream(new File(fileName));
      }

      if (inputStream == null) {
        throw new FileNotFoundException("property file '" + fileName
            + "' not found in the classpath");
      }

      return FileUtils.getBufferedUTF8Reader(inputStream);
    }

    public static <T> T walk(String location, Function<Stream<Path>, T> l, int maxDepth) throws Exception {
        T result;
        try {
            URI uri = ClassPathUtils.class.getClassLoader().getResource(location).toURI();
            if ("jar".equals(uri.getScheme())) {
                result = safeWalkJar(location, uri, l);
            } else {
                result = l.apply(Files.walk(getFileFromClassPath(location).toPath(), maxDepth));
            }
            ;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

  public static <T> T walkFileTree(String location, Function<Path, T> f, Set<FileVisitOption> options, FileVisitor<Path> fileVisitor, int maxDepth) throws Exception {
    T result;
    try {
      URI uri = ClassPathUtils.class.getClassLoader().getResource(location).toURI();
      if ("jar".equals(uri.getScheme())) {
        result = safeWalkFileTreeJar(location, uri, f, options, fileVisitor, maxDepth);
      } else {
        result = f.apply(Files.walkFileTree(getFileFromClassPath(location).toPath(), options, maxDepth, fileVisitor ));
      }
      ;

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return result;
  }

  private static <T> T safeWalkFileTreeJar(String path, URI uri, Function<Path, T> f, Set<FileVisitOption> options, FileVisitor<Path> fileVisitor, int maxDepth) throws Exception {

    synchronized (getLock(uri)) {
      // this'll close the FileSystem object at the end
      try (FileSystem fs = getFileSystem(uri)) {
        //                walkFiles(fs.getPath(path));
        Path walk = Files.walkFileTree(fs.getPath(path), options, maxDepth, fileVisitor);
        return f.apply(walk);
      }
    }
  }

    private static <T> T safeWalkJar(String path, URI uri, Function<Stream<Path>, T> f) throws Exception {

        synchronized (getLock(uri)) {
            // this'll close the FileSystem object at the end
            try (FileSystem fs = getFileSystem(uri)) {
//                walkFiles(fs.getPath(path));
                Stream<Path> walk = Files.walk(fs.getPath(path), 1);
                return f.apply(walk);
            }
        }
    }

    private static Object getLock(URI uri) {

        String fileName = parseFileName(uri);
        locks.computeIfAbsent(fileName, s -> new Object());
        return locks.get(fileName);
    }

    private static String parseFileName(URI uri) {

        String schemeSpecificPart = uri.getSchemeSpecificPart();
        return schemeSpecificPart.substring(0, schemeSpecificPart.indexOf("!"));
    }

    private static FileSystem getFileSystem(URI uri) throws IOException {

        try {
            return FileSystems.getFileSystem(uri);
        } catch (FileSystemNotFoundException e) {
            return FileSystems.newFileSystem(uri, Collections.<String, String>emptyMap());
        }
    }

}
