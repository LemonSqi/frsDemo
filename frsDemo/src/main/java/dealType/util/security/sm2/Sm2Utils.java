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
package dealType.util.security.sm2;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.math.ec.ECAlgorithms;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECPoint;

import dealType.util.common.StringUtil;
import dealType.util.security.sm3.Sm3Utils;

/**
 * SM2 非对称、公钥加密，签名验签：基于数学函数 ECC算法变形
 */
public class Sm2Utils {


	/**
	 * 椭圆曲线y2 = x3 + ax + b系统参数： q: a b G(x,y) n
	 */
	/**
	 * 有限域Fq中的元素数目
	 */
	private static BigInteger q;
	/**
	 * Fq中的元素，定义一条椭圆曲线
	 */
	private static BigInteger a;
	private static BigInteger b;
	/**
	 * 基点G的阶
	 */
	private static BigInteger n;
	/**
	 * 椭圆曲线的一个基点，其阶为素数
	 */
	private static BigInteger xG;
	private static BigInteger yG;
	private static ECPoint G;

	private static ECCurve curve;
	private static ECDomainParameters ec;
	static {
		q = new BigInteger("FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF00000000FFFFFFFFFFFFFFFF", 16);
		a = new BigInteger("FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF00000000FFFFFFFFFFFFFFFC", 16);
		b = new BigInteger("28E9FA9E9D9F5E344D5A9E4BCF6509A7F39789F515AB8F92DDBCBD414D940E93", 16);
		n = new BigInteger("FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFF7203DF6B21C6052B53BBF40939D54123", 16);
		xG = new BigInteger("32C4AE2C1F1981195F9904466A39C9948FE30BBFF2660BE1715A4589334C74C7", 16);
		yG = new BigInteger("BC3736A2F4F6779C59BDCEE36B692153D0A9877CC62A474002DF32E52139F0A0", 16);

		curve = new ECCurve.Fp(q, a, b);
		G = createPoint(xG, yG);
		ec = new ECDomainParameters(curve, G, n);
	}

	/**
	 * 根据坐标获得椭圆曲线上的点
	 * 
	 * @param x
	 *            x坐标
	 * @param y
	 *            y坐标
	 * @return 点
	 */
	@SuppressWarnings("deprecation")
	public static ECPoint createPoint(BigInteger x, BigInteger y) {
		ECFieldElement ecc_gx_fieldelement;
		ECFieldElement ecc_gy_fieldelement;

		ecc_gx_fieldelement = new ECFieldElement.Fp(q, x);
		ecc_gy_fieldelement = new ECFieldElement.Fp(q, y);
		return new ECPoint.Fp(curve, ecc_gx_fieldelement, ecc_gy_fieldelement);
	}

	/**
	 * 转为指定bit(bit/8)的byte[]
	 * 
	 * @param val
	 * @param bitLength
	 * @return
	 */
	public static byte[] bigIntegerToByteArray(BigInteger val, int bitLength) {
		int len = val.toByteArray().length;
		int fixedLength = bitLength / 8;
		if (len == fixedLength) {
			return val.toByteArray();
		}
		byte[] tmp = new byte[fixedLength];
		if (len < fixedLength) {
			System.arraycopy(val.toByteArray(), 0, tmp, fixedLength - len, len);
		}
		if (len > fixedLength) {// 截断
			System.arraycopy(val.toByteArray(), len - fixedLength, tmp, 0, fixedLength);
		}
		return tmp;
	}

	/**
	 * 转为256bit（32byte）的byte[]
	 * 
	 * @param big
	 * @return
	 */
	public static byte[] bigIntegerToByteArray(BigInteger big) {
		return bigIntegerToByteArray(big, 256);
	}

	/// 生成SM2私钥
	public static ECPrivateKeyParameters sm2PriKeyGet(String dH) {
		BigInteger d = new BigInteger(dH, 16);
		return new ECPrivateKeyParameters(d, ec);
	}

	/// 生成SM2私钥
	public static ECPrivateKeyParameters sm2PriKeyGet(byte[] dH) {
		BigInteger d = new BigInteger(1, dH);
		return new ECPrivateKeyParameters(d, ec);
	}

	/// 生成SM2公钥
	public static ECPublicKeyParameters sm2PubKeyGet(String xH, String yH) {
		BigInteger qx = new BigInteger(xH, 16);
		BigInteger qy = new BigInteger(yH, 16);
		ECPoint q = createPoint(qx, qy);
		return new ECPublicKeyParameters(q, ec);
	}

	public static ECPublicKeyParameters sm2PubKeyGet(byte[] xH, byte[] yH) {
		BigInteger qx = new BigInteger(1, xH);
		BigInteger qy = new BigInteger(1, yH);
		ECPoint q = createPoint(qx, qy);
		return new ECPublicKeyParameters(q, ec);
	}

	/**
	 * 根据私钥d获取公钥P
	 * 
	 * @param d
	 *            私钥d
	 * @return 公钥P
	 */
	public static ECPoint GetPublicKey(BigInteger d) {
		/**
		 * 加解密基于数学难题，K=kG，其中 K,G为Ep(a,b)上的点，k是整数，小于G点的阶
		 * 给定k和G，计算K很容易；但给定K和G，求k就困难了。这样，G就叫做基点，k是私钥，K是公钥
		 * 公钥是曲线在离散坐标系中，满足条件的一个曲线上的点。
		 */
		return G.multiply(d);
	}

	/**
	 * SM2 公钥加密
	 * 
	 * @param param
	 * @param data
	 * @return
	 */
	public static byte[] Encrypt(CipherParameters param, byte[] data) {
		Sm2Engine engine = new Sm2Engine();
		byte[] result = null;
		try {
			engine.init(true, param);
			result = engine.processBlock(data, 0, data.length);
		} catch (Exception e) {
			System.out.println("Fail: SM2 Encrypt"+e);
			return null;
		}
		return result;
	}

	/**
	 *  SM2 私钥解密
	 * 
	 * @param priKey
	 * @param data
	 * @return
	 */
	public static byte[] Decrypt(CipherParameters priKey, byte[] data) {
		Sm2Engine engine = new Sm2Engine();
		byte[] result = null;
		try {
			engine.init(false, priKey);
			result = engine.processBlock(data, 0, data.length);
		} catch (Exception e) {
			System.out.println("Fail: SM2 Decrypt"+e);
			return null;
		}
		return result;
	}

	/**
	 *  SM2 私钥签名
	 * 
	 * @param param
	 * @param userId
	 * @param msg
	 * @return
	 */
	public static byte[] SignWithSm3(CipherParameters param, byte[] userId, byte[] msg,byte[] pubkey) {

		// 先计算一次摘要
		byte[] sm3_dig1 = Sm3Utils.digest(msg);
		//System.out.println("sm3 in sm2 reslt:"+StringUtil.byte2HexStr(e));

		// 再计算一次摘要
		byte[] sm3_dig2 = Sm3Utils.DigestForSM2SignWithPK(sm3_dig1, userId, pubkey);

		// Sm2计算签名
		BigInteger[] sig;
		Sm2Signer signer = new Sm2Signer();

		try {
			signer.init(true, param); 
			sig = signer.generateSignature(sm3_dig2);
		} catch (Exception ex) {
			System.out.println("Fail: SignWithSm3"+ex);
			return null;
		}

		// 拼接RS
		byte[] signature;
		signature = Sm2Engine.combineByteArray(bigIntegerToByteArray(sig[0]), bigIntegerToByteArray(sig[1]));

		return signature;
	}

	/**
	 *  SM2 公钥验签
	 * 
	 * @param pubKey
	 * @param userId
	 * @param msg
	 * @param sign
	 * @return
	 */
	public static boolean VerifyWithSm3(CipherParameters pubKey, byte[] userId, byte[] msg, byte[] sign) {
		// e=Hv(M_), M_=ZA||M
		/*		byte[] za = null;
		za = CalculateZA((ECPublicKeyParameters) pubKey, userId);
		SM3Digest digest = new SM3Digest();
		byte[] e = new byte[digest.getDigestSize()];
		digest.update(za, 0, za.length);
		digest.update(msg, 0, msg.length);
		digest.doFinal(e, 0);*/

		byte[] e = Sm3Utils.digest(msg);

		System.out.println("sm3 in sm2 reslt:"+StringUtil.byte2HexStr(e));

		BigInteger r;
		BigInteger s;

		InputStream is = new ByteArrayInputStream(sign);
		byte[] rb = new byte[sign.length / 2];
		byte[] sb = new byte[sign.length / 2];
		try {
			is.read(rb, 0, sign.length / 2);
			is.read(sb, 0, sign.length / 2);
		} catch (IOException e1) {
			System.out.println("Fail: get r and s"+e1);
			return false;
		}
		r = new BigInteger(1, rb);
		s = new BigInteger(1, sb);

		boolean result = false;
		Sm2Signer signer = new Sm2Signer();
		try {
			signer.init(false, pubKey);
			result = signer.verifySignature(e, r, s);
		} catch (Exception ex) {
			System.out.println("Fail: VerifyWithSm3"+ex);
		} 
		return result;
	}

	/**
	 * ZA: 用户的标志、部分椭圆曲线参数、用户公钥的 杂凑值
	 * @param G
	 * @param a
	 * @param b
	 * @param A
	 * @param id
	 * @return
	 */
	private static byte[] CalculateZA(ECPoint G, BigInteger a, BigInteger b, ECPoint A, byte[] id) {
		SM3Digest digest = new SM3Digest();
		byte[] e = new byte[digest.getDigestSize()];
		byte[] tmp = null;

		// ENTLA
		int entla = id.length * 8;
		digest.update((byte) ((entla >> 8) & 0xFF));
		digest.update((byte) (entla & 0xFF));

		// IDA
		digest.update(id, 0, id.length);

		// a
		tmp = bigIntegerToByteArray(a);
		digest.update(tmp, 0, tmp.length);

		// b
		tmp = bigIntegerToByteArray(b);
		digest.update(tmp, 0, tmp.length);

		// xG
		tmp = bigIntegerToByteArray(G.getAffineXCoord().toBigInteger());
		digest.update(tmp, 0, tmp.length);

		// yG
		tmp = bigIntegerToByteArray(G.getAffineYCoord().toBigInteger());
		digest.update(tmp, 0, tmp.length);

		// xA
		tmp = bigIntegerToByteArray(A.getAffineXCoord().toBigInteger());
		digest.update(tmp, 0, tmp.length);

		// yA
		tmp = bigIntegerToByteArray(A.getAffineYCoord().toBigInteger());
		digest.update(tmp, 0, tmp.length);

		digest.doFinal(e, 0);
		return e;
	}

	public static byte[] CalculateZA(ECPrivateKeyParameters priKey, byte[] id) {
		ECPoint G = priKey.getParameters().getG();
		BigInteger a = priKey.getParameters().getCurve().getA().toBigInteger();
		BigInteger b = priKey.getParameters().getCurve().getB().toBigInteger();

		ECPoint A = ECAlgorithms.referenceMultiply(G, priKey.getD()).normalize();

		return CalculateZA(G, a, b, A, id);
	}

	public static byte[] CalculateZA(ECPublicKeyParameters pubKey, byte[] id) {

		ECPoint G = pubKey.getParameters().getG();
		BigInteger a = pubKey.getParameters().getCurve().getA().toBigInteger();
		BigInteger b = pubKey.getParameters().getCurve().getB().toBigInteger();

		ECPoint A = pubKey.getQ();

		return CalculateZA(G, a, b, A, id);
	}


}
