package base.ifa

import org.jpos.iso.IFA_NUMERIC
import org.jpos.iso.ISOField
import org.jpos.iso.ISOUtil

/*
 * @author yinheli <yinheli@gmail.com>
 *
 * NOTE:
 *     IFA_NUMERIC  IF_CHAR  区别
 *     前补0         后补空格
 */
def filedPackager = new IFA_NUMERIC(6, 'just for a test')
def field = new ISOField(0, '123456')
byte[] result = filedPackager.pack(field)

// dump it
println ISOUtil.hexdump(result)
