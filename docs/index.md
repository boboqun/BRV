
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

#### Step 1. configure your app to use data binding

Enable the dataBinding build option in your build.gradle file in the app module, as shown in the following example:

```groovy
apply plugin: "kotlin-kapt" // Using the kapt plugin to generate dataBinding

android {
	/.../
    buildFeatures.dataBinding = true
}
```

#### Step 2. create an variable in your Item layout


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
Then bind data to UI items, as shown in the highlighted line above.  


#### Step 3. Set an `id` for data binding to xml


<!-- 第三步, 赋值一个用于`自定绑定数据到XML布局的Id`(DataBinding基础知识) -->
<!-- > rv是一个列表. 里面的models是一个list集合, 每个元素对应一个item. dataBinding会根据你`赋值的Id`自动绑定models中元素到xml中赋值 <br> -->
> `rv` is the RecyclerView. `models` is a list collection. Each item in models corresponds to a RecyclerView item. According to the `id` you specify, dataBinding will automatically bind your data to XML
<br>

1. The Data Binding Library generates accessor methods for each variable declared in the layout.  
Remember to declare a variable `name="m"` in the `<data>` tag, so `BR.m` will be generated automatically.  

    <img src="https://i.loli.net/2021/08/14/rgX12ZSwkVMqQG3.png" width="450"/>  

2. Please double check when importing the module BR class, so that BRV can automatically bind all your data according to the `id` that you specify.  

    <img src="https://i.loli.net/2021/08/14/VhYlAp1J7ZR9rIs.png" width="350"/>
    <img src="https://i.loli.net/2021/08/14/Yh5Ge1qQIObJpDn.png" width="350"/>  

3. If nothing changes, please click the green smartisan button to `make project`  
    <img src="https://i.loli.net/2021/08/14/IEh3H8VaFM6d1LR.png" width="150"/>

> `m` (m is short name of model) can rename to any other name. if you set `name="data"` then you can use `BR.data`.  

> Like `BR.data` and commonly `R.id.data` in Android. These are all ID constants. Each constant is essentially an integer value.  

> If you declare `BRV.model = BR.m` in your application class, all of your item layouts that use BRV must use `name="m"` in the <data> tag.  

> `onBind` allows you to manually bind data, but it must be more tedious than having it done automatically. So we recommended to use `m`  


```kotlin
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        // init the default id of BindingAdapter
        // only if DataBinding is used
        BRV.modelId = BR.m
    }
}
```

Using this strategy eliminates the requirement for manual data processing.
<!-- 这种方式创建列表无需处理数据 -->

```kotlin
rv.linear().setup {
    addType<SimpleModel>(R.layout.item_simple)
}.models = getData()
```

Despite how sophisticated the third method looks to be in the document, it actually has the least amount of code and is the most decoupled.


> a Extensions Demo with DataBinding:  [DataBindingComponent.kt](https://github.com/liangjingkanji/Engine/blob/master/engine/src/main/java/com/drake/engine/databinding/DataBindingComponent.kt)<br>
> read more about DataBinding [The most complete instructions for DataBinding](https://juejin.cn/post/6844903549223059463/)

