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

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithRandom;

public class Sm2BasicAgreement {

	protected ECPrivateKeyParameters privKey;

	public void init(CipherParameters parameters) {
		if (parameters instanceof ParametersWithRandom) {
			parameters = ((ParametersWithRandom) parameters).getParameters();
		}
		this.privKey = (ECPrivateKeyParameters) parameters;
	}

	public int getFieldSize() {
		return (privKey.getParameters().getCurve().getFieldSize() + 7) / 8;
	}

	public BigInteger[] calculateAgreement(CipherParameters pubKey) throws Exception {
		ECPublicKeyParameters pub = (ECPublicKeyParameters) pubKey;
		if (!pub.getParameters().equals(privKey.getParameters())) {
			throw new Exception("ECDH public key has wrong domain parameters");
		}

		org.bouncycastle.math.ec.ECPoint P = pub.getQ().multiply(privKey.getD()).normalize();
		if (P.isInfinity()) {
			throw new Exception("Infinity is not a valid agreement value for ECDH");
		}
		return new BigInteger[] { P.getAffineXCoord().toBigInteger(), P.getAffineYCoord().toBigInteger() };
	}
}
