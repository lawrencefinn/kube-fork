package com.cloudsidecar.kubefork;
import com.cloudsidecar.kubefork.annotation.ForkableFunction;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1PodBuilder;
import io.kubernetes.client.util.Config;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException, ApiException, InterruptedException {
        ApiClient client = Config.defaultClient();
        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();
        var pod =
                new V1PodBuilder()
                        .withNewMetadata()
                        .withName("apod")
                        .endMetadata()
                        .withNewSpec()
                        .addNewContainer()
                        .withName("www")
                        .withImage("nginx")
                        .endContainer()
                        .endSpec()
                        .build();
        api.createNamespacedPod("default", pod, null, null, null);
        client.getHttpClient().dispatcher().executorService().shutdownNow();
        client.getHttpClient().connectionPool().evictAll();
        Thread.sleep(10000);
    }

    @ForkableFunction(destinationClassName = "Poopers")
    public String Bongo(String inty) {
        System.out.println("Bongo " + inty);
        return inty.toString();
    }
}
