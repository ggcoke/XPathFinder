import java.util.ArrayList;
import java.util.List;

import edu.itec.ggcoke.xpath.service.XPathService;



public class Test {
	public static void main(String[] args) {
    	String url = "http://baike.baidu.com/view/27781.htm";
    	String key = "分布情况";
    	String value = "产于辽宁、吉林、内蒙古、河北、山西、河南西北部及陕西等省区";
    	XPathService service = new XPathService();
    	
    	List<String> srcXPathes = new ArrayList<String>();
    	List<String> destXPathes = new ArrayList<String>();
    	
    	// 获取key和value的xpath列表
//    	service.getKVXPath(url, key, value, srcXPathes, destXPathes);
//    	System.out.println(srcXPathes);
//    	System.out.println(destXPathes);
    	
    	// 添加相对xpath到文件
//    	service.addXPath(url, srcXPathes, destXPathes);
    	
    	// 根据url和key获取value值
    	String targetKey = "分布情况";
    	List<String> result = service.getTargetContent(url, targetKey);
    	for (String content : result) {
    		System.out.println(content);
    	}
    }
}
