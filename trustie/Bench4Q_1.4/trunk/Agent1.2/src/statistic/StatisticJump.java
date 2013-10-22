/**
 * =========================================================================
 * 					Bench4Q_Script version 1.3.1
 * =========================================================================
 * 
 * Bench4Q is available on the Internet at http://www.trustie.net/projects/project/show/Bench4Q
 * You can find latest version there. 
 * 
 * Bench4Q_Script adds a script module for Internet application to Bench4Q
 * you can access it at http://www.trustie.com/projects/project/show/Bench4Q_Script
 * 
 * Distributed according to the GNU Lesser General Public Licence. 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by   
 * the Free Software Foundation; either version 2.1 of the License, or any
 * later version.
 * 
 * SEE Copyright.txt FOR FULL COPYRIGHT INFORMATION.
 * 
 * This source code is distributed "as is" in the hope that it will be
 * useful.  It comes with no warranty, and no author or distributor
 * accepts any responsibility for the consequences of its use.
 *
 *
 * This version is a based on the implementation of TPC-W from University of Wisconsin. 
 * This version used some source code of The Grinder.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
 *  * Initial developer(s): WuYulong, Wangsa , Tianfei , Zhufeng
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
 * 
 */

package src.statistic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
/**
 * ���ݽű�����һ����ά��ת����ʵ����������ת
 * @author WU Yulong
 *
 */
public class StatisticJump {
	private double[][] m_statTable;
	/**
	 * ��script����ҳ�ĳ���˳��洢����
	 */
	protected ArrayList<Integer> scriptPageTrace;
	public StatisticJump() {
		scriptPageTrace = new ArrayList<Integer>(128);
	}
	/**
	 * ���������ݵĳ�ʼ������
	 * @param scriptPage �ű���ȡ��Ψһ�Դ洢�ġ���ҳ��URL
	 * @param scriptState �Խű��г���˳��洢��ǰ����������ҳ��scriptPage�е��±�
	 */
	public void init(ArrayList<String> scriptPage,
			ArrayList<Integer> scriptState) {
		initTableStruc(scriptPage.size());
		initTrace(scriptState);
		initStaticTable();
	}
	/**
	 * ���ݵ�ǰ������ҳ���±꣬���ű����ʻ�ȡ��һ����index
	 * @param index ��ǰ������ҳ���±�
	 * @return ��һ����index
	 */
	public int getNextJump(int index) {
		//����һ��0-1֮���α�����
		Random rnd = new Random(System.currentTimeMillis());
		double rand = rnd.nextDouble();
		
		if(rand < m_statTable[index][0]) {
			return 0;
		}
		double prev = m_statTable[index][0];
		for(int j=1; j<m_statTable[index].length; j++) {
			if(m_statTable[index][j]==0.0) 
				continue;
			if(prev <= rand && rand < prev + m_statTable[index][j]) {
				return j;
			}
			else {
				prev += m_statTable[index][j];
			}
		}
		try {
			throw new Exception("Data or logical Error.");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
	/**
	 * Ϊm_statTable���ٿռ�
	 * @param size ��ά�ռ�size
	 */
	protected void initTableStruc(int size) {
		m_statTable = new double[size][size];
		for(int i=0; i<size; i++) {
			for(int j=0; j<size; j++) {
				m_statTable[i][j] = 0.0; 
			}
		}
	}
	/**
	 * ����scriptState����ű��е���ҳִ��·��
	 * @param scriptState
	 */
	private void initTrace(ArrayList<Integer> scriptState) {
		int prev=-1, index=-1;
		Iterator<Integer> k = scriptState.iterator();
		index = prev = k.next();
		scriptPageTrace.add(index);
		while(k.hasNext()) {
			index = k.next();
			if(index != prev) {
				scriptPageTrace.add(index);
				prev = index;
			}
		}
	}
	/**
	 * ��ʼ����ת���ʴ洢�ṹm_statTable
	 */
	protected void initStaticTable() {
		int prev = -1, index = -1;
		Iterator<Integer> k = scriptPageTrace.iterator();
		prev = k.next();
		while(k.hasNext()) {
			index = k.next();
			m_statTable[prev][index]++;
			prev = index;
		}
		//Ϊ���γ�һ���պ�·���������һ����ҳû����ת��������ҳ�ļ�¼ʱ�����˹�����һ����ת����һ����ҳ�ļ�¼
		breakFor: {
			for(int i=0; i<m_statTable[m_statTable.length-1].length; i++) {
				if(m_statTable[m_statTable.length-1][i]!=0.0)
					break breakFor;
			}
			m_statTable[m_statTable.length-1][0] = 1.0;
		}
		convertTable();
	}
	/**
	 * ��m_statTable�еļ���ת��Ϊ����
	 */
	private void convertTable() {
		for(int i=0; i<m_statTable.length; i++) {
			double sum=0;
			for(int j=0; j<m_statTable[i].length; j++) {
				sum += m_statTable[i][j];
			}
			for(int j=0; j<m_statTable[i].length; j++) {
				if(m_statTable[i][j]!=0)
					m_statTable[i][j] /= sum;
			}
		}
	}
}
