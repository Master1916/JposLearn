package com.myjpos.channel;

import java.io.IOException;
import java.net.ServerSocket;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.BaseChannel;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.ISOUtil;

public class MyHEXChannel extends BaseChannel {
    public MyHEXChannel() {
    }

    public MyHEXChannel(String host, int port, ISOPackager p, byte[] TPDU) {
        super(host, port, p);
        this.header = TPDU;
    }

    public MyHEXChannel(ISOPackager p, byte[] TPDU) throws IOException {
        super(p);
        this.header = TPDU;
    }

    public MyHEXChannel(ISOPackager p, byte[] TPDU, ServerSocket serverSocket) throws IOException {
        super(p, serverSocket);
        this.header = TPDU;
    }

    protected void sendMessageLength(int len) throws IOException {
        this.serverOut.write(len >> 8);
        this.serverOut.write(len);
    }

    protected int getMessageLength() throws IOException, ISOException {
        byte[] b = new byte[2];
        this.serverIn.readFully(b, 0, 2);
        return (b[0] & 255) << 8 | b[1] & 255;
    }

    protected void sendMessageHeader(ISOMsg m, int len) throws IOException {
        byte[] h = m.getHeader();
        if(h != null) {
            if(h.length == 5) {
                byte[] tmp = new byte[2];
                System.arraycopy(h, 1, tmp, 0, 2);
                System.arraycopy(h, 3, h, 1, 2);
                System.arraycopy(tmp, 0, h, 3, 2);
            }
        } else {
            h = this.header;
        }

        if(h != null) {
            this.serverOut.write(h);
        }

    }

    public void setHeader(String header) {
        super.setHeader(ISOUtil.str2bcd(header, false));
    }

    public void setConfiguration(Configuration cfg) throws ConfigurationException {
        super.setConfiguration(cfg);
        String header = cfg.get("header");
        this.setHeader(header);
    }
}
