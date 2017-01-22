package com.zh.pos.jemeter;


import com.alibaba.druid.pool.DruidDataSource;
import com.zh.pos.jemeter.GetDBInfo;
import org.jpos.core.SimpleConfiguration;
import org.jpos.ext.hsm.SJL22Adapter;
import org.jpos.ext.security.MyJCEHandler;
import org.jpos.ext.security.SimpleDBKeyStore;
import org.jpos.ext.security.SoftSecurityModule;
import org.jpos.security.SecureKeyStore;
import org.jpos.util.Log;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;
import org.jpos.util.SimpleLogListener;

public class KeyTool {
    protected Log log;
    protected MyJCEHandler jceHandler;
    protected SoftSecurityModule sm;
    protected SJL22Adapter hsm;
    protected SecureKeyStore fileKeyStore;
    protected SecureKeyStore dbKeyStore;
    protected GetDBInfo getDBInfo;
    private String hsm_host = null;
    private String hsm_port = null;
    private String hsm_timeout = null;
    private String lmk_path = null;
    private String driverClassName = null;
    private String url = null;
    private String username = null;
    private String password = null;

    public KeyTool(String hsm_host, String hsm_port, String hsm_timeout, String lmk_path, String driverClassName, String url, String username, String password) {
        this.hsm_host = hsm_host;
        this.hsm_port = hsm_port;
        this.hsm_timeout = hsm_timeout;
        this.lmk_path = lmk_path;
        this.driverClassName = driverClassName;
        this.url = url;
        this.username = username;
        this.password = password;
        this.init();
    }

    public void init() {
        try {
            Logger e = new Logger();
            e.addListener(new SimpleLogListener());
            this.log = new Log(e, "key-tool");
            String provider = "com.sun.crypto.provider.SunJCE";
            SimpleConfiguration cfg = new SimpleConfiguration();
            cfg.put("provider", provider);
            cfg.put("host", this.hsm_host);
            cfg.put("port", this.hsm_port);
            cfg.put("timeout", this.hsm_timeout);
            cfg.put("lmk", this.lmk_path);
            this.hsm = new SJL22Adapter();
            this.hsm.setLogger(e, "hsm");
            this.hsm.setConfiguration(cfg);
            this.jceHandler = new MyJCEHandler(provider);
            DruidDataSource ds = new DruidDataSource();
            ds.setDriverClassName(this.driverClassName);
            ds.setUrl(this.url);
            ds.setUsername(this.username);
            ds.setPassword(this.password);
            ds.setValidationQuery("select 1 from dual");
            NameRegistrar.register("ds", ds);
            this.dbKeyStore = new SimpleDBKeyStore();
            ((SimpleDBKeyStore)this.dbKeyStore).init("ds", "key_store", "seq_keystore");
            this.getDBInfo = new GetDBInfo();
            this.getDBInfo.init("ds", "merchant_terminal", "seq_merchantterminal");
            this.sm = new SoftSecurityModule();
            this.sm.setLogger(e, "sm");
            this.sm.setConfiguration(cfg);
        } catch (Exception var5) {
            var5.printStackTrace();
        }

    }
}
