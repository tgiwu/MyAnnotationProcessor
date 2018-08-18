package com.zhy.processor;

public interface AbstractInjector<T> {
    void inject(Finder finder, T target, Object source);
}
