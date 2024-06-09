/*
 * Copyright ©2024. Jingfeng Wu.
 */

package entity;

public class Resource {
    private int ram;
    private int share;
    private double mips;
    private double receiveBw;
    private double transmitBw;

    public Resource(int ram, int share, double mips, double receiveBw, double transmitBw) {
        this.ram = ram;
        this.share = share;
        this.mips = mips;
        this.receiveBw = receiveBw;
        this.transmitBw = transmitBw;
    }
}
