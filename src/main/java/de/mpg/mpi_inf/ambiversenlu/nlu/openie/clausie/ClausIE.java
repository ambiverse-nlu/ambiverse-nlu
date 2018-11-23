package de.mpg.mpi_inf.ambiversenlu.nlu.openie.clausie;

import de.mpg.mpi_inf.ambiversenlu.nlu.openie.clausie.Constituent.Flag;
import de.mpg.mpi_inf.ambiversenlu.nlu.openie.clausie.JavaUtils.MapUtil;
import edu.stanford.nlp.io.EncodingPrintWriter.out;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.parser.lexparser.LexicalizedParserQuery;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.ScoredObject;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;

public class ClausIE {

  Tree depTree;

  SemanticGraph semanticGraph;

  List<ScoredObject<Tree>> trees;

  Set<Clause> clauses = new HashSet<>();

  boolean kparse = false;

  int k = 10;

  double bestScore;

  Tree bestDT;

  SemanticGraph bestSemanticGraph;

  Set<Clause> bestClauses;

  List<Proposition> propositions = new ArrayList<Proposition>();

  Map<Proposition, Double> scoredPropositions = new HashMap<Proposition, Double>();

  Options options;

  PropositionGenerator propositionGenerator;
  {
    if(options.reverbRelationStyle) {
      propositionGenerator = new ReverbPropositionGeneration(this);
    } else {
      propositionGenerator = new DefaultPropositionGenerator(this);
    }
  }

  private LexicalizedParser lp;

  private TokenizerFactory<CoreLabel> tokenizerFactory;

  private LexicalizedParserQuery lpq;

  // Indicates if the clause processed comes from an xcomp constituent of the
  // original sentence
  boolean xcomp = false;

  // -- construction
  // ----------------------------------------------------------------------------

  public ClausIE(Options options) {
    this.options = options;
  }

  public ClausIE() throws IOException, URISyntaxException {
    this(new Options());
  }

  public void setPropositionGenerator(PropositionGenerator propositionGenerator) {
    this.propositionGenerator = propositionGenerator;
  }

  public double getConfidence() {
    return bestScore;
  }

  public ClausIE(LexicalizedParser lp, TokenizerFactory<CoreLabel> tokenizerFactory, LexicalizedParserQuery lpq)
      throws IOException, URISyntaxException {
    this(new Options());
    this.lp = lp;
    this.tokenizerFactory = tokenizerFactory;
    this.lpq = lpq;
  }

  public ClausIE(SemanticGraph semanticGraph, Tree tree) throws IOException, URISyntaxException {
    this(semanticGraph, tree, new Options());
  }

  public ClausIE(SemanticGraph semanticGraph, Tree tree, Options options) throws IOException, URISyntaxException {
    this(options);
    this.semanticGraph = semanticGraph;
    this.depTree = tree;
  }

  public ClausIE(SemanticGraph semanticGraph) throws IOException, URISyntaxException {
    this(new Options());
    this.semanticGraph = semanticGraph;
  }

  // -- misc method
  // -----------------------------------------------------------------------------

  public Options getOptions() {
    return options;
  }

  public void clear() {
    semanticGraph = null;
    depTree = null;
    clauses.clear();
    propositions.clear();
  }

  // -- parsing
  // ---------------------------------------------------------------------------------

  /** Initializes the Stanford parser. */
  public void initParser() {
    lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
    tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
    lpq = lp.lexicalizedParserQuery();
  }

  /** Clears and parses a new sentence. */
  public void parse(String sentence) {
    clear();
    baseParse(sentence);
    depTree = lpq.getBestParse();
    bestScore = lpq.getPCFGScore();
    // use uncollapsed dependencies to facilitate tree creation
    semanticGraph = SemanticGraphFactory.generateUncollapsedDependencies(depTree);
  }

  public void kparse(String sentence) {
    clear();
    baseParse(sentence);
    trees = lpq.getKBestPCFGParses(k);
  }

  private void baseParse(String sentence) {
    List<CoreLabel> tokenizedSentence = tokenizerFactory.getTokenizer(new StringReader(sentence)).tokenize();
    lpq.parse(tokenizedSentence); // what about the confidence?
  }

  /** Returns the constituent tree for the sentence. */
  public Tree getDepTree() {
    return depTree;
  }

  /** Returns the dependency tree for the sentence. */
  public SemanticGraph getSemanticGraph() {
    return semanticGraph;
  }

  // -- clause detection
  // ------------------------------------------------------------------------

  /** Detects clauses in the sentence. */
  public void detectClauses() {
    ClauseDetector.detectClauses(this);
  }

  /** Returns clauses in the sentence. */
  public Set<Clause> getClauses() {
    return clauses;
  }

  // -- proposition generation
  // ------------------------------------------------------------------

  /** Generates propositions from the clauses in the sentence. */
  public void generatePropositions() {
    propositions.clear();

    // holds alternative options for each constituents (obtained by
    // processing coordinated conjunctions and xcomps)
    final List<List<Constituent>> constituents = new ArrayList<List<Constituent>>();

    // which of the constituents are required?
    final List<Boolean> include = new ArrayList<Boolean>();

    // holds all valid combination of constituents for which a proposition
    // is to be generated
    final List<List<Boolean>> includeConstituents = new ArrayList<List<Boolean>>();

    // let's start
    for (Clause clause : clauses) {
      // process coordinating conjunctions
      constituents.clear();
      for (int i = 0; i < clause.getConstituents().size(); i++) {
        Constituent constituent = clause.getConstituents().get(i);
        List<Constituent> alternatives;
        if (!(xcomp && clause.getSubject() == i) && constituent instanceof IndexedConstituent
            // the processing of the xcomps is done in Default
            // proposition generator.
            // Otherwise we get duplicate propositions.
            && !clause.getXcomps().contains(i) && ((i == clause.getVerb() && options.processCcAllVerbs) || (i != clause.getVerb()
            && options.processCcNonVerbs))) {
          if(depTree != null) {
            alternatives = ProcessConjunctions.processCC(depTree, constituent, false, false);
          } else {
            alternatives = new ArrayList<>();
            alternatives.add(constituent);
          }
        } else if (!(xcomp && clause.getSubject() == i) && clause.getXcomps().contains(i)) {
          alternatives = new ArrayList<Constituent>();
          ClausIE xclausIE = new ClausIE(options);
          xclausIE.semanticGraph = clause.getSemanticGraph();
          xclausIE.depTree = depTree;
          xclausIE.xcomp = true;
          xclausIE.clauses = ((XcompConstituent) clause.getConstituents().get(i)).getClauses();
          xclausIE.generatePropositions();
          for (Proposition p : xclausIE.propositions) {
            StringBuilder sb = new StringBuilder();
            String sep = "";
            for (int j = 0; j < p.constituents.size(); j++) {
              if (j == 0)    // to avoid including the subjecct, We
                continue;  // could also generate the prop
              // without the subject
              sb.append(sep);
              sb.append(p.constituents.get(j));
              sep = " ";
            }
            alternatives.add(new TextConstituent(sb.toString(), constituent.type));
          }
        } else {
          alternatives = new ArrayList<Constituent>(1);
          alternatives.add(constituent);
        }
        constituents.add(alternatives);
      }

      // create a list of all combinations of constituents for which a
      // proposition should be generated
      includeConstituents.clear();
      include.clear();
      for (int i = 0; i < clause.getConstituents().size(); i++) {
        Flag flag = clause.getFlag(i, options);
        clause.setFlag(i, flag);
        include.add(!flag.equals(Flag.IGNORE));
      }
      if (options.nary || options.keepOnlyLongest) {
        // we always include all constituents for n-ary ouput
        // (optional parts marked later)
        includeConstituents.add(include);
      } else {
        // triple mode; determine which parts are required
        for (int i = 0; i < clause.getConstituents().size(); i++) {
          include.set(i, clause.getFlag(i).equals(Flag.REQUIRED));
        }

        // create combinations of required/optional constituents
        new Runnable() {

          int noOptional;

          @Override public void run() {
            noOptional = 0;
            for (Flag f : clause.getFlags().values()) {
              if (f.equals(Flag.OPTIONAL)) noOptional++;
            }
            run(0, 0, new ArrayList<Boolean>());
          }

          private void run(int pos, int selected, List<Boolean> prefix) {
            if (pos >= include.size()) {
              if (selected >= Math.min(options.minOptionalArgs, noOptional) && selected <= options.maxOptionalArgs) {
                includeConstituents.add(new ArrayList<Boolean>(prefix));
              }
              return;
            }
            prefix.add(true);
            if (include.get(pos)) {
              run(pos + 1, selected, prefix);
            } else {
              if (!clause.getFlag(pos).equals(Flag.IGNORE)) {
                run(pos + 1, selected + 1, prefix);
              }
              prefix.set(prefix.size() - 1, false);
              run(pos + 1, selected, prefix);
            }
            prefix.remove(prefix.size() - 1);
          }
        }.run();
      }

      // create a temporary clause for which to generate a proposition
      final Clause tempClause = clause.clone();

      // generate propositions
      new Runnable() {

        @Override public void run() {
          // select which constituents to include
          for (List<Boolean> include : includeConstituents) {
            // now select an alternative for each constituent
            selectConstituent(0, include);
          }
        }

        void selectConstituent(int i, List<Boolean> include) {
          if (i < constituents.size()) {
            if (include.get(i)) {
              List<Constituent> alternatives = constituents.get(i);
              for (int j = 0; j < alternatives.size(); j++) {
                tempClause.getConstituents().set(i, alternatives.get(j));
                selectConstituent(i + 1, include);
              }
            } else {
              selectConstituent(i + 1, include);
            }
          } else {
            // everything selected; generate
            propositionGenerator.generate(propositions, tempClause, include);
          }
        }
      }.run();
    }
  }

  public Collection<Proposition> getPropositions() {
    if (!kparse) {return propositions;}
    else return scoredPropositions.keySet();
  }

  // -- command-line interface
  // ------------------------------------------------------------------

  public static void main(String[] args) throws IOException, URISyntaxException {
    OptionParser optionParser = new OptionParser();
    optionParser.accepts("f", "input file (if absent, ClausIE reads from stdin)").withRequiredArg().describedAs("file").ofType(String.class);
    optionParser.accepts("l", "if set, sentence identifier is read from input file (with lines of form: <id>\\t<sentence>)");
    optionParser.accepts("o", "output file (if absent, ClausIE writes to stdout)").withRequiredArg().describedAs("file").ofType(String.class);
    optionParser.accepts("c", "configuration file").withRequiredArg().describedAs("file").ofType(String.class);
    optionParser.accepts("v", "verbose output");
    optionParser.accepts("h", "print help");
    optionParser.accepts("s", "print sentence");
    optionParser.accepts("p", "print best parse confidence");
    optionParser.accepts("k", "extract from k best parses (default 10)");
    OptionSet options;
    try {
      options = optionParser.parse(args);
    } catch (OptionException e) {
      System.err.println(e.getMessage());
      out.println("");
      optionParser.printHelpOn(System.out);
      return;
    }
    // help
    if (options.has("h")) {
      optionParser.printHelpOn(System.out);
    }

    // setup input and output
    InputStream in = System.in;
    OutputStream out = System.out;
    if (options.has("f")) {
      in = new FileInputStream((String) options.valueOf("f"));
    }
    if (options.has("o")) {
      out = new FileOutputStream((String) options.valueOf("o"));
    }

    // is there an options file

    // create a ClausIE instance and set options
    final ClausIE clausIE;
    if (options.has("c")) {
      clausIE = new ClausIE(new Options((String) options.valueOf("c")));
    } else {
      clausIE = new ClausIE();
    }
    clausIE.initParser();
    if (options.has("v")) {
      clausIE.getOptions().print(out, "# ");
    }

    if (options.has("k")) {
      if (options.valueOf("k") != null) clausIE.k = (Integer) options.valueOf("k");
      clausIE.kparse = true;
    }

    // run
    DataInput din = new DataInputStream(in);
    PrintStream dout = new PrintStream(out);
    int lineNo = 1;
    for (String line = din.readLine(); line != null; line = din.readLine(), lineNo++) {
      line = line.trim();
      if (line.isEmpty() || line.startsWith("#")) continue;
      int sentenceId = lineNo;
      if (options.has("l")) {
        int tabIndex = line.indexOf('\t');
        sentenceId = Integer.parseInt(line.substring(0, tabIndex));
        line = line.substring(tabIndex + 1).trim();
      }
      if (options.has("v")) {
        dout.print("# Line ");
        dout.print(lineNo);
        if (options.has("l")) {
          dout.print(" (id ");
          dout.print(sentenceId);
          dout.print(")");
        }
        dout.print(": ");
        dout.print(line);
        dout.println();
      }

      if (!clausIE.kparse) {
        clausIE.parse(line);
        clausIE.detectClauses();
        clausIE.generatePropositions();
      } else {
        clausIE.kparse(line);

        for (ScoredObject<Tree> tree : clausIE.trees) {
          clausIE.semanticGraph = SemanticGraphFactory.generateUncollapsedDependencies(tree.object());
          clausIE.depTree = tree.object();
          double score = Math.exp(tree.score());
          clausIE.detectClauses();
          clausIE.generatePropositions();
          //To store the best tree
          if (score > clausIE.bestScore) {
            clausIE.bestSemanticGraph = clausIE.semanticGraph;
            clausIE.bestDT = clausIE.depTree;
            clausIE.bestScore = score;
            clausIE.bestClauses = new HashSet<>(clausIE.clauses);
          }

          for (Proposition p : clausIE.propositions) {
            if (!clausIE.scoredPropositions.containsKey(p)) {
              clausIE.scoredPropositions.put(p, score);
            } else {
              clausIE.scoredPropositions.put(p, clausIE.scoredPropositions.get(p) + score);
            }

          }
          clausIE.clear();
        }
      }

      clausIE.scoredPropositions = MapUtil.sortByValue(clausIE.scoredPropositions);

      if (options.has("v")) {
        if (clausIE.kparse) {
          clausIE.semanticGraph = clausIE.bestSemanticGraph;
        }
        dout.print("# Best Semantic graph: ");
        dout.println(clausIE.getSemanticGraph().toFormattedString().replaceAll("\n", "\n#                ").trim());
      }

      if (options.has("v")) {
        if (clausIE.kparse) {
          clausIE.clauses = clausIE.bestClauses;
        }
        dout.print("#   Detected ");
        dout.print(clausIE.getClauses().size());
        dout.println(" clause(s).");
        for (Clause clause : clausIE.getClauses()) {
          dout.print("#   - ");
          dout.print(clause.toString(clausIE.options));
          dout.println();
        }
      }

      if (options.has("s")) {
        dout.print(line);
        dout.println();
      }

      for (Proposition p : clausIE.getPropositions()) {
        dout.print(sentenceId);
        for (String c : p.getConstituents().values()) {
          // TODO: correct escaping
          dout.print("\t\"");
          dout.print(c);
          dout.print("\"");
        }
        if (clausIE.kparse) {
          dout.print("\t");
          dout.print(clausIE.scoredPropositions.get(p));
        } else if (options.has("p")) {
          dout.print("\t");
          dout.print(clausIE.lpq.getPCFGScore());
        }
        dout.println();
      }
    }

    // shutdown
    if (options.has("f")) {
      in.close();
    }
    if (options.has("o")) {
      out.close();
    }
  }


}
