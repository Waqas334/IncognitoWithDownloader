package com.androidbull.incognito.browser.adapter;

public interface Selectable<T>
{
    T getItemKey(int position);

    int getItemPosition(T key);
}
