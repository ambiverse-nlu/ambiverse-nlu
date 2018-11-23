package de.mpg.mpi_inf.ambiversenlu.nlu.openie.clausie;

import de.mpg.mpi_inf.ambiversenlu.nlu.openie.clausie.Constituent.Type;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.Tree;

import java.util.*;

/** This is a provisory implementation of the processing of coordinating conjunctions.
 *
 * Coordinating conjunctions are still a difficult issue for the parser and therefore 
 * the source of a significant loss in precision by ClausIE.
 *
 * Code is not clean or optimally efficient. More work needs to be done in how to handle CCs.
 *
 * @date $  $
 * @version $ $ */
public class ProcessConjunctions {

  public static List<SemanticGraph> processCC(SemanticGraph semanticGraph, Options options) {
    List<SemanticGraph> semanticGraphs = new ArrayList<>();
    semanticGraphs.add(new SemanticGraph(semanticGraph));
    processCC(semanticGraphs, options);
    return semanticGraphs;
  }

  public static void processCC(List<SemanticGraph> semanticGraphs, Options options) {
    boolean exit = false;
    for(SemanticGraph semanticGraph: semanticGraphs) {
      Iterator<SemanticGraphEdge> edges = semanticGraph.edgeIterable().iterator();
      while(edges.hasNext()) {
        SemanticGraphEdge edge = edges.next();
        if(DpUtils.isAnyConj(edge)) {
          boolean addDependenciesToGovernor = false;
          boolean addDependenciesToDependent = false;
          if(edge.getGovernor().get(CoreAnnotations.PartOfSpeechAnnotation.class).startsWith("V")) {
            if(countRelevantOutgoing(edge, semanticGraph, true)) {
              addDependenciesToGovernor = true;
            } else if (countRelevantOutgoing(edge, semanticGraph, false)){
              addDependenciesToDependent = true;
            }
          }
          SemanticGraph nSemanticGraph = createSemanticGraph(semanticGraph, edge,
              addDependenciesToGovernor, addDependenciesToDependent);
          semanticGraphs.add(nSemanticGraph);
          exit = true;
          break;
        }
      }
      if(exit) {
        break;
      }
    }
  }

  private static boolean countRelevantOutgoing(SemanticGraphEdge edge, SemanticGraph semanticGraph, boolean b) {
    Set<GrammaticalRelation> relationsGovernor = collectRelations(semanticGraph.getOutEdgesSorted(edge.getGovernor()));
    Set<GrammaticalRelation> relationsDependent = collectRelations(semanticGraph.getOutEdgesSorted(edge.getGovernor()));
    if(relationsGovernor.size() < 2) {
      relationsGovernor.removeAll(relationsDependent);
      if(relationsGovernor.size() == 0) {
        return true;
      }
    } else if(relationsDependent.size() < 2) {
      relationsDependent.removeAll(relationsGovernor);
      if(relationsDependent.size() == 0) {
        return true;
      }
    }
    return false;
  }

  private static SemanticGraph createSemanticGraph(SemanticGraph semanticGraph, SemanticGraphEdge r,
      boolean addDependenciesToGovernor, boolean addDependenciesToDependent) {
    SemanticGraph result = new SemanticGraph(semanticGraph);
    DpUtils.replaceNodeFromSemanticGraph(r.getGovernor(), r.getDependent(), result, true);
    DpUtils.disconectNodeFromSemanticGraph(r.getDependent(), semanticGraph);
    DpUtils.removeEdges(semanticGraph, DpUtils.findAllRelations(semanticGraph.getOutEdgesSorted(r.getGovernor()),
        EnglishGrammaticalRelations.COORDINATION));
    if(addDependenciesToGovernor) {
      addEdges(result.getOutEdgesSorted(r.getDependent()), r.getGovernor(), semanticGraph);
    }
    if(addDependenciesToDependent) {
      addEdges(semanticGraph.getOutEdgesSorted(r.getGovernor()), r.getDependent(), result);
    }
    result.prettyPrint();
    return result;
  }



  private static void addEdges(List<SemanticGraphEdge> outEdgesSorted, IndexedWord governor, SemanticGraph semanticGraph) {
    Set<GrammaticalRelation> relations = collectRelations(outEdgesSorted);
    Set<GrammaticalRelation> noRelations = collectRelations(semanticGraph.getOutEdgesSorted(governor));
    relations.removeAll(noRelations);
    for(SemanticGraphEdge edge: outEdgesSorted) {
      if(!relations.contains(edge.getRelation())) {
        continue;
      }
      SemanticGraphEdge nedge = new SemanticGraphEdge(governor, edge.getDependent(), edge.getRelation(), edge.getWeight(), edge.isExtra());
      semanticGraph.addEdge(nedge);
    }
  }

  private static Set<GrammaticalRelation> collectRelations(List<SemanticGraphEdge> outEdgesSorted) {
    Set<GrammaticalRelation> relations = new HashSet<>();
    for(SemanticGraphEdge edge: outEdgesSorted) {
      relations.add(edge.getRelation());
    }
    return  relations;
  }

  /** Process CCs of a given constituent */
  public static List<Constituent> processCC(Tree depTree, Constituent constituent, boolean processVerb, boolean completeProcess) {
    return generateConstituents(depTree, (IndexedConstituent) constituent, processVerb, completeProcess);
  }

  /** Generates a set of constituents from a CC for a given constituent */
  private static List<Constituent> generateConstituents(Tree depTree, IndexedConstituent constituent, boolean processVerb, boolean completeProcess) {
    IndexedConstituent copy = constituent.clone();
    //copy.setSemanticGraph( copy.createReducedSemanticGraph() );
    copy.setSemanticGraph(copy.getSemanticGraph());
    List<Constituent> result = new ArrayList<Constituent>();
    result.add(copy);
    if (processVerb || !copy.type.equals(Type.VERB)) {
      generateConstituents(copy.getSemanticGraph(), depTree, copy, copy.getRoot(), result, true, processVerb, completeProcess);
      removeUnnecessary(result); //To assure consistency, vertex cannot be eliminated because it affects indices so we eliminate edges
    }
    return result;

  }

  private static void removeUnnecessary(List<Constituent> result) {
    for (Constituent c : result) {
      SemanticGraph semanticGraph = ((IndexedConstituent) c).getSemanticGraph();
      IndexedWord root = ((IndexedConstituent) c).getRoot();
      List<SemanticGraphEdge> edges = semanticGraph.edgeListSorted();
      Set<IndexedWord> descendants = semanticGraph.descendants(root);
      for (int i = 0; i < edges.size(); i++) {
        if (!descendants.contains(edges.get(i).getDependent())) {
          semanticGraph.removeEdge(edges.get(i));
          edges = semanticGraph.edgeListSorted();
          i--;
        }
      }
    }

  }

  // Process CCs by exploring the graph from one constituent and generating more constituents as
  // it encounters ccs
  private static void generateConstituents(SemanticGraph semanticGraph, Tree depTree, IndexedConstituent constituent, IndexedWord root,
      List<Constituent> constituents, boolean firstLevel, boolean processVerb, boolean completeProcess) {

    List<SemanticGraphEdge> outedges = semanticGraph.getOutEdgesSorted(root);
    List<SemanticGraphEdge> conjunct = DpUtils.getEdges(outedges, EnglishGrammaticalRelations.COORDINATION);

    Boolean processCC = true;
    SemanticGraphEdge predet = null;

    //to avoid processing under certain circunstances must be design properly when final setup is decided
    if (conjunct != null && !conjunct.isEmpty()) {
      for (SemanticGraphEdge edge : outedges) {
        if (edge.getDependent().lemma().equals("between")) {
          processCC = false;
          break;
        }
      }
      SemanticGraphEdge con = DpUtils.findFirstOfRelation(outedges, EnglishGrammaticalRelations.QUANTIFIER_MODIFIER);
      if (con != null && con.getDependent().lemma().equals("between")) processCC = false;
      List<SemanticGraphEdge> inedg = semanticGraph.getIncomingEdgesSorted(root);
      SemanticGraphEdge pobj = DpUtils.findFirstOfRelation(inedg, EnglishGrammaticalRelations.PREPOSITIONAL_OBJECT);
      // this wont work with collapsed dependencies
      if (pobj != null && pobj.getGovernor().lemma().equals("between")) processCC = false;
      Collection<IndexedWord> sibs = null;
      try {
        sibs = semanticGraph.getSiblings(root);
      } catch (Exception e) {
        System.out.print(root.toString());
      }
      for (IndexedWord sib : sibs) {
        List<SemanticGraphEdge> insib = semanticGraph.getIncomingEdgesSorted(sib);
        predet = DpUtils.findFirstOfRelation(insib, EnglishGrammaticalRelations.PREDETERMINER);
        if (predet == null) predet = DpUtils.findFirstOfRelation(insib, EnglishGrammaticalRelations.DETERMINER);
        if (predet != null) break;
      }
    }

    for (SemanticGraphEdge edge : outedges) {
      if (DpUtils.isParataxis(edge) || DpUtils.isRcmod(edge) || DpUtils
          .isAppos(edge)) //||(DpUtils.isDep(edge) && constituent.type.equals(Type.VERB)
        continue;//to avoid processing relative clauses and appositions which are included as an independent clause in the clauses list of the sentence, also no dep in verbs are processed. To reproduce the results of the paper comment this line and eliminate the duplicate propositions that may be generated.
      if (DpUtils.isAnyConj(edge) && processCC) {
        boolean cont = false;
        for (SemanticGraphEdge c : conjunct) {
          if (c.getDependent().lemma().equals("&") && nextToVerb(depTree, semanticGraph, root, edge.getDependent(), c.getDependent())) {
            cont = true;
            break;
          }
        }

        if (cont) continue;

        IndexedWord newRoot = edge.getDependent();
        if (predet != null && predet.getDependent().lemma().equals("both")) constituent.getExcludedVertexes().add(predet.getDependent());

        List<IndexedConstituent> newConstituents = new ArrayList<IndexedConstituent>();
        for (Constituent c : constituents) {
          if (!((IndexedConstituent) c).getSemanticGraph().descendants(((IndexedConstituent) c).getRoot()).contains(root)
              || !containsDescendant(depTree, ((IndexedConstituent) c).getSemanticGraph(), semanticGraph, root, newRoot) && shareAllAncestors(
              semanticGraph, ((IndexedConstituent) c).getSemanticGraph(), root)) continue;

          IndexedConstituent newConstituent = ((IndexedConstituent) c).clone();
          if (firstLevel) {
            newConstituent.setRoot(newRoot);
          }
          newConstituents.add(newConstituent);
        }

        boolean isTreeRoot = false;
        //To process verbs if the main conj is the root of the graph
        if (!semanticGraph.getRoots().contains(root)) {
          // Assign all the parents to the conjoint
          for (Constituent c : newConstituents) {
            SemanticGraph newSemanticGraph = ((IndexedConstituent) c).getSemanticGraph();
            Collection<IndexedWord> parents = newSemanticGraph.getParents(root);
            for (IndexedWord parent : parents) {
              GrammaticalRelation reln = newSemanticGraph.reln(parent, root);
              double weight = newSemanticGraph.getEdge(parent, root).getWeight();
              newSemanticGraph.addEdge(parent, newRoot, reln, weight, false);
            }
          }
          //To process verbs if the main conj is the root of the graph. Assign verb as new root of the graph, remove the oother
        } else {
          isTreeRoot = true;

          for (IndexedConstituent c : newConstituents) {
            c.getSemanticGraph().setRoot(newRoot);
            c.setRoot(newRoot);
          }

        }

        // Checks if the children also belongs to the conjoint and if they do, it assignes
        // them
        for (Constituent c : newConstituents) {
          SemanticGraph newSemanticGraph = ((IndexedConstituent) c).getSemanticGraph();
          List<SemanticGraphEdge> outed = newSemanticGraph.getOutEdgesSorted(root);
          for (SemanticGraphEdge ed : outed) {
            IndexedWord child = ed.getDependent();
            if (DpUtils.isPredet(ed) && ed.getDependent().lemma().equals("both")) { //if it is one level down
              for (Constituent cc : constituents) {
                if (((IndexedConstituent) cc).getSemanticGraph().containsEdge(ed)) ((IndexedConstituent) cc).getSemanticGraph().removeEdge(ed);
              }
            } else if (!DpUtils.isAnyConj(ed) && !DpUtils.isCc(ed) && !DpUtils.isPreconj(ed)
                //  && isDescendant(depTree, newRoot.index(), root.index(), child.index())) {
                && isDescendant(depTree, newSemanticGraph, newRoot, root, child)) {
              GrammaticalRelation reln = newSemanticGraph.reln(root, child);
              double weight = newSemanticGraph.getEdge(root, child).getWeight();
              newSemanticGraph.addEdge(newRoot, child, reln, weight, false);

            }
          }
        }

        // disconect the root of the conjoint from the new graph
        for (Constituent c : newConstituents) {
          SemanticGraph newSemanticGraph = ((IndexedConstituent) c).getSemanticGraph();
          List<SemanticGraphEdge> inedges = newSemanticGraph.getIncomingEdgesSorted(root);
          for (SemanticGraphEdge redge : inedges)
            newSemanticGraph.removeEdge(redge);
        }

        for (Constituent c : constituents) {
          ((IndexedConstituent) c).getSemanticGraph().removeEdge(edge);
        }

        if (isTreeRoot) {
          for (IndexedConstituent c : newConstituents) {
            c.getSemanticGraph().removeVertex(root);
          }
        }

        constituents.addAll(newConstituents);

        if (firstLevel || completeProcess) {
          generateConstituents(((IndexedConstituent) newConstituents.get(0)).getSemanticGraph(), depTree, newConstituents.get(0), newRoot,
              constituents, false, processVerb, completeProcess);
        } else {
          generateConstituents(((IndexedConstituent) newConstituents.get(0)).getSemanticGraph(), depTree, constituent, newRoot, constituents, false,
              processVerb, completeProcess);
        }

        // deletes the edge containing the conjunction e.g. and, or, but, etc
      } else if ((DpUtils.isCc(edge) || DpUtils.isPreconj(edge)) && processCC && !edge.getDependent().lemma().equals("&")) {
        for (Constituent c : constituents) {
          ((IndexedConstituent) c).getSemanticGraph().removeEdge(edge);
        }
      } else if (!DpUtils.isPredet(edge) && !constituent.excludedVertexes.contains(edge.getDependent()))
        generateConstituents(semanticGraph, depTree, constituent, edge.getDependent(), constituents, false, processVerb, completeProcess);
    }

  }

  private static boolean shareAllAncestors(SemanticGraph semanticGraph1, SemanticGraph semanticGraph2, IndexedWord root) {

    Set<IndexedWord> d2 = semanticGraph2.descendants(root);
    if (d2 == null || d2.isEmpty()) {
      return false;
    }

    Set<IndexedWord> d1 = semanticGraph1.descendants(root);

    Set<IndexedWord> v1 = semanticGraph1
        .descendants(semanticGraph1.getFirstRoot()); //Assumes only one root, otherwhise one could delete the non used nodes and call vertexset
    Set<IndexedWord> v2 = semanticGraph2.descendants(semanticGraph2.getFirstRoot());

    int asize1 = v1.size() - d1.size();
    int asize2 = v2.size() - d2.size();

    if (asize1 != asize2) return false;

    for (IndexedWord v : v1) {
      if (d1.contains(v)) continue;
      if (!v2.contains(v)) return false;
    }
    return true;
  }

  private static boolean containsDescendant(Tree parse, SemanticGraph semanticGraphRoot, SemanticGraph semanticGraphNew, IndexedWord root,
      IndexedWord newRoot) {
    Set<IndexedWord> d1 = semanticGraphRoot.descendants(root);
    Set<IndexedWord> d2 = semanticGraphNew.descendants(root);

    for (IndexedWord w : d1) {
      //if(!d2.contains(w) && isDescendant(parse, newRoot.index(), root.index(), w.index()) )
      if (isDescendant(parse, semanticGraphRoot, newRoot, root, w)) return true;
    }

    return false;

  }

  /** Checks if a node depending on one conjoint also depends to the other */
  //"He buys and sells electronic products" "Is products depending on both sells and buys?"
  private static boolean isDescendant(Tree parse, int indexCheck, int indexPivot, int indexElement) {
    Tree pivot = parse.getLeaves().get(indexPivot - 1); // because tree parse indexing system
    // starts with 0
    Tree check = parse.getLeaves().get(indexCheck - 1);
    Tree element = parse.getLeaves().get(indexElement - 1);

    while ((!element.value().equals("ROOT"))) {// find a common parent between the head conjoint
      // and the constituent of the element
      if (element.pathNodeToNode(element, pivot) != null) // is this efficient enough?
        break;
      element = element.parent(parse);
    }

    List<Tree> path = element.pathNodeToNode(element, check); // find a path between the common
    // parent and the other conjoint

    if (path != null) return true;
    else return false;
  }

  /** Checks if a node depending on one conjoint also depends to the other */
  //"He buys and sells electronic products" "Is products depending on both sells and buys?"
  private static boolean isDescendant(Tree parse, SemanticGraph semanticGraph, IndexedWord checkIW, IndexedWord pivotIW, IndexedWord elementIW) {

    Tree pivot = DpUtils.getNode(pivotIW, parse, semanticGraph);
    Tree check = DpUtils.getNode(checkIW, parse, semanticGraph);
    Tree element = DpUtils.getNode(elementIW, parse, semanticGraph);

    while ((!element.value().equals("ROOT"))) {// find a common parent between the head conjoint
      // and the constituent of the element
      if (element.pathNodeToNode(element, pivot) != null) // is this efficient enough?
        break;
      element = element.parent(parse);
    }

    return element.dominates(check);
  }

  /** Retrieves the heads of the clauses according to the CCs processing options. The result contains
   * verbs conjoined and a complement if it is conjoined with a verb.*/
  public static List<IndexedWord> getIndexedWordsConj(SemanticGraph semanticGraph, Tree depTree, IndexedWord root, GrammaticalRelation rel,
      List<SemanticGraphEdge> toRemove, Options option, boolean firstLevel) {
    List<IndexedWord> ccs = new ArrayList<IndexedWord>(); // to store the conjoints
    if (firstLevel) ccs.add(root);
    List<SemanticGraphEdge> outedges = semanticGraph.outgoingEdgeList(root);
    for (SemanticGraphEdge edge : outedges) {
      if (edge.getRelation().equals(rel)) {
        List<SemanticGraphEdge> outed = semanticGraph.outgoingEdgeList(edge.getDependent());
        // first condition tests if verbs are involved in the conjoints. Conjunctions between complements are treated elsewhere.
        boolean ccVerbs = edge.getDependent().tag().charAt(0) == 'V' || edge.getGovernor().tag().charAt(0) == 'V';
        //This condition will check if there is a cop conjoined with a verb
        boolean ccCop = DpUtils.findFirstOfRelationOrDescendent(outed, EnglishGrammaticalRelations.COPULA) != null;
        // this condition checks if there are two main clauses conjoined by the CC
        boolean ccMainClauses = DpUtils.findFirstOfRelationOrDescendent(outed, EnglishGrammaticalRelations.SUBJECT) != null
            || DpUtils.findFirstOfRelationOrDescendent(outed, EnglishGrammaticalRelations.EXPLETIVE) != null;

        // This flag will check if the cc should be processed according to the flag and the
        // shared elements.
        boolean notProcess = !option.processCcAllVerbs && outed.isEmpty() && shareAll(outedges, depTree, semanticGraph, root, edge.getDependent());

        if ((ccVerbs || ccCop) && !ccMainClauses && !notProcess) {
          ccs.add(edge.getDependent());
        }

        // Disconnects the conjoints. Independent clauses are always disconnected.
        if (((ccVerbs || ccCop) && !notProcess) || ccMainClauses) {
          toRemove.add(edge);

          //To remove the coordination
          if (option.processCcAllVerbs || !notProcess) {
            List<SemanticGraphEdge> conjunct = DpUtils.getEdges(outedges, EnglishGrammaticalRelations.COORDINATION);
            for (SemanticGraphEdge e : conjunct) {
              if (e.getDependent().index() > edge.getDependent().index()) continue;
              if (nextToVerb(depTree, semanticGraph, root, edge.getDependent(), e.getDependent())) {
                toRemove.add(e);
                break;
              }
            }
          }
        }
        ccs.addAll(getIndexedWordsConj(semanticGraph, depTree, edge.getDependent(), rel, toRemove, option, false));
      }
    }
    if (ccs.size() > 1) rewriteGraph(semanticGraph, depTree, ccs);
    return ccs;
  }

  /** Rewrites the graph so that each conjoint is independent from each other.
   * They will be disconnected and each dependent correspondignly assigned */
  private static void rewriteGraph(SemanticGraph semanticGraph, Tree depTree, List<IndexedWord> ccs) {

    for (int i = 0; i < ccs.size(); i++) {
      for (int j = i + 1; j < ccs.size(); j++) {
        //Connect each node in ccs to its parent
        for (SemanticGraphEdge ed : semanticGraph.getIncomingEdgesSorted(ccs.get(i))) {
          if (semanticGraph.getParents(ccs.get(j)).contains(ed.getGovernor()) || ccs.contains(ed.getGovernor())) continue;
          semanticGraph.addEdge(ed.getGovernor(), ccs.get(j), ed.getRelation(), ed.getWeight(), false);
        }

        //Check if the dependents of the main conjoint are also dependent on each of the conjoints
        // and assign them in each case.
        for (SemanticGraphEdge ed : semanticGraph.getOutEdgesSorted(ccs.get(i))) {
          IndexedWord child = ed.getDependent();
          if (semanticGraph.getChildren(ccs.get(j)).contains(child)) continue;
          if (!DpUtils.isAnyConj(ed) && !DpUtils.isCc(ed)
              //&& isDescendant(depTree, ccs.get(j).index(), ccs.get(i).index(), child.index())) {
              && isDescendant(depTree, semanticGraph, ccs.get(j), ccs.get(i), child)) {
            semanticGraph.addEdge(ccs.get(j), child, ed.getRelation(), ed.getWeight(), false);
          }
        }

      }
    }
  }

  /** Checks if two nodes are conjoined by a given coordination */
  private static boolean nextToVerb(Tree depTree, SemanticGraph semanticGraph, IndexedWord firstVerb, IndexedWord secondVerb, IndexedWord conj) {
    Tree fverb = DpUtils.getNode(firstVerb, depTree, semanticGraph);
    Tree sverb = DpUtils.getNode(secondVerb, depTree, semanticGraph);
    Tree conjv = DpUtils.getNode(conj, depTree, semanticGraph);

    // This will lead us to the level in the tree we want to compare
    conjv = conjv.parent(depTree);

    List<Tree> siblings = conjv.siblings(depTree);
    List<Tree> children = conjv.parent(depTree).getChildrenAsList();
    if (children.size() == 0) return false;

    // This will give the node of the conjoint dominating the coordination
    while (!siblings.contains(fverb)) {
      fverb = fverb.parent(depTree);
      if (fverb.equals(depTree)) return false;
    }

    // same for the other conjoint
    while (!siblings.contains(sverb)) {
      sverb = sverb.parent(depTree);
      if (sverb.equals(depTree)) return false;
    }

    //indices in children
    int indexStart = children.indexOf(sverb);
    int indexEnd = children.indexOf(fverb);
    int indexCC = children.indexOf(conjv);

    //if it is not between them return false (it assumes that children is ordered)
    if (indexCC - indexStart == -1) return true;
    else if (indexCC < indexEnd || indexCC > indexStart) return false;

    //if there is other conjoin in the middle
    for (int i = indexStart - 1; i > indexCC; i--) {
      if (!children.get(i).value().matches("\\p{Punct}")) return false;
    }

    return true;
  }

  /** Checks if two conjoints verbs share all dependents */
  private static boolean shareAll(List<SemanticGraphEdge> outedges, Tree depTree, SemanticGraph semanticGraph, IndexedWord root, IndexedWord conj) {
    for (SemanticGraphEdge edge : outedges) {
      if (DpUtils.isAnySubj(edge) || edge.getDependent().equals(conj)) continue;
        // else if (!isDescendant(depTree, conj.index(), root.index(), edge.getDependent()
        //         .index()))
      else if (isDescendant(depTree, semanticGraph, conj, root, edge.getDependent())) return false;
    }

    return true;
  }

}
