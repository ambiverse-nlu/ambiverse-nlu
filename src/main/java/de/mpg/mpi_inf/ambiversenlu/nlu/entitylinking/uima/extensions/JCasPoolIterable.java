package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.extensions;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.internal.ResourceManagerFactory;
import org.apache.uima.fit.util.LifeCycleUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.CasManager;
import org.apache.uima.resource.Resource;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceManager;

import java.io.IOException;
import java.util.Iterator;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;

public class JCasPoolIterable implements Iterable<JCas>, Iterator<JCas> {

  private final CollectionReader collectionReader;

  private final AnalysisEngine[] analysisEngines;

  private boolean selfComplete;

  private boolean selfDestroy;

  private boolean destroyed;

  private CasManager casManager;

  private int casPoolSize;

  public JCasPoolIterable(int casPoolSize, CollectionReaderDescription aReader, AnalysisEngineDescription... aEngines)
      throws CASException, ResourceInitializationException {
    this.selfComplete = false;
    this.selfDestroy = false;
    this.destroyed = false;
    this.casPoolSize = casPoolSize;
    this.collectionReader = CollectionReaderFactory.createReader(aReader);
    analysisEngines = new AnalysisEngine[aEngines.length];
    for (int i = 0; i < aEngines.length; i++) {
      analysisEngines[i] = createEngine(aEngines[i]);
    }
    ResourceManager rm = ResourceManagerFactory.newResourceManager();
    rm.getCasManager().addMetaData(collectionReader.getProcessingResourceMetaData());
    AnalysisEngine[] resMgr = analysisEngines;
    int var5 = aEngines.length;
    for (int var6 = 0; var6 < var5; ++var6) {
      AnalysisEngine ae = resMgr[var6];
      rm.getCasManager().addMetaData(ae.getProcessingResourceMetaData());
    }
    casManager = rm.getCasManager();
    casManager.defineCasPool("iterableJcas", casPoolSize, null);
  }

  public boolean hasNext() {
    if (this.destroyed) {
      return false;
    } else {
      boolean error = true;

      boolean var3;
      try {
        boolean e = this.collectionReader.hasNext();
        error = false;
        var3 = e;
      } catch (CollectionException var8) {
        throw new IllegalStateException(var8);
      } catch (IOException var9) {
        throw new IllegalStateException(var9);
      } finally {
        if (error && this.selfDestroy) {
          this.destroy();
        }

      }

      return var3;
    }
  }

  public JCas next() {
    JCas jCas;
    try {
      jCas = casManager.getCas("iterableJcas").getJCas();
    } catch (UIMAException e) {
      throw new RuntimeException(e);
    }
    boolean error = true;
    boolean destroyed = false;

    try {
      this.collectionReader.getNext(jCas.getCas());
      AnalysisEngine[] e = this.analysisEngines;
      int var4 = e.length;

      for (int var5 = 0; var5 < var4; ++var5) {
        AnalysisEngine engine = e[var5];
        engine.process(jCas);
      }

      if ((this.selfComplete || this.selfDestroy) && !this.hasNext()) {
        if (this.selfComplete) {
          this.collectionProcessComplete();
        }

        if (this.selfDestroy) {
          this.destroy();
          destroyed = true;
        }
      }
      error = false;
      return jCas;
    } catch (CollectionException var12) {
      throw new IllegalStateException(var12);
    } catch (IOException var13) {
      throw new IllegalStateException(var13);
    } catch (AnalysisEngineProcessException var14) {
      throw new IllegalStateException(var14);
    } finally {
      if (error && this.selfDestroy && !destroyed) {
        this.destroy();
      }
    }
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }

  public void collectionProcessComplete() throws AnalysisEngineProcessException {
    LifeCycleUtil.collectionProcessComplete(this.analysisEngines);
  }

  public void destroy() {
    if (!this.destroyed) {
      LifeCycleUtil.close(this.collectionReader);
      LifeCycleUtil.destroy(new Resource[] { this.collectionReader });
      LifeCycleUtil.destroy(this.analysisEngines);
      this.destroyed = true;
    }

  }

  public boolean isSelfComplete() {
    return this.selfComplete;
  }

  public void setSelfComplete(boolean aSelfComplete) {
    this.selfComplete = aSelfComplete;
  }

  public boolean isSelfDestroy() {
    return this.selfDestroy;
  }

  public void setSelfDestroy(boolean aSelfDestroy) {
    this.selfDestroy = aSelfDestroy;
  }

  @Override public Iterator<JCas> iterator() {
    return this;
  }
}

