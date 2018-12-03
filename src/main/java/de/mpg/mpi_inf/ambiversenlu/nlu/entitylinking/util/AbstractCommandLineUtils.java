package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util;

import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.util.FileUtils;
import org.apache.commons.cli.*;

import java.io.File;
import java.util.Collection;

public abstract class AbstractCommandLineUtils {

    private String shortName;
    private String fullName;
    public AbstractCommandLineUtils(String shortName, String fullName) {
        this.shortName = shortName;
        this.fullName = fullName;
    }

    public AbstractCommandLineUtils(String shortName) {
        this.shortName = shortName;
        this.fullName = shortName;
    }

    public CommandLine parseCommandLineArgs(String[] args) throws ParseException {
        Options commandLineOptions = buildCommandLineOptions();
        CommandLineParser parser = new PosixParser();
        CommandLine cmd = parser.parse(commandLineOptions, args);
        return cmd;
    }

    protected abstract Options buildCommandLineOptions() throws ParseException;

    private void printHelp(Options commandLineOptions) {
        String header = "\n\nRun " + shortName + ":\n\n";
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(fullName, header,
                commandLineOptions, "", true);
//        System.exit(0);
        throw new RuntimeException("Not enough arguments!");
    }

    public static File[] readFilesFromDir(String dir) {
        File inputFile = new File(dir);
        if(!inputFile.isDirectory()) {
            System.out.println(dir + " Error: expected to be a directory.");
        }
        Collection<File> files = FileUtils.getAllFiles(inputFile);
        return files.toArray(new File[files.size()]);
    }
}