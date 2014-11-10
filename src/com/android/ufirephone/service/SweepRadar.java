/*
 * 还需添加功能：
 * 检测描点是否超出雷达背景
 * 选择合适距离的缩放比例
 * 根据屏幕缩放背景图片
 */
package com.android.ufirephone.service;

import com.android.ufirephone.R;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.PaintFlagsDrawFilter;
import android.util.DisplayMetrics;
import com.android.ufirephone.util.*;
import com.android.ufirephone.wifiservice.WiFiDirectActivity;

import java.util.LinkedList;

public class SweepRadar extends View implements OnClickListener {
	private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private float mRotate;
	private Matrix mMatrix = new Matrix();
	private Shader mShader;
	private Bitmap Bg; // 旋钮背景图片
	private Bitmap Point; // 用户标点图片

	private float centerx; // 旋转的原点x
	private float centery; // 旋转的原点y
	private int bgwidth; // 图片的宽度
	private int bgheight; // 图片的高度
	private int rand; // 雷达半径
	private int screenWidth; // 屏幕宽
	private int screenHeight; // 屏幕高
	private float scaleWidth; // 缩放比
	private float scaleHeight;

	final int rate = 12; // 雷达扫描速度
	final int MAPSCALE = 50; // 位置缩放比
	final int MAXINT = Integer.MAX_VALUE; // 最大的int型
	private PaintFlagsDrawFilter pfd; // 抗锯齿
	private float mValues; // 方向向量数组
	private double longitude; // 本机经度
	private double latitude; // 本机纬度
	private int posionx; // 相对X坐标
	private int posiony; // 相对Y坐标
	private LinkedList<Person> pointlist = new LinkedList<Person>(); // 使用链表保存用户列表

	public SweepRadar(Context context) {
		super(context);
	}

	public SweepRadar(Context context, AttributeSet paramAttributeSet) {
		super(context, paramAttributeSet);
		setFocusable(true);
		setFocusableInTouchMode(true);

		// 必须设置背景
		setResource(R.drawable.component_radar_board,
				R.drawable.component_radar_light_point);

		mShader = new SweepGradient(centerx, centery, new int[] { Color.GREEN,
				Color.TRANSPARENT }, null);
		mPaint.setShader(mShader);
		setOnClickListener(this);
	}

	private void setResource(int bg, int point) // 设置图片资源,外部接口
	{
		BitmapDrawable bgdrawable = (BitmapDrawable) getContext()
				.getResources().getDrawable(bg);
		BitmapDrawable ptdrawable = (BitmapDrawable) getContext()
				.getResources().getDrawable(point);
		Bg = bgdrawable.getBitmap();
		Point = ptdrawable.getBitmap();
		bgwidth = Bg.getWidth(); // 图片大小
		bgheight = Bg.getHeight();
		rand = bgwidth / 2; // 计算雷达半径

		DisplayMetrics dm = new DisplayMetrics(); // 获取手机屏幕大小
		dm = getResources().getDisplayMetrics();
		screenWidth = dm.widthPixels;
		screenHeight = dm.heightPixels;

		scaleWidth = ((float) screenWidth / (float) bgwidth); // 计算缩放比例
		scaleHeight = ((float) screenHeight / (float) bgheight);

		centerx = (float) screenWidth / 2;
		centery = (float) bgheight / 2;

		postInvalidate(); // 非UI线程刷新界面

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) // 测量控件占用控件大小
	{
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension((int) (2 * centerx), (int) (2 * centery));

	}

	@Override
	protected void onDraw(Canvas canvas) {
		Person cur = null; // 暂存链表中的Person
		Paint paint = mPaint;
		paint.setStyle(Style.FILL);

		Paint ppPaint = new Paint();
		ppPaint.setColor(Color.WHITE);
		canvas.drawColor(Color.TRANSPARENT);
		canvas.setDrawFilter(pfd); // 抗锯齿

		canvas.translate(centerx, centery); // 设置原点
		canvas.rotate(-mValues); // 刷新旋转
		canvas.drawBitmap(Bg, -centery, -centery, mPaint); // 绘制指南针盘
		for (int i = 0; i < pointlist.size(); i++) // 依次刷新标点
		{
			cur = pointlist.get(i);
			posionx = (int) cur.longitude;
			posiony = (int) cur.latitude;
			canvas.drawBitmap(Point, posionx, posiony, mPaint); // 绘制用户标点
		}

		canvas.translate(-centerx, -centery); // 设置原点
		mMatrix.setRotate(mRotate, centerx, centery); // 设置旋转中心
		mShader.setLocalMatrix(mMatrix); // 旋转
		mRotate = (mRotate + rate) % 360; // 设置旋转速度

		invalidate();

		canvas.drawCircle(centerx, centery, centery - 5, paint); // 绘制扫描线
		canvas.drawCircle(centerx, centery, 5, ppPaint); // 绘制旋转中心圈

	}

	private Person CoordinateTransform(Person p) // 大地坐标转相对坐标
	{
		Person cp = new Person();
		// 越界检测未添加
		cp.latitude = (p.latitude - latitude) / MAPSCALE;
		cp.longitude = (p.longitude - longitude) / MAPSCALE;

		return cp;
	}

	private int FindNode(Person p) // 查询某个标点是位置
	{
		for (int i = 0; i < pointlist.size(); i++) // 查询位置
		{
			if (pointlist.get(i).personId == p.personId) {
				return i;
			}
		}

		return MAXINT;
	}

	public void SetCompassDegree(float dgree) // 设置指南针旋转角度
	{
		mValues = dgree;
		invalidate();
	}

	public boolean AddCompassNode(Person p) // 添加/刷新用户标点(添加节点时保存转换值)
	{
		Person np = new Person();
		np = CoordinateTransform(p); // 坐标转换

		if (MAXINT == FindNode(p)) // 若不存在则添加
		{
			pointlist.add(np); // 添加节点
		} else {
			for (int i = 0; i < pointlist.size(); i++) // 查询位置
			{
				if (pointlist.get(i).personId == np.personId) {
					pointlist.set(i, np);
					break;
				}
			}
		}
		invalidate();

		return true;
	}

	public boolean DelCompassNode(Person p) // 删除用户标点
	{
		if (MAXINT != FindNode(p)) // 若存在则删除
		{
			pointlist.remove(p);
			invalidate();
		} else {
			return false;
		}

		return true;
	}

	public boolean SetCompassCurNode(Person p) // 设置当前活动用户标点
	{

		// 使用 component_radar_light_point_sel.png 标注
		return true;
	}

	public boolean SetCompassSelfNode(Person me) // 设置本机位置标点
	{
		longitude = me.longitude;
		latitude = me.latitude;

		return true;
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent(Intent.ACTION_VIEW, null, v.getContext(), WiFiDirectActivity.class);
		//intent.setClass(this, WiFiDirectActivity.class);
		Context context = v.getContext();  
		context.startActivity(intent);
		
		//invalidate();
	}

}
