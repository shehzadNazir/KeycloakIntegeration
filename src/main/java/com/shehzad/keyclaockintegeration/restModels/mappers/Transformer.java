package com.shehzad.keyclaockintegeration.restModels.mappers;

public interface Transformer<T,U> {
    public U transform(T t);
    public Transformer<T,U> addDependencies(Object... dependency);
}
