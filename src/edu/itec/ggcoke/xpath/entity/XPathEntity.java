package edu.itec.ggcoke.xpath.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.Setter;

public class XPathEntity {
	private String relativeXPath;
	private String domain;
	private int weight;
	
	public XPathEntity(String domain, String relativeXPath, int weight) {
		this.domain = domain;
		this.relativeXPath = relativeXPath;
		this.weight = weight;
	}
	
	public String getRelativeXPath() {
		return relativeXPath;
	}

	public void setRelativeXPath(String relativeXPath) {
		this.relativeXPath = relativeXPath;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}
	
	public String getDomain() {
		return this.domain;
	}
	
	public void setDomain(String domain) {
		this.domain = domain;
	}
	
	/**
	 * 获取xpath上一级
	 * @param xpath
	 * @return
	 */
	public static String getParentXPath(String xpath) {
		return xpath.substring(0, xpath.lastIndexOf("/"));
	}
	
	/**
	 * 获取两个xpath之间的距离。<br/>
	 * 两个xpath的距离定义为最近公共节点到两个叶子节点距离的和。<br/>
	 * 例如，有两个xpath：<br/>
	 * xpath1: <code>html>body>div:eq(2)>table>tr:eq(4)>td:eq(2)</code><br/>
	 * xpath2: <code>html>body>div:eq(2)>div:eq(4)>table:eq(1)>tr>td:eq(5)</code><br/>
	 * 
	 *                         |--table--tr:eq(4)--td:eq(2)             (最近公共节点到xpath1叶子节点距离为3)<br/>
	 * html--body--div:eq(2) --|<br/>
	 *                         |--div:eq(4)--table:eq(1)--tr--td:eq(5)   (最近公共节点到xpath1叶子节点距离为4)<br/>
	 * xpath1跟xpath2的距离为7
	 * @param src
	 * @param comp
	 * @return
	 */
	public static int getStepsBetweenXPath(String src, String comp) {
		List<String> srcList = new ArrayList(Arrays.asList(src.split(">")));
		List<String> compList = new ArrayList(Arrays.asList(comp.split(">")));
		while (srcList.size() >0 && compList.size() > 0 && srcList.get(0).equalsIgnoreCase(compList.get(0))) {
			srcList.remove(0);
			compList.remove(0);
		}
		return srcList.size()+compList.size();
	}
	
	/**
	 * 获取comp相对于src的xpath路径，首先找到最近的公共祖父节点，然后比较在公共祖父节点下的
	 * 两个节点的相对距离，只有第一层需要比较，后面的就没有关系了
	 * 例如，有两个xpath：<br/>
	 * src:  <code>html:eq(0)>body:eq(1)>div:eq(2)>table:eq(1)>tr:eq(4)>td:eq(2)</code><br/>
	 * comp: <code>html:eq(0)>body:eq(1)>div:eq(2)>div:eq(4)>table:eq(1)>tr>td:eq(5)</code><br/>
	 * 则comp相对于src的xpath路径为<code>../../../div:eq(+3)/table:eq(1)/tr/td:eq(5)</code>
	 * @param src
	 * @param comp
	 * @return
	 */
	public static String getRelativeBetweenXPath(String src, String comp) {
		StringBuilder sb = new StringBuilder();
		List<String> srcList = new ArrayList(Arrays.asList(src.split(">")));
		List<String> compList = new ArrayList(Arrays.asList(comp.split(">")));
		while (srcList.size() >0 && compList.size() > 0 && srcList.get(0).equalsIgnoreCase(compList.get(0))) {
			srcList.remove(0);
			compList.remove(0);
		}
		
		// key是value的父节点
		if (srcList.size() <= 0) {
			sb.append("./");
			while (compList.size() > 0) {
				sb.append(compList.remove(0)).append("/");
			}
			return sb.toString().substring(0, sb.toString().length() - 1);
		}
		
		// value是 key的父节点
		if (compList.size() <= 0) {
			while (srcList.size() > 0) {
				sb.append("../");
				srcList.remove(0);
			}
			return sb.toString().substring(0, sb.toString().length() - 1);
		}
		
		// 计算第一级不同节点的相对距离
		Pattern p = Pattern.compile("eq\\((\\d+)\\)");
		String srcChild = srcList.get(0);
		String compChild = compList.get(0);
		int srcIndex = 0, compIndex = 0;
		Matcher m1 = p.matcher(srcChild);
		Matcher m2 = p.matcher(compChild);
		
		if (m1.find()) {
			srcIndex = Integer.parseInt(m1.group(1));
		}
		if (m2.find()) {
			compIndex = Integer.parseInt(m2.group(1));
		}
		
		String relative = String.valueOf(compIndex - srcIndex);
		
		while (srcList.size() > 0) {
			sb.append("../");
			srcList.remove(0);
		}
		
		String firstNode = compList.remove(0);
		sb.append(firstNode.replaceAll(String.valueOf(compIndex), relative)).append("/");
		
		while (compList.size() > 0) {
			sb.append(compList.remove(0)).append("/");
		}
		
		return sb.toString().substring(0, sb.toString().length() - 1);
	}
	
	/**
     * 根据源xpath和相对xpath获取目的xpath。例如：<br/>
     * src: <code>html>body>div:eq(2)>table>tr:eq(4)>td:eq(2)</code><br/>
     * relative: <code>../../../div:eq(4)/table:eq(1)/tr/td:eq(5)</code><br/>
     * 则目的xpath为：<code>html>body>div:eq(2)>div:eq(4)>table:eq(1)>tr>td:eq(5)</code><br/>
     * @param src
     * @param relative
     * @return
     */
    public static String getDestXPath(String src, String relative) {
    	StringBuilder sb = new StringBuilder();
    	List<String> srcList = new ArrayList(Arrays.asList(src.split(">")));
		List<String> relativeList = new ArrayList(Arrays.asList(relative.split("/")));
		boolean isChild = false;    // 源key和value直接是否是父子关系
		boolean firstChild = true;    // 判断最近公共父节点的子节点
    	String lastSrc = srcList.get(0);
		
		for (int i = 0; i < relativeList.size(); i++) {
			String tmp = relativeList.get(i);
			if (tmp.equals("..")) {
				// ".." 表示上一级，需要判断srcList中是否还有上一级目录，如果没有，则为非法的xpath，返回null
				if (srcList.size() > 0) {
					lastSrc = srcList.remove(srcList.size() - 1);
				} else {
					return null;
				}
			} else if (tmp.equals(".")) {
				isChild = true;
			} else {
				if (firstChild && !isChild) {
					// 不包含父子关系的第一个子节点，需要根据相对路径的相对距离进行计算
					Pattern pattern = Pattern.compile("eq\\(([-]?\\d+)\\)");
					Matcher m1 = pattern.matcher(lastSrc);
					Matcher m2 = pattern.matcher(tmp);
					if (m1.find() && m2.find()) {
						int srcIndex = Integer.parseInt(m1.group(1));
						int relativeIndex = Integer.parseInt(m2.group(1));
						tmp = tmp.replaceAll(String.valueOf(relativeIndex), String.valueOf(srcIndex + relativeIndex));
					} else {
						return null;
					}
				}
				srcList.add(tmp);
			}
		}
		
		for (int i = 0; i < srcList.size(); i++) {
			sb.append(srcList.get(i)).append(">");
		}
		
    	return sb.toString().substring(0, sb.toString().length() - 1);
    }
	
	public static void main(String[] args) {
		String src  = "html:eq(0)>body:eq(1)>div:eq(0)>div:eq(3)>div:eq(0)>div:eq(0)>div:eq(0)>div:eq(0)>div:eq(0)>div:eq(8)>h2:eq(3)>span:eq(1)";
		String comp = "html:eq(0)>body:eq(1)>div:eq(0)>div:eq(3)>div:eq(0)>div:eq(0)>div:eq(0)>div:eq(0)>div:eq(0)>div:eq(8)>div:eq(14)";
		
		String relative = getRelativeBetweenXPath(src, comp);
		System.out.println(relative);
		System.out.println(getDestXPath(src, relative));
	}
}
