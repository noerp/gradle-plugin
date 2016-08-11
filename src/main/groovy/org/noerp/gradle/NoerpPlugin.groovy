package org.noerp.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Action;
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.GroovyPlugin
import org.gradle.api.tasks.bundling.Jar
import org.gradle.plugins.ide.eclipse.EclipsePlugin
import org.gradle.internal.reflect.Instantiator;

import org.noerp.gradle.conventions.NoerpSubConvention
import org.noerp.gradle.task.*

import javax.inject.Inject;

/**
 * NoERP插件
 * 
 * @author Kevin
 *
 */
class NoerpPlugin implements Plugin<Project> {

	/**
	 * 插件名称
	 */
	static final String NOERP_PLUGIN_NAME = "noerp"

	/**
	 * 根项目
	 */
	private Project project
	
	private final Instantiator instantiator;
	
	@Inject
	public NoerpPlugin(Instantiator instantiator) {
		this.instantiator = instantiator;
	}
	
	void apply(final Project project) {
		this.project = project
		addPluginConvention()
		init()
		setupTasks()
	}

	/**
	 * 默认配置
	 */
	private void addPluginConvention() {
		NoerpPluginConvention noerpPluginConvention = instantiator.newInstance(NoerpPluginConvention.class, project)
		project.getConvention().getPlugins().put("noerp", noerpPluginConvention)
		project.subprojects.each {subproject->
			subproject.convention.plugins.noerp = new NoerpSubConvention(subproject)
		}
	}

	/**
	 * 初始化
	 */
	void init() {
		
		//只有启用gradle的eclipse插件,并且未启用java插件，才启用自定义IDE插件
		if(project.plugins.hasPlugin(EclipsePlugin)){
			if(!project.plugins.hasPlugin(JavaPlugin)){
				project.apply(plugin: NoerpEclipsePlugin)
			}
		}
		
		//子项目配置group
		project.subprojects.each {subproject->
			subproject.configurations {
				runlib {
					description = "NoERP copy libs for run"
					transitive = false
				}
				compile { extendsFrom runlib }
			}
		}
	}

	/**
	 * 设置任务
	 */
	void setupTasks(){
		
		NoerpPluginConvention noerpConvention = project.getConvention().getPlugin(NoerpPluginConvention.class);

		project.tasks.create("install", InstallTask)
		project.tasks.create("update", UpdateTask)

		project.task("start", type: RunTask){
			description = "Start your application"
			configTask(noerpConvention)
		}

		project.task("start-debug", type: RunTask){
			description = "Start your application with debug model"
			debug = true
			configTask(noerpConvention)
		}

		project.task("stop", type: RunTask){
			description = "Stop noerp application"
			args = ["-shutdown"]
			configTask(noerpConvention)
		}
		
		project.task("status", type: RunTask){
			description = "Get current status of your application"
			args = ["-status"]
			configTask(noerpConvention)
		}
		
		project.task("load-seed", type: RunTask){
			description = "Load ONLY the seed data for your application"
			args = ["load-data", "readers=seed"]
			configTask(noerpConvention)
		}
		
		project.task("load-file", type: RunTask){
			description = "Load data using the command line argument 'data-file' to load data "
						  
			args = ["load-data", "readers=seed"]
			configTask(noerpConvention)
		}
		
		project.task('load-admin-user-login', type: RunTask){
			description = "Create admin user with temporary password equal to noerp. You must provide userLoginId"
			/*
			args = ["load-data", "file=/runtime/tmp/AdminUserLoginData.xml"]
			configTask(noerpConvention)
			
			doFirst {
				copy {
					from ("${rootDir}/vendor/noerp/resources/templates/AdminUserLoginData.xml") {
						filter(ReplaceTokens, tokens: [userLoginId: userLoginId])
					}
					into "${rootDir}/runtime/tmp/"
				}
			}
			
			doLast {
				delete("${rootDir}/runtime/tmp/AdminUserLoginData.xml");
			}
			*/
		}
		
		project.task("run-test", type: RunTask){
			description = "Run NoERP default tests; you have to manually execute 'gradle load-demo' before (and if needed even clear your data before) and see results in runtime/logs/test-results/html/all-tests.html. Use -Dportoffset=portNumber to shift all ports with the portNumber value."
			args = ["test"]
			configTask(noerpConvention)
		}

		/*
		 project.task("create-component", type: CreateComponentTask){
		 }
		 project.task("generate-CRUD", type: CreateComponentTask){
		 }*/
	}
}
