package dealType.frsDemo;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.crypto.CipherParameters;

import dealType.util.common.StringUtil;
import dealType.util.security.sm2.Sm2Utils;

/*处理交易证书秘钥字段工具类*/
public  class HandleCerKey {

	/**--------------------------------------------------------------------------------------------------*
	Name:		    handelEncKey
	Discribe:		使用银联提供的加密公钥对人脸对称密钥和敏感信息对称密钥进行加密再Base64，返回加密后的密钥（加密方式采用SM2国密算法）
	Parameter:      String encPubKey1, String encPubKey2, String fldValue
	Return:		    String
	**---------------------------------------------------------------------------------------------------*/
	public static String handelEncKey(String encPubKey1, String encPubKey2, String fldValue)
	{
		String encode = null;
		String Pubkey1 = encPubKey1;
		String Pubkey2 = encPubKey2;
		
		// 获取公钥
		CipherParameters publicKey = Sm2Utils.sm2PubKeyGet(StringUtil.hexStr2Bytes(Pubkey1), StringUtil.hexStr2Bytes(Pubkey2));
		
		byte[] srcHex = StringUtil.hexStr2Bytes(fldValue);
		
		// 使用公钥加密对称密钥（使用SM2国密算法）
		byte[] result = Sm2Utils.Encrypt(publicKey, srcHex);
		
		System.out.println(StringUtil.byte2HexStr(result));
		
		// base64编码
	    encode = Base64.encodeBase64String(result);
		
		return encode;
	}
}
