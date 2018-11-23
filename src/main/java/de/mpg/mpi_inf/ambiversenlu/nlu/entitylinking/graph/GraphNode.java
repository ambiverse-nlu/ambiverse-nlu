package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph;

import gnu.trove.iterator.TIntDoubleIterator;
import gnu.trove.map.hash.TIntDoubleHashMap;

public class GraphNode {

  private Integer id;

  private GraphNodeTypes type;

  private Object NodeData = null;

  private TIntDoubleHashMap successors;

  public GraphNode() {
    successors = new TIntDoubleHashMap();
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public GraphNodeTypes getType() {
    return type;
  }

  public void setType(GraphNodeTypes type) {
    this.type = type;
  }

  public Object getNodeData() {
    return NodeData;
  }

  public void setNodeData(Object nodeData) {
    NodeData = nodeData;
  }

  public TIntDoubleHashMap getSuccessors() {
    return successors;
  }

  public void setSuccessors(TIntDoubleHashMap successors) {
    this.successors = successors;
  }

	
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(type).append(":").append(id);
    sb.append("_D_").append(NodeData);
    sb.append("_S");
    for (TIntDoubleIterator itr = successors.iterator(); itr.hasNext(); ) {
      itr.advance();
      sb.append("_").append(itr.key()).append(":").append(itr.value());
    }
    return sb.toString();
  }
}
