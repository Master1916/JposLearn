package base.ifb

import org.jpos.iso.IFB_LLNUM
import org.jpos.iso.ISOField
import org.jpos.iso.ISOUtil

/*
 * @author yinheli <yinheli@gmail.com>
 */

def filedPackager = new IFB_LLNUM(10, 'just for a test', false)
def field = new ISOField(0, '12345678')
byte[] result = filedPackager.pack(field)

// dump it
println ISOUtil.hexdump(result)