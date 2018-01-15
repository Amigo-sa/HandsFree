package by.citech.handsfree.logic;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public enum ECallerState {

    PhaseZero {public HashSet<ECallerState> availableStates() {
            return new HashSet<> (Arrays.asList(Failure, DebugLoop, DebugRecord, PhaseReadyExt, PhaseReadyInt));
        }},
    PhaseReadyExt {public HashSet<ECallerState> availableStates() {
            return new HashSet<> (Arrays.asList(Failure, PhaseZero, PhaseReadyInt, ReadyToWork));
        }},
    PhaseReadyInt {public HashSet<ECallerState> availableStates() {
            return new HashSet<> (Arrays.asList(Failure, PhaseZero, PhaseReadyExt, ReadyToWork));
        }},
    ReadyToWork {public HashSet<ECallerState> availableStates() {
            return new HashSet<> (Arrays.asList(Failure, PhaseReadyExt, PhaseReadyInt, OutStarted, InDetected));
        }},
    OutStarted {public HashSet<ECallerState> availableStates() {
            return new HashSet<> (Arrays.asList(Failure, Error, ReadyToWork, PhaseReadyExt, PhaseReadyInt, OutConnected));
        }},
    OutConnected {public HashSet<ECallerState> availableStates() {
            return new HashSet<> (Arrays.asList(Failure, Error, ReadyToWork, PhaseReadyExt, PhaseReadyInt, Call));
        }},
    InDetected {public HashSet<ECallerState> availableStates() {
            return new HashSet<> (Arrays.asList(Failure, Error, ReadyToWork, PhaseReadyExt, PhaseReadyInt, Call));
        }},
    Call {public HashSet<ECallerState> availableStates() {
            return new HashSet<> (Arrays.asList(Failure, Error, ReadyToWork, PhaseReadyExt, PhaseReadyInt));
        }},
    Error {public HashSet<ECallerState> availableStates() {
            return new HashSet<>(Collections.singletonList(ReadyToWork));
        }},
    Failure {public HashSet<ECallerState> availableStates() {
            return new HashSet<>(Collections.singletonList(PhaseZero));
        }},

    //--------------------- debug

    DebugRecord {public HashSet<ECallerState> availableStates() {
            return new HashSet<> (Collections.singletonList(DebugRecorded));
        }},
    DebugRecorded {public HashSet<ECallerState> availableStates() {
            return new HashSet<> (Collections.singletonList(DebugPlay));
        }},
    DebugPlay {public HashSet<ECallerState> availableStates() {
            return new HashSet<> (Collections.singletonList(DebugRecorded));
        }},
    DebugLoop {public HashSet<ECallerState> availableStates() {
            return new HashSet<> (Collections.singletonList(PhaseZero));
        }};

    public abstract HashSet<ECallerState> availableStates();

}
