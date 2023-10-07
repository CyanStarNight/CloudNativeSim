package org.cloudbus.nativesim.event;

import lombok.Data;
import org.cloudbus.nativesim.entity.DataCenter;
import org.cloudbus.nativesim.entity.ServiceChain;
import org.cloudbus.nativesim.util.Edge;

import java.util.Calendar;
import java.util.List;

/**
 * @author JingFeng Wu
 */
@Data
public class NativeSim {
    private static final String NativeSim_VERSION = "0.1";
    private static Calendar calendar = null;
    private static int num_user = 1;

    ServiceChain serviceChain;
    List<DataCenter> dataCenters;

    public void init(int numUser, Calendar cal){
        calendar = cal;
        num_user = numUser;
    }
    public void register(String config_file, String commu_file, List<String> parameters){
        Register register = new Register(config_file,commu_file,parameters);
        serviceChain = register.ServiceChainRegistry();
        dataCenters = register.DataCenterRegistry();
    }

    public void register(String config_file, String commu_file, List<String> parameters,int[] costs){
        Register register = new Register(config_file,commu_file,parameters,costs);
        serviceChain = register.ServiceChainRegistry();
        dataCenters = register.DataCenterRegistry();
    }
    public void pause(){

    }

    public void stop(){

    }

    public void log(){}
}
