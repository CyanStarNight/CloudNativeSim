package org.cloudbus.nativesim.util;

public enum Status {
    Idle, Busy, // for controllers' status
    Ready, Processing, Error, END, // for events' status
    Received, Wait, Denied, // for messages' status
}
