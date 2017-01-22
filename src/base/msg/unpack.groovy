package base.msg

import org.jpos.iso.ISOMsg
import org.jpos.iso.ISOUtil
import org.jpos.iso.packager.GenericPackager

/*
 * @author yinheli <yinheli@gmail.com>
 */

def data = '08000020000000C000120000013132333435363738303030303030303030303030303031001100000001001000023031'

def msg = new ISOMsg()
msg.setPackager(new GenericPackager('E:\\idea_workspace\\jpos.demo\\cfg\\packager\\sample.xml'))
msg.unpack(ISOUtil.hex2byte(data))

msg.dump(System.out, '')