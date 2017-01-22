package base.ifa

import org.jpos.iso.IF_CHAR
import org.jpos.iso.ISOField
import org.jpos.iso.ISOUtil

/*
 * @author yinheli <yinheli@gmail.com>
 */
def filedPackager = new IF_CHAR(6, 'just for a test')
def field = new ISOField(0, 'abcd')
byte[] result = filedPackager.pack(field)

// dump it
println ISOUtil.hexdump(result)