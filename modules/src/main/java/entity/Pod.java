/*
 * Copyright ©2024. Jingfeng Wu.
 */

package entity;

import java.util.List;

import lombok.*;

/**
 * @author JingFeng Wu
 */

@EqualsAndHashCode(callSuper = false)
@Getter
@Setter
public class Pod extends Instance{

    public final String type = "Pod";

    private List<Container> containerList;

    public Pod(int appId,String prefix) {
        super(appId,prefix);
    }


}
