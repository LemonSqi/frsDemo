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
import java.io.InputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.SecureRandom;
import java.util.Arrays;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.DigestDerivationFunction;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.generators.KDF2BytesGenerator;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.KDFParameters;
import org.bouncycastle.crypto.params.ParametersWithRandom;

/**
 * SM2 非对称、公钥加密,签名验签：基于椭圆曲线
 */
public class Sm2Engine {
	

	// 密钥派生函数
	private final DigestDerivationFunction kdf;
	// 杂凑函数、摘要/哈希算法
	private final Digest digest;
	private final Sm2BasicAgreement basicAgreement;

	private Boolean forEncryption;
	private ECPublicKeyParameters publicKey;
	private ECPrivateKeyParameters privateKey;
	private SecureRandom secureRandom;

	public Sm2Engine() {
		this.kdf = new KDF2BytesGenerator(new SM3Digest());
		this.digest = new SM3Digest();
		this.basicAgreement = new Sm2BasicAgreement();
	}

	public Sm2Engine(DigestDerivationFunction kdf, Digest digest) {
		this.kdf = kdf;
		this.digest = digest;
		this.basicAgreement = new Sm2BasicAgreement();
	}

	public void init(boolean forEncryption, CipherParameters parameters) throws InvalidKeyException {
		ECPrivateKeyParameters priKey = null;
		ECPublicKeyParameters pubKey = null;
		SecureRandom sr = null;

		this.forEncryption = forEncryption;
		if (forEncryption) {//加密
			if (parameters instanceof ParametersWithRandom) {
				ParametersWithRandom rParam = (ParametersWithRandom) parameters;
				pubKey = (ECPublicKeyParameters) rParam.getParameters();
				sr = rParam.getRandom();
			} else if (parameters instanceof ECPublicKeyParameters) {
				pubKey = (ECPublicKeyParameters) parameters;
				sr = new SecureRandom();
			} else {
				throw new InvalidKeyException("EC public key required for encryption");
			}
		} else {//解密
			if (parameters instanceof ParametersWithRandom) {
				ParametersWithRandom rParam = (ParametersWithRandom) parameters;
				priKey = (ECPrivateKeyParameters) rParam.getParameters();
				sr = rParam.getRandom();
			} else if (parameters instanceof ECPrivateKeyParameters) {
				priKey = (ECPrivateKeyParameters) parameters;
				sr = new SecureRandom();
			} else {
				throw new InvalidKeyException("EC private key required for decryption");
			}

		}
		this.publicKey = pubKey;
		this.privateKey = priKey;
		this.secureRandom = sr;
	}

	public byte[] processBlock(byte[] input, int inOff, int inLen) throws Exception {
		if (this.forEncryption) {
			return encryptBlock(input, inOff, inLen);
		} else {
			return DecryptBlock(input, inOff, inLen);
		}
	}

	/**
	 * SM2 加密
	 * @param input 明文
	 * @param inOff 位移
	 * @param inLen 明文长度
	 * @return 密文
	 * @throws Exception 
	 */
	private byte[] encryptBlock(byte[] input, int inOff, int inLen) throws Exception {
		AsymmetricCipherKeyPair tempPair;
		ECKeyPairGenerator keyGen = new ECKeyPairGenerator();
		keyGen.init(new ECKeyGenerationParameters(publicKey.getParameters(), this.secureRandom));
		tempPair = keyGen.generateKeyPair();
	
		//C1=[k]G=(x1,y1)
		ECPublicKeyParameters C1 = (ECPublicKeyParameters) tempPair.getPublic(); // get temp's public key
		ECPrivateKeyParameters k = (ECPrivateKeyParameters) tempPair.getPrivate(); //get temp's private key
		byte[] x1 = Sm2Utils.bigIntegerToByteArray(C1.getQ().getAffineXCoord().toBigInteger());
		byte[] y1 = Sm2Utils.bigIntegerToByteArray(C1.getQ().getAffineYCoord().toBigInteger());
		this.basicAgreement.init(k);
	
		//点 S = [k]Pb，若S为无形远点，报错【vs.规范描述:h=k】
		BigInteger[] s;
		s = basicAgreement.calculateAgreement(publicKey);
		//[k]Pb=(x2,y2)
		byte[] x2 = Sm2Utils.bigIntegerToByteArray(s[0]);
		byte[] y2 = Sm2Utils.bigIntegerToByteArray(s[1]);
	
		//t = KDF(x2||y2, klen)
		byte[] z = new byte[x2.length + y2.length];
	    System.arraycopy(x2, 0, z, 0, x2.length);
	    System.arraycopy(y2, 0, z, x2.length, y2.length);
		KDFParameters kParam = new KDFParameters(z, null);
		byte[] t = KDFBytes(kParam, inLen);
		//C2=M(XOR)t
		byte[] C2 = new byte[inLen];
		for (int i = 0; i < inLen; i++) {
			C2[i] = (byte) (t[i] ^ input[inOff + i]);
		}
		//C3=Hash(x2||M||y2)
		byte[] C3 = new byte[digest.getDigestSize()];
		digest.update(x2, 0, x2.length);
		digest.update(input, inOff, inLen);
		digest.update(y2, 0, y2.length);
		digest.doFinal(C3, 0);
	
		//密文 C=C1||C2||C3,  无卡规范为： PC||C1||C3||C2
		byte[] PC = { (byte) 4 };
		byte[] C = combineByteArray(PC, x1, y1, C3, C2);
		return C;
	}

	/**
	 * SM2 解密
	 * @param input 密文
	 * @param inOff 位移
	 * @param inLen 密文长度
	 * @return 明文
	 */
	private byte[] DecryptBlock(byte[] inEnc, int inOff, int inLen) throws Exception {
		InputStream is = new ByteArrayInputStream(inEnc);
		// 04
		Byte PC = (byte) is.read();
		if (!PC.equals((byte)04)) {
			throw new InvalidCipherTextException("Invalid PC value.");
		}
		//C1=[k]G=(x1,y1)
		byte[] x1 = new byte[256 / 8];
		byte[] y1 = new byte[256 / 8];
		is.read(x1, 0, x1.length);
		is.read(y1, 0, y1.length);
		//验证C1是否买组椭圆曲线方程
		ECPublicKeyParameters tempPublic = Sm2Utils.sm2PubKeyGet(x1, y1);
		
		//C3=Hash(x2||M||y2)	
		byte[] C3 = new byte[digest.getDigestSize()];
		is.read(C3, 0, C3.length);
		
		//C2=M(XOR)t
		int msgLen = inLen - 1 - x1.length - y1.length - C3.length;
		byte[] C2 = new byte[msgLen];
		is.read(C2, 0, C2.length);
		
		//点 S=[k]C1【加密：S = [k]Pb】，若S为无形远点，报错【vs规范描述:h=k】
		basicAgreement.init(privateKey);
		BigInteger[] s = basicAgreement.calculateAgreement(tempPublic);		
		//点 [dB]C1=(x2,y2)【加密：[k]Pb=(x2,y2)】
		byte[] x2 = Sm2Utils.bigIntegerToByteArray(s[0]);
		byte[] y2 = Sm2Utils.bigIntegerToByteArray(s[1]);
		
		//t = KDF(x2||y2, klen)
		byte[] z = new byte[x2.length + y2.length];
	    System.arraycopy(x2, 0, z, 0, x2.length);
	    System.arraycopy(y2, 0, z, x2.length, y2.length);
		KDFParameters kParam = new KDFParameters(z, null);
		byte[] t = KDFBytes(kParam, inLen);
		
		//明文 M'=C2(XOR)t 【加密：C2=M(XOR)t】
		byte[] result = new byte[msgLen];
		for (int i = 0; i < msgLen; i++) {
			result[i] = (byte) (t[i] ^ C2[i]);
		}
		//C3=Hash(x2||M||y2)，并比较u和C3
		byte[] u = new byte[digest.getDigestSize()];
		digest.update(x2, 0, x2.length);
		digest.update(result, 0, result.length);
		digest.update(y2, 0, y2.length);
		digest.doFinal(u, 0);
	
		boolean check = Arrays.equals(u, C3);
		if (!check) {
			throw new InvalidCipherTextException("Invalid HASH VALUE.");
		}
		return result;
	}

	/**
	 * 密钥派生函数
	 * @param kParam
	 * @param length
	 * @return
	 */
	private byte[] KDFBytes(KDFParameters kParam, int length) {
		byte[] buf = new byte[length];
		kdf.init(kParam);
		kdf.generateBytes(buf, 0, buf.length);
		return buf;
	}
	
	/**
	 * 将多个字节数组拼凑成一个字节数组
	 * @param args 源字节数组
	 * @return 得到的字节数组
	 */
	public static byte[] combineByteArray(byte[]... args) {
		if (args == null || args.length == 0){
			return null;
		}

		int len = 0;
		for (int i = 0; i < args.length; i++) {
			if (args[i] == null){
				continue;
			}
			System.out.println("args[" + i + "]'s length:" + args[i].length);
			len += args[i].length;
		}
		
		byte[] ret = new byte[len];
		int pos = 0;
		for (int i = 0; i < args.length; i++) {
			if (args[i] == null || args[i].length == 0){
				continue;
			}
			System.arraycopy(args[i], 0, ret, pos, args[i].length);
			pos += args[i].length;
		}
		System.out.println("combine length:" + ret.length);
		return ret;
	}

}
