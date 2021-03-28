/*****************************************************************************
 ** File Name:        HttpClient
 ** Author:           SqWang
 ** Date:             2019/05/13
 ** Version:          0.0.0
 ** Description:      Http通信工具类
 ******************************************************************************
 **                         Important Edit History                            *
 ** --------------------------------------------------------------------------*
 ** DATE           		NAME             				DESCRIPTION           *
 ** 2019/05/08     		SqWang                      	Create                *
 ** --------------------------------------------------------------------------*
 ******************************************************************************
 **                         Important Edit Update                             *
 ** --------------------------------------------------------------------------*
 ** DATE           		NAME             				DESCRIPTION           *
 ** 2019/05/08     		SqWang                       	ReqJson               *
 ** --------------------------------------------------------------------------*
 ** 1）修改通信代理
 ******************************************************************************
 ** Copyright(c) 2019, Sq_Wang
 ** All rights reserved.
 ** Distributed under the BSD license.
 ** (See accompanying file LICENSE.txt at https://LICENSE.txt)
 ******************************************************************************/
package dealType.frsDemo;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.io.IOUtils;

import dealType.util.common.StringUtil;
import dealType.frsDemo.BaseHttpSSLSocketFactory.TrustAnyHostnameVerifier;

public class HttpClient {

	// 目标地址
	private URL url;

	// 通信连接超时时间
	private int connectionTimeout;

	// 通信读超时时间
	private int readTimeOut;

	// 通信结果
	private String result;

	// 请求类型 如果没有设置则默认为application/x-www-form-urlencoded
	static String contentType;

	/**--------------------------------------------------------------------------------------------------*
	Name:		    HttpClient
	Discribe:		构造函数
	Parameter:      String url, int connectionTimeout, int readTimeOut
	Return:		    
	 **---------------------------------------------------------------------------------------------------
	 * @throws MalformedURLException */
	public HttpClient(String url, int connectionTimeout, int readTimeOut) throws MalformedURLException {
		this.url = new URL(url);
		this.connectionTimeout = connectionTimeout;
		this.readTimeOut = readTimeOut;
	}

	/**--------------------------------------------------------------------------------------------------*
	Name:		    send
	Discribe:		发送信息到服务端
	Parameter:      String data, String encoding
	Return:		    String
	 **---------------------------------------------------------------------------------------------------
	 * @throws Exception */
	public String send(String data, String encoding) throws Exception {

		//logger.info("Request Message:[{"+data+"}]");
		HttpURLConnection httpURLConnection = createConnection(encoding);
		if (null == httpURLConnection) {
			throw new Exception("Create httpURLConnection Failure");
		}
		
		this.requestServer(httpURLConnection, data, encoding);
		this.result = this.response(httpURLConnection, encoding);
		//logger.info("Response Message:[{"+result+"}]");
		
		return result;
	}

	/**--------------------------------------------------------------------------------------------------*
	Name:		    createConnection
	Discribe:		创建连接
	Parameter:      String encoding
	Return:		    HttpURLConnection
	 **---------------------------------------------------------------------------------------------------
	 * @throws IOException */
	private HttpURLConnection createConnection(String encoding) throws IOException {

		HttpURLConnection httpURLConnection = null;

		httpURLConnection = (HttpURLConnection) url.openConnection();

		httpURLConnection.setConnectTimeout(this.connectionTimeout);	// 连接超时时间
		httpURLConnection.setReadTimeout(this.readTimeOut);				// 读取结果超时时间
		httpURLConnection.setDoInput(true); 							// 可读
		httpURLConnection.setDoOutput(true); 							// 可写
		httpURLConnection.setUseCaches(false);							// 取消缓存
		
		String rawContentType = StringUtil.isEmpty(contentType) ? "application/json" : contentType;
		
		httpURLConnection.setRequestProperty("Content-type", rawContentType + ";charset=" + encoding);
		httpURLConnection.setRequestMethod("POST");

		if ("https".equalsIgnoreCase(url.getProtocol())) {
			HttpsURLConnection husn = (HttpsURLConnection) httpURLConnection;
			husn.setSSLSocketFactory(new BaseHttpSSLSocketFactory());
			husn.setHostnameVerifier(new TrustAnyHostnameVerifier());	// 解决由于服务器证书问题导致HTTPS无法访问的情况
			return husn;
		}
		return httpURLConnection;
	}

	/**--------------------------------------------------------------------------------------------------*
	Name:		    requestServer
	Discribe:		HTTP Post发送消息
	Parameter:      final URLConnection connection, String message, String encoder
	Return:		    void
	 **---------------------------------------------------------------------------------------------------*/
	private void requestServer(final URLConnection connection, String message, String encoder) throws IOException {
		PrintStream out = null;
		
		try {
			connection.connect();
			out = new PrintStream(connection.getOutputStream(), false, encoder);
			
			out.print(message);
			
			out.flush();
		} finally {
			if (null != out) {
				out.close();
			}
		}
	}

	/**--------------------------------------------------------------------------------------------------*
	Name:		    response
	Discribe:		显示Response消息
	Parameter:      final HttpURLConnection connection, String encoding
	Return:		    String
	 **---------------------------------------------------------------------------------------------------*/
	private String response(final HttpURLConnection connection, String encoding) throws IOException {
		InputStream in = null;
		StringBuilder sb = new StringBuilder(1024);
		try {
			if (200 == connection.getResponseCode()) {
				in = connection.getInputStream();
				sb.append(IOUtils.toString(in, encoding));
			} else {
				in = connection.getErrorStream();
				sb.append(IOUtils.toString(in, encoding));
			}
			return sb.toString();
		} finally {
			if (null != in) {
				in.close();
			}
			if (null != connection) {
				connection.disconnect();
			}
		}
	}
}
