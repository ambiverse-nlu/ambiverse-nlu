package de.mpg.mpi_inf.ambiversenlu.nlu.openie.clausie;

import de.mpg.mpi_inf.ambiversenlu.nlu.openie.clausie.Constituent.Flag;

import java.util.*;

/** Currently the default proposition generator generates 3-ary propositions out of a clause.
 *
 * @date $LastChangedDate: 2013-08-26 15:31:37 +0200 (Mon, 26 Aug 2013) $
 * @version $LastChangedRevision: 977 $ */
public class DefaultPropositionGenerator extends PropositionGenerator {

  boolean onlyKeepLongest = false;

  public DefaultPropositionGenerator(ClausIE clausIE) {
    super(clausIE);
  }

  @Override public void generate(List<Proposition> result, Clause clause, List<Boolean> include) {
    Proposition proposition = new Proposition();
    List<Proposition> propositions = new ArrayList<Proposition>();
    if(clause.processed) {
      Collections.fill(include, Boolean.TRUE);
    }

    int position = 0;
    // process subject
    if (clause.getSubject() > -1 && include.get(clause.getSubject())) { // subject is -1 when there is an xcomp
      generate(clause, clause.getSubject(), position++, proposition);
    } else {
      //throw new IllegalArgumentException();
    }

    // process verb
    if (include.get(clause.getVerb())) {
      generate(clause, clause.getVerb(), position++, proposition);
    } else {
      throw new IllegalArgumentException();
    }

    propositions.add(proposition);

    // process arguments
    SortedSet<Integer> sortedIndexes = new TreeSet<Integer>();
    sortedIndexes.addAll(clause.getIobjects());
    sortedIndexes.addAll(clause.getDobjects());
    sortedIndexes.addAll(clause.getXcomps());
    sortedIndexes.addAll(clause.getCcomps());
    sortedIndexes.addAll(clause.getAcomps());
    sortedIndexes.addAll(clause.getAdverbials());
    sortedIndexes.removeAll(inactiveConstituents);
    if (clause.getComplement() >= 0) sortedIndexes.add(clause.getComplement());
    for (Integer index : sortedIndexes) {
      if (clause.getConstituents().get(clause.getVerb()) instanceof IndexedConstituent && clause.getAdverbials().contains(index)
          && ((IndexedConstituent) clause.getConstituents().get(index)).getRoot().index() < ((IndexedConstituent) clause.getConstituents()
          .get(clause.getVerb())).getRoot().index()) continue;
      for (Proposition p : propositions) {
        if (include.get(index)) {
          generate(clause, index, position++, proposition);
        }
      }
    }

    // process adverbials  before verb
    sortedIndexes.clear();
    sortedIndexes.addAll(clause.getAdverbials());
    sortedIndexes.removeAll(inactiveConstituents);
    for (Integer index : sortedIndexes) {
      if (clause.getConstituents().get(clause.getVerb()) instanceof TextConstituent
          || ((IndexedConstituent) clause.getConstituents().get(index)).getRoot().index() > ((IndexedConstituent) clause.getConstituents()
          .get(clause.getVerb())).getRoot().index()) break;
      if (include.get(index)) {
        for (Proposition p : propositions) {
          generate(clause, index, position++, proposition);
          if (clause.getFlag(index, clausIE.options).equals(Flag.OPTIONAL)) {
            p.optional.add(p.constituents.size());
          }
        }
      }
    }

    // make 3-ary if needed
    if (!clausIE.options.nary) {
      for (Proposition p : propositions) {
        p.toString();
        p.optional.clear();
        if (p.constituents.size() > 3) {
          StringBuilder arg = new StringBuilder();
          for (int i = 2; i < p.constituents.size(); i++) {
            if (i > 2) {
              arg.append(" ");
              p.addTokens(2, p.getTokensForConstituent(i));
            }
              arg.append(p.constituents.get(i));
          }
          p.constituents.put(2, arg.toString());
          for (int i = p.constituents.size() - 1; i > 2; i--) {
            p.constituents.remove(i);
            p.removeTokensForConstituent(i);
          }
        }
      }
    }

    // we are done
    result.addAll(propositions);
  }
}
