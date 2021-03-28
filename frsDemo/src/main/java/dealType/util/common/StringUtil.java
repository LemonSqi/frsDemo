package dealType.util.common;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class StringUtil {
	
	 public static final boolean isEmpty(String value) {
	        boolean ret = true;
	        if (value != null && value.trim().length() > 0) {
	            ret = false;
	        }
	        return ret;
	    }
	

	public static boolean isNumber(String s) {

		try {
			Float.parseFloat(s);
		} catch (Exception ex) {
			return false;
		}
		return true;
	}

	/**
	 * 比较第一个数组中的 元素是否和 bt2 中相同 （从srcPos开始比较）
	 * 
	 * @param src
	 *            被比较的byte数组
	 * @param srcPos
	 *            开始点
	 * @param bt2
	 *            目标数组
	 * @param length
	 *            长度
	 * @return
	 */
	public static boolean isEqualsByte(byte[] src, int srcPos, byte[] bt2,
			int length) {

		byte[] temp = new byte[length];
		System.arraycopy(src, srcPos, temp, 0, length);

		return Arrays.equals(temp, bt2);

	}

	/**
	 * 字符串格式化为日期时间格式
	 * 
	 * @param format
	 *            原来格式 yyyyMMdd HHmmss
	 * @param toformat
	 *            目标格式 yyyy-MM-dd HH:mm:ss
	 * @param time
	 *            时间或日期
	 * @return 目标日期时间字符串
	 */
	public static String str2DateTime(String format, String toformat,
			String time) {
		String str = "";
		Date date;

		try {
			date = new SimpleDateFormat(format).parse(time);
			str = new SimpleDateFormat(toformat).format(date);
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}

		return str;

	}

	/**
	 * short转换为字节
	 * 
	 * @param s
	 * @return
	 */
	public static byte[] shortToByteArray(short s) {
		byte[] targets = new byte[2];
		// for (int i = 0; i < 2; i++) {
		// int offset = (targets.length - 1 - i) * 8;
		// targets[i] = (byte) ((s >>> offset) & 0xff);
		// }
		targets[0] = (byte) (s & 0x00ff);
		targets[1] = (byte) ((s & 0xff00) >> 8);
		return targets;
	}

	/**
	 * short转换为字节
	 * 
	 * @param s
	 * @return
	 */
	public static byte[] shortToByteArrayTwo(short s) {
		byte[] targets = new byte[2];
		// for (int i = 0; i < 2; i++) {
		// int offset = (targets.length - 1 - i) * 8;
		// targets[i] = (byte) ((s >>> offset) & 0xff);
		// }
		targets[1] = (byte) (s & 0x00ff);
		targets[0] = (byte) ((s & 0xff00) >> 8);
		return targets;
	}

	/**
	 * short[]转换为字节[]
	 * 
	 * @param s
	 * @return
	 */
	public static byte[] shortArrayToByteArray(short[] s) {
		byte[] targets = new byte[s.length * 2];
		for (int i = 0; i < s.length; i++) {
			byte[] tmp = shortToByteArray(s[i]);

			targets[2 * i] = tmp[0];
			targets[2 * i + 1] = tmp[1];
		}
		return targets;
	}

	/**
	 * byte[]到Short
	 * 
	 * @param buf
	 * @return
	 */
	public static short[] byteArraytoShort(byte[] buf) {
		short[] targets = new short[buf.length / 2];
		short vSample;
		int len = 0;
		for (int i = 0; i < buf.length; i += 2) {
			vSample = (short) (buf[i] & 0x00FF);
			vSample |= (short) ((((short) buf[i + 1]) << 8) & 0xFF00);
			targets[len++] = vSample;
		}
		return targets;
	}

	/**
	 * 字符串转换成十六进制字符串
	 * 
	 * @param String
	 *            str 待转换的ASCII字符串
	 * @return String 每个Byte之间空格分隔，如: [61 6C 6B]
	 */
	public static String str2HexStr(String str) {

		char[] chars = "0123456789ABCDEF".toCharArray();
		StringBuilder sb = new StringBuilder("");
		byte[] bs = str.getBytes();
		int bit;

		for (int i = 0; i < bs.length; i++) {
			bit = (bs[i] & 0x0f0) >> 4;
			sb.append(chars[bit]);
			bit = bs[i] & 0x0f;
			sb.append(chars[bit]);
			sb.append(' ');
		}
		return sb.toString().trim();
	}

	/**
	 * 十六进制转换字符串
	 * 
	 * @param String
	 *            str Byte字符串(Byte之间无分隔符 如:[616C6B])
	 * @return String 对应的字符串
	 */
	public static String hexStr2Str(String hexStr) {
		String str = "0123456789ABCDEF";
		char[] hexs = hexStr.toCharArray();
		byte[] bytes = new byte[hexStr.length() / 2];
		int n;

		for (int i = 0; i < bytes.length; i++) {
			n = str.indexOf(hexs[2 * i]) * 16;
			n += str.indexOf(hexs[2 * i + 1]);
			bytes[i] = (byte) (n & 0xff);
		}
		try {
			return new String(bytes, "ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
		return "";
	}

	/**
	 * bytes转换成十六进制字符串
	 * 
	 * @param byte[] b byte数组
	 * @return String 每个Byte值之间空格分隔
	 */
	public static String byte2HexStr(byte[] b) {
		if(b == null)
			return "";
		String stmp = "";
		StringBuilder sb = new StringBuilder("");
		for (int n = 0; n < b.length; n++) {
			stmp = Integer.toHexString(b[n] & 0xFF);
			sb.append((stmp.length() == 1) ? "0" + stmp : stmp);
			// sb.append(" ");
		}
		return sb.toString().toUpperCase().trim();
	}

	/**
	 * bytes字符串转换为Byte值
	 * 
	 * @param String
	 *            src Byte字符串，每个Byte之间没有分隔符
	 * @return byte[]
	 */
	public static byte[] hexStr2Bytes(String src) {
		int m = 0, n = 0;
		if((src.length() & 1) != 0)
			src = "0" + src;
		int l = src.length() / 2;

		byte[] ret = new byte[l];
		for (int i = 0; i < l; ++i) {
			m = i * 2 + 1;
			n = m + 1;
			ret[i] = Integer.decode(
					"0x" + src.substring(i * 2, m) + src.substring(m, n))
					.byteValue();
		}
		return ret;
	}

	/**
	 * String的字符串转换成unicode的String
	 * 
	 * @param String
	 *            strText 全角字符串
	 * @return String 每个unicode之间无分隔符
	 * @throws Exception
	 */
	public static String strToUnicode(String strText) throws Exception {
		char c;
		StringBuilder str = new StringBuilder();
		int intAsc;
		String strHex;
		for (int i = 0; i < strText.length(); i++) {
			c = strText.charAt(i);
			intAsc = (int) c;
			strHex = Integer.toHexString(intAsc);
			if (intAsc > 128)
				str.append("\\u" + strHex);
			else
				// 低位在前面补00
				str.append("\\u00" + strHex);
		}
		return str.toString();
	}

	/**
	 * unicode的String转换成String的字符串
	 * 
	 * @param String
	 *            hex 16进制值字符串 （一个unicode为2byte）
	 * @return String 全角字符串
	 */
	public static String unicodeToString(String hex) {
		int t = hex.length() / 6;
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < t; i++) {
			String s = hex.substring(i * 6, (i + 1) * 6);
			// 高位需要补上00再转
			String s1 = s.substring(2, 4) + "00";
			// 低位直接转
			String s2 = s.substring(4);
			// 将16进制的string转为int
			int n = Integer.valueOf(s1, 16) + Integer.valueOf(s2, 16);
			// 将int转换为字符
			char[] chars = Character.toChars(n);
			str.append(new String(chars));
		}
		return str.toString();
	}

	/**
	 * @author Junhua Wu
	 * @param src
	 *            byteêy×é 3¤?èó|μ±?a4
	 * @return ×a??oóμ???Dí?á1? ×a??°′??D???×??úDòà′?D?¨
	 */
	public static int byteToInt(byte[] src) {
		int tmp = 0;
		for (int i = 0; i < src.length; i++) {
			tmp += ((int) src[i] << (i * 8)) & (0xFF << (i * 8));
		}

		return tmp;
	}

	/**
	 * @author Junhua Wu
	 * @param src
	 *            ??Díêy?Y
	 * @return ×a??oóμ?byteêy×é?á1? ×a??°′??D???×??úDòà′?D?¨
	 */
	public static byte[] intToByte(int src) {
		byte[] tmp = new byte[4];
		for (int i = 0; i < tmp.length; i++) {
			tmp[i] = (byte) (((int) src >> (i * 8)) & 0xFF);
		}
		return tmp;
	}

	public static byte[] intToByte1024(int src) {
		byte[] tmp = new byte[1024];
		for (int i = 0; i < tmp.length; i++) {
			tmp[i] = (byte) (((int) src >> (i * 8)) & 0xFF);
		}
		return tmp;
	}


	public static String byteTostrGBK(byte[] data) {
		String result = "";
		try {

			result = new String(data, "GBK");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}
    /* *
     * 
     * 把16进制字符串转换成字节数组 @param hex @return 
     */  
    public static byte[] hexStringToByte(String hex) {  
        int len = (hex.length() / 2);  
        byte[] result = new byte[len];  
        char[] achar = hex.toCharArray();  
        for (int i = 0; i < len; i++) {  
            int pos = i * 2;  
            result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));  
        }  
        return result;  
    }  
  
    private static byte toByte(char c) {  
        byte b = (byte) "0123456789ABCDEF".indexOf(c);  
        return b;  
    }  
  
    /** 
     * 把字节数组转换成16进制字符串 
     *  
     * @param bArray 
     * @return 
     */  
    public static final String bytesToHexString(byte[] bArray) {  
        StringBuffer sb = new StringBuffer(bArray.length);  
        String sTemp;  
        for (int i = 0; i < bArray.length; i++) {  
            sTemp = Integer.toHexString(0xFF & bArray[i]);  
            if (sTemp.length() < 2)  
                sb.append(0);  
            sb.append(sTemp.toUpperCase());  
        }  
        return sb.toString();  
    }  
    
    public static final String bytesToBcdString(byte[] bArray) {
        StringBuffer sb = new StringBuffer(bArray.length);  
        for (int i = 0; i < bArray.length; i++) {  
            char first=(char) (0x30&((0xF0&bArray[i])>>4));
        	char second=(char) (0x30&(0x0F&bArray[i]));
            sb.append(first);  
            sb.append(second);  
        }  
        return sb.toString(); 
    }
    public static byte[] bcdStr2Bytes(String src) {
		int m = 0, n = 0;
		if((src.length()%2)!=0)
			src = src+"F";
		int l = src.length() / 2;
		//System.out.println(l);
		byte[] ret = new byte[l];
		for (int i = 0; i < l; i++) {
			m = i * 2 + 1;
			n = m + 1;
			ret[i] = Integer.decode(
					"0x" + src.substring(i * 2, m) + src.substring(m, n))
					.byteValue();
		}
		return ret;
	}
    public static byte[] intToByteArray(int i) {
		byte[] targets = new byte[4];
		targets[0] = (byte) (i & 0xFF);
		targets[1] = (byte) ((i>>8) & 0xFF);
		targets[2] = (byte) ((i>>16) & 0xFF);
		targets[3] = (byte) ((i>>24) & 0xFF);
		return targets;
	}
    
   /**
    * 把字节数组转化成int类型，小端模式
    * @param b
    * @return
    */
   public static int byteArrayToInt(byte[] b) {
		int result = 0;
		result = (b[0]&0xFF)|(b[1]<<8&0xFFFF)|(b[2]<<16&0xFFFFFF)|(b[3]<<24&0xFFFFFFFF);
		return result;
	}
   
   
   //moved from class "XTimeUtil" 2016-1-5-Liang-begin
   /**
    * @ref http://blog.csdn.net/feifei454498130/article/details/6540133
    * @return
    */
   public static final String getTime() {
       //
       SimpleDateFormat sDateFormat = new SimpleDateFormat(
               "yyyy-MM-dd HH:mm:ss");
       String date = sDateFormat.format(new java.util.Date());

       return date;
   }

   /**
    * 获取牙买跌
    * 
    * @return
    */
   public static final String getYMD() {
       SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd");
       String date = sDateFormat.format(new java.util.Date());
       return date;
   }

    
   
   //moved from class "StringUtils" 2016-1-5-Liang-begin
   /**
    * 判断GBK字符串长度是否符合规则。
    * 
    */
   public static boolean isGBKStringLengthValid(String in, int minLen,
           int maxLen) {
       boolean ret = false;

       byte[] bs = null;
       try {
           bs = in.getBytes("GBK");
       } catch (UnsupportedEncodingException e1) {
           e1.printStackTrace();
       }

       if (bs != null && bs.length >= minLen && bs.length <= maxLen) {
           ret = true;
       }

       return ret;
   }

   
   public static String keepLength(String raw ,int maxLength,String encode){
   	
   	
   	byte[] bs = null;
       try {
           bs = raw.getBytes(encode);
       } catch (UnsupportedEncodingException e1) {
           e1.printStackTrace();
       }

       int validLength =  Math.min(bs.length, maxLength);
       String ret = "";
       try {
			ret = new String(bs,0,validLength,encode);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
       
   	return ret;
   }
   
   
   public static final boolean isNumberOrAlphabet(String str) {

       for (int i = 0; i < str.length(); i++) {
           char c = str.charAt(i);
           if (!Character.isLetter(c) && !Character.isDigit(c)) {
               return false;
           }
       }
       return true;
   }

   public static final boolean isCharacterX(String str) {

       for (int i = 0; i < str.length(); i++) {
           char c = str.charAt(i);
           if (!Character.isLetter(c) && !Character.isDigit(c)
                   && !isNormalChar(c)) {
               return false;
           }
       }
       return true;
   }

   public static final boolean isCharacterXAndChinese(String str) {

       for (int i = 0; i < str.length(); i++) {
           char c = str.charAt(i);
           if (!Character.isLetter(c) && !Character.isDigit(c)
                   && !isNormalChar(c) || !isChinese(c)) {
               return false;
           }
       }
       return true;
   }

   private static boolean isNormalChar(char c) {
       // . - _ ( ) / = + ? ! & * < > ; @ #
       if (c == '.' || c == '-' || c == '_' || c == '(' || c == ')'
               || c == '/' || c == '=' || c == '+' || c == '?' || c == '!'
               || c == '&' || c == '*' || c == '<' || c == '>' || c == ';'
               || c == '@' || c == '#') {

           return true;
       }
       return false;
   }

   private static boolean isChinese(char c) {
       Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
       if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
               || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
               || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
               || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
               || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
               || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
               || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
           return true;
       }
       return false;
   }

   /**
    * @param amt
    * @return
    */
   public static final String amountToPlainString(String amt) {
       BigDecimal bg = null;

       String input = amt;

       try {
           bg = new BigDecimal(input);
           bg = bg.divide(new BigDecimal(100));
       } catch (Exception e) {
           bg = BigDecimal.ZERO;
       }

       bg = bg.setScale(2, RoundingMode.HALF_UP);

       return bg.toPlainString();
   }

   // 转成人民币表示
   public static final String amountToRMB(String amt) {
       if (amt == null) {
           return null;
       }
       return "RMB " + amountToPlainString(amt);
   }

   public static final String amountToYuan(String amt) {
       if (amt == null) {
           return null;
       }
       return amountToPlainString(amt) + "元";
   }

   /**
    * @param str
    * @return
    */
   public static final String str2PlainString(String str) {
       BigDecimal bg = null;

       String input = str;

       try {
           bg = new BigDecimal(input);
       } catch (Exception e) {
           bg = BigDecimal.ZERO;
       }

       return bg.toPlainString();
   }

   public static final String addCommaSplite(String str) {
       // 17179869231
       StringBuffer strBuffer;

       int end = str.length() - 1;
       int index = str.indexOf('.');
       if (index > 0) {
           strBuffer = new StringBuffer(str.substring(index));
           end -= strBuffer.length();
       } else {
           strBuffer = new StringBuffer();
       }

       int mark = 0;
       for (int i = end; i >= 0; i--) {

           strBuffer.insert(0, str.charAt(i));
           mark++;
           if (mark <= end && mark % 3 == 0)
               strBuffer.insert(0, ",");
       }

       return strBuffer.toString();
   }

   public static final boolean amountIsValid(String amt) {
       boolean ret = false;

       try {
           BigDecimal bg = new BigDecimal(amt);
           bg = bg.setScale(2, RoundingMode.HALF_UP);
           String amount = bg.toPlainString().replace(".", "");
           int len = amount.length();
           ret = len <= 12 ? true : false;
       } catch (Exception ex) {

       }

       return ret;
   }

   /**
    * @param amt
    * @return
    */
   public static final String formatAmount(String amt) {
       String ZERO = "000000000000";
       if (amt == null || amt.length() <= 0) {
           return ZERO;
       }

       BigDecimal bg = BigDecimal.ZERO;
       boolean isNumber = false;
       try {
           bg = new BigDecimal(amt);
           isNumber = true;
       } catch (Exception ex) {

       } finally {
           if (!isNumber) {
               return ZERO;
           }
       }

       bg = bg.setScale(2, RoundingMode.HALF_UP);
       String amount = bg.toPlainString().replace(".", "");

       int len = amount.length();

       StringBuffer sb = new StringBuffer();
       if (len == 12) {
           sb = sb.append(amount);
       } else if (len < 12) {
           sb = sb.append(ZERO.subSequence(0, 12 - len));
           sb.append(amount);
       }else{
       	sb = sb.append(amount);
       }

//       XLogUtil.d("p=" + sb.toString());
       return sb.toString();
   }

   /**
    * @param input
    * @return
    */
   public static String formatLocation(String input) {
       int len = 10;
       StringBuffer buffer = new StringBuffer();

       if (input != null) {
           int preLen = input.length();
           if (preLen > len) {
               buffer.append(input.subSequence(0, len));
           } else {
               buffer.append("0000000000".subSequence(0, len - preLen));
               buffer.append(input);
           }
       } else {
           buffer.append("0000000000");
       }

       return buffer.toString();
   }

   public static String formatNumber(String input) {
       int len = 9;
       String zero = "000000000";
       StringBuffer buffer = new StringBuffer();
       if (input != null) {
           int preLen = input.length();
           if (preLen > len) {
               buffer.append(input.subSequence(0, len));
           } else {
               buffer.append(zero.subSequence(0, len - preLen));
               buffer.append(input);
           }
       } else {
           buffer.append(zero);
       }

       return buffer.toString();
   }



   public static String formatBCNum(String bcNo) {
       String stars = "***************************";//27
       String ret = "";
       if (!isEmpty(bcNo)) {
           int len = bcNo.length();
           if (len > 10) {
               StringBuffer buffer = new StringBuffer();
               buffer.append(bcNo.subSequence(0, 6));
               buffer.append(stars.subSequence(0, len - 10));
               buffer.append(bcNo.substring(len - 4));
               ret = buffer.toString();
           }else{
           	ret = bcNo;
           }
       }
       return ret;
   }
   
   public static String formatPhoneNum(String phoneNo){
   	String stars = "***************************";//27
   	String ret = "";
   	if(!isEmpty(phoneNo)){
   		 int len = phoneNo.length();
            if (len > 10) {
                StringBuffer buffer = new StringBuffer();
                buffer.append(phoneNo.subSequence(0, 3));
                buffer.append(stars.subSequence(0, len - 7));
                buffer.append(phoneNo.substring(len - 4));
                ret = buffer.toString();
            }else{
           	 ret = phoneNo;//如果不满足隐藏条件，返回输入字符
            }
   	}
   	return ret;
   }

   public static String formatBCNumWithNoStar(String bcNo) {
       String ret = "";
       if (!isEmpty(bcNo)) {
           int len = bcNo.length();
           StringBuffer buffer = new StringBuffer();
           for (int i = 0; i < len; i++) {                
               if (i != 0 && i % 4 == 0) {
                   buffer.append(" ");
               }
               buffer.append(bcNo.charAt(i));
           }
           ret = buffer.toString();
       }
       return ret;
   }
   
   public static String formatHolderName(String holderName){
	   String ret = "";
	   if(!isEmpty(holderName)){
		   int len = holderName.length();
		   StringBuffer buffer = new StringBuffer();
		   buffer.append(holderName.charAt(0));
		   for(int i=0;i<len-1;i++){
			   buffer.append("*");
		   }
		   ret = buffer.toString();
	   }
	   
	   return ret;
   }

   public static final boolean isTransSuccess(String cd) {
       boolean ret = false;

       ret = "00".equalsIgnoreCase(cd) || "10".equalsIgnoreCase(cd)
               || "11".equalsIgnoreCase(cd) || "16".equalsIgnoreCase(cd)
               || "A2".equalsIgnoreCase(cd) || "A4".equalsIgnoreCase(cd)
               || "A5".equalsIgnoreCase(cd) || "A6".equalsIgnoreCase(cd);

       return ret;
   }

   public static ArrayList<String> getTimes(String baseT, int count) {
       ArrayList<String> times = null;

       SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
       try {
           long baseLong = sdf.parse(baseT).getTime();

           times = new ArrayList<String>();
           for (int i = 0; i < count; i++) {
               baseLong += 1000;
               times.add(sdf.format(new Date(baseLong)));
           }
       } catch (Exception ex) {

       }

       return times;
   }
   
   public static byte intToHex(int value)
   {
	   if(value == 0)
		   return 0x00;
	   else if(value == 1)
		   return 0x01;
	   else if(value == 2)
		   return 0x02;
	   else if(value == 3)
		   return 0x03;
	   else if(value == 4)
		   return 0x04;
	   else if(value == 5)
		   return 0x05;
	   else if(value == 6)
		   return 0x06;
	   else if(value == 7)
		   return 0x07;
	   else if(value == 8)
		   return 0x08;
	   else if(value == 9)
		   return 0x09;
	   else if(value == 10)
		   return 0x0A;
	   else if(value == 11)
		   return 0x0B;
	   else if(value == 12)
		   return 0x0C;
	   else if(value == 13)
		   return 0x0D;
	   else if(value == 14)
		   return 0x0E;
	   else if(value == 15)
		   return 0x0F;
	   else 
		   return 0x10;
	   
   }


}
