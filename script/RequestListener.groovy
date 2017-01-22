

import org.jpos.ext.security.MyJCEHandler
import org.jpos.ext.security.SoftSecurityModule
import org.jpos.iso.ISODate
import org.jpos.iso.ISOUtil
import org.jpos.security.SMAdapter
import org.jpos.security.SecureDESKey
import org.jpos.security.SecureKey
import org.jpos.security.SimpleKeyFile
import org.jpos.util.NameRegistrar

import java.security.Key
import java.security.KeyStore

def returnMsg = { respMsg, retCode = null ->
	if (!respMsg) return
	if (retCode) respMsg.set 39, retCode
	if (respMsg.isRequest()) respMsg.setResponseMTI()
	source.send respMsg
}

log.info "receive:\n" + ISOUtil.hexdump(req.pack())

def mti = req.getMTI()
def f60 = req.getString(60)

def transCode = ''
if (mti in ['0320', '0500', '0800', '0820','0200']) {
	transCode = "$mti/${f60[8..10]}" as String
} else {
	transCode = "$mti/${f60[0..1]}" as String
}

now = new Date()
req.set 12, ISODate.formatDate(now, 'HHmmss')
req.set 13, ISODate.formatDate(now, 'MMdd')
tid = req.getString(41)
switch (transCode) {
	case '0800/001':
	case '0800/003':
		SoftSecurityModule sm = NameRegistrar.get('sm') as SoftSecurityModule
		SimpleKeyFile ks = NameRegistrar.get('keyStore') as SimpleKeyFile

		def lmk_tmk = ks.getKey("sample.${tid}.tmk")
		def lmk_tpk = sm.generateKey(SMAdapter.LENGTH_DES, SMAdapter.TYPE_TPK)
		def lmk_tak = sm.generateKey(SMAdapter.LENGTH_DES, SMAdapter.TYPE_TAK)

		def tmk_tpk = new SecureDESKey(SMAdapter.LENGTH_DES, SMAdapter.TYPE_TPK,
				sm.exportKey(lmk_tpk, lmk_tmk), lmk_tpk.keyCheckValue)
		def tmk_tak = new SecureDESKey(SMAdapter.LENGTH_DES, SMAdapter.TYPE_TAK,
				sm.exportKey(lmk_tak, lmk_tmk), lmk_tak.keyCheckValue)

		ks.setKey("sample.${tid}.tpk", tmk_tpk)
		ks.setKey("sample.${tid}.tak", tmk_tak)

		def wk = ISOUtil.hexString(tmk_tpk.keyBytes) + ISOUtil.hexString(tmk_tpk.keyCheckValue) +
				 ISOUtil.hexString(tmk_tak.keyBytes) + ISOUtil.hexString(tmk_tak.keyCheckValue)

		req.set 62, wk
		returnMsg req, '00'
		break
	case '0820/002':
		returnMsg req, '00'
		break
	case '0200/000':
		returnMsg req, '00'
		break
	default:
		returnMsg req, '40'
}