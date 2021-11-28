package com.cloudsidecar.kubefork.executor;

import com.google.protobuf.GeneratedMessageV3;
import info.schnatterer.mobynamesgenerator.MobyNamesGenerator;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.BatchV1Api;
import io.kubernetes.client.openapi.apis.CoreApi;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1JobBuilder;
import io.kubernetes.client.openapi.models.V1PodBuilder;
import io.kubernetes.client.util.Config;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Supplier;

public class Kube implements Executor {
  private Supplier<Long> currentTimeFunc;
  public Kube(Supplier<Long> currentTimeFunc) {
    this.currentTimeFunc = currentTimeFunc;
  }
  public Kube() {
    this(System::currentTimeMillis);
  }
  public void execute(String classpath, Map<String, String> input) throws ExecutorException {
    var name = classpath.replaceAll("\\.", "") + "" + currentTimeFunc.get();
    name= MobyNamesGenerator.getRandomName().replaceAll("_", "-");
    try {
      var client = Config.defaultClient();
      Configuration.setDefaultApiClient(client);

      CoreV1Api api = new CoreV1Api();
      var batchApi = new BatchV1Api();
      var command = new LinkedList<String>();
      command.add("java");
      input.forEach((key, val) -> {
        command.add("-D" + key + "=" + val + "");
      });
      command.add("-cp");
      command.add("/opt/app/dummy-1.0-SNAPSHOT-jar-with-dependencies.jar");
      command.add(classpath);
      var labels = new HashMap<>(Map.of("class", StringUtils.right(classpath, 63)));
      // labels.putAll(input);
      var job = new V1JobBuilder()
          .withNewMetadata()
          .withName(name)
          .withLabels(labels)
          .endMetadata()
          .withNewSpec()
          .withNewTemplate()
          .withNewMetadata()
          .withLabels(labels)
          .endMetadata()
          .editOrNewSpec()
          .addNewContainer()
          .withName("forked-proc")
          .withImage("docker.io/fork/dummy:latest")
          .withImagePullPolicy("Never")
          .withCommand(command)
          .endContainer()
          .withRestartPolicy("Never")
          .endSpec()
          .endTemplate()
          .endSpec()
          .build();
      var pod =
          new V1PodBuilder()
              .withNewMetadata()
              .withName(name)
              .endMetadata()
              .withNewSpec()
              .addNewContainer()
              .withName("www")
              .withImage("docker.io/fork/dummy:latest")
              .withImagePullPolicy("Never")
              .withCommand(command)
              .endContainer()
              .endSpec()
              .build();
      System.out.println(job);
      // api.createNamespacedPod("default", pod, null, null, null);
      batchApi.createNamespacedJob("default", job, null, null, null);
    } catch (IOException | ApiException e) {
      throw new ExecutorException(e);
    }
  }
}
