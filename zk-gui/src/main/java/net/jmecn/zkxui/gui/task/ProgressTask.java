/**
 * Huatu.com Inc. Copyright (c) 2014-2020 All Rights Reserved.
 */
package net.jmecn.zkxui.gui.task;

import java.util.concurrent.Callable;

/**
 * @title ProgressTask
 * @author yanmaoyuan
 * @date 2020年5月6日
 * @version 1.0
 */
public interface ProgressTask<T> extends Callable<T> {

    default boolean isIndeterminate() {
        return true;
    }

    default int value() {
        return 0;
    };

    default int maximun() {
        return 0;
    };
}