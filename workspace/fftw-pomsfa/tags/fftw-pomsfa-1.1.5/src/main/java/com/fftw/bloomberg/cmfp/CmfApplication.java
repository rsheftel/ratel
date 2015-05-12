package com.fftw.bloomberg.cmfp;



public interface CmfApplication
{
    /**
     * This callback provides you with a peek at the administrative messages
     * that are being sent from you to Bloomberg POMS. This is
     * normally not useful for an application however it is provided for any
     * logging you may wish to do.
     * 
     * @param message
     *            CmfpMessage message
     * @param sessionId
     *            CmfpSessionID session ID
     */
    void toAdmin(CmfMessage message, CmfSessionID sessionId);

    /**
     * This callback notifies you when an administrative message is sent from 
     * Bloomberg to you. 
     * 
     * @param message
     *            CmfMessage message
     * @param sessionId
     *            CmfpSessionID session ID
     */
    void fromAdmin(CmfMessage message, CmfSessionID sessionId);

    /**
     * This is a callback for application messages that you are sending to
     * Bloomberg.  You may add fields before an application message
     * before it is sent out.
     * 
     * @param message
     *            CmfMessage message
     * @param sessionId
     *            CmfpSessionID session ID
     */
    void toApp(CmfMessage message, CmfSessionID sessionId);

    /**
     * This callback receives messages for the application. This is one of the
     * core entry points for your CMF application. Every application level
     * request will come through here. 
     * 
     * @param message
     *            CmfMessage message
     * @param sessionId
     *            CmfpSessionID session ID
     */
    void fromApp(CmfMessage message, CmfSessionID sessionId);
}
