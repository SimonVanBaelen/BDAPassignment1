/**
 * Copyright (c) DTAI - KU Leuven – All rights reserved. Proprietary, do not copy or distribute
 * without permission. Written by Pieter Robberechts, 2021
 */
import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileReader;
import org.junit.*;

public class VfdtSanityChecks {

  private Vfdt learner;
  private Example<Integer> example1;
  private Example<Integer> example2;
  private Example<Integer> example3;
  private Example<Integer> example4;

  @Before
  public void before() {
    learner = new Vfdt(new int[] {3, 3, 2}, 0.05, 0.05, 1);
    example1 = new Example<Integer>(new Integer[] {1, 0, 1}, 1);
    example2 = new Example<Integer>(new Integer[] {2, 1, 0}, 0);
    example3 = new Example<Integer>(new Integer[] {0, 1, 1}, 1);
    example4 = new Example<Integer>(new Integer[] {2, 0, 0}, 0);
  }

  @Test
  public void readModelAndNbExamplesProcessed() throws Exception {
    learner.readModel("models/vfdtSanity0.model", 3);
    assertEquals(
        "The number of processed examples was not set correctly", 3, learner.nbExamplesProcessed);
  }

  @Test
  public void readModelAndMakePrediction() throws Exception {
    learner.readModel("models/vfdtSanity0.model", 0);
    assertEquals(
        "A vfdt model without children or counts, should always give 50% probability for an"
            + " example.",
        0.5, learner.makePrediction(example1.attributeValues), 0.0000001);
  }

  @Test
  public void readUpdateAndWrite() throws Exception {
    learner.readModel("models/vfdtSanity1.model", 0);
    learner.update(example1);
    learner.update(example2);
    String path0 = "output/vfdtSanityOut1.model";
    learner.writeModel(path0);
    Vfdt learner2 = new Vfdt(new int[] {3, 3, 2}, 0.05, 0.05, 1);
    learner2.readModel(path0, learner.nbExamplesProcessed);
    learner.update(example3);
    learner2.update(example3);
    learner.update(example4);
    learner2.update(example4);
    String path1 = "output/vfdtSanityOut2.model";
    String path2 = "output/vfdtSanityOut3.model";
    learner.writeModel(path1);
    learner2.writeModel(path2);

    BufferedReader reader1 = new BufferedReader(new FileReader(path1));
    BufferedReader reader2 = new BufferedReader(new FileReader(path2));
    boolean hasNext = true;
    while (hasNext) {
      String line1 = reader1.readLine();
      String line2 = reader2.readLine();
      //             System.out.println(line1);
      assertEquals("The two models should be equal", line1, line2);
      if (line1 == null || line2 == null) {
        hasNext = false;
      }
    }
  }

  @Test
  public void readAndVisualize() throws Exception {
    learner.readModel("models/vfdtSanity1.model", 0);
    String expectedVisualization =
        "2=0:\n"
            + "| 0=0:\n"
            + "| | Leaf\n"
            + "| 0=1:\n"
            + "| | Leaf\n"
            + "| 0=2:\n"
            + "| | Leaf\n"
            + "2=1:\n"
            + "| Leaf\n";
    String actualVisualization = learner.getVisualization();
    assertEquals(
        "The visualization of the read tree does not match the structure of the file",
        expectedVisualization,
        actualVisualization);
  }

  @Test
  public void vfdtNode() throws Exception {
    int[] featureValues = new int[] {3, 3, 2};
    VfdtNode root = new VfdtNode(new int[] {3, 3, 2}, new int[] {0, 1, 2});
    VfdtNode left = new VfdtNode(featureValues, new int[] {0, 1});
    left.addChildren(
        0,
        new VfdtNode[] {
          new VfdtNode(featureValues, new int[] {1}),
          new VfdtNode(featureValues, new int[] {1}),
          new VfdtNode(featureValues, new int[] {1})
        });
    VfdtNode right = new VfdtNode(featureValues, new int[] {0, 1});
    root.addChildren(2, new VfdtNode[] {left, right});

    String expectedVisualization =
        "2=0:\n"
            + "| 0=0:\n"
            + "| | Leaf\n"
            + "| 0=1:\n"
            + "| | Leaf\n"
            + "| 0=2:\n"
            + "| | Leaf\n"
            + "2=1:\n"
            + "| Leaf\n";
    String actualVisualization = root.getVisualization("");
    System.out.println(actualVisualization);
    assertEquals(
        "The tree did not get constructed correctly.", expectedVisualization, actualVisualization);
  }

  @Test
  public void informationGainNoExamples() throws Exception {
    int[][][] nijk = new int[1][][]; // 1 feature...
    nijk[0] = new int[2][]; // with two possible values...
    nijk[0][0] = new int[2]; // two possible class values...
    nijk[0][1] = new int[2]; // two possible class values...
    nijk[0][0][0] = 0; // zero count
    nijk[0][0][1] = 0; // zero count
    nijk[0][1][0] = 0; // zero count
    nijk[0][1][1] = 0; // zero count

    double ig = VfdtNode.informationGain(0, nijk);
    assertEquals(
        "When there are no examples, the information gain should be 0", 0.0, ig, 0.0000001);
  }

  @Test
  public void informationGainTwoExamples() throws Exception {
    int[][][] nijk = new int[1][][]; // 1 feature...
    nijk[0] = new int[2][]; // with two possible values...
    nijk[0][0] = new int[2]; // two possible class values...
    nijk[0][1] = new int[2]; // two possible class values...
    nijk[0][0][0] = 1; // one count
    nijk[0][0][1] = 0; // zero count
    nijk[0][1][0] = 0; // zero count
    nijk[0][1][1] = 1; // one count

    double ig = VfdtNode.informationGain(0, nijk);
    assertEquals(
        "from completely random to perfect division should be information gain 1.0",
        1.0,
        ig,
        0.0000001);
  }
}
