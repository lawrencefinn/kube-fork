package com.cloudsidecar.kubefork;

import java.lang.reflect.Method;
import java.util.Objects;

public class FunctionInfo {
    private String name;
    private Method function;

    public FunctionInfo(String name, Method function) {
        this.name = name;
        this.function = function;
    }

    public String getName() {
        return name;
    }

    public Method getFunction() {
        return function;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FunctionInfo that = (FunctionInfo) o;
        return Objects.equals(name, that.name) && Objects.equals(function, that.function);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, function);
    }
}
