/*
 * Copyright ©2024. Jingfeng Wu.
 */

package request;

import lombok.Getter;
import lombok.Setter;
import request.Request;

import java.util.List;

@Getter
@Setter
public class Distribution {
    private List<Request> totalRequests;
}
