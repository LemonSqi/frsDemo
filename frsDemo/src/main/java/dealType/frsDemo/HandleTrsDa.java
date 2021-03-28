package dealType.frsDemo;

import java.util.Random;

import org.apache.commons.codec.binary.Base64;

import dealType.util.common.Constants;
import dealType.util.common.StringUtil;
import dealType.util.security.sm3.Sm3Utils;
import dealType.util.security.sm4.Sm4Utils;

/*处理交易数据特殊字段工具类*/
public class HandleTrsDa {

	/**--------------------------------------------------------------------------------------------------*
	Name:		    handlePic
	Discribe:		处理人脸路由照片(pic中配置的是二进制图片base64后的结果,因此处理过程先base64解码， 要按照规范用sm4对称加密后再base64编码)
	Parameter:      String enc_key_pic, String pic
	Return:		    String
	 **---------------------------------------------------------------------------------------------------*/
	public static String handlePic(String enc_key_pic, String pic)
	{
		String encode = null;

		// base64解码(获取图片二进制原字节流)
		byte picDa [] = Base64.decodeBase64(pic);

		// 获取人脸对称密钥字节流
		byte key [] = StringUtil.hexStringToByte(enc_key_pic);

		// 使用SM4加密 （padding 方式PAD_PKCS7）
		byte[] result = Sm4Utils.sm4CbcEncrypt(key, picDa, Constants.PAD_PKCS7);

		// base64编码	
		encode = Base64.encodeBase64String(result);

		return encode;
	}

	/**--------------------------------------------------------------------------------------------------*
	Name:		    handelIndex
	Discribe:		处理路由索引(先通过sm3计算摘要，再与交易日期时间异或后，使用敏感信息对称密要经sm4加密，最后再base64编码)
	Parameter:      String enc_key_sen, String timeStamp, String trx_tp, String pin
	Return:		    String
	 **---------------------------------------------------------------------------------------------------*/
	public static String handelIndex(String enc_key_sen, String timeStamp, String trx_tp, String pin)
	{
		String encode = null;

		// 获取敏感信息对称密钥
		byte key [] = StringUtil.hexStringToByte(enc_key_sen);

		// 人脸路由信息同步（新增、更改 ）
		if("FPR00101".equalsIgnoreCase(trx_tp) || "FPR00102".equalsIgnoreCase(trx_tp)) {
			// SM3计算摘要
			byte[] sm3rlst = Sm3Utils.digest(pin.getBytes());

			System.out.println(StringUtil.byte2HexStr(sm3rlst)); 

			// 获取时间 并左补0 准备进行异或计算
			String tmpStp = "000000000000000000" + timeStamp;

			// 获取异或时间字节流
			byte[] tmp = tmpStp.getBytes();

			// 异或计算
			byte[] xorRlt = new byte[sm3rlst.length];
			for (int i = 0; i < sm3rlst.length; i++) {
				xorRlt[i] = (byte) (sm3rlst[i] ^ tmp[i]);
			}

			// 对异或后结果进行加密 
			byte[] result = Sm4Utils.sm4CbcEncrypt(key, xorRlt, Constants.PAD_NO);

			//System.out.println(StringUtil.byte2HexStr(result).length());

			// base64编码
			encode = Base64.encodeBase64String(result);
		}

		return encode;
	}

	/**--------------------------------------------------------------------------------------------------*
	Name:		    handelSenseInf
	Discribe:		处理敏感信息(由卡号、身份证号和完整姓名组成，用敏感信息对称密钥经SM4加密，再base64编码)
	Parameter:      String enc_key_sen, String trx_tp, String inf
	Return:		    String
	 **---------------------------------------------------------------------------------------------------*/
	public static String handelSenseInf(String enc_key_sen, String trx_tp, String inf)
	{
		String encode = "";
		String idInf = "";
		String cardNo = "";		// 卡号
		String IDNo = ""; 		// 身份证号
		String Name = "";       // 完整姓名
		String ret = "";

		byte key [] = StringUtil.hexStringToByte(enc_key_sen);

		if("FPR00101".equalsIgnoreCase(trx_tp) || "FPR00102".equalsIgnoreCase(trx_tp) ||
				"FPR00103".equalsIgnoreCase(trx_tp) || "FPR00202".equalsIgnoreCase(trx_tp)) {
			cardNo = inf.substring(0, inf.indexOf(",") + 1); 	// 卡号带上后面的,
			IDNo = inf.substring(inf.indexOf(",") + 1, inf.indexOf("."));  // 身份证
			Name = inf.substring(inf.indexOf(".") + 1);	 // 完整姓名

			byte[] b_card_no = cardNo.getBytes();

			idInf += Name + sblank(30 - Name.length()) + IDNo + sblank(20 - IDNo.length());

			// 计算身份信息摘要
			byte[] sm3Rlt = Sm3Utils.digest(idInf.getBytes());
			
			System.out.println(StringUtil.byte2HexStr(sm3Rlt));

			byte[] merge = new byte[b_card_no.length + sm3Rlt.length];

			System.arraycopy(b_card_no, 0, merge, 0, b_card_no.length);
			System.arraycopy(sm3Rlt, 0, merge, b_card_no.length, sm3Rlt.length);

			byte[] result = Sm4Utils.sm4CbcEncrypt(key, merge, Constants.PAD_PKCS7);

			encode = Base64.encodeBase64String(result);
		}
		else if ("FPR00201".equalsIgnoreCase(trx_tp)) //人脸路由信息查询 请求仅上传了身份信息摘要
		{
			IDNo = inf.substring(inf.indexOf(",") + 1, inf.indexOf("."));  // 身份证
			Name = inf.substring(inf.indexOf(".") + 1);	 // 完整姓名

			idInf += Name + sblank(30 - Name.length()) + IDNo + sblank(20 - IDNo.length());

			byte[] sm3Rlt = Sm3Utils.digest(idInf.getBytes());

			byte[] result = Sm4Utils.sm4CbcEncrypt(key, sm3Rlt, Constants.PAD_PKCS7);

			encode = Base64.encodeBase64String(result);
		}

		return encode;
	}

	/**--------------------------------------------------------------------------------------------------*
	Name:		    handlePin
	Discribe:		人脸识别线下支付口令(由"口令加密因子"和人脸识别线下支付口令异或后得到PinBlock,再SM4加密得到128bit,再Base64编码，当路由方式为"1"或"2"时出现)
	Parameter:      String enc_key_sen, String fldValue, String xor_salt
	Return:		    String
	 **---------------------------------------------------------------------------------------------------*/
	public static String handlePin(String enc_key_sen, String pin, String xor_salt)
	{
		String encode = null;

		// 获取敏感数据密钥字节流
		byte key [] = StringUtil.hexStringToByte(enc_key_sen);

		//按照卡号要求进行截取
		String Strpanblock = xor_salt.substring(xor_salt.length()-1-12, xor_salt.length()-1);

		byte[] panBlock = new byte[16];
		System.arraycopy(StringUtil.hexStr2Bytes(Strpanblock), 0, panBlock, 10, 6);
		
		System.out.println(StringUtil.byte2HexStr(panBlock));
		

		String Spinblock = "06" + pin + "FFFFFFFFFFFFFFFFFFFFFFFF";

		byte[] pinBlock = StringUtil.hexStr2Bytes(Spinblock);

		// 异或
		byte[] xorRlt = new byte[16];
		for (int i = 0; i < xorRlt.length; i++) {
			xorRlt[i] = (byte) (panBlock[i] ^ pinBlock[i]);
		}
		
		System.out.println(StringUtil.byte2HexStr(xorRlt));

		//使用SM4加密(padding 方式PAD_NO) 
		byte[] result = Sm4Utils.sm4CbcEncrypt(key, xorRlt, Constants.PAD_NO);

		//base64编码	
		encode = Base64.encodeBase64String(result);

		return encode;
	}

	public static String sblank(final int n) 
	{
		String res = "";
		for (int i = 0; i < n; ++i)
			res += " ";
		return res;
	}

//	public static void main(String[] args) 
//	{
//		String pk = "ABCDEFABCDEFABCD1234567890123456";
//		
//		String pin = "123456";
//		// 口令加密因子 (yyMMDDhhmmss + 6位随机数代替主账号生成PinBlock)
//		String timeStamp = "20190629203630";
//		int radom = new Random().nextInt(999999);
//		if (radom < 100000) radom += 100000;
//		
//		radom = 138239;
//		
//		String xor_salt = timeStamp + String.valueOf(radom);
//		String ret = handlePin(pk, pin, xor_salt);
//
//		System.out.println(ret);
//
//	}
	
	public static void main(String[] args) 
	{
		String pk = "9407FDCA35C28A1087B29C7605B3BB56";
		String trx = "FPR00101";
		String pin = "158247";
		String inf = "6217905300000444017,31010119890909416X.JACKJONES";
		// 口令加密因子 (yyMMDDhhmmss + 6位随机数代替主账号生成PinBlock)
		String timeStamp = "20190505200010";
		
		String ret = handelSenseInf(pk, trx, inf);

		//System.out.println(ret);

	}

}
