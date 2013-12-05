package edu.itec.ggcoke.xpath.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class PageContentService {
	
	/**
	 * 根据属性键和属性值获取网页内容
	 * @param siteUrl
	 * @param key
	 * @param value
	 * @return
	 */
	public static String getPageContent(String siteUrl, String key, String value) {
		String content = getPageContent(siteUrl);
		// 替换关键字
		content = content.replaceAll(key, "<span rel='mark_key'>" + key + "</span>");
		content = content.replaceAll(value, "<span rel='mark_value'>" + value + "</span>");
		
		return content;
	}
	
	/**
	 * 根据属性键获取网页内容
	 * @param siteUrl
	 * @param key
	 * @return
	 */
	public static String getKPageContent(String siteUrl, String key) {
		String content = getPageContent(siteUrl);
		// 替换关键字
		content = content.replaceAll(key, "<span rel='mark_key'>" + key + "</span>");
		
		return content;
	}
	
	public static String getVPageContent(String siteUrl, String value) {
		String content = getPageContent(siteUrl);
		// 替换关键字
		content = content.replaceAll(value, "<span rel='mark_value'>" + value + "</span>");
		
		return content;
	}
	
	/**
	 * 获取网页内容，删除其中的script标签和a标签
	 * @param siteUrl
	 * @return
	 */
	public static String getPageContent(String siteUrl) {
		StringBuffer sb = new StringBuffer();
		try {
			URL url = new URL(siteUrl);
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), "utf-8"));
			String line;
			while ((line = in.readLine()) != null) {
				sb.append(line).append("\r\n");
			}
			in.close();
		} catch (Exception e) {
			return null;
		}
		String content = sb.toString();
		
		// 去掉源码中的script片段，注意其中script嵌套
		content = content.replaceAll("<script(?:[^<]++|<(?!/script>))*+</script>", "<script>");
		while(content.contains("</script>")){ 
			content = content.replaceAll("<script(?:[^<]++|<(?!/script>))*+</script>", "");
		} 
		
		while(content.contains("<script>")){ 
			content = content.replaceAll("<script>", ""); 
		} 
		
		// 百科中很多内容都有超链接，影响关键词替换，因此需要将a标签去掉
		content = content.replaceAll("<a [^>]*+>", "");
		content = content.replaceAll("</a>", "");
		
		return content;
	}
	
	public static void main(String[] args) {
		String url = "http://baike.baidu.com/view/27781.htm";
		String key = "苗期";
		String result = PageContentService.getKPageContent(url, key);
		System.out.println(result);
	}
}
