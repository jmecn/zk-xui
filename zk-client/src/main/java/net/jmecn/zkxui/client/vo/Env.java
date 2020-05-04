/**
 * Huatu.com Inc. Copyright (c) 2014-2020 All Rights Reserved.
 */
package net.jmecn.zkxui.client.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @title Env
 * @author yanmaoyuan
 * @date 2020年5月2日
 * @version 1.0
 */
@Getter
@Setter
@AllArgsConstructor
public class Env {

    private String name;

    private String zkServers;

    public String toString() {
        return name + "=" + zkServers;
    }
}