import edu.itec.ggcoke.xpath.service.XPathService;



public class Test {
	public static void main(String[] args) {
    	String url = "http://baike.baidu.com/view/27781.htm";
    	String key = "生长环境";
    	String value = "耐寒、耐旱、碱性土亦能生长";
    	XPathService service = new XPathService();
    	
//    	List<String> srcXPathes = new ArrayList<String>();
//    	List<String> destXPathes = new ArrayList<String>();
    	
    	// 获取key和value的xpath列表
//    	service.getKVXPath(url, key, value, srcXPathes, destXPathes);
//    	System.out.println("KEY: " + srcXPathes.toString());
//    	System.out.println("DEST: " + destXPathes.toString());
    	
    	// 添加相对xpath到文件
//    	service.addXPath(srcXPathes, destXPathes);
    	
    	// 根据url和key获取value值
    	System.out.println(service.getTargetContent(url, key));
    }
}
