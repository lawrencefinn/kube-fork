package com.cloudsidecar.kubefork;

import java.util.Objects;

public class ParameterInfo {
    private String name;
    private String className;

    public ParameterInfo(String name, String className) {
        this.name = name;
        this.className = className;
    }

    public String getName() {
        return name;
    }

    public String getClassName() {
        return className;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParameterInfo that = (ParameterInfo) o;
        return name.equals(that.name) && className.equals(that.className);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, className);
    }
}
