/**
 * Copyright (c) DTAI - KU Leuven – All rights reserved. Proprietary, do not copy or distribute
 * without permission. Written by Pieter Robberechts, 2021
 */
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

public abstract class IncrementalLearner<T> {

  protected int nbExamplesProcessed;

  /**
   * This method will update the parameters of you model using the given example.
   *
   * @param example is a training example
   */
  public void update(Example<T> example) {
    nbExamplesProcessed++;
  }

  /**
   * Uses the current model to calculate the probability that an attributeValues belongs to class
   * "1";
   *
   * @param example is a test attributeValues
   * @return the probability that attributeValues belongs to class "1"
   */
  abstract double makePrediction(T[] example);

  /**
   * Writes the current model to a file.
   *
   * <p>The written file can be read in with readModel.
   *
   * @param path the path to the file
   * @throws IOException
   */
  abstract void writeModel(String path) throws IOException;

  /**
   * Reads in the model in the file and sets it as the current model. Sets the number of examples
   * processed.
   *
   * @param path the path to the model file
   * @param nbExamplesProcessed the nb of examples that were processed to get to the model in the
   *     file.
   * @throws IOException
   */
  void readModel(String path, int nbExamplesProcessed) throws IOException {
    this.nbExamplesProcessed = nbExamplesProcessed;
  }

  /**
   * This method produces a file with the accuracy of the model after seeing different numbers of
   * examples. The file can be used to plot the learning curve.
   *
   * <p>If the file doesn't exist, a new file named "out + .acc" is created. The lines have the
   * form: "nbExamplesProcessed <tab> accuracy\n".
   *
   * <p>The reporting period decides how often the accuracy is reported to the file. Because
   * learning curves change more in the beginning, the actual used period will start at 10 and grow
   * exponentially until it reaches the given reporting period.
   *
   * <p>This method can optionally also write out all the predictions made by the learner. The
   * predictions are then written to a file named "out + .probs". The file format has the form of
   * Prob(attributeValues[ix] belongs to class "1") + tab + true label of attributeValues. This
   * option should only be used for debugging purposes because it will create too much output in a
   * real setting.
   *
   * <p>DO NOT CHANGE THIS METHOD
   *
   * @param data is the data
   * @param thresh is the threshold for labeling an attributeValues as belonging to class "1"
   * @param out the stem of the output file(s). The accuracy is written to out.acc, the predictions
   *     to out.probs
   * @param reportingPeriod How often the accuracy should be reported (once every period, where
   *     period is expressed in number of examples)
   * @param writeOutAllPredictions when this is true, all the predictions are written to file.
   */
  public void makeLearningCurve(
      Data<T> data, double thresh, String out, int reportingPeriod, boolean writeOutAllPredictions)
      throws FileNotFoundException {

    System.out.println(out);
    PrintWriter accuracyWriter = new PrintWriter(out + ".acc");
    PrintWriter predictionWriter = writeOutAllPredictions ? new PrintWriter(out + ".probs") : null;

    int nbToTest = 10;

    System.out.println("Start training/testing");

    Iterator<Example<T>> iterator = data.iterator();
    ArrayList<Example<T>> buffer = new ArrayList<Example<T>>(nbToTest);

    // initialize buffer
    int i = 0;
    while (iterator.hasNext() && i < nbToTest) {
      i++;
      buffer.add(iterator.next());
    }

    boolean hasNext = iterator.hasNext();

    while (hasNext) {
      nbToTest = Math.min(reportingPeriod, nbToTest * 2);
      ArrayList<Example<T>> testExamples = new ArrayList<Example<T>>(nbToTest);

      // calculate accuracy with test examples
      i = 0;
      double accuracy = 0;

      while (hasNext && i < nbToTest) {
        i++;
        Example<T> example = iterator.next();
        double prob = makePrediction(example.attributeValues);

        double prediction = (prob > thresh) ? 1 : 0;
        if (prediction - example.classValue == 0) accuracy += 1;

        // write prediction to file
        if (writeOutAllPredictions) {
          predictionWriter.println(prob + "\t" + example.classValue);
        }

        testExamples.add(example);
        hasNext = iterator.hasNext();
      }
      accuracyWriter.println(nbExamplesProcessed + "\t" + accuracy / (i - 1));
      accuracyWriter.flush();
      System.out.println(
          "trained with: "
              + nbExamplesProcessed
              + "\taccuracy: "
              + accuracy / (i - 1)
              + "\t"
              + getInfo());

      // update the model with the examples from the buffer
      for (Example<T> example : buffer) {
        update(example);
      }
      buffer = testExamples;
    }
    accuracyWriter.close();
  }

  /**
   * Info to print when testing. This is mainly for debug purpose
   *
   * <p>You can override this method in VFDT or LogisticRegression to print the information you
   * wish.
   */
  public String getInfo() {
    return "";
  }
}
