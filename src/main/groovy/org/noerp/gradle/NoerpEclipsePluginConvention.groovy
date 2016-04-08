package org.noerp.gradle

import org.gradle.api.JavaVersion

/**
 * rootProject默认配置
 * 
 * @author Kevin
 *
 */
class NoerpEclipsePluginConvention {
	
	/**
	 * java兼容版本
	 */
	private JavaVersion srcCompat
	
	/**
	 * java目标版本
	 */
	private JavaVersion targetCompat
	
	/**
	 * Returns the source compatibility used for compiling Java sources.
	 */
	JavaVersion getSourceCompatibility() {
		srcCompat ?: JavaVersion.current()
	}

	/**
	 * Sets the source compatibility used for compiling Java sources.
	 *
	 * @value The value for the source compatibility as defined by {@link JavaVersion#toVersion(Object)}
	 */
	void setSourceCompatibility(def value) {
		srcCompat = JavaVersion.toVersion(value)
	}

	/**
	 * Returns the target compatibility used for compiling Java sources.
	 */
	JavaVersion getTargetCompatibility() {
		targetCompat ?: sourceCompatibility
	}

	/**
	 * Sets the target compatibility used for compiling Java sources.
	 *
	 * @value The value for the target compatibilty as defined by {@link JavaVersion#toVersion(Object)}
	 */
	void setTargetCompatibility(def value) {
		targetCompat = JavaVersion.toVersion(value)
	}
}