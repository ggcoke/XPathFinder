package edu.itec.ggcoke.xpath.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.sun.org.apache.xalan.internal.xsltc.compiler.util.NodeType;

import jodd.jerry.Jerry;
import jodd.lagarto.dom.Node;

import edu.itec.ggcoke.xpath.entity.XPathEntity;
import edu.itec.ggcoke.xpath.util.XPathConstant;

public class XPathService {
	
	/**
	 * 添加新的xpath记录 ，默认权重为 {@link edu.itec.ggcoke.util.XPathConstant.XPATH_WEIGHT_DEFAULT}
	 * @param xpath
	 */
    public void addXPath(String xpath) {
    	addXPath(xpath, XPathConstant.XPATH_WEIGHT_DEFAULT);
    }
    
    /**
     * 添加新的带权重的xpath记录
     * @param xpath
     * @param weight
     */
    public void addXPath(String xpath, int weight) {
    	updateXPathWeight(xpath, weight);
    }
    
    /**
     * 更新xpath权重，如果不存在对应的xpath，则添加对应权重的xpath
     * @param xpath
     * @param weight
     */
    public void updateXPathWeight(String xpath, int weight) {
    	BufferedReader br = null;
    	BufferedWriter bw = null;
    	String line = "";
    	StringBuilder sb = new StringBuilder();
    	boolean exist = false;
    	
    	try {
    		br = new BufferedReader(new InputStreamReader(
					new FileInputStream(new File(XPathConstant.XPATH_LIST))));
    		while ((line = br.readLine()) != null) {
    			String tmpXPath = line.split("\t")[0];
    			if (!tmpXPath.startsWith("#") && tmpXPath.equalsIgnoreCase(xpath)){
    				sb.append(xpath + "\t" + weight + "\r\n");
    				exist = true;
    			} else {
    				sb.append(line+"\r\n");
    			}
    		}
    		
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(XPathConstant.XPATH_LIST))));
			bw.write(sb.toString());
			if (!exist) {
				bw.write(xpath + "\t" + weight + "\r\n");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
    }
    
    /**
     * 通过两个xpath添加xpath相对路径及权重
     * @param src
     * @param comp
     */
    public void addXPath(String src, String comp) {
    	String relativeXPath = XPathEntity.getRelativeBetweenXPath(src, comp);
    	this.addXPath(relativeXPath);
    }
    
    /**
     * 通过关键词xpath数组和值xpath数组添加xpath，首先计算两组xpath之间距离最小的一对，
     * 然后将这对xpath的相对路径添加到文件中
     * @param src
     * @param comp
     */
    public void addXPath(List<String> src, List<String> comp) {
    	String minScr = "";
    	String minComp = "";
    	int minSteps = Integer.MAX_VALUE;
    	
    	if (src == null || comp == null)
    		return;
    	for (int i = 0; i < src.size(); i++) {
    		for (int j = 0; j < comp.size(); j++) {
    			int tmpSteps = XPathEntity.getStepsBetweenXPath(src.get(i), comp.get(j));
    			if (tmpSteps < minSteps) {
    				minScr = src.get(i);
    				minComp = comp.get(j);
    				minSteps = tmpSteps;
    			}
    		}
    	}
    	
    	addXPath(minScr, minComp);
    }
    
    /**
     * XPath是否已经存在
     * @param xpath
     * @return
     */
    public static boolean XPathExist(String xpath) {
    	String line = "";
    	BufferedReader br = null;
    	try {
			br = new BufferedReader(new InputStreamReader(
							new FileInputStream(new File(XPathConstant.XPATH_LIST))));
			while ((line = br.readLine()) != null) {
				String tmpXPath = line.split("\t")[0];
				if (tmpXPath.startsWith("#"))
					continue;
				if (tmpXPath.equals(xpath))
					return true;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return false;
    }
    
    /**
     * 获取xpath
     * 得到的格式为<code>html>body>div>div:eq(1)>div>ul>li:eq(2)>span>span</code>
     * @param doc
     * @return
     */
    private static String getXPathCore(Jerry doc) {
		List<String> arr = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();
		
		while (doc != null) {
			String fix = ":eq(" + getTagIndex(doc) + ")";
			String tagName = doc.get(0).getNodeName();
			// body后面不能加eq，否则xpath无效
			arr.add(tagName + (tagName.equalsIgnoreCase("body") ? "" : fix));
			if (doc.get().length > 0) {
				if (!doc.get(0).getNodeName().equalsIgnoreCase("html")) {
					doc = doc.parent();
				} else {
					break;
				}
			}
		}
		
		// 不能取最后一个标签，最后一个标签是处理页面是添加上去的span
		for (int i = arr.size() - 1; i > 0; i--) {
			sb.append(arr.get(i)).append(">");
		}
		
		return sb.toString().substring(0, sb.toString().length()-1);
	}
    
    /**
     * 获取当前标签在父标签中的index，从0开始计算
     * @param doc
     * @return
     */
    private static int getTagIndex(Jerry doc) {
		int index = 0;
		Node currentNode = doc.get(0);
		Jerry parent = doc.parent();
		if (parent.children().get().length > 0) {
			Node[] children = parent.children().get();
			for (int i = 0; i < children.length; i++) {
				if (children[i].getNodeType().equals(Node.NodeType.ELEMENT) && children[i].equals(currentNode)) {
					return index;
				}
				index++;
			}
		}
		return -1;
	}
    
    /**
     * 获取url对应网页中key的xpath list
     * @param url 网页url
     * @param key 关键字
     * @return
     */
    public List<String> getXPath(String url, String key) {
    	List<String> xpaths = new ArrayList<String>();
    	String content = PageContentService.getKPageContent(url, key);
    	Jerry doc = Jerry.jerry(content);
    	for (Jerry jerry : doc.$("span[rel='mark_key']")) {
    		xpaths.add(getXPathCore(jerry));
		}
    	
    	return xpaths;
    }
    
    /**
     * 获取url对应网页中关键字及其对应值的xpath
     * @param url
     * @param key
     * @param value
     * @param keyXPathList
     * @param valueXPathList
     */
    public void getKVXPath(String url, String key, String value, List<String> keyXPathList, List<String> valueXPathList) {
    	String content = PageContentService.getPageContent(url, key, value);
    	Jerry doc = Jerry.jerry(content);
    	for (Jerry jerry : doc.$("span[rel='mark_key']")) {
    		keyXPathList.add(getXPathCore(jerry));
		}
    	
    	for (Jerry jerry : doc.$("span[rel='mark_value']")) {
    		valueXPathList.add(getXPathCore(jerry));
    	}
    }
    
    public List<String> getContent(String url, String xpath) {
    	List<String> contents = new ArrayList<String>();
    	Document doc = Jsoup.parse(PageContentService.getPageContent(url));
		Elements elems = doc.select(xpath);
		for (Element element : elems) {
			contents.add(element.text());
		}
		return contents;
    }
    
    public static void main(String[] args) {
    	String url = "http://baike.baidu.com/view/27781.htm";
    	String key = "辽宁、吉林、内蒙古、河北、山西、河南西北部及陕西等省区";
    	String value = "分布区域";
    	XPathService service = new XPathService();
//    	String content = PageContentService.getPageContent(url, key, value);
//    	System.out.println(Jsoup.parse(PageContentService.getPageContent(url, key, value)));
    	List<String> srcXPathes = new ArrayList<String>();
    	List<String> destXPathes = new ArrayList<String>();
    	service.getKVXPath(url, key, value, srcXPathes, destXPathes);
    	System.out.println("KEY: " + srcXPathes.toString());
    	System.out.println("DEST: " + destXPathes.toString());
//    	String xpath = "html>body>div>div:eq(3)>div>div:eq(1)>div>div:eq(2)>div>div>div>div:eq(7)>dl>dd>div:eq(0)";
    	List<String> results = service.getContent(url, srcXPathes.get(0));
    	System.out.println(results);
    }
}
