import java.util.ArrayList;
import java.util.List;

import edu.itec.ggcoke.xpath.service.XPathService;



public class Test {
	public static void main(String[] args) {
    	String url = "http://zh.wikipedia.org/zh-cn/%E9%BA%BB%E9%BB%84";
    	String key = "产地";
    	String value = "主要产地为中国河北、";
    	XPathService service = new XPathService();
    	
    	List<String> srcXPathes = new ArrayList<String>();
    	List<String> destXPathes = new ArrayList<String>();
    	
    	// 获取key和value的xpath列表
    	service.getKVXPath(url, key, value, srcXPathes, destXPathes);
//    	System.out.println(srcXPathes);
//    	System.out.println(destXPathes);
    	
    	// 添加相对xpath到文件
    	service.addXPath(url, srcXPathes, destXPathes);
    	
    	// 根据url和key获取value值
    	String targetKey = "功效";
    	List<String> result = service.getTargetContent(url, targetKey);
    	for (String content : result) {
    		System.out.println(content);
    	}
    }
}
