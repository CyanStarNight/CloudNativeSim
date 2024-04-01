/*
 * Copyright ©2024. Jingfeng Wu.
 */

package org.cloudbus.nativesim.core;

public enum Status {
    Idle, Busy, // for pes
    Ready, Created, Denied, Processing, Failed, Success // for entities
}
