package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.readers.util.nyt;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entities;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entity;
import edu.stanford.nlp.util.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

import static de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.readers.util.nyt.NYTAnnotationReader.getAllIds;

public class Freebase2Wikidata2Yago {

    public static Map<String, String> freebase2Wikidata(String path) throws IOException {
        final Map<String, String> result = new HashMap<>();
        Files.lines(Paths.get(path)).forEach(l -> {
            String[] split = l.split("\t");
            result.put(split[1], split[0]);
        });
        return result;
    }

    public static Map<String, String> wikidata2Freebase(String path) throws IOException {
        final Map<String, String> result = new HashMap<>();
        Files.lines(Paths.get(path)).forEach(l -> {
            String[] split = l.split("\t");
            result.put(split[0], split[1]);
        });
        return result;
    }

    public static Map<String, String> fb2w(String path, String delimiter) throws IOException {
        final Map<String, String> result = new HashMap<>();
        File[] files = null;
        if(Paths.get(path).toFile().isDirectory()) {
            files = Paths.get(path).toFile().listFiles();
        } else {
            files = new File[1];
            files[0] = Paths.get(path).toFile();
        }

        System.out.println("Searching in files : "+Arrays.toString(files));

        for(File file : files) {
            if(!file.getName().startsWith(".")) {
                Files.lines(file.toPath()).forEach(l -> {
                    String[] split = l.split(delimiter);
                    String fbId = split[0];

                    fbId = fbId.replaceAll("<", "").replaceAll(">", "").substring("http://rdf.freebase.com/ns".length()).replace(".", "/");

                    String wdId = split[2];
                    wdId = wdId.replaceAll("<", "").replaceAll(">", "").substring(wdId.lastIndexOf("/")).replace(" .", "");
                    result.put(fbId, wdId);
                });
            }
        }
        return result;
    }

    public static void saveToFile(String path, Map<String, String> data) throws IOException {
        Files.write(Paths.get(path), () -> data.entrySet().stream().<CharSequence>map(e-> e.getKey() + "\t"+e.getValue()).iterator());
    }

    private static void fb2yago1() throws EntityLinkingDataAccessException, IOException {
        EntityLinkingManager.init();

        Map<String, String> fb2wd = fb2w("/Users/dmilchev/Development/collections/freebase-instance-sameas", " ");
        Set<String> ids = getAllIds("/Users/dmilchev/Development/collections/nyt/nyt-all");

        System.out.println("All Training entities "+ids.size());

        Set<String> notFound = new HashSet<>();
        Map<String, String> wk2fb = new HashMap<>();
        List<String> wikiDataIds = new ArrayList<>();
        for(String fbId : ids) {
            if(fb2wd.containsKey(fbId)) {
                wk2fb.put(fb2wd.get(fbId), fbId);
                wikiDataIds.add(fb2wd.get(fbId));
            } else {
                notFound.add(fbId);
            }
        }
        System.out.println("Not Found "+notFound.size());
        System.out.println("Found: "+wikiDataIds.size());

        for(int i=0; i<10; i++) {
            System.out.println(wikiDataIds.get(i));
        }

        Map<String, Integer> internalIdsfromWikidataIds = DataAccess.getInternalIdsfromWikidataIds(wikiDataIds);
        System.out.println("Found internal ids "+internalIdsfromWikidataIds.size());

        Integer[] internalIds = internalIdsfromWikidataIds.values().toArray(new Integer[internalIdsfromWikidataIds.size()]);

        Entities entities = DataAccess.getAidaEntitiesForInternalIds(ArrayUtils.toPrimitive(internalIds));

        Map<String, String> fb2yId = new HashMap<>();
        int i=0;
        for(Map.Entry<String, Integer> entry : internalIdsfromWikidataIds.entrySet()) {
            Entity e = entities.getEntityById(entry.getValue());
            String yagoId = e.getKbIdentifiedEntity().getDictionaryKey();
            String fbId = wk2fb.get(entry.getKey());
            fb2yId.put(fbId, yagoId);
            if(i<10) {
                System.out.println(fbId+" : "+yagoId);
                i++;
            }
        }

        System.out.println("The final map is "+fb2yId.size());

        saveToFile("/Users/dmilchev/Development/collections/nyt/fb2yagoIds.tsv", fb2yId);
    }

    private static void fb2yago2() throws EntityLinkingDataAccessException, IOException, SQLException {
        EntityLinkingManager.init();

        Map<String, String> fb2title = wikidata2Freebase("/Users/dmilchev/Downloads/mid2name.tsv");

        Set<String> ids = getAllIds("/Users/dmilchev/Development/collections/nyt/nyt-all");

        List<String> titles = new ArrayList<>();
        Map<String, String> title2wb = new HashMap<>();
        Set<String> notFound = new HashSet<>();
        for(String fbId : ids) {
            if(fb2title.containsKey(fbId)) {
                title2wb.put(fb2title.get(fbId), fbId);
                titles.add(fb2title.get(fbId));
            } else {
                notFound.add(fbId);
            }
        }
        List<Integer> internalIds = title2Id(titles);

        System.out.println("Total: "+ids.size());
        System.out.println("Not Found "+notFound.size());
        System.out.println("Found: "+titles.size());
        System.out.println("Internal Ids: "+internalIds.size());

        //Entities entities = DataAccess.getAidaEntitiesForInternalIds(ArrayUtils.toPrimitive(internalIds));


    }

    private static List<Integer> title2Id(List<String> titles) throws SQLException {
        Connection conn = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
        Statement stmt = null;

        conn.setAutoCommit(false);
        stmt = conn.createStatement();
        stmt.setFetchSize(200000);
        String titleQuery = getTitleuery(titles);
        //String sql = "SELECT entity FROM entity_metadata WHERE humanreadablererpresentation IN ("+ titleQuery+")";
        //ResultSet rs = stmt.executeQuery(sql);
        String sql = "SELECT entity FROM entity_metadata WHERE humanreadablererpresentation =?";

        PreparedStatement pstmt = conn.prepareStatement(sql);
        List<Integer> result = new ArrayList<>();
        for(String title : titles) {
            pstmt.setString(1, title);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()) {
                result.add(rs.getInt("entity"));
            } else {
                System.out.println("Nothing found for "+title);
            }
        }

//        while (rs.next()) {
//            Integer entity = rs.getInt("entity");
//            result.add(entity);
//        }
        return result;
    }

    public static String getTitleuery(List<String> titles) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < titles.size(); ++i) {
            String title = "'"+titles.get(i).replaceAll("'", "''")+"'";
            sb.append(title);
            if (i < titles.size() - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    public static void main(String[] args) throws IOException, EntityLinkingDataAccessException, SQLException {
        fb2yago1();
    }
}
