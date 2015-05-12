package com.fftw.sbp;

import java.util.Observer;

/**
 * Definition of a session based application
 */
public interface SessionApplication<S, M> extends Observer {

    void onCreate(S session);

    void onLogon(S session);

    void onLogout(S session);

    void toAdmin(S session, M message);

    void fromAdmin(S session, M message);

    void toApp(S session, M message);

    void fromApp(S session, M message);
    
}
