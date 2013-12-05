import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jodd.jerry.Jerry;
import jodd.lagarto.dom.Node;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.itec.ggcoke.xpath.service.PageContentService;



public class JerryTest {
	public static String getXPathCore(Jerry doc) {
		List<String> arr = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();
		
		while (doc != null) {
			int index = getTagIndex(doc);
			String fix = index == 0 ? "" : ":eq(" + getTagIndex(doc) + ")";
			arr.add(doc.get(0).getNodeName() + fix);
			if (doc.get().length > 0) {
				if (!doc.get(0).getNodeName().equalsIgnoreCase("html")) {
					doc = doc.parent();
				} else {
					break;
				}
			}
		}
		
		for (int i = arr.size() - 1; i >= 0; i--) {
			sb.append(arr.get(i)).append(">");
		}
		
		return sb.toString().substring(0, sb.toString().length()-1);
	}
	
	public static int getTagIndex(Jerry doc) {
		int index = 0;
		Node currentNode = doc.get(0);
		Jerry parent = doc.parent();
		if (parent.children().get().length > 0) {
			Node[] children = parent.children().get();
			for (int i = 0; i < children.length; i++) {
				if (children[i].equals(currentNode)) {
					return index;
				}
				
				if (children[i].getNodeName().equalsIgnoreCase(currentNode.getNodeName())) {
					index++;
				}
			}
		}
		return -1;
	}
	
	public static String getContent (String content, String xpath) {
		Document doc = Jsoup.parse(content);
		Elements elems = doc.select(xpath);
		for (Element element : elems) {
			System.out.println(element.html());
		}
		return "";
	}
	
	public static void main(String[] args) throws IOException {
		String url = "http://baike.baidu.com/view/27781.htm";
		String xpath = "html>body>div>div:eq(1)>div>ul>li:eq(2)>span>span";
//		String xpath = "html";
//		String xpath = "html:eq(1)/body[1]/div[1]/div[2]/div[1]/ul[1]/li[3]/span[1]";
		String key = "特色百科";
		String content = PageContentService.getKPageContent(url, key);
//		
//		Jerry doc = Jerry.jerry(content);
//		int count = doc.$("span[rel='mark_key']").length();
//		for (Jerry jerry : doc.$("span[rel='mark_key']")) {
//			System.out.println(JerryTest.getXPathCore(jerry));
//		}
		
		getContent(content, xpath);
	}
}
