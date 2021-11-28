package com.cloudsidecar.kubefork;
import com.cloudsidecar.kubefork.annotation.ForkableFunction;
import com.cloudsidecar.kubefork.executor.ExecutorException;
import com.cloudsidecar.kubefork.executor.Kube;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1PodBuilder;
import io.kubernetes.client.util.Config;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws ExecutorException {
        var json = "{\"name\": \"poop\"}";
        new Kube().execute("com.cloudsidecar.dummy.Something", Map.of("arg0", json));
    }

    @ForkableFunction(destinationClassName = "Poopers")
    public String Bongo(String inty) {
        System.out.println("Bongo " + inty);
        return inty.toString();
    }
}
