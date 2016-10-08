package org.noerp.gradle.conventions

import org.gradle.api.Project

/**
 * 子项目默认配置
 * 
 * @author Kevin
 *
 */
class NoerpSubConvention {
	
	/**
	 * 当前项目
	 */
	private Project project

	/**
	 * 构造方法
	 * @param project
	 */
	public NoerpSubConvention(Project project) {
		this.project = project
	}

	/**
	 * 解析vendor方法
	 * 
	 * @param notation
	 * @return
	 */
	Project vendor(String notation) {

		def args = notation.split(":")
		def vendorName = args[0]
		def module = args[1]
		def version = args[2]
		
		//todo: download vendor

		return project.project(":" + vendorName + "-" + module)
	}
}