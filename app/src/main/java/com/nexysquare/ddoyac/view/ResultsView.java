package com.nexysquare.ddoyac.view;
import com.nexysquare.ddoyac.tflite.Classifier;

import java.util.List;


public interface ResultsView {
    public void setResults(final List<Classifier.Recognition> results);
}
