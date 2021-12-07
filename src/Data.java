/**
 * Copyright (c) DTAI - KU Leuven – All rights reserved. Proprietary, do not copy or distribute
 * without permission. Written by Pieter Robberechts, 2021
 */
import java.io.*;
import java.util.Arrays;
import java.util.Iterator;

/**
 * This class read data from disk.
 *
 * <p>The data can be stored on several csv files. This class reads all the csv files in a given
 * directory in alphabetical order.
 *
 * <p>The data files are of csv format with "," as delimiter. The last value is the class value. The
 * features will be parsed to type T, the class to an integer.
 */
public abstract class Data<T> implements Iterable<Example<T>> {

  public Data(String dataDir, String sep) throws FileNotFoundException {
    this.sep = sep;

    dataFiles =
        new File(dataDir)
            .listFiles(
                new FilenameFilter() {
                  @Override
                  public boolean accept(File file, String s) {
                    return s.endsWith(".csv");
                  }
                });

    Arrays.sort(dataFiles);
  }

  /**
   * Get the number of features, based on the first line of data
   *
   * @return number of features
   */
  public int getNbFeatures() {
    Iterator<Example<T>> it = this.iterator();
    if (it.hasNext()) {
      return it.next().attributeValues.length;
    } else return 0;
  }

  private String sep;
  private File[] dataFiles;

  /**
   * Returns an iterator over all examples
   *
   * @return iterator over all examples
   */
  @Override
  public Iterator<Example<T>> iterator() {
    return new Iterator<Example<T>>() {

      private int currentFileIndex = -1;
      private BufferedReader currentFile = null;

      private String nextLine;

      /** @return true if there are still examples to process */
      @Override
      public boolean hasNext() {
        boolean hasNext = false;

        while (!hasNext) {
          // open new file if needed
          while (currentFile == null && currentFileIndex + 1 < dataFiles.length) {
            currentFileIndex++;
            try {
              currentFile = new BufferedReader(new FileReader(dataFiles[currentFileIndex]));
            } catch (IOException e) {
              System.err.println(e.toString());
            }
          }
          if (currentFile == null) return false; // there are no datafiles left

          // read next line of current file
          try {
            nextLine = currentFile.readLine();
            hasNext = nextLine != null;
            if (!hasNext) {
              currentFile.close();
              currentFile = null;
            }
          } catch (IOException e) {
            hasNext = false;
            currentFile = null;
          }
        }
        return true;
      }

      /**
       * This method returns the next training attributeValues. If there are no examples left,
       * "null" is returned.
       *
       * @return the next num training examples.
       */
      @Override
      public Example<T> next() {
        return parseLine(nextLine);
      }

      @Override
      public void remove() {
        // we do not remove examples
      }

      /** Closes the opened data file (if any). */
      public void close() {
        if (currentFile != null) {
          try {
            currentFile.close();
            currentFile = null;
          } catch (IOException e) {

          }
        }
      }
    };
  }

  /**
   * Parses a line from a csv file to an example
   *
   * @param line the line to parse
   * @return the example
   */
  private Example<T> parseLine(String line) {
    String[] splitLine = line.split(sep);
    T[] attributes = emptyAttributes(splitLine.length - 1);
    for (int i = 0; i < attributes.length; i++) {
      attributes[i] = parseAttribute(splitLine[i]);
    }
    return new Example<T>(attributes, Integer.parseInt(splitLine[splitLine.length - 1]));
  }

  protected abstract T parseAttribute(String attrString);

  protected abstract T[] emptyAttributes(int i);

  /** @return the string representation of any 2D integer vector */
  public String dataToString(Example<T> data) {
    String output = "";
    for (T v : data.attributeValues) output += v.toString() + ", ";
    output += " : " + data.classValue.toString();
    return output;
  }

  public void print() {
    for (Example<T> example : this) {
      System.out.println(dataToString(example));
    }
  }
}
