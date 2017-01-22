package com.zh.pos.jemeter;


import com.myjpos.channel.MyHEXChannel;
import java.io.IOException;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.packager.GenericPackager;
import org.jpos.iso.packager.XMLPackager;

public class PosSampler extends AbstractJavaSamplerClient {
    public PosSampler() {
    }

    public Arguments getDefaultParameters() {
        Arguments params = new Arguments();
        params.addArgument("address", "127.0.0.1");
        params.addArgument("port", "8888");
        params.addArgument("tpdu", "6000070000");
        params.addArgument("packagerConfig", "");
        params.addArgument("testPackage", "");
        return params;
    }

    public void setupTest(JavaSamplerContext arg0) {
    }

    public SampleResult runTest(JavaSamplerContext arg0) {
        MyHEXChannel channel = null;
        SampleResult res = new SampleResult();
        res.setSamplerData(arg0.getParameter("testPackage"));
        res.sampleStart();
        boolean ret = true;

        try {
            channel = new MyHEXChannel(arg0.getParameter("address"), Integer.parseInt(arg0.getParameter("port")), new GenericPackager(arg0.getParameter("packagerConfig")), ISOUtil.hex2byte(arg0.getParameter("tpdu")));
            channel.connect();
            ISOMsg e = new ISOMsg();
            e.setPackager(new XMLPackager());
            e.unpack(arg0.getParameter("testPackage").getBytes());
            channel.send(e);
            channel.getSocket().getOutputStream().flush();
            ISOMsg resp = channel.receive();
            resp.setPackager(new XMLPackager());
            res.setDataType("text");
            res.setResponseData(resp.pack());
        } catch (Exception var15) {
            var15.printStackTrace();
            ret = false;
        } finally {
            try {
                channel.getSocket().getOutputStream().close();
                channel.getSocket().close();
                channel.disconnect();
            } catch (IOException var14) {
                var14.printStackTrace();
            }

            res.sampleEnd();
        }

        res.setSuccessful(ret);
        return res;
    }

    public void teardownTest(JavaSamplerContext arg0) {
    }

    private static void sendMsg(String address, String port, String packagerConfig, String tpdu, String testPackage) {
        MyHEXChannel channel = null;

        try {
            System.out.println("address:" + address + " port:" + port);
            channel = new MyHEXChannel(address, Integer.parseInt(port), new GenericPackager(packagerConfig), ISOUtil.hex2byte(tpdu));
            channel.connect();
            ISOMsg e = new ISOMsg();
            e.setPackager(new XMLPackager());
            e.unpack(testPackage.getBytes());
            channel.send(e);
            channel.getSocket().getOutputStream().flush();
            ISOMsg resp = channel.receive();
            resp.setPackager(new XMLPackager());
            System.out.println(new String(resp.pack()));
        } catch (Exception var16) {
            var16.printStackTrace();
        } finally {
            try {
                channel.getSocket().getOutputStream().close();
                channel.getSocket().close();
                channel.disconnect();
            } catch (IOException var15) {
                var15.printStackTrace();
            }

        }

    }

    public static void main(String[] args) {
        String pack = "<isomsg>\n      <header>6000050001603100311001</header>\n      <field id=\"0\" value=\"0200\"/>\n      <field id=\"2\" value=\"6217001111111113822\"/>\n      <field id=\"3\" value=\"000000\"/>\n      <field id=\"4\" value=\"000000000001\"/>\n      <field id=\"11\" value=\"000012\"/>\n      <field id=\"22\" value=\"022\"/>\n      <field id=\"25\" value=\"00\"/>\n      <field id=\"35\" value=\"6217001111111113822=11111111111111111\"/>\n      <field id=\"41\" value=\"68001222\"/>\n      <field id=\"42\" value=\"Z08000000030502\"/>\n      <field id=\"49\" value=\"156\"/>\n      <field id=\"60\" value=\"2200000100000\"/>\n      <field id=\"120\" value=\"000012903430\"/>\n      <field id=\"121\" value=\"20140702165457\"/>\n</isomsg>";
        sendMsg("192.168.1.12", "5555", "/Users/mmx/Documents/posp-v1.xml", "6000060000603000000000", pack);
    }
}
