package com.cloudsidecar.kubefork;

import com.cloudsidecar.kubefork.annotation.ForkableFunction;
import com.google.protobuf.GeneratedMessageV3;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.io.StringWriter;
import java.lang.reflect.Parameter;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.reflections.scanners.Scanners.MethodsAnnotated;

public class Scanner {
    private String packageName;
    public Scanner(String packageName) {
        this.packageName = packageName;
    }
    public Set<FunctionInfo> getFunctions() {
        var results = new HashSet<FunctionInfo>();
        var reflections = new Reflections(
                new ConfigurationBuilder().setUrls(
                        ClasspathHelper.forPackage(this.packageName)
                ).setScanners(
                        MethodsAnnotated
                )
        );
        var methods = reflections.getMethodsAnnotatedWith(ForkableFunction.class);
        methods.forEach(method -> {
            ForkableFunction annotation = method.getAnnotation(ForkableFunction.class);
            results.add(new FunctionInfo(annotation.destinationClassName(), method));
        } );
        return results;
    }
    public void generate(String destinationPackage) {
        var p = new Properties();
        p.setProperty("resource.loader", "class");
        p.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init(p);
        var template = Velocity.getTemplate("templates/main.vm");

        getFunctions().forEach(funcInfo -> {
            var func = funcInfo.getFunction();
            var context = new VelocityContext();
            var writer = new StringWriter();
            var funcName = func.getName();
            var className = StringUtils.capitalize(funcInfo.getName());
            var paramsStr = Stream.of(func.getParameters()).map(Parameter::getName).collect(Collectors.joining(", "));
            context.put("params", func.getParameters());
            context.put("className", className);
            context.put("funcClassName", funcInfo.getFunction().getDeclaringClass().getName());
            context.put("funcName", funcName);
            context.put("paramsStr", paramsStr);
            context.put("package", destinationPackage);
            template.merge(context, writer);
            System.out.println("CODE " + writer.toString());
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
