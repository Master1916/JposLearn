package base.msg

import org.jpos.iso.ISOMsg
import org.jpos.iso.ISOUtil
import org.jpos.iso.packager.GenericPackager

/*
 * @author yinheli <yinheli@gmail.com>
 */

def msg = new ISOMsg()
msg.setPackager(new GenericPackager('E:\\idea_workspace\\jpos.demo\\cfg\\packager\\sample.xml'))
msg.setMTI('0800')
msg.set 11, '000001'
msg.set 41, '12345678'
msg.set 42, '000000000000001'
msg.set 60, '00' + '000001' + '001'
msg.set 63, '01'

byte[] data = msg.pack()

println ISOUtil.hexdump(data)
println ISOUtil.hexString(data)