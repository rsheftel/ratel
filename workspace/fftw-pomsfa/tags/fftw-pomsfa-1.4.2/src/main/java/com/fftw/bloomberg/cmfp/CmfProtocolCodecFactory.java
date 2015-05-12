package com.fftw.bloomberg.cmfp;

import org.apache.mina.filter.codec.demux.DemuxingProtocolCodecFactory;

import com.fftw.bloomberg.cmfp.filter.CmfDecoder;
import com.fftw.bloomberg.cmfp.filter.CmfEncoder;

public class CmfProtocolCodecFactory extends DemuxingProtocolCodecFactory
{
    public CmfProtocolCodecFactory() {
        super.register(CmfDecoder.class);
        super.register(CmfEncoder.class);
    }
}
