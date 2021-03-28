/*
 * 版权说明：
 *1.中国银联股份有限公司（以下简称“中国银联”）对该代码保留全部知识产权权利， 包括但不限于版权、专利、商标、商业秘密等。
 *  任何人对该代码的任何使用都要 受限于在中国银联成员机构服务平台（http://member.unionpay.com/）与中国银
 *  联签 署的协议之规定。中国银联不对该代码的错误或疏漏以及由此导致的任何损失负 任何责任。中国银联针  对该代码放弃所有明
 *  示或暗示的保证,包括但不限于不侵 犯第三方知识产权。
 *  
 *2.未经中国银联书面同意，您不得将该代码用于与中国银联合作事项之外的用途和目的。未经中国银联书面同意，不得下载、
 *  转发、公开或以其它任何形式向第三方提供该代码。如果您通过非法渠道获得该代码，请立即删除，并通过合法渠道 向中国银
 *  联申请。
 *  
 *3.中国银联对该代码或与其相关的文档是否涉及第三方的知识产权（如加密算法可 能在某些国家受专利保护）不做任何声明和担
 *  保，中国银联对于该代码的使用是否侵犯第三方权利不承担任何责任，包括但不限于对该代码的部分或全部使用。
 *
 */
package dealType.util.security.sm3;

import org.bouncycastle.crypto.digests.SM3Digest;

/**
 * 消息摘要：国密 sm3
 *
 */
public class Sm3Utils {
	
	static final byte[] sm2_par_dig = {
			(byte) 0xFF,(byte) 0xFF,(byte) 0xFF,(byte) 0xFE,(byte) 0xFF,(byte) 0xFF,(byte) 0xFF,(byte) 0xFF,(byte) 0xFF,(byte) 0xFF,(byte) 0xFF,(byte) 0xFF,(byte) 0xFF,(byte) 0xFF,(byte) 0xFF,(byte) 0xFF,
			(byte) 0xFF,(byte) 0xFF,(byte) 0xFF,(byte) 0xFF,0x00,0x00,0x00,0x00,(byte) 0xFF,(byte) 0xFF,(byte) 0xFF,(byte) 0xFF,(byte) 0xFF,(byte) 0xFF,(byte) 0xFF,(byte) 0xFC,
			0x28,(byte) 0xE9,(byte) 0xFA,(byte) 0x9E,(byte) 0x9D,(byte) 0x9F,0x5E,0x34,0x4D,0x5A,(byte) 0x9E,0x4B,(byte) 0xCF,0x65,0x09,(byte) 0xA7,
			(byte) 0xF3,(byte) 0x97,(byte) 0x89,(byte) 0xF5,0x15,(byte) 0xAB,(byte) 0x8F,(byte) 0x92,(byte) 0xDD,(byte) 0xBC,(byte) 0xBD,0x41,0x4D,(byte) 0x94,0x0E,(byte) 0x93,
			0x32,(byte) 0xC4,(byte) 0xAE,0x2C,0x1F,0x19,(byte) 0x81,0x19,0x5F,(byte) 0x99,0x04,0x46,0x6A,0x39,(byte) 0xC9,(byte) 0x94,
			(byte) 0x8F,(byte) 0xE3,0x0B,(byte) 0xBF,(byte) 0xF2,0x66,0x0B,(byte) 0xE1,0x71,0x5A,0x45,(byte) 0x89,0x33,0x4C,0x74,(byte) 0xC7,
			(byte) 0xBC,0x37,0x36,(byte) 0xA2,(byte) 0xF4,(byte) 0xF6,0x77,(byte) 0x9C,0x59,(byte) 0xBD,(byte) 0xCE,(byte) 0xE3,0x6B,0x69,0x21,0x53,
			(byte) 0xD0,(byte) 0xA9,(byte) 0x87,0x7C,(byte) 0xC6,0x2A,0x47,0x40,0x02,(byte) 0xDF,0x32,(byte) 0xE5,0x21,0x39,(byte) 0xF0,(byte) 0xA0,
		};

	public static byte[] digest(byte[] data) {
		SM3Digest sm3 = new SM3Digest();
		byte[] result = null;
		try {
			sm3.update(data, 0, data.length);
			result = new byte[sm3.getDigestSize()];
			sm3.doFinal(result, 0);
		} catch (Exception e) {
			System.out.println("Fail: SM3 byte[] to byte[]"+ e);
		}
		return result;
	}
	
	public static byte [] DigestForSM2SignWithPK(byte[] sm3_dig1, byte[] userid, byte[] PK)
	{
		byte[]	sm3Data;
		byte[]	tmpBuf = new byte[2 + userid.length + 128 + 64];
		byte[]	oriHashData = new byte[sm3_dig1.length + 32];
		
		// userid的长度
		int	userid_bitlen = userid.length << 3;
		
		tmpBuf[0] = (byte) ((userid_bitlen >> 8) & 0xFF);
		tmpBuf[1] = (byte) (userid_bitlen & 0xFF);
		
		// 填充userid域
		System.arraycopy(userid, 0, tmpBuf, 2, userid.length);
		
		// 填充sm2参数
		System.arraycopy(sm2_par_dig, 0, tmpBuf, 2 + userid.length, sm2_par_dig.length);
		
		// 填充公钥域
		System.arraycopy(PK, 0, tmpBuf, 2 + userid.length + sm2_par_dig.length, PK.length);
		
		// 计算组合域摘要
		sm3Data = digest(tmpBuf);

		// 将组合域摘要拷贝到Hash数组
		System.arraycopy(sm3Data, 0, oriHashData, 0, 32);
		
		// 将第一次的摘要结果填充至组合域之后
		System.arraycopy(sm3_dig1, 0, oriHashData, 32, sm3_dig1.length);
		
		// 再计算一次摘要
		sm3Data = digest(oriHashData);
		
		return sm3Data;
	}
}
