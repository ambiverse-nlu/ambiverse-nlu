package de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.uima.hadoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.PathMatcher;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Set;

public class HdfsResourceLoader implements ResourcePatternResolver, PriorityOrdered, Closeable {
    private static final String PREFIX_DELIMITER = ":";
    private final FileSystem fs;
    private final PathMatcher pathMatcher;
    private final boolean internalFS;

    public HdfsResourceLoader() {
        this(new Configuration(), (URI)null);
    }

    public HdfsResourceLoader(Configuration config) {
        this(config, (URI)null);
    }

    public HdfsResourceLoader(Configuration config, URI uri, String user) {
        this.pathMatcher = new AntPathMatcher();
        this.internalFS = true;
        FileSystem tempFS = null;

        try {
            if (uri == null) {
                uri = FileSystem.getDefaultUri(config);
            }

            tempFS = user != null ? FileSystem.get(uri, config, user) : FileSystem.get(uri, config);
        } catch (Exception var9) {
            tempFS = null;
            throw new IllegalStateException("Cannot create filesystem", var9);
        } finally {
            this.fs = tempFS;
        }

    }

    public HdfsResourceLoader(Configuration config, URI uri) {
        this(config, uri, (String)null);
    }

    public HdfsResourceLoader(FileSystem fs) {
        this.pathMatcher = new AntPathMatcher();
        Assert.notNull(fs, "a non-null file-system required");
        this.fs = fs;
        this.internalFS = false;
    }

    public FileSystem getFileSystem() {
        return this.fs;
    }

    public ClassLoader getClassLoader() {
        return this.fs.getConf().getClassLoader();
    }

    public Resource getResource(String location) {
        return new HdfsResource(location, this.fs);
    }

    public Resource[] getResources(String locationPattern) throws IOException {
        return this.pathMatcher.isPattern(stripPrefix(locationPattern)) ? this.findPathMatchingResources(locationPattern) : new Resource[]{this.getResource(locationPattern)};
    }

    protected Resource[] findPathMatchingResources(String locationPattern) throws IOException {
        if (locationPattern.startsWith("~/")) {
            locationPattern = locationPattern.substring(2);
        }

        String rootDirPath = this.determineRootDir(locationPattern);
        String subPattern = locationPattern.substring(rootDirPath.length());
        if (rootDirPath.isEmpty()) {
            rootDirPath = ".";
        }

        Resource rootDirResource = this.getResource(rootDirPath);
        Set<Resource> result = new LinkedHashSet(16);
        result.addAll(this.doFindPathMatchingPathResources(rootDirResource, subPattern));
        return (Resource[])result.toArray(new Resource[result.size()]);
    }

    protected String determineRootDir(String location) {
        int prefixEnd = location.indexOf(":") + 1;

        int rootDirEnd;
        for(rootDirEnd = location.length(); rootDirEnd > prefixEnd && this.pathMatcher.isPattern(location.substring(prefixEnd, rootDirEnd)); rootDirEnd = location.lastIndexOf(47, rootDirEnd - 2) + 1) {
            ;
        }

        if (rootDirEnd == 0) {
            rootDirEnd = prefixEnd;
        }

        return location.substring(0, rootDirEnd);
    }

    private Set<Resource> doFindPathMatchingPathResources(Resource rootDirResource, String subPattern) throws IOException {
        Path rootDir = rootDirResource instanceof HdfsResource ? ((HdfsResource)rootDirResource).getPath() : new Path(rootDirResource.getURI().toString());
        Set<Resource> results = new LinkedHashSet();
        String pattern = subPattern;
        if (!subPattern.startsWith("/")) {
            pattern = "/".concat(subPattern);
        }

        this.doRetrieveMatchingResources(rootDir, pattern, results);
        return results;
    }

    private void doRetrieveMatchingResources(Path rootDir, String subPattern, Set<Resource> results) throws IOException {
        if (!this.fs.isFile(rootDir)) {
            FileStatus[] statuses = null;
            statuses = this.fs.listStatus(rootDir);
            if (!ObjectUtils.isEmpty(statuses)) {
                String root = rootDir.toUri().getPath();
                FileStatus[] var6 = statuses;
                int var7 = statuses.length;

                for(int var8 = 0; var8 < var7; ++var8) {
                    FileStatus fileStatus = var6[var8];
                    Path p = fileStatus.getPath();
                    String location = p.toUri().getPath();
                    if (location.startsWith(root)) {
                        location = location.substring(root.length());
                    }

                    if (fileStatus.isDir() && this.pathMatcher.matchStart(subPattern, location)) {
                        this.doRetrieveMatchingResources(p, subPattern, results);
                    } else if (this.pathMatcher.match(subPattern.substring(1), location)) {
                        results.add(new HdfsResource(p, this.fs));
                    }
                }
            }
        } else if (this.pathMatcher.match(subPattern, stripPrefix(rootDir.toUri().getPath()))) {
            results.add(new HdfsResource(rootDir, this.fs));
        }

    }

    private static String stripPrefix(String path) {
        int index = path.indexOf(":");
        return index > -1 ? path.substring(index + 1) : path;
    }

    public int getOrder() {
        return -2147483648;
    }

    public void destroy() throws IOException {
        this.close();
    }

    public void close() throws IOException {
        if (this.fs != null & this.internalFS) {
            this.fs.close();
        }

    }
}