package dealType.frsDemo;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.crypto.CipherParameters;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import dealType.util.common.StringUtil;
import dealType.util.security.sm2.Sm2Utils;

public class HandleSignature {

	/**--------------------------------------------------------------------------------------------------*
	Name:		    getSign
	Discribe:		计算签名（对明文先做一次摘要，然后对第一次的摘要连同userId和公钥通过SM3再计算一次摘要，然后再使用私钥通过SM2加密生成签名）
	Parameter:      JSONObject reqJson, String signPriKey, String signCertId, String signPubKey
	Return:		    String
	**---------------------------------------------------------------------------------------------------*/
	public static String getSign(JSONObject reqJson, String signPriKey, String signCertId, String signPubKey) {

		// 获取明文
		String signBString = reqJson.toJSONString();

		// 获取密钥
		CipherParameters privateKey = Sm2Utils.sm2PriKeyGet(signPriKey);

		String sign = null;
		
		// 获取签名（先对摘要-userId-公钥使用SM3计算一次摘要，然后再使用私钥通过SM2计算得到签名信息）
		byte[] result = Sm2Utils.SignWithSm3(privateKey, signCertId.getBytes(), signBString.getBytes(), StringUtil.hexStr2Bytes(signPubKey));
		
		// base64编码
		sign = Base64.encodeBase64String(result);

		return sign;
	}

	/**--------------------------------------------------------------------------------------------------*
	Name:		    verifySign
	Discribe:		验签（将签名串同公钥计算获得摘要，然后通过原串解析出来的摘要连同userId以及公钥一起计算一次摘要，比对两个摘要是否一致，一致则验签成功，否则失败）
	Parameter:      String strEncryptJson, String userId, String verifySignPubKey, String signFldName
	Return:		    boolean
	**---------------------------------------------------------------------------------------------------*/
	public static boolean verifySign(String strEncryptJson, String userId, String verifySignPubKey, String signFldName) {

		JSONObject jsonObject = JSON.parseObject(strEncryptJson);
		
		if(jsonObject == null) 
			return false;

		if(!jsonObject.containsKey(signFldName)) 
			return false;

		String rspSign = jsonObject.getString(signFldName);

		// 获取签名串
		int pos1 = strEncryptJson.indexOf("signature") - 2;
		String rsp_1 = strEncryptJson.substring(0, pos1);
		String rsp_2 = strEncryptJson.substring(pos1+1);
		String rsp_3 = rsp_2.substring(rsp_2.indexOf("\"", 13)+1);
		String signBString = rsp_1 + rsp_3;

		// base64解码
		byte[] desrcHex = Base64.decodeBase64(rspSign);

		boolean verify = false;

		// 获取签名公钥
		CipherParameters publicKey = Sm2Utils.sm2PubKeyGet(StringUtil.hexStr2Bytes(verifySignPubKey.substring(0, 32)), StringUtil.hexStr2Bytes(verifySignPubKey.substring(32)));

		// 验签
		verify = Sm2Utils.VerifyWithSm3(publicKey, StringUtil.hexStr2Bytes(userId), signBString.getBytes(), desrcHex);

		return verify;

	}

}
