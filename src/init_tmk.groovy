import org.jpos.ext.security.MyJCEHandler
import org.jpos.ext.security.SoftSecurityModule
import org.jpos.core.SimpleConfiguration
import org.jpos.iso.ISOUtil
import org.jpos.security.SMAdapter
import org.jpos.security.SimpleKeyFile


def tid = '88888888'
def tmk = '719B53A9BF49C6E2'


SimpleKeyFile ks = new SimpleKeyFile('E:\\idea_workspace\\jposLearn\\deploy\\.keyfile') //key store
def clearZMK = ISOUtil.hex2byte(tmk)//
def tmk_alias = "sample.${tid}.tmk"
MyJCEHandler jceHandler = new MyJCEHandler('com.sun.crypto.provider.SunJCE')
SoftSecurityModule sm = new SoftSecurityModule()
SimpleConfiguration cfg = new SimpleConfiguration()
cfg.put("lmk", "E:\\idea_workspace\\jposLearn\\deploy\\.lmk")
sm.setConfiguration(cfg)

def lmk_tmk = sm.encryptToLMK(SMAdapter.LENGTH_DES, SMAdapter.TYPE_TMK,jceHandler.formDESKey(SMAdapter.LENGTH_DES, clearZMK))
ks.setKey(tmk_alias, lmk_tmk)
println ISOUtil.hexString(lmk_tmk.getKeyBytes())
println ISOUtil.hexString(lmk_tmk.getKeyCheckValue())
