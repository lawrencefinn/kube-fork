package com.cloudsidecar.kubefork;

import com.cloudsidecar.kubefork.annotation.ForkableFunction;
import com.google.protobuf.GeneratedMessageV3;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.reflections.scanners.Scanners.MethodsAnnotated;

public class Scanner {
    private String packageName;
    private Boolean throwOnInvalidFunction;
    public Scanner(String packageName, Boolean throwOnInvalidFunction) {
        this.packageName = packageName;
        this.throwOnInvalidFunction = throwOnInvalidFunction;
    }

    public Scanner(String packageName) {
        this(packageName, true);
    }

    public Set<FunctionInfo> getFunctions() throws InvalidFunctionException {
        var results = new HashSet<FunctionInfo>();
        var reflections = new Reflections(
            new ConfigurationBuilder().setUrls(
                ClasspathHelper.forPackage(this.packageName)
            ).setScanners(
                MethodsAnnotated
            )
        );
        var methods = reflections.getMethodsAnnotatedWith(ForkableFunction.class);
        try {
            methods.forEach(method -> {
                ForkableFunction annotation = method.getAnnotation(ForkableFunction.class);
                boolean goodFunction = true;
                for (Parameter param : method.getParameters()) {
                    if (!GeneratedMessageV3.class.isAssignableFrom(param.getType())) {
                        if (this.throwOnInvalidFunction) {
                            throw new RuntimeException(
                                new InvalidFunctionException(
                                    "function " + method.getName() + " parameter " + param.getName() + " is of type " + param.getType() + " but need proto type"
                                )
                            );
                        } else {
                            goodFunction = false;
                        }
                    }
                }
                var returnType = method.getReturnType();
                if (!GeneratedMessageV3.class.isAssignableFrom(returnType) && !returnType.equals(Void.TYPE)){
                    if (this.throwOnInvalidFunction) {
                        throw new RuntimeException(
                            new InvalidFunctionException(
                                "function " + method.getName() + " return type is of " + returnType + " but need proto type or void"
                            )
                        );
                    } else {
                        goodFunction = false;
                    }
                }
                if (goodFunction) {
                    results.add(new FunctionInfo(annotation.destinationClassName(), method));
                }
            } );
        } catch (RuntimeException e) {
            if (e.getCause() != null && e.getCause() instanceof InvalidFunctionException) {
                throw (InvalidFunctionException)e.getCause();
            }
        }
        return results;
    }
    public void generate(String destinationPackage, @Nullable String desinationPathOpt) throws InvalidFunctionException {
        var p = new Properties();
        p.setProperty("resource.loader", "class");
        p.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init(p);
        var template = Velocity.getTemplate("templates/main.vm");
        var destinationPath = Optional.ofNullable(desinationPathOpt).orElse("src/main/java/");

        getFunctions().forEach(funcInfo -> {
            var func = funcInfo.getFunction();
            var context = new VelocityContext();
            // var writer = new StringWriter();
            var funcName = func.getName();
            var className = StringUtils.capitalize(funcInfo.getName());
            var paramsStr = Stream.of(func.getParameters()).map(Parameter::getName).collect(Collectors.joining(", "));
            var packageAsPath = destinationPackage.replaceAll("\\.", "/");
            context.put("params", func.getParameters());
            context.put("className", className);
            context.put("funcClassName", funcInfo.getFunction().getDeclaringClass().getName());
            context.put("funcName", funcName);
            context.put("paramsStr", paramsStr);
            context.put("package", destinationPackage);
            try(var writer = new FileWriter(new File(destinationPath + "/" + packageAsPath + "/" + className + ".java"))){
                template.merge(context, writer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Func " + func);

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
