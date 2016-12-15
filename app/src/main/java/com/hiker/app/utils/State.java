package com.hiker.app.utils;

public class State {
    private static volatile boolean serviceTrackerState = false;
    private static volatile long currentSession = -1;

    public static boolean isServiceTrackerRuning() {
        return serviceTrackerState;
    }

    public static void setServiceTrackerState(boolean b) {
        serviceTrackerState = b;
    }

    public static long getCurrentSession() {
        return currentSession;
    }

    public static void setCurrentSession(long currentSession) {
        State.currentSession = currentSession;
    }
}
