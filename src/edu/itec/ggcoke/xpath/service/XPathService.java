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
import java.util.Collections;
import java.util.Comparator;
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
     * 通过关键词xpath数组和值xpath数组添加xpath，<br/>
     * 首先计算两组xpath之间距离最小的一对，然后将这对xpath的相对路径添加到文件中
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
			arr.add(doc.get(0).getNodeName() + ":eq(" + getTagIndex(doc) + ")");
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
				
				// 此处需注意，不需要相同的tag时index才递增，而是统计所有的tag
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
    public List<String> getKXPath(String url, String key) {
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
//    	System.out.println(content);
    	Jerry doc = Jerry.jerry(content);
    	for (Jerry jerry : doc.$("span[rel='mark_key']")) {
    		keyXPathList.add(getXPathCore(jerry));
		}
    	
    	for (Jerry jerry : doc.$("span[rel='mark_value']")) {
    		valueXPathList.add(getXPathCore(jerry));
    	}
    }
    
    /**
     * 根据网页url和目标xpath获取内容
     * @param url
     * @param xpath
     * @return
     */
    private List<String> getContentCore(Document doc, String xpath) {
    	List<String> contents = new ArrayList<String>();
    	
		Elements elems = doc.select(xpath);
		for (Element element : elems) {
			contents.add(element.text());
		}
		return contents;
    }
    
    private List<XPathEntity> getRelativeXPath() {
    	List<XPathEntity> xpaths = new ArrayList<XPathEntity>();
    	String line = "";
    	BufferedReader br = null;
    	try {
			br = new BufferedReader(new InputStreamReader(
							new FileInputStream(new File(XPathConstant.XPATH_LIST))));
			while ((line = br.readLine()) != null) {
				if (!line.startsWith("#")) {
					String xpath = line.split("\t")[0];
					int weight = Integer.parseInt(line.split("\t")[1]);
					XPathEntity entity = new XPathEntity(xpath, weight);
					xpaths.add(entity);
				}
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
    	
    	// 将相对路径按照权重排序，有限选择权重大的进行爬取
    	Collections.sort(xpaths, new Comparator<XPathEntity>() {

			@Override
			public int compare(XPathEntity xpath1, XPathEntity xpath2) {
				return xpath2.getWeight() - xpath1.getWeight();
			}
    		
		});
    	
    	return xpaths;
    }
    
    public List<String> getTargetContent(String url, String key) {
    	List<String> result = new ArrayList<String>();
    	List<XPathEntity> relativeXPathes = getRelativeXPath();
    	List<String> srcXPathes = getKXPath(url, key);
    	
    	if (relativeXPathes == null || srcXPathes == null)
    		return null;
    	
    	Document doc = Jsoup.parse(PageContentService.getPageContent(url));
    	
    	for (int i = 0; i < srcXPathes.size(); i++) {
    		for (int j = 0; j < relativeXPathes.size(); j++) {
    			String srcXPath = srcXPathes.get(i);
    			String relativeXPath = relativeXPathes.get(j).getRelativeXPath();
    			String targetXPath = XPathEntity.getDestXPath(srcXPath, relativeXPath);
    			List<String> content = getContentCore(doc, targetXPath);
    			if (content != null) {
    				for (int k = 0; k < content.size(); k++) {
    					result.add(content.get(k));
    				}
    				// 只查找权重最大的相对xpath得到的结果
    				break;
    			}
    		}
    	}
    	
    	return result;
    }
    
    
}
