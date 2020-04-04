# apple-connect-java

## 说明
该代码片段是一块独立代码片段，是个简单接口调用SDK。
采用Java封装苹果开放平台的接口，所有能力都通过AppleConnectFluentClient类来输出

## 设计思路
苹果开放平台调用认证需要JWT Token ,在SDK中实现了该椭圆双曲线算法（ES256），请求接口前
都需要获取JWT Token. Token 生成之后在guava 中缓存20分钟，接口可以复用。

## 如何使用
首先开发者 需要创建AppleConnectFluentClient对象 传入参数为三个，issuer，key_id,key
这三个参数可从App Store Connect 后台创建并下载

## 目前已经实现的能力
1. 生成JWT Token
2. 获取产品的所有build信息
3. 获取指定的build信息T
4. 获取某个build下的所有测试组信息
5. 获取某个测试组下测试人员的数量

## 代码包说明
constants/ApiContant 类存放接口名称
utils/*  存放文件读取，json 解析，http 请求发送等工具类
AppleConnectFluentClient 对外输出接口能力的客户端类
AppleConnectFluentClientTest 功能测试类

## 收款码

## 支付宝

![收款码](https://github.com/lipeishen/apple-connect-java/blob/master/img/alipay.jpeg)

## 微信

![收款码](https://github.com/lipeishen/apple-connect-java/blob/master/img/weichat.png)
