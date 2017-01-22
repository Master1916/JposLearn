package com.zh.pos.jemeter;



import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.sql.DataSource;
import org.jpos.iso.ISOUtil;
import org.jpos.security.SecureKeyStore.SecureKeyStoreException;
import org.jpos.util.NameRegistrar;

public class GetDBInfo {
    protected String dsName;
    protected String tableName;
    protected String seqName;

    public GetDBInfo() {
    }

    public void init(String dsName, String tableName, String seqName) throws SecureKeyStoreException {
        try {
            this.dsName = dsName;
            this.tableName = tableName;
            this.seqName = seqName != null && seqName.length() != 0?seqName.trim():null;
        } catch (Exception var5) {
            var5.printStackTrace();
        }

    }

    public String getBatchNo(String terminal_no) throws Exception {
        String batchNo = null;

        try {
            Connection e = null;
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                e = ((DataSource)NameRegistrar.get(this.dsName)).getConnection();
                ps = e.prepareStatement("select batch_no from " + this.tableName + " where terminal_no = ?");
                ps.setString(1, terminal_no);
                rs = ps.executeQuery();
                if(!rs.next()) {
                    if(rs != null) {
                        rs.close();
                    }

                    if(ps != null) {
                        ps.close();
                    }

                    if(e != null) {
                        e.close();
                    }
                }

                batchNo = rs.getString("batch_no");
            } finally {
                if(rs != null) {
                    rs.close();
                }

                if(ps != null) {
                    ps.close();
                }

                if(e != null) {
                    e.close();
                }

            }
        } catch (Exception var10) {
            var10.printStackTrace();
        }

        return ISOUtil.padleft(batchNo, 6, '0');
    }

    public static void main(String[] args) {
        String a = "22000109001";
        String b = a.substring(0, 2) + "123456" + a.substring(8);
        System.out.println(b);
    }
}
