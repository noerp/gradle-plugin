package org.noerp.gradle.extention

public class NoerpExtention {
	
	String name
	String version
	String description

	Map<String, ?> requireCache = [:]
	
	/**
	 * 
	 * @param notation
	 * @return
	 */
	public require(String notation){
		def (vendor, component, version) = notation.split(":")
		this.cache(vendor, component, version)
	}
	
	/**
	 * 
	 * @param config
	 * @return
	 */
	public require(Map config){
		this.cache(config.vendor, config.component, config.version)
	}
	
	/**
	 * 
	 * @param vendor
	 * @param module
	 * @param version
	 * @return
	 */
	private cache(vendor, component, version){
		
		def cacheKey = "$vendor:$component:$version"
		
		if(!requireCache.containsKey(cacheKey)){
			def newVendorComponent = new VendorComponent("vendor": vendor, "component": component, "version":version)
			requireCache.put(cacheKey, newVendorComponent);
		}
	}
}