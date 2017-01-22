package com.zh.pos.jemeter;


import com.myjpos.channel.MyHEXChannel;
import com.zh.pos.jemeter.KeyTool;
import java.io.IOException;
import java.security.Key;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.jpos.core.CardHolder;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.packager.GenericPackager;
import org.jpos.iso.packager.XMLPackager;
import org.jpos.security.EncryptedPIN;
import org.jpos.security.SecureDESKey;

public class PosSampler_posp_mac extends AbstractJavaSamplerClient {
    public PosSampler_posp_mac() {
    }

    public Arguments getDefaultParameters() {
        Arguments params = new Arguments();
        params.addArgument("address", "127.0.0.1");
        params.addArgument("port", "8888");
        params.addArgument("tpdu", "6000060000603000000000");
        params.addArgument("packagerConfig", "");
        params.addArgument("testPackage", "");
        params.addArgument("driverClassName", "oracle.jdbc.OracleDriver");
        params.addArgument("url", "jdbc:oracle:thin:@192.168.1.16:1521:payment");
        params.addArgument("username", "acq_v3_dev");
        params.addArgument("password", "acq_v3_dev");
        params.addArgument("lmk", "D://zh//svn//encry//posv4_update_encry_mode_20150521//deploy//lmk");
        params.addArgument("hsm_host", "127.0.0.1");
        params.addArgument("hsm_port", "8000");
        params.addArgument("hsm_timeout", "5000");
        params.addArgument("pin", "123456");
        params.addArgument("encry_flag", "0");
        params.addArgument("posp", "posptu_agency");
        params.addArgument("trans_type", "sale");
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
            KeyTool kt = new KeyTool(arg0.getParameter("hsm_host"), arg0.getParameter("hsm_port"), arg0.getParameter("hsm_timeout"), arg0.getParameter("lmk"), arg0.getParameter("driverClassName"), arg0.getParameter("url"), arg0.getParameter("username"), arg0.getParameter("password"));
            int encry_flag = Integer.parseInt(arg0.getParameter("encry_flag"));
            SecureDESKey tak_lmk = null;
            SecureDESKey tpk_lmk = null;
            String posp = arg0.getParameter("posp");
            String trans_type = arg0.getParameter("trans_type");
            if("sale".equals(trans_type)) {
                if(encry_flag == 0) {
                    tak_lmk = this.getKeyFromSoft(kt, "posptu_agency".equals(posp)?"agency." + e.getString(32) + ".zak":"tid." + e.getString(41) + ".tak");
                    tpk_lmk = this.getKeyFromSoft(kt, "posptu_agency".equals(posp)?"agency." + e.getString(32) + ".zpk":"tid." + e.getString(41) + ".tpk");
                } else if(encry_flag == 1) {
                    tak_lmk = this.getKeyFromHsm(kt, "posptu_agency".equals(posp)?"agency." + e.getString(32) + ".zak":"tid." + e.getString(41) + ".tak");
                    tpk_lmk = this.getKeyFromHsm(kt, "posptu_agency".equals(posp)?"agency." + e.getString(32) + ".zpk":"tid." + e.getString(41) + ".tpk");
                }

                String resp = arg0.getParameter("pin");
                CardHolder cardHolder = new CardHolder(e);
                EncryptedPIN lpin = kt.sm.encryptPIN(resp, cardHolder.getPAN());
                byte[] pinBlock = kt.sm.exportPIN(lpin, tpk_lmk, (byte)0).getPINBlock();
                e.set(52, pinBlock);
                String f60 = e.getString(60);
                f60 = f60.substring(0, 2) + kt.getDBInfo.getBatchNo(e.getString(41)) + f60.substring(8);
                e.set(60, f60);
                ISOMsg msg = (ISOMsg)e.clone();
                msg.setPackager(new GenericPackager(arg0.getParameter("packagerConfig")));
                msg.set(64, new byte[8]);
                byte[] data = msg.pack();
                e.set(64, kt.sm.generateUnpayV1_MACImpl(ISOUtil.trim(data, data.length - 8), tak_lmk));
            }

            channel.send(e);
            channel.getSocket().getOutputStream().flush();
            ISOMsg resp1 = channel.receive();
            resp1.setPackager(new XMLPackager());
            res.setDataType("text");
            res.setResponseData(resp1.pack());
        } catch (Exception var27) {
            var27.printStackTrace();
            ret = false;
        } finally {
            try {
                channel.getSocket().getOutputStream().close();
                channel.getSocket().close();
                channel.disconnect();
            } catch (IOException var26) {
                var26.printStackTrace();
            }

            res.sampleEnd();
        }

        res.setSuccessful(ret);
        return res;
    }

    public SecureDESKey getKeyFromSoft(KeyTool kt, String alias) throws Exception {
        SecureDESKey secureDESKey = (SecureDESKey)kt.dbKeyStore.getKey(alias);
        return secureDESKey;
    }

    public SecureDESKey getKeyFromHsm(KeyTool kt, String alias) throws Exception {
        SecureDESKey secureDESKey = (SecureDESKey)kt.dbKeyStore.getKey(alias);
        Key clear_tmk = kt.jceHandler.formDESKey((short)128, new byte[16]);
        SecureDESKey lmk_tmk = (SecureDESKey)kt.dbKeyStore.getKey("system.clear.tmk");
        byte[] clear_key = kt.jceHandler.decryptData(kt.hsm.exportKey(secureDESKey, lmk_tmk), clear_tmk);
        SecureDESKey sm_key = kt.sm.encryptToLMK(secureDESKey.getKeyLength(), secureDESKey.getKeyType(), kt.jceHandler.formDESKey(secureDESKey.getKeyLength(), clear_key));
        return sm_key;
    }

    public static void main(String[] args) throws Exception {
        MyHEXChannel channel = null;

        try {
            channel = new MyHEXChannel("localhost", Integer.parseInt("4022"), new GenericPackager("D://zh//svn//encry//posv4_update_encry_mode_20150521//cfg//packager//posp-v1.xml"), ISOUtil.hex2byte("6000060000603000000000"));
            String e = "<isomsg direction=\'incoming\'>     <!-- org.jpos.iso.packager.GenericPackager[cfg/packager/posp-v1.xml] -->     <header>6000050001603100311001</header>     <field id=\'0\' value=\'0200\'/>     <field id=\'2\' value=\'6217995840013205711\'/> <field id=\'3\' value=\'000000\'/>     <field id=\'4\' value=\'000000000110\'/>     <field id=\'11\' value=\'888888\'/>     <field id=\'22\' value=\'021\'/>     <field id=\'25\' value=\'00\'/>     <field id=\'26\' value=\'12\'/>    <field id=\'41\' value=\'30023287\'/>     <field id=\'42\' value=\'Z06000000019550\'/>     <field id=\'49\' value=\'156\'/>     <field id=\'52\' value=\'0000000000000000\' type=\'binary\'/> <field id=\'53\' value=\'2610000000000000\'/>   <field id=\'60\' value=\'22000109001\'/>  <field id=\'63\' value=\'01 \'/>   <field id=\'64\' value=\'0000000000000000\' type=\'binary\'/>   </isomsg>";
            channel.connect();
            ISOMsg e1 = new ISOMsg();
            e1.setPackager(new XMLPackager());
            e1.unpack(e.getBytes());
            System.out.println(e1.getString(41));
            channel.send(e1);
            channel.getSocket().getOutputStream().flush();
            ISOMsg resp = channel.receive();
            resp.setPackager(new XMLPackager());
            System.out.println(new String(resp.pack()));
        } catch (Exception var15) {
            var15.printStackTrace();

            try {
                channel.getSocket().getOutputStream().close();
                channel.getSocket().close();
                channel.disconnect();
            } catch (IOException var14) {
                var14.printStackTrace();
            }
        } finally {
            try {
                channel.getSocket().getOutputStream().close();
                channel.getSocket().close();
                channel.disconnect();
            } catch (IOException var13) {
                var13.printStackTrace();
            }

        }

    }
}
