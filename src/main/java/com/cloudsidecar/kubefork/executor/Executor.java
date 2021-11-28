package com.cloudsidecar.kubefork.executor;

import com.google.protobuf.GeneratedMessageV3;

import java.util.List;
import java.util.Map;

public interface Executor {
  public void execute(String classpath, Map<String, String> input) throws ExecutorException;
}
