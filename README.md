# MyAnnotationProcessor

一个简单的view注入器 


主要为了尝试写一个注解解释器，没有ButterKnife那样强大，而且还存在很多不完善的地方。


## 思路
获取注解对象的类型、变量名 生成相应的注入类，最后用放射的方式对被注解对象进行赋值。

## maven
使用时需要自己配置maven库，项目中采用的是本地自定义库

## 关于配置
还需要注意编译器使用的是java 8，如果Android的minSdkVersion低于26，需要加入java1.8编译属性，  
这里还存在问题，因为之前使用butterKnife时并不需要这些配置，具体怎样解决还在研究中。  
因为需要编译生成的java文件需要加入配置 javaCompileOptions.annotationProcessorOptions.includeCompileClasspath true

## 实现
实现基本的注解解释器也简单
- 定义或引入注解
- 定义处理器，该类需要继承AbstractProcessor类，并且注明要处理哪些注解。
- 在resource中需要创建一个处理器的说明文件，内容就是处理器类的完整类名


## 关于测试
这里引入了com.google.testing.compile:compile-testing和com.google.truth:truth。  
感觉很好用可以利用断言直接对生成结果和期望结果进行对比。
