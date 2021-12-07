/**
 * Copyright (c) DTAI - KU Leuven – All rights reserved. Proprietary, do not copy or distribute
 * without permission. Written by Pieter Robberechts, 2021
 */

/** This class is a stub for VFDT. */
public class VfdtNode {

  private VfdtNode[] children; /* child children (null if node is a leaf) */

  private final int[] possibleSplitFeatures; /* The features that this node can split on */

  private int splitFeature; /* splitting feature */

  private int[][][] nijk; /* instance counts (see paper) */

  /* FILL IN HERE */

  /**
   * Create and initialize a leaf node.
   *
   * <p>THIS METHOD IS REQUIRED.
   *
   * @param nbFeatureValues are the nb of values for each feature in this node. If a feature has k
   *     values, then the values are [0:k-1].
   */
  public VfdtNode(int[] nbFeatureValues, int[] possibleSplitFeatures) {
    this.possibleSplitFeatures = possibleSplitFeatures;
    children = null;

    /* FILL IN HERE */
  }


  /**
   * Turn a leaf node into a internal node.
   *
   * <p>THIS METHOD IS REQUIRED.
   *
   * @param splitFeature is the feature to test on this node.
   * @param nodes are the children (the index of the node is the value of the splitFeature).
   */
  public void addChildren(int splitFeature, VfdtNode[] nodes) {
    if (nodes == null) throw new IllegalArgumentException("null children");
    nbSplits++;

    /* FILL IN HERE */

  }

  /**
   * Returns the leaf node corresponding to the test attributeValues.
   *
   * <p>THIS METHOD IS REQUIRED.
   *
   * @param example is the test attributeValues to sort.
   */
  public VfdtNode sortExample(Integer[] example) {

    VfdtNode leaf; // change this

    /* FILL IN HERE */

    return leaf;
  }

  /**
   * Split evaluation method (function G in the paper)
   *
   * <p>Compute a splitting score for the feature featureId. For now, we'll use information gain,
   * but this may be changed. You can test your code with other split evaluations, but be sure to
   * change it back to information gain in the submitted code and for the experiments with default
   * values.
   *
   * @param featureId is the feature to be considered.
   */
  public double splitEval(int featureId) {
    return informationGain(featureId, nijk);
  }

  /**
   * Compute the information gain of a feature for this leaf node.
   *
   * <p>THIS METHOD IS REQUIRED.
   *
   * @param featureId is the feature to be considered.
   * @param nijk are the instance counts.
   */
  public static double informationGain(int featureId, int[][][] nijk) {
    double ig = 0;

    /* FILL IN HERE */

    return ig;
  }


  /**
   * Return the visualization of the tree.
   *
   * <p>DO NOT CHANGE THIS METHOD.
   *
   * @return Visualization of the tree
   */
  public String getVisualization(String indent) {
    if (children == null) {
      return indent + "Leaf\n";
    } else {
      String visualization = "";
      for (int v = 0; v < children.length; v++) {
        visualization += indent + splitFeature + "=" + v + ":\n";
        visualization += children[v].getVisualization(indent + "| ");
      }
      return visualization;
    }
  }


}
