package edu.itec.ggcoke.xpath.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class XPathEntity {
	private String relativeXPath;
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

	private int weight;
	
	public XPathEntity(String relativeXPath, int weight) {
		this.relativeXPath = relativeXPath;
		this.weight = weight;
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
	 * 获取comp相对于src的xpath路径
	 * 例如，有两个xpath：<br/>
	 * src: <code>html>body>div:eq(2)>table>tr:eq(4)>td:eq(2)</code><br/>
	 * comp: <code>html>body>div:eq(2)>div:eq(4)>table:eq(1)>tr>td:eq(5)</code><br/>
	 * 则comp相对于src的xpath路径为<code>../../../div:eq(4)/table:eq(1)/tr/td:eq(5)</code>
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
		
		while (srcList.size() > 0) {
			sb.append("../");
			srcList.remove(0);
		}
		
		while (compList.size() > 0) {
			sb.append(compList.remove(0) + "/");
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
    	
		for (int i = 0; i < relativeList.size(); i++) {
			String tmp = relativeList.get(i);
			if (tmp.equals("..")) {
				// ".." 表示上一级，需要判断srcList中是否还有上一级目录，如果没有，则为非法的xpath，返回null
				if (srcList.size() > 0) {
					srcList.remove(srcList.size() - 1);
				} else {
					return null;
				}
			} else {
				srcList.add(tmp);
			}
		}
		
		for (int i = 0; i < srcList.size(); i++) {
			sb.append(srcList.get(i)).append(">");
		}
		
    	return sb.toString().substring(0, sb.toString().length() - 1);
    }
	
	public static void main(String[] args) {
		String src = "html>body>div:eq(2)>table>tr:eq(4)>td:eq(2)";
		String comp = "../../../../../../../../../../div:eq(4)/table:eq(1)/tr/td:eq(5)";
		System.out.println(getDestXPath(src, comp));
	}
}
