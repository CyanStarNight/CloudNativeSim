/*
 * Copyright ©2024. Jingfeng Wu.
 */

package core;

public enum Status {
    Idle, Busy, // for pes
    Processing, Waiting,Finished, // for cloudlets
    Ready, Created, Denied, Failed, Success // for entities
}
