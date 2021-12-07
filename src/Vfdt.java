/**
 * Copyright (c) DTAI - KU Leuven – All rights reserved. Proprietary, do not copy or distribute
 * without permission. Written by Pieter Robberechts, 2021
 */
import java.io.*;
import java.util.*;
import java.util.List;

/** This class is a stub for VFDT. */
public class Vfdt extends IncrementalLearner<Integer> {

  private int[] nbFeatureValues;
  private double delta;
  private double tau;
  private double nmin;

  private VfdtNode root;

  /**
   * Vfdt constructor
   *
   * <p>THIS METHOD IS REQUIRED
   *
   * @param nbFeatureValues are nb of values of each feature. e.g. nbFeatureValues[3]=5 means that
   *     feature 3 can have values 0,1,2,3 and 4.
   * @param delta is the parameter used for the Hoeffding bound
   * @param tau is the parameter that is used to deal with ties
   * @param nmin is the parameter that is used to limit the G computations
   */
  public Vfdt(int[] nbFeatureValues, double delta, double tau, int nmin) {
    this.nbFeatureValues = nbFeatureValues;
    this.delta = delta;
    this.tau = tau;
    this.nmin = nmin;

    nbExamplesProcessed = 0;
    int[] possibleFeatures = new int[nbFeatureValues.length];
    for (int i = 0; i < nbFeatureValues.length; i++) possibleFeatures[i] = i;
    root = new VfdtNode(nbFeatureValues, possibleFeatures);

    /*
      FILL IN HERE
    */
  }

  /**
   * This method will update the parameters of you model using the given example.
   *
   * <p>THIS METHOD IS REQUIRED
   *
   * @param example is a training example
   */
  @Override
  public void update(Example<Integer> example) {
    super.update(example);

    /*
      FILL IN HERE
    */


  }


  /**
   * Uses the current model to calculate the probability that an attributeValues belongs to class
   * "1";
   *
   * <p>THIS METHOD IS REQUIRED
   *
   * @param example is a the test instance to classify
   * @return the probability that attributeValues belongs to class "1"
   */
  @Override
  public double makePrediction(Integer[] example) {

    double prediction = 0;

    /*
      FILL IN HERE
    */

    return prediction;
  }

  /**
   * Writes the current model to a file.
   *
   * <p>The written file can be read in with readModel.
   *
   * <p>THIS METHOD IS REQUIRED
   *
   * @param path the path to the file
   * @throws IOException
   */
  @Override
  public void writeModel(String path) throws IOException {
    /*
      FILL IN HERE
    */
  }


  /**
   * Reads in the model in the file and sets it as the current model. Sets the number of examples
   * processed.
   *
   * <p>THIS METHOD IS REQUIRED
   *
   * @param path the path to the model file
   * @param nbExamplesProcessed the nb of examples that were processed to get to the model in the
   *     file.
   * @throws IOException
   */
  @Override
  public void readModel(String path, int nbExamplesProcessed) throws IOException {
    super.readModel(path, nbExamplesProcessed);

    /* FILL IN HERE */
  }


  /**
   * Return the visualization of the tree.
   *
   * <p>DO NOT CHANGE THIS METHOD.
   *
   * @return Visualization of the tree
   */
  public String getVisualization() {
    return root.getVisualization("");
  }


  /**
   * This runs your code to generate the required output for the assignment.
   *
   * <p>DO NOT CHANGE THIS METHOD.
   */
  public static void main(String[] args) {
    if (args.length < 7) {
      System.err.println(
          "Usage: java Vfdt <delta> <tau> <nmin> <data set> <nbFeatureValues> <output file>"
              + " <reportingPeriod> [-writeOutAllPredictions]");
      throw new Error("Expected 7 or 8 arguments, got " + args.length + ".");
    }
    try {
      // parse input
      double delta = Double.parseDouble(args[0]);
      double tau = Double.parseDouble(args[1]);
      int nmin = Integer.parseInt(args[2]);
      Data<Integer> data = new IntData(args[3], ",");
      int[] nbFeatureValues = parseNbFeatureValues(args[4]);
      String out = args[5];
      int reportingPeriod = Integer.parseInt(args[6]);
      boolean writeOutAllPredictions =
          args.length > 7 && args[7].contains("writeOutAllPredictions");

      // initialize learner
      Vfdt vfdt = new Vfdt(nbFeatureValues, delta, tau, nmin);

      // generate output for the learning curve
      vfdt.makeLearningCurve(data, 0.5, out + ".vfdt", reportingPeriod, writeOutAllPredictions);

    } catch (IOException e) {
      System.err.println(e.toString());
    }
  }

  /**
   * This method parses the file that specifies the nb of possible values for each feature.
   *
   * <p>DO NOT CHANGE THIS METHOD.
   */
  private static int[] parseNbFeatureValues(String path) throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader(path));
    reader.readLine(); // skip header
    String[] splitLine = reader.readLine().split(",");
    int[] nbFeatureValues = new int[splitLine.length];

    for (int i = 0; i < nbFeatureValues.length; i++) {
      nbFeatureValues[i] = Integer.parseInt(splitLine[i]);
    }
    reader.close();
    return nbFeatureValues;
  }
}
/**
 * This class implements Data for Integers
 *
 * <p>DO NOT CHANGE THIS CLASS
 */
class IntData extends Data<Integer> {

  public IntData(String dataDir, String sep) throws FileNotFoundException {
    super(dataDir, sep);
  }

  @Override
  protected Integer parseAttribute(String attrString) {
    return Integer.parseInt(attrString);
  }

  @Override
  protected Integer[] emptyAttributes(int i) {
    return new Integer[i];
  }

  public static void main(String[] args) {
    if (args.length < 3) {
      throw new Error("Expected 2 arguments, got " + args.length + ".");
    }

    try {
      Data<Integer> d = new IntData(args[0], args[1]);
      d.print();
    } catch (FileNotFoundException e) {
      System.err.print(e.toString());
    }
  }
}
