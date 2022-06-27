
<!-- 本框架在不影响RecyclerView的任何函数组件使用基础上开发. 本框架也将一直保持维护状态 -->
BRV is a LTS library based on RecyclerView, All original functions and methods are supported.
<br>
<p align="center"><strong>strongly recommended for pull requests to this library and document revise. <br>
For editing this page, just click the top right pen icon.  ↗</strong></p>
<br>

## create a simple list

```kotlin
rv.linear().setup {
    addType<SimpleModel>(R.layout.item_simple)
}.models = getData()
```
<sup>*</sup>`BRV` referred to this library. `RV` referred to RecyclerView. `setup` is just a simple function for creating BindingAdapter

## filling data

`BRV` offers three easy ways to fill in data.`Model` refers to a JavaBean / POJO / Data class in this document
<!-- BRV支持三种方式, 灵活使用; 这里提及的Model就等同于数据类/JavaBean/POJO -->


### 1) Callback

fill data in `onBind` block

```kotlin
rv.linear().setup {
    addType<SimpleModel>(R.layout.item_simple)
    onBind {
        findView<TextView>(R.id.tv_simple).text = getModel<SimpleModel>().name
    }
}.models = getData()
```





### 2) Implementing an Interface

**❎ not  recommended**

Implementing the `ItemBind` Interface, override `onBind` method, fill in data;

While this is widely supported in this kind of library, like BRVAH. We generally don't recommend using this feature, since Models should only be used for storing data and computation logic, not for UI elements.

```kotlin
class SimpleModel(var name: String = "BRV") : ItemBind {

    override fun onBind(holder: BindingAdapter.BindingViewHolder) {
        val appName = holder.context.getString(R.string.app_name)
        holder.findView<TextView>(R.id.tv_simple).text = appName + itemPosition
    }
}
```


### 3) DataBinding

**✅ recommended**

DataBinding offers a flexible and powerful way to bind data to your UIs,that allows you to bind UI components in your XML layouts to data sources in your app using a declarative format rather than programmatically, reducing boilerplate code.
通过DataBinding数据绑定形式自动填充数据, 推荐, 这是代码量最少最灵活的一种方式.

第一步, 启用DataBinding, 在module中的build.gradle文件中

```groovy
apply plugin: "kotlin-kapt" // kapt插件用于生成dataBinding

android {
	/.../
    buildFeatures.dataBinding = true
}
```

第二步, 在Item的布局文件中创建变量, 然后绑定变量到视图控件上

```xml hl_lines="24"
<layout xmlns:android="http://schemas.android.com/apk/res/android">

    <data>

        <variable
            name="m"
            type="com.drake.brv.sample.model.SimpleModel" />
    </data>

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <FrameLayout
            android:id="@+id/item"
            android:layout_width="match_parent"
            android:layout_height="100dp"
            android:layout_margin="16dp"
            android:background="@drawable/bg_card"
            android:foreground="?selectableItemBackgroundBorderless">

            <TextView
				android:id="@+id/tv_simple"
                android:text="@{String.valueOf(m.name)}"
                android:gravity="center"
                android:layout_width="match_parent"
                android:layout_height="match_parent" />

        </FrameLayout>

    </LinearLayout>
</layout>
```
选中行是DataBinding使用方法

第三步, 赋值一个用于`自定绑定数据到XML布局的Id`(DataBinding基础知识)

> rv是一个列表. 里面的models是一个list集合, 每个元素对应一个item. dataBinding会根据你`赋值的Id`自动绑定models中元素到xml中赋值 <br>
<br>

1. 注意要先在某个布局或Item布局声明`<layout>`布局中的变量`name="m"`, `BR.m`才能被生成 <br>
   <img src="https://i.loli.net/2021/08/14/rgX12ZSwkVMqQG3.png" width="450"/>
1. 导包注意导入你所在module的BR, 这样所有使用该Id来声明数据模型的布局都会被BRV自动绑定数据 <br>
   <img src="https://i.loli.net/2021/08/14/VhYlAp1J7ZR9rIs.png" width="350"/>
   <img src="https://i.loli.net/2021/08/14/Yh5Ge1qQIObJpDn.png" width="350"/>
1. 如果依然没有生成请`make project`(即图中绿色小锤子图标) <br>
   <img src="https://i.loli.net/2021/08/14/IEh3H8VaFM6d1LR.png" width="150"/>

> m(m是model的简称)可以是任何其他的名称, model或者sb都可以, 比如你`name="data"`, 那么你就应该使用BR.data <br>
> BR.data和Android中常见的`R.id.data`都属于Id常量, 本质上都是Int值. 你可以点击查看BR.m源码<br>
> 但是一旦声明`BRV.model = BR.m`你的所有BRV使用的item布局都得使用`name="m"`来声明数据模型, 否则会无法自动绑定 <br>
> 当然你也可以在`onBind`里面手动绑定, 但是肯定比自动麻烦, 而且名称本身只是代号我建议都使用m <br>

```kotlin
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        // 初始化BindingAdapter的默认绑定ID, 如果不使用DataBinding并不需要初始化
        BRV.modelId = BR.m
    }
}
```

这种方式创建列表无需处理数据

```kotlin
rv.linear().setup {
    addType<SimpleModel>(R.layout.item_simple)
}.models = getData()
```

别看文档中第三种方式复杂, 实际第三种方式代码量最少, 同时最解耦

> 使用DataBinding可以复制或者引用我的常用自定义属性:  [DataBindingComponent.kt](https://github.com/liangjingkanji/Engine/blob/master/engine/src/main/java/com/drake/engine/databinding/DataBindingComponent.kt)<br>
> 如果你想更加了解DataBinding请阅读[DataBinding最全使用说明](https://juejin.cn/post/6844903549223059463/)

