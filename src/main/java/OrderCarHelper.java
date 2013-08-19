import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@SuppressWarnings("deprecation")
public class OrderCarHelper {
	
	public static final String HOST="http://218.205.165.196:8080";

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args){
		//一直尝试。
		while(true){
			try {
				orderCar();
				//约车成功后，退出。
				break;
			} catch (Exception e) {
				//抛异常后，等待1秒钟继续尝试。
				e.printStackTrace();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
				}
			}
		}
	}
	
	private static void orderCar() throws Exception{
		DefaultHttpClient httpclient = new DefaultHttpClient();
		//登录
		autoLogin(httpclient);
		
		//每3秒钟重试一次。
		while(true){
			doOrderCar(httpclient);
			Thread.sleep(3000);
		}
	}
	
	
	/**
	 * 自动登录东方时尚约车服务。
	 * @param httpclient
	 */
	private static void autoLogin(DefaultHttpClient httpclient) throws Exception{
		// 请求登录页面
		HttpGet httpGet = new HttpGet(HOST);
		CloseableHttpResponse response1 = httpclient.execute(httpGet);
		String eventValid = null;
		String viewState = null;
		try {
			System.out.println(response1.getStatusLine());
			HttpEntity entity1 = response1.getEntity();
			// do something useful with the response body
			// and ensure it is fully consumed
			String loginPage = EntityUtils.toString(entity1);
			EntityUtils.consume(entity1);
			// 获得隐藏域值。
			Document document = Jsoup.parse(loginPage);
			if(document!=null){
				Elements elems = document.select("#__EVENTVALIDATION");
				if(elems==null || elems.size()==0){
					throw new Exception("未找到隱藏域__EVENTVALIDATION");
				}
				eventValid = elems.first().val();
				System.out.println("__EVENTVALIDATION:"+eventValid);
				if(eventValid==null || eventValid.length()==0){
					throw new Exception("未找到隱藏域__EVENTVALIDATION");
				}
				
				elems = document.select("#__VIEWSTATE");
				if(elems==null || elems.size()==0){
					throw new Exception("未找到隱藏域__VIEWSTATE");
				}
				viewState = elems.first().val();
				System.out.println("__VIEWSTATE:"+viewState);
				if(viewState==null || viewState.length()==0){
					throw new Exception("未找到隱藏域__VIEWSTATE");
				}
			}
		} finally {
			response1.close();
		}
		
		// 请求验证码图片.
		httpGet = new HttpGet(HOST+"/image.aspx");
		response1 = httpclient.execute(httpGet);
		try {
			System.out.println(response1.getStatusLine());
			HttpEntity entity1 = response1.getEntity();
			EntityUtils.consume(entity1);
		} finally {
			response1.close();
		}

		// 获得验证码的值
		String checkCode = null;
		List<Cookie> cookies = httpclient.getCookieStore().getCookies();
		if (cookies != null && cookies.size() > 0) {
			for (Cookie cookie : cookies) {
				System.out.println(cookie.getName() + "##:##"
						+ cookie.getValue());
				if ("CheckCode".equalsIgnoreCase(cookie.getName())) {
					checkCode = cookie.getValue().trim();
					System.out.println("验证码：" + checkCode);
				}
			}
		}
		if (checkCode == null || checkCode.length() == 0) {
			throw new Exception("cookie中没有验证码，需要升级程序版本");
		}

		// 发送登录请求
		HttpPost httpPost = new HttpPost(
				HOST+"/XYYC21DR1.aspx");
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("txtname", "15032995"));
		nvps.add(new BasicNameValuePair("txtpwd", "10240"));
		nvps.add(new BasicNameValuePair("yanzheng", checkCode));
		nvps.add(new BasicNameValuePair("__EVENTVALIDATION", eventValid));
		nvps.add(new BasicNameValuePair("__VIEWSTATE", viewState));
		nvps.add(new BasicNameValuePair("button.x", String.valueOf(29)));
		nvps.add(new BasicNameValuePair("button.y", String.valueOf(10)));
		httpPost.setEntity(new UrlEncodedFormEntity(nvps));
		CloseableHttpResponse response2 = httpclient.execute(httpPost);

		try {
			System.out.println(response2.getStatusLine());
			HttpEntity entity2 = response2.getEntity();
			// do something useful with the response body
			// and ensure it is fully consumed
			//System.out.println(EntityUtils.toString(entity2));
			EntityUtils.consume(entity2);
		} finally {
			response2.close();
		}
	}
	
	/**
	 * 约车服务
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	private static void doOrderCar(DefaultHttpClient httpclient) throws Exception{
		//登成功后，访问约车页面。
		HttpGet httpGet = new HttpGet(HOST+"/aspx/car/XYYC22.aspx");
		CloseableHttpResponse response1 = httpclient.execute(httpGet);
		try {
			System.out.println(response1.getStatusLine());
			HttpEntity entity1 = response1.getEntity();
			String carPage = EntityUtils.toString(entity1);
			EntityUtils.consume(entity1);
			
			//分析得到的页面内容，查询是否有周六、周日，13-17,17-19时间段的可约车。
			Document document = Jsoup.parse(carPage);
			//查找id为gv的表格元素。
			Elements tableEle = document.select("#gv");
			if(tableEle==null ||tableEle.size()==0){
				throw new RuntimeException("id为gv的表格不存在，需要升级程序版本");
			}
			//获取第一个表格的各行元素。
			Elements rows = tableEle.first().select("tr");
			if(rows==null ||rows.size()<2){
				throw new RuntimeException("id为gv的表格的数据行数不对，应该超过2行以上，需要升级程序版本");
			}
			//循环每一行，跳过第一行，为表头。
			for(int i=1; i<rows.size(); i++){
				Element row = rows.get(i);
				//日期列
				Element dateCol = row.select("td").first();
				String date = dateCol.text().trim();
				//星期六或者星期日则约车
				if(date.contains("星期六") || date.contains("星期日")){
					//且预约的时间应该距离现在时间超过一天以上。
					Date now = new Date();
					String time = date.substring(0, date.indexOf("("));
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
					Date curDate = format.parse(time);
					//距离当前时间超过1天以上的才预约。
					if(now.getDay()-curDate.getDay()>0){
						//检查是否有可以约车时段。
						Elements inputs = row.select("input[type=submit]");
						if(inputs!=null && inputs.size()>0){
							for(Element input:inputs){
								//如果没有disabled属性，则该时间可约车。
								if(!input.hasAttr("disabled")){
									System.out.println("有可预约档期，日期是：："+date+"，时间段是："+input.attr("name")+"，可以约车数量："+input.val());
									//预约该车。
								}
							}
						}
					}
				}
			}
			
		} finally {
			response1.close();
		}
	}

}
