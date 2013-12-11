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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jodd.jerry.Jerry;
import jodd.lagarto.dom.Node;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import edu.itec.ggcoke.xpath.entity.XPathEntity;
import edu.itec.ggcoke.xpath.util.XPathConstant;

public class XPathService {
    /**
     * 添加新的xpath记录 ，默认权重为 {@link edu.itec.ggcoke.util.XPathConstant.XPATH_WEIGHT_DEFAULT}<br/>
     * 如果存在，则权重+1
     * @param xpath
     * @param weight
     */
    public void addXPath(String url, String xpath) {
    	updateXPathWeight(url, xpath);
    }
    
    /**
     * 更新xpath权重，如果不存在对应的xpath，则添加对应权重的xpath
     * @param xpath
     * @param weight
     */
    public void updateXPathWeight(String url, String xpath) {
    	BufferedReader br = null;
    	BufferedWriter bw = null;
    	String line = "";
    	StringBuilder sb = new StringBuilder();
    	boolean exist = false;
    	String domain = "";
    	try {
			domain = new URL(url).getHost();
		} catch (MalformedURLException e1) {
		}
    	
    	try {
    		br = new BufferedReader(new InputStreamReader(
					new FileInputStream(new File(XPathConstant.XPATH_LIST))));
    		while ((line = br.readLine()) != null && line.length() > 0) {
    			if (line.startsWith("#")) {
    				sb.append(line+"\r\n");
    			} else {
    				String tmpDomain = line.split("\t")[0];
        			String tmpXPath = line.split("\t")[1];
        			int weight = Integer.parseInt(line.split("\t")[2]);
        			if (tmpXPath.equalsIgnoreCase(xpath) && tmpDomain.equalsIgnoreCase(domain)){
        				sb.append(domain + "\t" + xpath + "\t" + (weight+1) + "\r\n");
        				exist = true;
        			} else {
        				sb.append(line+"\r\n");
        			}
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
				bw.write(domain + "\t" + xpath + "\t" + XPathConstant.XPATH_WEIGHT_DEFAULT + "\r\n");
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
    public void addXPath(String url, String src, String comp) {
    	String relativeXPath = XPathEntity.getRelativeBetweenXPath(src, comp);
    	this.addXPath(url, relativeXPath);
    }
    
    /**
     * 通过关键词xpath数组和值xpath数组添加xpath，<br/>
     * 首先计算两组xpath之间距离最小的一对，然后将这对xpath的相对路径添加到文件中
     * @param src
     * @param comp
     */
    public void addXPath(String url, List<String> src, List<String> comp) {
    	String minScr = "";
    	String minComp = "";
    	int minSteps = Integer.MAX_VALUE;
    	
    	if (src == null || comp == null)
    		return;
    	for (int i = 0; i < src.size(); i++) {
    		for (int j = 0; j < comp.size(); j++) {
    			int tmpSteps = XPathEntity.getStepsBetweenXPath(src.get(i), comp.get(j));
    			if (tmpSteps == 0) continue;
    			if (tmpSteps < minSteps) {
    				minScr = src.get(i);
    				minComp = comp.get(j);
    				minSteps = tmpSteps;
    			}
    		}
    	}
    	
    	addXPath(url, minScr, minComp);
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
    		String c = jerry.text();
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
    	String content = PageContentService.getKPageContent(url, key);
    	Jerry doc = Jerry.jerry(content);
    	for (Jerry jerry : doc.$("span[rel='mark_key']")) {
    		keyXPathList.add(getXPathCore(jerry));
		}
    	
    	content = PageContentService.getVPageContent(url, value);
    	doc = Jerry.jerry(content);
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
    private String getContentCore(Document doc, String xpath) {
		Elements elems = doc.select(xpath);
		return ((elems == null || elems.size() <= 0) ? "" : elems.get(0).text());
    }
    
    /**
     * 获取相对xpath list，首先根据domain分类，跟target domain相同的有限查找，然后以权重排序
     * @param domain
     * @return
     */
    private List<XPathEntity> getRelativeXPath(String domain) {
    	List<XPathEntity> sameDomainXPaths = new ArrayList<XPathEntity>();
    	List<XPathEntity> otherDomainXPaths = new ArrayList<XPathEntity>();
    	String line = "";
    	BufferedReader br = null;
    	try {
			br = new BufferedReader(new InputStreamReader(
							new FileInputStream(new File(XPathConstant.XPATH_LIST))));
			while ((line = br.readLine()) != null) {
				if (!line.startsWith("#")) {
					String[] infos = line.split("\t");
					if (infos == null || infos.length != 3) {
						continue;
					}
					XPathEntity entity = new XPathEntity(infos[0], infos[1], Integer.parseInt(infos[2]));
					if (infos[0].equalsIgnoreCase(domain)) {
						sameDomainXPaths.add(entity);
					} else {
						otherDomainXPaths.add(entity);
					}
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
    	
    	// 将相对路径按照权重排序，优先选择权重大的进行爬取
    	Collections.sort(sameDomainXPaths, new Comparator<XPathEntity>() {

			@Override
			public int compare(XPathEntity xpath1, XPathEntity xpath2) {
				return xpath2.getWeight() - xpath1.getWeight();
			}
    		
		});
    	
    	Collections.sort(otherDomainXPaths, new Comparator<XPathEntity>() {

			@Override
			public int compare(XPathEntity xpath1, XPathEntity xpath2) {
				return xpath2.getWeight() - xpath1.getWeight();
			}
    		
		});
    	
    	sameDomainXPaths.addAll(otherDomainXPaths);
    	
    	return sameDomainXPaths;
    }
    
    public List<String> getTargetContent(String url, String key) {
    	List<String> result = new ArrayList<String>();
    	String domain = "";
    	try {
    		domain = new URL(url).getHost();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
    	
    	List<XPathEntity> relativeXPathes = getRelativeXPath(domain);
    	List<String> srcXPathes = getKXPath(url, key);
    	
    	if (relativeXPathes == null || srcXPathes == null)
    		return null;
    	Document doc = Jsoup.parse(PageContentService.getPageContent(url));
    	
    	// 对关键字按照匹配度排序
    	Compare compare = new Compare(doc);
    	Collections.sort(srcXPathes, compare);

    	for (int i = 0; i < srcXPathes.size(); i++) {
    		for (int j = 0; j < relativeXPathes.size(); j++) {
    			String srcXPath = srcXPathes.get(i);
    			String relativeXPath = relativeXPathes.get(j).getRelativeXPath();
    			String targetXPath = XPathEntity.getDestXPath(srcXPath, relativeXPath);
    			String content = getContentCore(doc, targetXPath);
    			if (content != null && content.length() > 0) {
    				result.add(content);
    				break;
    			}
    		}
    	}
    	
    	return result;
    }
    
    // 关键字匹配度排序算法，按照文字长度
    private class Compare implements Comparator<String>{
    	private Document doc;
    	public Compare(Document doc) {
    		this.doc = doc;
    	}
    	
		@Override
		public int compare(String o1, String o2) {
			return getContentCore(doc, o1).length() - getContentCore(doc, o2).length();
		}
    	
    }
}
