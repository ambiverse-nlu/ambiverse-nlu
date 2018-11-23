/**
 * 
 */
package de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.annotators;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entities;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.KBIdentifiedEntity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.MentionObject;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaEntity;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.consts.Constants;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.datastructure.BmeowTypeDictionary;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.datastructure.BmeowTypePair;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.datastructure.NerType;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.type.BmeowType;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Dominic Seyler (dseyler@mpi-inf.mpg.de)
 * 
 * Annotates the BMEOW (also BILOU) type tags based on the entities that
 * AIDA found in the document. You need to run this as
 * part of you pre-processing pipeline when you want to use
 * document dictionary features.
 *
 */
public class BmeowTypeAnnotator extends JCasAnnotator_ImplBase {

	public static final String GOLD = "gold";
	@ConfigurationParameter(name = GOLD, mandatory = true)
	private boolean gold;
	
	private static Logger logger = LoggerFactory.getLogger(BmeowTypeAnnotator.class);
	

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		BmeowTypeDictionary dict = null;
		try {
			if (gold) {
				JCas goldView = jCas.getView("gold");
				dict = constructBmeowTypeDictionaryFromGoldStandard(goldView);
			} else {
				dict = constructBmeowTypeDictionaryFromAida(jCas);
			}
			Collection<Token> tokens = JCasUtil.select(jCas, Token.class);

			for(Token token : tokens){
				annotateBmeowType(jCas, dict, token);
			}
		} catch (EntityLinkingDataAccessException | CASException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

	private void annotateBmeowType(JCas jCas, BmeowTypeDictionary dict, Token token) {
		Set<BmeowTypePair> bmeowTypes = dict.getBmeowTypes(token.getCoveredText(), jCas.getDocumentLanguage());
		if(bmeowTypes.isEmpty()){
            BmeowType bmeowType = new BmeowType(jCas, token.getBegin(), token.getEnd());
            bmeowType.setBmeowType("OTHER");
            bmeowType.addToIndexes();
            logger.trace("BmeowType added: " + token.getCoveredText() + ": OTHER");
        } else {
            for(BmeowTypePair type : bmeowTypes){
                BmeowType bmeowType = new BmeowType(jCas, token.getBegin(), token.getEnd());
                bmeowType.setBmeowType(type.getFeatureString());
                bmeowType.addToIndexes();
                logger.trace("BmeowType added: " + token.getCoveredText() + ": " + type.getFeatureString());
            }
        }
	}


	private static BmeowTypeDictionary constructBmeowTypeDictionaryFromGoldStandard(JCas jCas) throws EntityLinkingDataAccessException {
		Entities allEntitiesInDocument = new Entities();
		Collection<de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Entity> entities = JCasUtil.select(jCas, de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Entity.class);

		for(de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Entity e : entities){
			KBIdentifiedEntity kbEntity = new KBIdentifiedEntity(Constants.YAGO_KB_IDENTIFIER + ":" + e.getID());
			allEntitiesInDocument.add(new Entity(kbEntity, DataAccess.getInternalIdForKBEntity(kbEntity)));
		}
		return getDicitionaryForEntities(allEntitiesInDocument);
	}


	private static BmeowTypeDictionary constructBmeowTypeDictionaryFromAida(JCas jCas) throws EntityLinkingDataAccessException {
		Entities allEntitiesInDocument = new Entities();
		Collection<AidaEntity> aidaEntities = JCasUtil.select(jCas, AidaEntity.class);

		for(AidaEntity ae : aidaEntities){
			KBIdentifiedEntity kbId = new KBIdentifiedEntity(ae.getID());
			allEntitiesInDocument.add(new Entity(kbId, DataAccess.getInternalIdForKBEntity(kbId)));
		}

		return getDicitionaryForEntities(allEntitiesInDocument);
	}

	private static BmeowTypeDictionary getDicitionaryForEntities(Entities entities) throws EntityLinkingDataAccessException {
		logger.trace("Entities retrieved from DB: " + entities.getEntities().stream()
				.map(Entity::getNMEnormalizedName).collect(Collectors.joining(", ")));

		TIntObjectHashMap<List<MentionObject>> mentions = DataAccess.getMentionsForEntities(entities);
		int[] entityIds = mentions.keys();

		TIntObjectHashMap<int[]> typeIds = DataAccess.getTypesIdsForEntitiesIds(entityIds);

		BmeowTypeDictionary dict = new BmeowTypeDictionary();

		for (int i = 0; i < entityIds.length; i++) {
			int[] typeId = typeIds.get(entityIds[i]); //for each entity its own set of type ids
			Set<NerType.Label> types = NerType.getNerTypesForTypeIds(typeId);
			for(MentionObject mention : mentions.get(entityIds[i])){
				for (NerType.Label nerType : types) {
					dict.put(mention.getMention(), nerType);
					logger.trace("Dict entry added: " + mention + ", " + nerType);
				}
			}
		}
		return dict;
	}
}
