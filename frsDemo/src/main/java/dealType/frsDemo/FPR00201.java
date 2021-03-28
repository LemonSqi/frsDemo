/*****************************************************************************
 ** File Name:        FPR00201
 ** Author:           SqWang
 ** Date:             2019/06/21
 ** Version:          1.0.0
 ** Description:      查询人脸路由信息
 ******************************************************************************
 **                         Important Edit History                            *
 ** --------------------------------------------------------------------------*
 ** DATE           		NAME             				DESCRIPTION           *
 ** 2019/05/10	     	SqWang                 	        Create                *
 ** --------------------------------------------------------------------------*
 ******************************************************************************
 **                         Important Edit Update                             *
 ** --------------------------------------------------------------------------*
 ** DATE           		NAME             				DESCRIPTION           *
 ** 2019/06/21     		SqWang                       	ReqJson               *
 ** --------------------------------------------------------------------------*
 ** Copyright(c) 2019, SqWang in China Unionpay Corporation
 ** All rights reserved.
 ** Distributed under the BSD license.
 ******************************************************************************
 ** 1. China UnionPay Co., Ltd. (hereinafter referred to as “China UnionPay”) 
 ** reserves all intellectual property rights in this code, including but not 
 ** limited to copyright, patents, trademarks, trade secrets, etc. Any use of 
 ** this code by anyone is subject to the China UnionPay member institution 
 ** service platform (http://member.unionpay.com/) and China Silver. The 
 ** provisions of the agreement signed by the Joint Commission. China UnionPay 
 ** is not responsible for any errors or omissions in the code and any 
 ** resulting losses. China UnionPay Needs to give up all the code Warranty, 
 ** express or implied, including but not limited to, not infringing third 
 ** party intellectual property rights.
 **	2. You may not use the code for purposes and purposes other than those 
 ** with China UnionPay without the written consent of China UnionPay. May 
 ** not be downloaded without the written consent of China UnionPay.
 **	Forward, publicly, or in any other form provide the code to a third party. 
 ** If you obtain the code through an illegal channel, please delete it 
 ** immediately and pass the legal channel to Bank of China.
 **	3. China UnionPay does not make any statement or responsibility on whether 
 **	the code or its related documents involve third-party intellectual property 
 ** rights (such as encryption algorithms may be protected by patents in some 
 ** countries).
 **	China UnionPay does not assume any responsibility for the use of this code 
 ** infringing the rights of third parties, including but not limited to the 
 **	use of some or all of the code.
 ******************************************************************************/

package dealType.frsDemo;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.alibaba.fastjson.JSONObject;

public class FPR00201 
{ 	
	static String trxDaFldName = "trx_data";    // 交易数据字段
	static String cerKeyFldName = "cert_key";   // 证书和秘钥字段
	static String signFldName = "signature";    // 签名字段

	static JSONObject reqJson = new JSONObject(true); 			// 请求报文
	static JSONObject trxDaJson = new JSONObject(true);			// 交易字段子Json
	static JSONObject cerKeyJson = new JSONObject(true);		// 证书秘钥子Json

	/**--------------------------------------------------------------------------------------------------*
	Name:		    setReqJson
	Discribe:		配置请求报文
	Parameter:      void
	Return:		    void
	 **---------------------------------------------------------------------------------------------------*/
	public static void setReqJson() {

		/* ---------------------------------------添加最外层Json---------------------------------------- */	
		reqJson.put("ver_no", "1.0.0");                     // 报文版本号
		String time_stamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());   
		String strSeq = "00000000000000001";  				// 报文发送方提供的17位序列号 

		reqJson.put("seq_id", time_stamp + "0" + strSeq);  	// 交易流水号
		reqJson.put("time_stamp", time_stamp);				// 14位交易时间戳
		reqJson.put("access_id", "00010001");               // 接入方ID
		String trx_tp = "FPR00201";
		reqJson.put("trx_tp", trx_tp);                  	// 交易类型

		/* -----------------------------------添加交易数据到内层Json------------------------------------ */
		reqJson.put(trxDaFldName, trxDaJson);

		String enc_key_sen = "ABCDEFABCDEFABCD1234567890123456"; 	// 敏感信息对称密钥（需要使用银联提供的加密公钥经SM2算法进行加密）                                               

		// 敏感信息（由卡号和身份证号组成，逗号隔开，用敏感信息对称密钥经SM4加密，再base64编码）
		String inf = "6217905300000444017,310101198909094166.JackJones";  // 卡号,身份证号.完整姓名

		trxDaJson.put("sense_inf", HandleTrsDa.handelSenseInf(enc_key_sen, trx_tp, inf));                                  

		/* ------------------------------------内层证书秘钥子Json------------------------------------ */
		reqJson.put(cerKeyFldName, cerKeyJson);

		String sign_certID = "4000370700";
		cerKeyJson.put("sign_certID", sign_certID);      // 签名证书序列号
		cerKeyJson.put("enc_certID", "4000370700");      // 加密证书序列号

		/* 银联提供的加密公钥  */
		String encPubKey1 = "AF0EA01E61236C863009B4174D1EC550DE327DB602AE49A29EBAA4C2583E6443";	  // 加密公钥前半部分
		String encPubKey2 = "BAC6735F06888D4516484D5BFC575EE7F5E8B6DD7F5BDC0D172B2568148A2F2E";	  // 加密公钥后半部分

		cerKeyJson.put("enc_key_sen", HandleCerKey.handelEncKey(encPubKey1, encPubKey2, enc_key_sen));  // 敏感信息对称密钥（需要使用银联提供的加密公钥经SM2算法进行加密）

		reqJson.put("req_resv", "####");           

		// 签名字段
		String signPriKey = "9DB5DB4C74141AB033132C1EB11ED6A5C77C0F7652DF0715A6735D606A61FCE5";  // 签名密钥
		String signPubKey = "AF0EA01E61236C863009B4174D1EC550DE327DB602AE49A29EBAA4C2583E6443BAC6735F06888D4516484D5BFC575EE7F5E8B6DD7F5BDC0D172B2568148A2F2E"; // 签名公钥
		String signature = HandleSignature.getSign(reqJson, signPriKey, sign_certID, signPubKey);
		reqJson.put("signature", signature);

	}

	/**--------------------------------------------------------------------------------------------------*
	Name:		    sendPost
	Discribe:		http请求报文发送(Post方法)
	Parameter:      void
	Return:		    void
	 **---------------------------------------------------------------------------------------------------
	 * @throws Exception */
	public static String sendPost() throws Exception {

		String reqUrl = "http://172.21.53.53:22022";

		HttpClient httpClient = new HttpClient(reqUrl, 30000, 40000);

		String rsp = httpClient.send(reqJson.toString(), "GB18030");

		return rsp;
	}

	/**--------------------------------------------------------------------------------------------------*
	Name:		    main
	Discribe:		测试主函数
	Parameter:      String[] args
	Return:		    void
	**---------------------------------------------------------------------------------------------------*/
	public static void main(String[] args) throws Exception {

		setReqJson();
		
		// 打印请求报文
		System.out.println(reqJson.toJSONString());

		String rsp = sendPost();
		
		// 打印应答报文
		System.out.println(rsp);
	}
}

