/*
 * Copyright ©2024. Jingfeng Wu.
 */

package entity;

import lombok.*;

import java.util.*;

/**
 * @author JingFeng Wu
 * the memeber of containers will be specified in file inputs.
 */


@Getter
@Setter
public class Container extends Instance{

    public final String type = "Container";

    private double availableMips;


    public Container(int appId,String prefix) {
        super(appId,prefix);
        setPrefix(prefix);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Container container = (Container) o;
        return Objects.equals(getUid(), container.getUid());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUid());
    }

}