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
		//һֱ���ԡ�
		while(true){
			try {
				orderCar();
				//Լ���ɹ����˳���
				break;
			} catch (Exception e) {
				//���쳣�󣬵ȴ�1���Ӽ������ԡ�
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
		//��¼
		autoLogin(httpclient);
		
		//ÿ3��������һ�Ρ�
		while(true){
			doOrderCar(httpclient);
			Thread.sleep(3000);
		}
	}
	
	
	/**
	 * �Զ���¼����ʱ��Լ������
	 * @param httpclient
	 */
	private static void autoLogin(DefaultHttpClient httpclient) throws Exception{
		// �����¼ҳ��
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
			// ���������ֵ��
			Document document = Jsoup.parse(loginPage);
			if(document!=null){
				Elements elems = document.select("#__EVENTVALIDATION");
				if(elems==null || elems.size()==0){
					throw new Exception("δ�ҵ��[����__EVENTVALIDATION");
				}
				eventValid = elems.first().val();
				System.out.println("__EVENTVALIDATION:"+eventValid);
				if(eventValid==null || eventValid.length()==0){
					throw new Exception("δ�ҵ��[����__EVENTVALIDATION");
				}
				
				elems = document.select("#__VIEWSTATE");
				if(elems==null || elems.size()==0){
					throw new Exception("δ�ҵ��[����__VIEWSTATE");
				}
				viewState = elems.first().val();
				System.out.println("__VIEWSTATE:"+viewState);
				if(viewState==null || viewState.length()==0){
					throw new Exception("δ�ҵ��[����__VIEWSTATE");
				}
			}
		} finally {
			response1.close();
		}
		
		// ������֤��ͼƬ.
		httpGet = new HttpGet(HOST+"/image.aspx");
		response1 = httpclient.execute(httpGet);
		try {
			System.out.println(response1.getStatusLine());
			HttpEntity entity1 = response1.getEntity();
			EntityUtils.consume(entity1);
		} finally {
			response1.close();
		}

		// �����֤���ֵ
		String checkCode = null;
		List<Cookie> cookies = httpclient.getCookieStore().getCookies();
		if (cookies != null && cookies.size() > 0) {
			for (Cookie cookie : cookies) {
				System.out.println(cookie.getName() + "##:##"
						+ cookie.getValue());
				if ("CheckCode".equalsIgnoreCase(cookie.getName())) {
					checkCode = cookie.getValue().trim();
					System.out.println("��֤�룺" + checkCode);
				}
			}
		}
		if (checkCode == null || checkCode.length() == 0) {
			throw new Exception("cookie��û����֤�룬��Ҫ��������汾");
		}

		// ���͵�¼����
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
	 * Լ������
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	private static void doOrderCar(DefaultHttpClient httpclient) throws Exception{
		//�ǳɹ��󣬷���Լ��ҳ�档
		HttpGet httpGet = new HttpGet(HOST+"/aspx/car/XYYC22.aspx");
		CloseableHttpResponse response1 = httpclient.execute(httpGet);
		try {
			System.out.println(response1.getStatusLine());
			HttpEntity entity1 = response1.getEntity();
			String carPage = EntityUtils.toString(entity1);
			EntityUtils.consume(entity1);
			
			//�����õ���ҳ�����ݣ���ѯ�Ƿ������������գ�13-17,17-19ʱ��εĿ�Լ����
			Document document = Jsoup.parse(carPage);
			//����idΪgv�ı��Ԫ�ء�
			Elements tableEle = document.select("#gv");
			if(tableEle==null ||tableEle.size()==0){
				throw new RuntimeException("idΪgv�ı�񲻴��ڣ���Ҫ��������汾");
			}
			//��ȡ��һ�����ĸ���Ԫ�ء�
			Elements rows = tableEle.first().select("tr");
			if(rows==null ||rows.size()<2){
				throw new RuntimeException("idΪgv�ı��������������ԣ�Ӧ�ó���2�����ϣ���Ҫ��������汾");
			}
			//ѭ��ÿһ�У�������һ�У�Ϊ��ͷ��
			for(int i=1; i<rows.size(); i++){
				Element row = rows.get(i);
				//������
				Element dateCol = row.select("td").first();
				String date = dateCol.text().trim();
				//������������������Լ��
				if(date.contains("������") || date.contains("������")){
					//��ԤԼ��ʱ��Ӧ�þ�������ʱ�䳬��һ�����ϡ�
					Date now = new Date();
					String time = date.substring(0, date.indexOf("("));
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
					Date curDate = format.parse(time);
					//���뵱ǰʱ�䳬��1�����ϵĲ�ԤԼ��
					if(now.getDay()-curDate.getDay()>0){
						//����Ƿ��п���Լ��ʱ�Ρ�
						Elements inputs = row.select("input[type=submit]");
						if(inputs!=null && inputs.size()>0){
							for(Element input:inputs){
								//���û��disabled���ԣ����ʱ���Լ����
								if(!input.hasAttr("disabled")){
									System.out.println("�п�ԤԼ���ڣ������ǣ���"+date+"��ʱ����ǣ�"+input.attr("name")+"������Լ��������"+input.val());
									//ԤԼ�ó���
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
