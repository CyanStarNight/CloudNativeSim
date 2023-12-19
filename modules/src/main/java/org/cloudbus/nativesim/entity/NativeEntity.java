/*
 * Copyright ©2023. Jingfeng Wu.
 */

package org.cloudbus.nativesim.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class NativeEntity {
    private String uid; // the global id
    private int id;
    private int userId;
    private String name;

    public void setUid() {
        uid = UUID.randomUUID().toString();
    }

    public NativeEntity(int userId) {
        setUid();
        setUserId(userId);
    }
    public NativeEntity(int userId,String name) {
        setUid();
        setUserId(userId);
        setName(name);
    }
}
