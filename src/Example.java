/**
 * Copyright (c) DTAI - KU Leuven – All rights reserved. Proprietary, do not copy or distribute
 * without permission. Written by Pieter Robberechts, 2021
 */
public class Example<T> {

  public final T[] attributeValues;
  public final Integer classValue;

  public Example(T[] attributeValues, Integer classValue) {
    this.attributeValues = attributeValues;
    this.classValue = classValue;
  }
}
