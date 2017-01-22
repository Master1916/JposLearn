package base.ifa

import org.jpos.iso.IFA_LLCHAR
import org.jpos.iso.ISOField
import org.jpos.iso.ISOUtil

/*
 * @author yinheli <yinheli@gmail.com>
 */
def filedPackager = new IFA_LLCHAR(10, 'just for a test')
def field = new ISOField(0, '123456')
byte[] result = filedPackager.pack(field)

// dump it
println ISOUtil.hexdump(result)