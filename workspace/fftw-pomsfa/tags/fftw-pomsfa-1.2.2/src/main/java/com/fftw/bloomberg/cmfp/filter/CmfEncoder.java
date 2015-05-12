package com.fftw.bloomberg.cmfp.filter;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.filter.codec.demux.MessageEncoder;

@SuppressWarnings("unchecked")
//public class CmfEncoder implements ProtocolEncoder
public class CmfEncoder implements MessageEncoder
{

    private static final Set<Class> TYPES;

    static
    {
        Set<Class> types = new HashSet<Class>();
        types.add(String.class);
        TYPES = Collections.unmodifiableSet(types);
    }

    public void encode (IoSession session, Object message, ProtocolEncoderOutput out)
        throws Exception
    {
        String strMessage = (String)message;
        ByteBuffer buffer = ByteBuffer.allocate(strMessage.length(), false);
        buffer.putString(strMessage, Charset.forName("UTF-8").newEncoder());
        buffer.flip(); // we were adding data to the buffer, now we want to
                        // write it out
        out.write(buffer);
    }

    public Set<Class> getMessageTypes ()
    {
        return TYPES;
    }
}
