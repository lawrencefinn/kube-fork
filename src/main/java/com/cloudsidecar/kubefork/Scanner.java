package com.cloudsidecar.kubefork;

import com.google.protobuf.GeneratedMessageV3;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import static org.reflections.scanners.Scanners.MethodsAnnotated;

public class Scanner {
    private String packageName;
    public Scanner(String packageName) {
        this.packageName = packageName;
    }
    public Set<Method> getFunctions() {
        var reflections = new Reflections(
                new ConfigurationBuilder().setUrls(
                        ClasspathHelper.forPackage(this.packageName)
                ).setScanners(
                        MethodsAnnotated
                )
        );
        return reflections.getMethodsAnnotatedWith(com.cloudsidecar.kubefork.annotation.ForkableFunction.class);
    }
    public void generate() {
        getFunctions().forEach(func -> {
            System.out.println("Func " + func);
            List.of(func.getParameters()).forEach(param ->
                    System.out.println("Param " + param.getName() + "-" + param.getType()));
            List.of(func.getParameters()).forEach(param -> {
                if (GeneratedMessageV3.class.isAssignableFrom(param.getType())){
                    System.out.println("Is proto " + param.getType());
                } else {
                    System.out.println("Not prot " + param.getType());
                }
            });
        });
    }
}
