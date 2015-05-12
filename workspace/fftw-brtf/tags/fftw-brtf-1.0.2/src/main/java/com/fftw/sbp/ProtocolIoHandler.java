package com.fftw.sbp;

import org.apache.mina.common.IoHandler;

/**
 *
 */
public interface ProtocolIoHandler<ID, M> extends IoHandler {

    void setProtocolSession(ProtocolSession<ID, M> session);

    ProtocolSession<ID, M> getProtocolSession();
}
