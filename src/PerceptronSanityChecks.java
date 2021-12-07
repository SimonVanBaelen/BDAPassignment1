/**
 * Copyright (c) DTAI - KU Leuven – All rights reserved. Proprietary, do not copy or distribute
 * without permission. Written by Pieter Robberechts, 2021
 */
import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileReader;
import org.junit.*;

public class PerceptronSanityChecks {

  private Perceptron learner;
  private Example<Double> example1;
  private Example<Double> example2;
  private Example<Double> example3;
  private Example<Double> example4;

  @Before
  public void before() {
    learner = new Perceptron(3, 0.001);
    example1 = new Example<Double>(new Double[] {0.5, 0.0, 1.0}, 1);
    example2 = new Example<Double>(new Double[] {1.0, 0.5, 0.0}, 0);
    example3 = new Example<Double>(new Double[] {0.0, 0.5, 1.0}, 1);
    example4 = new Example<Double>(new Double[] {1.0, 0.0, 0.0}, 0);
  }

  @Test
  public void readModelAndNbExamplesProcessed() throws Exception {
    learner.readModel("models/lrSanity0.model", 3);
    assertEquals(
        "The number of processed examples was not set correctly", 3, learner.nbExamplesProcessed);
  }

  @Test
  public void readModelAndMakePrediction() throws Exception {
    learner.readModel("models/lrSanity0.model", 0);
    assertEquals(
        "A perceptron model with all weights zero should always return the threshold.",
        0,
        learner.makePrediction(example1.attributeValues),
        0.0000001);
  }

  @Test
  public void readUpdateAndWrite() throws Exception {
    learner.readModel("models/lrSanity0.model", 0);
    learner.update(example1);
    learner.update(example2);
    String path0 = "output/lrSanityOut1.model";
    learner.writeModel(path0);
    Perceptron learner2 = new Perceptron(3, 0.001);
    learner2.readModel(path0, learner.nbExamplesProcessed);
    learner.update(example3);
    learner2.update(example3);
    learner.update(example4);
    learner2.update(example4);
    String path1 = "output/lrSanityOut2.model";
    String path2 = "output/lrSanityOut3.model";
    learner.writeModel(path1);
    learner2.writeModel(path2);

    BufferedReader reader1 = new BufferedReader(new FileReader(path1));
    BufferedReader reader2 = new BufferedReader(new FileReader(path2));
    String[] splitLine1 = reader1.readLine().split(" ");
    String[] splitLine2 = reader2.readLine().split(" ");
    for (int i = 0; i <= 3; i++) {
      assertEquals(
          "The two models should be equal",
          Double.parseDouble(splitLine1[i]),
          Double.parseDouble(splitLine2[i]),
          0.0000001);
    }
  }
}
