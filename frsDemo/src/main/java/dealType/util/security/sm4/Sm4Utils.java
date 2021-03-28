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
package dealType.util.security.sm4;

import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * SM4 对称、分组加密：置换替换
 */
public class Sm4Utils {

	
	/**
	 * SM4 Cbc模式 加密
	 * @param key 密钥
	 * @param data 明文
	 * @param padMode 填充模式
	 * @return 密文
	 */
	public static byte[] sm4CbcEncrypt(byte[] key, byte[] data, String padMode){
		byte[] res = null;
		String algorithm = "SM4/CBC/" + padMode;
		try {
			Security.addProvider(new BouncyCastleProvider());
			Cipher cipher = Cipher.getInstance(algorithm,"BC");  		
			SecretKeySpec secretKeySpec = getSm4Key(key);
			IvParameterSpec ivParameterSpec = getIv(cipher.getBlockSize());
			//byte[] padData = padding(data, cipher.getBlockSize());改为pkcs7填充 由bc来完成
			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
	        res = cipher.doFinal(data); 
	        return res;  
		} catch (Exception e) {
			System.out.println("Fail: Sm4 Cbc Encrypt"+e);
		}
		return res;
	}
	
	/**
	 * SM4 Cbc模式 加密
	 * @param key 密钥
	 * @param data 明文
	 * @param padMode 填充模式
	 * @return 密文
	 */
	public static byte[] sm4CbcEncrypt2(byte[] key, byte[] data, String padMode){
		System.out.println("begin1 sm4CbcEncrypt:");	
		byte[] res = null;
		String algorithm = "SM4/CBC/" + padMode;
		try {
			Security.addProvider(new BouncyCastleProvider());
			Cipher cipher = Cipher.getInstance(algorithm,"BC");  		
			SecretKeySpec secretKeySpec = getSm4Key(key);
			System.out.println("4");	
			IvParameterSpec ivParameterSpec = getIv(cipher.getBlockSize());
			System.out.println("5");	
			//byte[] padData = padding(data, cipher.getBlockSize());改为pkcs7填充 由bc来完成
			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
			System.out.println("6");	
	        res = cipher.doFinal(data); 
			System.out.println("7");	
	        return res;  
		} catch (Exception e) {
			System.out.println("Fail: Sm4 Cbc Encrypt"+e);
		}
		return res;
	}
	
	/**
	 * SM4 Cbc模式 解密
	 * @param key 密钥
	 * @param data 密文
	 * @param padMode 填充模式
	 * @return 明文
	 */
	public static byte[] sm4CbcDecrypt(byte[] key, byte[] data, String padMode){
		byte[] res = null;
		String algorithm = "SM4/CBC/" + padMode;
		try {
			Security.addProvider(new BouncyCastleProvider());
			Cipher cipher = Cipher.getInstance(algorithm,"BC");
			SecretKeySpec secretKeySpec = getSm4Key(key);
			IvParameterSpec ivParameterSpec = getIv(cipher.getBlockSize());
			//byte[] padData = padding(data, cipher.getBlockSize()); 改为pkcs7填充 由bc来完成
			cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
			res = cipher.doFinal(data); 
			return res;
		} catch (Exception e) {
			System.out.println("Fail: Sm4 Cbc Decrypt"+e);
		}
		return res;
	}
	
	/**
	 * SM4 Ecb模式 加密
	 * @param key 密钥
	 * @param data 明文
	 * @param padMode 填充模式
	 * @return 密文
	 */
	public static byte[] sm4EcbEncrypt(byte[] key, byte[] data, String padMode){
		byte[] res = null;
		String algorithm = "SM4/ECB/" + padMode;
		try {
			Security.addProvider(new BouncyCastleProvider());
			Cipher cipher = Cipher.getInstance(algorithm);
			SecretKeySpec secretKeySpec = getSm4Key(key);
			byte[] padData = padding(data, cipher.getBlockSize());
			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
			res = cipher.doFinal(padData);
		} catch (Exception e) {
			System.out.println("Fail: Sm4 Ecb Encrypt"+e);
		} 
		return res;
	}
	
	/**
	 * SM4 Ecb模式 解密
	 * @param key 密钥
	 * @param data 密文
	 * @param padMode 填充模式
	 * @return 明文
	 */
	public static byte[] sm4EcbDecrypt(byte[] key, byte[] data, String padMode) {	
		byte[] res = null;
		String algorithm = "SM4/ECB/" + padMode;
		try {
			Security.addProvider(new BouncyCastleProvider());
			Cipher cipher = Cipher.getInstance(algorithm);
			SecretKeySpec secretKeySpec = getSm4Key(key);
//			byte[] padData = padding(data, cipher.getBlockSize());
			cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
			res = cipher.doFinal(data);
		} catch (Exception e) {
			System.out.println("Fail: Sm4 Ecb Decrypt"+e);
		}
		return res;
	}

	/**
	 * 生成国密Key：SM4，密钥为 128bit， 16byte
	 */
	public static SecretKeySpec getSm4Key(byte[] key) {	
		if (key.length != 16) {
			System.out.println("SM4's key should be 16bytes, 128bits");
		}
		return new SecretKeySpec(key, "SM4");
	}
	
	/**
	 * 初始化向量
	 * @param len 长度
	 * @return
	 */
	public static IvParameterSpec getIv(int len) {
		//使用 IV 的例子是反馈模式中的密码，如，CBC 模式中的 DES 和使用 OAEP 编码操作的 RSA 密码
		byte[] zero = new byte[len];
		IvParameterSpec ivps = new IvParameterSpec(zero);
		return ivps;
	}

	/**
	 * 补足长度
	 * @param src
	 * @param len
	 * @return
	 */
	public static byte[] padding(byte[] src, int len) {
		int paddingLength = len - src.length % len;
		if (len == paddingLength) {
			return src;
		}
		byte[] newsrc = new byte[src.length + paddingLength];
		System.arraycopy(src, 0, newsrc, 0, src.length);
		for(int i = 0;i< paddingLength;i++)
		{
			newsrc[src.length+i] = (byte) 0xFF;
		}
		return newsrc;
	}

	
}
