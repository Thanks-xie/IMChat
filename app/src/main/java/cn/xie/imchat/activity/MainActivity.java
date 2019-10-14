package cn.xie.imchat.activity;

import android.animation.ArgbEvaluator;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import cn.xie.imchat.R;
import cn.xie.imchat.adapter.MyAdapter;
import cn.xie.imchat.fragment.CategoryFragment;
import cn.xie.imchat.fragment.FindFragment;
import cn.xie.imchat.fragment.HomeFragment;
import cn.xie.imchat.fragment.MineFragment;
import cn.xie.imchat.utils.Util;
import cn.xie.imchat.view.MyImageView;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private ViewPager mViewPager;
    private MyImageView mIvHome; // tab 消息的imageview
    private TextView mTvHome;   // tab 消息的imageview

    private MyImageView mIvCategory; // tab 通讯录的imageview
    private TextView mTvCategory;

    private MyImageView mIvFind;  // tab 发现的imageview
    private TextView mTvFind;

    private MyImageView mIvMine; // tab 我的imageview
    private TextView mTvMine;

    private ArrayList<Fragment> mFragments;
    private ArgbEvaluator mColorEvaluator;

    private int mTextNormalColor;// 未选中的字体颜色
    private int mTextSelectedColor;// 选中的字体颜色
    private LinearLayout mLinearLayoutHome;
    private LinearLayout mLinearLayoutCategory;
    private LinearLayout mLinearLayoutFind;
    private LinearLayout mLinearLayoutMine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initColor();//也就是选中未选中的textview的color
        initView();// 初始化控件
        initData(); // 初始化数据(也就是fragments)
        initSelectImage();// 初始化渐变的图片
        aboutViewpager(); // 关于viewpager
        setListener(); // viewpager设置滑动监听
    }

    private void initSelectImage() {
        mIvHome.setImages(R.mipmap.home_normal, R.mipmap.home_selected);
        mIvCategory.setImages(R.mipmap.category_normal, R.mipmap.category_selected);
        mIvFind.setImages(R.mipmap.find_normal, R.mipmap.find_selected);
        mIvMine.setImages(R.mipmap.mine_normal, R.mipmap.mine_selected);
    }

    private void initColor() {
        mTextNormalColor = getResources().getColor(R.color.main_bottom_tab_textcolor_normal);
        mTextSelectedColor = getResources().getColor(R.color.main_bottom_tab_textcolor_selected);
    }


    private void setListener() {
        //下面的tab设置点击监听
        mLinearLayoutHome.setOnClickListener(this);
        mLinearLayoutCategory.setOnClickListener(this);
        mLinearLayoutFind.setOnClickListener(this);
        mLinearLayoutMine.setOnClickListener(this);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPs) {
                setTabTextColorAndImageView(position,positionOffset);// 更改text的颜色还有图片
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


    private void setTabTextColorAndImageView(int position, float positionOffset) {
        mColorEvaluator = new ArgbEvaluator();  // 根据偏移量 来得到
        int  evaluateCurrent =(int) mColorEvaluator.evaluate(positionOffset,mTextSelectedColor , mTextNormalColor);//当前tab的颜色值
        int  evaluateThe =(int) mColorEvaluator.evaluate(positionOffset, mTextNormalColor, mTextSelectedColor);// 将要到tab的颜色值
        switch (position) {
            case 0:
                mTvHome.setTextColor(evaluateCurrent);  //设置消息的字体颜色
                mTvCategory.setTextColor(evaluateThe);  //设置通讯录的字体颜色

                mIvHome.transformPage(positionOffset);  //设置消息的图片
                mIvCategory.transformPage(1-positionOffset); //设置通讯录的图片
                break;
            case 1:
                mTvCategory.setTextColor(evaluateCurrent);
                mTvFind.setTextColor(evaluateThe);

                mIvCategory.transformPage(positionOffset);
                mIvFind.transformPage(1-positionOffset);
                break;
            case 2:
                mTvFind.setTextColor(evaluateCurrent);
                mTvMine.setTextColor(evaluateThe);

                mIvFind.transformPage(positionOffset);
                mIvMine.transformPage(1-positionOffset);
                break;

        }
    }

    private void initData() {
        mFragments = new ArrayList<>();
        mFragments.add(new HomeFragment());
        mFragments.add(new CategoryFragment());
        mFragments.add(new FindFragment());
        mFragments.add(new MineFragment());
    }

    private void aboutViewpager() {
        MyAdapter myAdapter = new MyAdapter(getSupportFragmentManager(), mFragments);// 初始化adapter
        mViewPager.setAdapter(myAdapter); // 设置adapter
    }

    private void initView() {
        mLinearLayoutHome = (LinearLayout) findViewById(R.id.ll_home);
        mLinearLayoutCategory = (LinearLayout) findViewById(R.id.ll_categroy);
        mLinearLayoutFind = (LinearLayout) findViewById(R.id.ll_find);
        mLinearLayoutMine = (LinearLayout) findViewById(R.id.ll_mine);
        mViewPager = (ViewPager) findViewById(R.id.vp);
        mIvHome = (MyImageView) findViewById(R.id.iv1);  // tab 微信 imageview
        mTvHome = (TextView) findViewById(R.id.rb1);  //  tab  微信 字

        mIvCategory = (MyImageView) findViewById(R.id.iv2); // tab 通信录 imageview
        mTvCategory = (TextView) findViewById(R.id.rb2);  // tab   通信录 字

        mIvFind = (MyImageView) findViewById(R.id.iv3); // tab 发现 imageview
        mTvFind = (TextView) findViewById(R.id.rb3);   //  tab  发现 字

        mIvMine = (MyImageView) findViewById(R.id.iv4);   // tab 我 imageview
        mTvMine = (TextView) findViewById(R.id.rb4);    // tab   我 字
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_home:
                mViewPager.setCurrentItem(0);
                break;
            case R.id.ll_categroy:
                mViewPager.setCurrentItem(1);
                break;
            case R.id.ll_find:
                mViewPager.setCurrentItem(2);
                break;
            case R.id.ll_mine:
                mViewPager.setCurrentItem(3);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Util.getLoginStatic(MainActivity.this)){
            Util.startChatService(MainActivity.this);
        }
    }
}
