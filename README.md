# RangeSeekBar 

![image](https://github.com/iielse/RangeSeekBar/blob/master/app/seek_001.png)

![image](https://github.com/iielse/RangeSeekBar/blob/master/app/seekbar_001.gif)

##通过布局文件设置属性
```
<org.ielse.widget.RangeSeekBar
	android:id="@+id/rsb_3"
	android:layout_width="200dip"
	app:min="0"         规则：可以拖动范围中的最小值
	app:max="20"        规则：可以拖动范围中的最大值
	app:cells="5"       刻度支持：将最大值和最小值四等分，比如1-20分成5份即0，5，10，15，20
	app:reserve="1"		保留距离支持：如果cells值为1，那么reserve最小保留1；此处cells分隔的最小单元为5，reserve将四舍五入为5
	app:seekBarResId="@mipmap/icon_seekbar"  UI:按钮样式
	app:lineColorSelected="#FF5151"			 UI:选中范围颜色
	app:lineColorEdge="#FF9797"              UI:边缘配色
	android:layout_height="36dip"/>
```

##通过代码设置属性

`RangeSeekBar rsb1 = (RangeSeekBar) findViewById(R.id.rsb_1);`

`rsb1.setRules(0, 100, 20, 1);` // 设置规则，逻辑同上

##设置初始默认值
`rsb1.setValue(15, 66);`  // 注意不符合规则rules的参数将抛出异常，RangeSeekBar不背这种锅

##获取响应值
* a)主动

`float[] results = rsb1.getCurrentRange();`
results[0] 表示当前选择的最小值
results[1] 表示当前选择的最大值
* b)回调
```
rsb1.setOnRangeChangedListener(new RangeSeekBar.OnRangeChangedListener() {
	@Override
	public void onRangeChanged(RangeSeekBar view, float min, float max) {
		t1.setText(min + " - " + max);
	}
});
```

##更加详细的源码分析和实现思路讲解
[请戳这里](http://blog.csdn.net/bfbx5173/article/details/51869776) 

##其它 
希望你喜欢我的作品。`Star`是对我的最大支持. 谢谢



