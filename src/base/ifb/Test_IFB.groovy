package base.ifb

import org.jpos.iso.IFB_NUMERIC
import org.jpos.iso.ISOBinaryField
import org.jpos.iso.ISOUtil

/*
 * @author yinheli <yinheli@gmail.com>
 */

def filedPackager = new IFB_NUMERIC(4, 'just for a test', false)
def field = new ISOBinaryField(0, ISOUtil.hex2byte('0800'))
byte[] result = filedPackager.pack(field)

// dump it
println ISOUtil.hexdump(result)
