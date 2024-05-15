/*
 * Copyright ©2024. Jingfeng Wu.
 */

package extend;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NativePe {

    /** Denotes Pe is FREE for allocation. */
    public static final int FREE = 1;

    /** Denotes Pe is allocated and hence busy in processing Cloudlet. */
    public static final int BUSY = 2;

    /**
     * Denotes Pe is failed and hence it can't process any Cloudlet at this moment. This Pe is
     * failed because it belongs to a machine which is also failed.
     */
    public static final int FAILED = 3;

    /** The id. */
    private int id;

    /** The status of Pe: FREE, BUSY, FAILED: . */
    private int status;

    // CPU share for time division multiplexing
    public int availableShare;

    // the total mips fo this pe
    private double mips;


    public NativePe(int id, double mips) {

        setId(id);

        setMips(mips);

        clear();
    }
    
    public void clear(){
        
        setStatus(FREE);

        setAvailableShare(1024);
    }

    public void addAvailableShare(int share){
        setAvailableShare(getAvailableShare() + share);
    }

}
