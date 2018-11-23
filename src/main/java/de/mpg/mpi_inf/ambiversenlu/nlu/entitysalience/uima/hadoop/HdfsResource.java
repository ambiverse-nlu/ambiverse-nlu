package de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.uima.hadoop;

import org.apache.hadoop.fs.*;
import org.springframework.core.io.ContextResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;

class HdfsResource implements ContextResource {
    private final String location;
    private final Path path;
    private final FileSystem fs;
    private boolean exists;
    private final FileStatus status;

    HdfsResource(String location, FileSystem fs) {
        this(location, (String)null, fs);
    }

    HdfsResource(String parent, String child, FileSystem fs) {
        this(StringUtils.hasText(child) ? new Path(new Path(URI.create(parent)), new Path(URI.create(child))) : new Path(URI.create(parent)), fs);
    }

    HdfsResource(Path path, FileSystem fs) {
        Assert.notNull(path, "a valid path is required");
        Assert.notNull(fs, "non null file system required");
        this.location = path.toString();
        this.fs = fs;
        this.path = path.makeQualified(fs);
        boolean exists = false;

        try {
            exists = fs.exists(path);
        } catch (Exception var7) {
            ;
        }

        this.exists = exists;
        FileStatus status = null;

        try {
            status = fs.getFileStatus(path);
        } catch (Exception var6) {
            ;
        }

        this.status = status;
    }

    public long contentLength() throws IOException {
        if (this.exists && this.status != null) {
            return this.status.getLen();
        } else {
            throw new IOException("Cannot access the status for " + this.getDescription());
        }
    }

    public Resource createRelative(String relativePath) throws IOException {
        return new HdfsResource(this.location, relativePath, this.fs);
    }

    public boolean exists() {
        return this.exists;
    }

    public String getDescription() {
        return "HDFS Resource for [" + this.location + "]";
    }

    public File getFile() throws IOException {
        if (this.fs instanceof RawLocalFileSystem) {
            return ((RawLocalFileSystem)this.fs).pathToFile(this.path);
        } else if (this.fs instanceof LocalFileSystem) {
            return ((LocalFileSystem)this.fs).pathToFile(this.path);
        } else {
            throw new UnsupportedOperationException("Cannot resolve File object for " + this.getDescription());
        }
    }

    public String getFilename() {
        return this.path.getName();
    }

    public URI getURI() throws IOException {
        return this.path.toUri();
    }

    public URL getURL() throws IOException {
        return this.path.toUri().toURL();
    }

    public boolean isOpen() {
        return this.exists;
    }

    public boolean isReadable() {
        return this.exists;
    }

    public long lastModified() throws IOException {
        if (this.exists && this.status != null) {
            return this.status.getModificationTime();
        } else {
            throw new IOException("Cannot get timestamp for " + this.getDescription());
        }
    }

    public InputStream getInputStream() throws IOException {
        if (this.exists) {
            return this.fs.open(this.path);
        } else {
            throw new IOException("Cannot open stream for " + this.getDescription());
        }
    }

    public String toString() {
        return this.getDescription();
    }

    public boolean equals(Object obj) {
        return obj == this || obj instanceof Resource && ((Resource)obj).getDescription().equals(this.getDescription());
    }

    public int hashCode() {
        return this.path.hashCode();
    }

    public String getPathWithinContext() {
        return this.path.getName();
    }

    public OutputStream getOutputStream() throws IOException {
        FSDataOutputStream var1;
        try {
            var1 = this.fs.create(this.path, true);
        } finally {
            this.exists = true;
        }

        return var1;
    }

    public boolean isWritable() {
        try {
            return this.exists && this.fs.isFile(this.path) || !this.exists;
        } catch (IOException var2) {
            return false;
        }
    }

    Path getPath() {
        return this.path;
    }
}
