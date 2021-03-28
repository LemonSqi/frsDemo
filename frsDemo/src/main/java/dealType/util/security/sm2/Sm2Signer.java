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

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.SecureRandom;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.crypto.params.ECKeyParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.math.ec.ECAlgorithms;
import org.bouncycastle.math.ec.ECPoint;

public class Sm2Signer {

	private Boolean forSigning;
	private ECKeyParameters key;
	private SecureRandom random;

	public String getAlgorithmName() {
		return "Sm2Signer";
	}

	public void init(boolean forSigning, CipherParameters parameters) throws InvalidKeyException {
		this.forSigning = forSigning;
		if (forSigning)// 签名
		{
			if (parameters instanceof ParametersWithRandom) {
				ParametersWithRandom rParam = (ParametersWithRandom) parameters;
				this.random = rParam.getRandom();
				parameters = rParam.getParameters();
			} else {
				this.random = new SecureRandom();
			}

			if (!(parameters instanceof ECPrivateKeyParameters)) {
				throw new InvalidKeyException("EC private key required for signing");
			}
			this.key = (ECPrivateKeyParameters) parameters;
		} else {// 验签
			if (!(parameters instanceof ECPublicKeyParameters)) {
				throw new InvalidKeyException("EC public key required for verification");
			}
			this.key = (ECPublicKeyParameters) parameters;
		}
	}

	public BigInteger[] generateSignature(byte[] message) throws Exception {
		if (!this.forSigning) {
			throw new Exception("not initialised for signing");
		}
		// 阶n
		BigInteger n = ((ECPrivateKeyParameters) this.key).getParameters().getN();
		int nBitLength = n.bitLength();
		// M_=ZA||M，e=Hv(M_)
		BigInteger e = new BigInteger(1, message);
		int eBitLength = e.bitLength();

		ECPrivateKeyParameters privKey = (ECPrivateKeyParameters) key;
		if (eBitLength > nBitLength) {
			throw new DataLengthException("input too large for ECNR key.");
		}
		BigInteger r = null;
		BigInteger s = null;
		do {	//判断s
			BigInteger k;	// 随机数 k∈[1,n-1], (x1,y1)=[k]G
			AsymmetricCipherKeyPair tempPair;
			do {// r=(e+x1) mod n
				//generate another, but very temporary, key pair using, the same EC parameters
				//生成随机数k，及(x1,y1),借用用户密钥对方式生成：PA=[dA]G={xA,yA}
				ECKeyPairGenerator keyGen = new ECKeyPairGenerator();
				keyGen.init(new ECKeyGenerationParameters(privKey.getParameters(), this.random));
				tempPair = keyGen.generateKeyPair();
				k = ((ECPrivateKeyParameters) tempPair.getPrivate()).getD();
				ECPublicKeyParameters V = (ECPublicKeyParameters) tempPair.getPublic();
				BigInteger Vx = V.getQ().getAffineXCoord().toBigInteger();
				r = Vx.add(e).mod(n);
			} while (r.signum() == 0 || r.equals(BigInteger.ZERO) || r.add(k).equals(n));
			// s=((1/(1+dA)·(k-r·dA)) mod n
			BigInteger dA = privKey.getD(); // private key value

			BigInteger tmp = dA.add(BigInteger.ONE).modInverse(n);// 1/(1+dA)
			BigInteger tmp2 = k.subtract(r.multiply(dA));// (k-r·dA)
			s = tmp.multiply(tmp2).mod(n);
		} while (s.equals(BigInteger.ZERO));
		return new BigInteger[] { r, s };
	}

	public boolean verifySignature(byte[] message, BigInteger r, BigInteger s) throws Exception {
		if (this.forSigning) {
			throw new Exception("not initialised for verifying");
		}
		ECPublicKeyParameters pubKey = (ECPublicKeyParameters) key;
		// 阶n
		BigInteger n = pubKey.getParameters().getN();
		int nBitLength = n.bitLength();
		// M_'=ZA||M'，e'=Hv(M_')
		BigInteger e = new BigInteger(1, message);
		
		int eBitLength = e.bitLength();
		if (eBitLength > nBitLength) {
			throw new DataLengthException("input too large for ECNR key.");
		}
		// r'∈[1,n-1]
		if (r.compareTo(BigInteger.ONE) < 0 || r.compareTo(n) >= 0) {
			return false;
		}
		// s' ∈ [1,n-1] NB: ECNR spec says 0
		if (s.compareTo(BigInteger.ONE) < 0 || s.compareTo(n) >= 0) {
			return false;
		}
		
		// t=(r'+s') mod n
		BigInteger t = r.add(s).mod(n);
		//不用判断t?
		if (t.compareTo(BigInteger.ZERO) == 0) {
			return false;
		}
		// (x1',y1')=[s']G+[t]PA, compute P = sG + rW
		ECPoint G = pubKey.getParameters().getG();
		ECPoint W = pubKey.getQ();
		// calculate P using Bouncy math
		ECPoint P = ECAlgorithms.sumOfTwoMultiplies(G, s, W, t).normalize();
		if (P.isInfinity())
			return false;
		//R=(e'+x1') mod n
		BigInteger x = P.getAffineXCoord().toBigInteger();
		BigInteger R = e.add(x).mod(n);
		return R.equals(r);
	}
}
