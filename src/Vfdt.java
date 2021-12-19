/**
 * Copyright (c) DTAI - KU Leuven – All rights reserved. Proprietary, do not copy or distribute
 * without permission. Written by Pieter Robberechts, 2021
 */
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

import static java.lang.Math.log;
import static java.lang.Math.sqrt;

/** This class is a stub for VFDT. */
public class Vfdt extends IncrementalLearner<Integer> {

  private int[] nbFeatureValues;
  private double delta;
  private double tau;
  private double nmin;
  private int nbSplits;

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
    nbSplits = 1;

    nbExamplesProcessed = 0;
    int[] possibleFeatures = new int[nbFeatureValues.length];
    for (int i = 0; i < nbFeatureValues.length; i++) possibleFeatures[i] = i;
    root = new VfdtNode(nbFeatureValues, possibleFeatures);
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

    // Step 1: add example to right node
    VfdtNode node = root.sortExample(example.attributeValues);
    int[][][] nijk = node.getNijk();
    node.update(example.classValue);
    if(example.attributeValues.length > 0){
      for(int i = 0; i < example.attributeValues.length ; i++){
        nijk[i][example.attributeValues[i]][example.classValue] += 1;
      }
    }

    // Step 2: check if update is necessary
    // Step 2.1: check size requirement
    if (node.getPossibleSplitFeatures().length > 0) {
      int sizeNijk = node.getNijkSize();
      if(sizeNijk % nmin == 0 && sizeNijk > 0){
        // Step 2.2: check Hoeffding bound
        int[] possibleSplitFeatures = node.getPossibleSplitFeatures();
        double Ga = 0;
        int a = 0;
        double Gb = 0;
        for(int i:possibleSplitFeatures){
          double G = node.splitEval(i);
          if(Ga < G){
            Gb = Ga;
            Ga = G; a = i;
          }else if (Gb < G){
            Gb = G;
          }
        }
        double deltaG = Ga - Gb;
        double epsilon = sqrt(log(1/tau)/ (2*sizeNijk));
        if(deltaG < delta || deltaG > epsilon){
          // Create all possible features for child nodes.
          Boolean skipped = false;
          node.setSplitFeature(a);
          int[] possibleFeatures = new int[node.getPossibleSplitFeatures().length-1];
          if (possibleFeatures.length != 0) {
            for (int i = 0; i < node.getPossibleSplitFeatures().length; i++){
              if (node.getSplitFeature() != node.getPossibleSplitFeatures()[i]) {
                if(!skipped){
                  int test = node.getPossibleSplitFeatures()[i];
                  possibleFeatures[i] = test;
                }else{possibleFeatures[i-1] = node.getPossibleSplitFeatures()[i]; }
              }else{skipped = true;}
            }
          }
          // Create all child nodes.
          int n = nbFeatureValues[a];
          VfdtNode[] children = new VfdtNode[n];
          for (int i = 0; i < n; i++){
            children[i] = new VfdtNode(this.nbFeatureValues,possibleFeatures);
          }
          node.addChildren(a, children);
          nbSplits += children.length;
        }
      }
    }
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
    VfdtNode node = root.sortExample(example);
    double totalOne = node.getTotalOnes();
    double totalZero = node.getTotalZeros();
    double prediction = 0;
    if(totalOne != 0 && totalZero != 0){
      prediction = totalOne/(totalZero+totalOne);
    }
    return prediction*2-1;
  }


  private String[] helperList;

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
    helperList = new String[nbSplits];
    int backup = nbSplits;
    getNodeStrings(root);
    nbSplits = backup;
    File modelFile = new File(path);
    modelFile.createNewFile();

    StringBuilder model = new StringBuilder(backup);
    for (String s : helperList){
      model.append(System.lineSeparator()).append(s);
    }

    FileWriter writer = new FileWriter(path);
    writer.write(model.toString());
    writer.close();
  }

  private void getNodeStrings(VfdtNode node){
    StringBuilder nodeInfo;
    VfdtNode[] children = node.getChildren();
    if(children != null){
      nbSplits--;
      node.setID(nbSplits);
      for(VfdtNode n : node.getChildren()){
        getNodeStrings(n);
      }
      nodeInfo = new StringBuilder(node.getID() + " D f:" + node.getSplitFeature() + " ch:[");
      for(VfdtNode n : children){
        nodeInfo.append(n.getID()).append(",");
      }
      nodeInfo.append("]");
      helperList[node.getID()] = nodeInfo.toString();
    }else{
      nbSplits--;
      node.setID(nbSplits);
      nodeInfo = new StringBuilder(node.getID() + " L pf:[");
      for(int i : node.getPossibleSplitFeatures()){
        nodeInfo.append(i).append(",");
      }
      nodeInfo.append("] nijk:[");
      int[][][] nijk = node.getNijk();
      for(int i = 0; i < nijk.length; i++){
        for(int j = 0; j < nijk[i].length; j++){
          for(int k = 0; k < nijk[i][j].length; k++){
            if(nijk[i][j][k] != 0){
              nodeInfo.append(i).append(":").append(j).append(":").append(k).append(":").append(nijk[i][j][k]).append(",");
            }
          }
        }
      }nodeInfo.append("]");
      helperList[node.getID()] = nodeInfo.toString();
    }
  }
  private int max;
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
    String[] content = Files.readString(Paths.get(path), StandardCharsets.US_ASCII).split(System.lineSeparator());
    VfdtNode[] allNodes = new VfdtNode[content.length-1];

    for(int n = 0; n < allNodes.length; n++){
      String[] nodeInfo = content[n+1].split(" ");

      if(nodeInfo[1].equals("L")){
        // Get all possible features
        String[] pf = nodeInfo[2].replaceAll("pf:", "").replace("[","").replace("]","").split(",");
        int[] possibleFeature = new int[pf.length];
        for(int j = 0; j < pf.length; j++){if(!pf[j].equals("")){possibleFeature[j] = Integer.parseInt(pf[j]);}}

        // Get nijk
        String[] nijkString = nodeInfo[3].replaceAll("nijk:", "").replace("[","").replace("]","").split(",");
        int maxSizeFeature = Arrays.stream(nbFeatureValues).max().getAsInt();
        int[][][] nijk = new int[nbFeatureValues.length][maxSizeFeature][2];
        for(int si = 0; si < nijkString.length; si++){
          String[] s = nijkString[si].split(":");
          if(s.length > 1) {
            nijk[Integer.parseInt(s[0])][Integer.parseInt(s[1])][Integer.parseInt(s[2])] = Integer.parseInt(s[3]);
          }
        }

        // Put node in list
        allNodes[n] = new VfdtNode(nbFeatureValues, possibleFeature);
        allNodes[n].setID(Integer.parseInt(nodeInfo[0]));
        allNodes[n].setNijk(nijk);
      }else if(nodeInfo[1].equals("D")){
        // Set feature
        allNodes[n] = new VfdtNode(nbFeatureValues, null);
        int feature = Integer.parseInt(nodeInfo[2].replaceAll("f:",""));
        allNodes[n].setSplitFeature(feature);

        String[] c = nodeInfo[3].replaceAll("ch:", "").replace("[","").replace("]","").split(",");
        int[] childIDs = new int[c.length];
        for(int i = 0; i < c.length;i++){
          childIDs[i] = Integer.parseInt(c[i]);
        }
        allNodes[n].setChildIDs(childIDs);

      }
    }

    for(int n = 0; n < allNodes.length; n++){
      if(allNodes[n].getChildIDs() != null){
        VfdtNode[] childNodes = new VfdtNode[allNodes[n].getChildIDs().length];
        int index = 0;
        for(int id : allNodes[n].getChildIDs()){
          childNodes[index] = allNodes[id];
          index++;
        }
        allNodes[n].addChildren(allNodes[n].getSplitFeature(), childNodes);
      }
      allNodes[n].setID(n);
    }
    root = allNodes[allNodes.length-1];
    nbSplits = allNodes.length;
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
