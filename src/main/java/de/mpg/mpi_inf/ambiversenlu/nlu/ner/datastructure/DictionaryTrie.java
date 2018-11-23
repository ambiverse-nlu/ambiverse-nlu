package de.mpg.mpi_inf.ambiversenlu.nlu.ner.datastructure;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author Dominic Seyler (dseyler@mpi-inf.mpg.de)
 * 
 * This is an implementation of a trie that is used to find matches in a dictionary.
 * 
 * REQUIREMENTS: You need to pass a file with the dictionary and the token delimiter
 * used in that dictionary as arguments to the constructor.
 */
public class DictionaryTrie {
	
    /**
     * If the given key matches an entire entity name the following will 
     * be the value in the trie:
     */
    private static final Integer VALUE_ENTITY = 1;
    
    /**
     * If the given key is part of an entity name the following will
     * be the value in the trie:
     */
    private static final Integer VALUE_MATCH = 0;

    private Map<String, Integer> trie;
    
    private Logger logger = Logger.getLogger(this.getClass());


    public DictionaryTrie(String file, String delimiter) throws IOException {
        logger.debug("Building Trie for file: " + file +"...");

        if(trie == null){
            trie = new HashMap<>();

            String line = null;
            BufferedReader reader;
            try {
                reader = new BufferedReader(new FileReader(file));
                while((line = reader.readLine()) != null)
                {
                    if (line.length() > 0) {
                        String label = line;
                        if(!trie.containsKey(label)){
                            StringTokenizer labelTokenizer = new StringTokenizer(label, delimiter);
                            int size = labelTokenizer.countTokens();
                            StringBuffer token = new StringBuffer();
                            for(int i = 1; i <= size; i++){
                                if (i != size){
                                    token.append(labelTokenizer.nextToken());
                                    if (!trie.containsKey(token.toString())){
                                        trie.put(token.toString(), VALUE_MATCH);
                                    }
                                    token.append(" ");
                                } else {
                                	token.append(labelTokenizer.nextToken());
                                    trie.put(token.toString(), VALUE_ENTITY);
                                }
                            }
                        }
                    }
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isMatch(String key){
    	if(trie.containsKey(key)){
    		return trie.get(key).equals(VALUE_MATCH);
    	}
        return false;
    }
    
    public boolean isEntity(String key){
    	if(trie.containsKey(key)){
    		return trie.get(key).equals(VALUE_ENTITY);
    	}
        return false;
    }
    
    public boolean contains(String key){
    	return trie.containsKey(key);
    }
}
