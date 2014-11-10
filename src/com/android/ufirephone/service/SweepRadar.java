/*
 * ������ӹ��ܣ�
 * �������Ƿ񳬳��״ﱳ��
 * ѡ����ʾ�������ű���
 * ������Ļ���ű���ͼƬ
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
	private Bitmap Bg; // ��ť����ͼƬ
	private Bitmap Point; // �û����ͼƬ

	private float centerx; // ��ת��ԭ��x
	private float centery; // ��ת��ԭ��y
	private int bgwidth; // ͼƬ�Ŀ��
	private int bgheight; // ͼƬ�ĸ߶�
	private int rand; // �״�뾶
	private int screenWidth; // ��Ļ��
	private int screenHeight; // ��Ļ��
	private float scaleWidth; // ���ű�
	private float scaleHeight;

	final int rate = 12; // �״�ɨ���ٶ�
	final int MAPSCALE = 50; // λ�����ű�
	final int MAXINT = Integer.MAX_VALUE; // ����int��
	private PaintFlagsDrawFilter pfd; // �����
	private float mValues; // ������������
	private double longitude; // ��������
	private double latitude; // ����γ��
	private int posionx; // ���X����
	private int posiony; // ���Y����
	private LinkedList<Person> pointlist = new LinkedList<Person>(); // ʹ���������û��б�

	public SweepRadar(Context context) {
		super(context);
	}

	public SweepRadar(Context context, AttributeSet paramAttributeSet) {
		super(context, paramAttributeSet);
		setFocusable(true);
		setFocusableInTouchMode(true);

		// �������ñ���
		setResource(R.drawable.component_radar_board,
				R.drawable.component_radar_light_point);

		mShader = new SweepGradient(centerx, centery, new int[] { Color.GREEN,
				Color.TRANSPARENT }, null);
		mPaint.setShader(mShader);
		setOnClickListener(this);
	}

	private void setResource(int bg, int point) // ����ͼƬ��Դ,�ⲿ�ӿ�
	{
		BitmapDrawable bgdrawable = (BitmapDrawable) getContext()
				.getResources().getDrawable(bg);
		BitmapDrawable ptdrawable = (BitmapDrawable) getContext()
				.getResources().getDrawable(point);
		Bg = bgdrawable.getBitmap();
		Point = ptdrawable.getBitmap();
		bgwidth = Bg.getWidth(); // ͼƬ��С
		bgheight = Bg.getHeight();
		rand = bgwidth / 2; // �����״�뾶

		DisplayMetrics dm = new DisplayMetrics(); // ��ȡ�ֻ���Ļ��С
		dm = getResources().getDisplayMetrics();
		screenWidth = dm.widthPixels;
		screenHeight = dm.heightPixels;

		scaleWidth = ((float) screenWidth / (float) bgwidth); // �������ű���
		scaleHeight = ((float) screenHeight / (float) bgheight);

		centerx = (float) screenWidth / 2;
		centery = (float) bgheight / 2;

		postInvalidate(); // ��UI�߳�ˢ�½���

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) // �����ؼ�ռ�ÿؼ���С
	{
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension((int) (2 * centerx), (int) (2 * centery));

	}

	@Override
	protected void onDraw(Canvas canvas) {
		Person cur = null; // �ݴ������е�Person
		Paint paint = mPaint;
		paint.setStyle(Style.FILL);

		Paint ppPaint = new Paint();
		ppPaint.setColor(Color.WHITE);
		canvas.drawColor(Color.TRANSPARENT);
		canvas.setDrawFilter(pfd); // �����

		canvas.translate(centerx, centery); // ����ԭ��
		canvas.rotate(-mValues); // ˢ����ת
		canvas.drawBitmap(Bg, -centery, -centery, mPaint); // ����ָ������
		for (int i = 0; i < pointlist.size(); i++) // ����ˢ�±��
		{
			cur = pointlist.get(i);
			posionx = (int) cur.longitude;
			posiony = (int) cur.latitude;
			canvas.drawBitmap(Point, posionx, posiony, mPaint); // �����û����
		}

		canvas.translate(-centerx, -centery); // ����ԭ��
		mMatrix.setRotate(mRotate, centerx, centery); // ������ת����
		mShader.setLocalMatrix(mMatrix); // ��ת
		mRotate = (mRotate + rate) % 360; // ������ת�ٶ�

		invalidate();

		canvas.drawCircle(centerx, centery, centery - 5, paint); // ����ɨ����
		canvas.drawCircle(centerx, centery, 5, ppPaint); // ������ת����Ȧ

	}

	private Person CoordinateTransform(Person p) // �������ת�������
	{
		Person cp = new Person();
		// Խ����δ���
		cp.latitude = (p.latitude - latitude) / MAPSCALE;
		cp.longitude = (p.longitude - longitude) / MAPSCALE;

		return cp;
	}

	private int FindNode(Person p) // ��ѯĳ�������λ��
	{
		for (int i = 0; i < pointlist.size(); i++) // ��ѯλ��
		{
			if (pointlist.get(i).personId == p.personId) {
				return i;
			}
		}

		return MAXINT;
	}

	public void SetCompassDegree(float dgree) // ����ָ������ת�Ƕ�
	{
		mValues = dgree;
		invalidate();
	}

	public boolean AddCompassNode(Person p) // ���/ˢ���û����(��ӽڵ�ʱ����ת��ֵ)
	{
		Person np = new Person();
		np = CoordinateTransform(p); // ����ת��

		if (MAXINT == FindNode(p)) // �������������
		{
			pointlist.add(np); // ��ӽڵ�
		} else {
			for (int i = 0; i < pointlist.size(); i++) // ��ѯλ��
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

	public boolean DelCompassNode(Person p) // ɾ���û����
	{
		if (MAXINT != FindNode(p)) // ��������ɾ��
		{
			pointlist.remove(p);
			invalidate();
		} else {
			return false;
		}

		return true;
	}

	public boolean SetCompassCurNode(Person p) // ���õ�ǰ��û����
	{

		// ʹ�� component_radar_light_point_sel.png ��ע
		return true;
	}

	public boolean SetCompassSelfNode(Person me) // ���ñ���λ�ñ��
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
