# Inject
注解模块

练习注解使用方法

使用方式
```gradle

allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}

android {
	...
	defaultConfig {
		...
		javaCompileOptions { 
			annotationProcessorOptions { 
				includeCompileClasspath = true 
			}
		}
  	}
 }

dependencies {
	implementation 'com.github.lakehubo:Inject:1.0.2'
}
  
  ```
