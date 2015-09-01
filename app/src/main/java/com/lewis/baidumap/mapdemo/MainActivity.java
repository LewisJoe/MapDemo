package com.lewis.baidumap.mapdemo;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;

import java.util.List;


public class MainActivity extends ActionBarActivity {

    private final static String TAG = "BaiduLocationApiDem";

    MapView mMapView = null;
    BaiduMap mBaiduMap = null;
    public LocationClient mLocationClient = null;
    public BDLocationListener mListener = new MyLocationListener();
    private double latitudePoint;
    private double longitudePoint;
    private BitmapDescriptor bitmap;
    private LatLng point = new LatLng(0.0,0.0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化SDK引用的Context全局变量
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要在setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.bmpView);
        mBaiduMap = mMapView.getMap();
        //TODO:定位功能
        //声明LocationClient类
        mLocationClient = new LocationClient(getApplicationContext());
        //注册监听函数
        mLocationClient.registerLocationListener(mListener);
        initLocation();
        //开始定位
        mLocationClient.start();
        //构建Marker图标
        bitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding);
    }

    /**
     * 配置定位SDK参数
     * 包括定位模式(高精度定位模式，低功耗定位模式和仅用设备定位模式)
     * 返回坐标类型
     * 是否打开GPS
     * 是否返回地址信息
     * 位置语义化信息
     * POI信息
     */
    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        //定位模式
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //返回坐标类型
        option.setCoorType("bd09ll");
        int span = 1000;
        //默认0，即仅定位1次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setScanSpan(span);
        //设置是否需要地址信息
        option.setIsNeedAddress(true);
        //设置是否使用gps
        option.setOpenGps(true);
        //设置是否当gps有效时按照1s1次频率输出gps结果
        option.setLocationNotify(true);
        //设置是否需要位置语义化结果
        option.setIsNeedLocationDescribe(true);
        //设置是否需要POI结果
        option.setIsNeedLocationPoiList(true);
        //定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死进程
        option.setIgnoreKillProcess(false);
        //设置是否手机cache信息
        option.SetIgnoreCacheException(false);
        //设置是否需要过滤gps仿真结果
        option.setEnableSimulateGps(false);
        mLocationClient.setLocOption(option);
    }
    /**
     * 接收异步返回的定位结果
     */
    public class MyLocationListener implements BDLocationListener{

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            //receive location
            StringBuffer sb = new StringBuffer(256);
            sb.append("time:");
            sb.append(bdLocation.getTime());
            sb.append("\nerror code:");
            sb.append(bdLocation.getLocType());
            sb.append("\nlatitude:");
            sb.append(bdLocation.getLatitude());
            latitudePoint = bdLocation.getLatitude();
            sb.append("\nlongitude:");
            sb.append(bdLocation.getLongitude());
            longitudePoint = bdLocation.getLongitude();
            //TODO:标注覆盖物，用于显示自己的位置
            //定义Marker坐标点
            point = new LatLng(latitudePoint,longitudePoint);
            Log.d("Point",latitudePoint+","+longitudePoint);
            //构建MarkerOption，用于在地图上添加Marker
            OverlayOptions option = new MarkerOptions().position(point).icon(bitmap);
            //在地图上添加Marker，并显示
            mBaiduMap.addOverlay(option);
            sb.append("\nradius:");
            sb.append(bdLocation.getRadius());
            if (bdLocation.getLocType() == BDLocation.TypeGpsLocation){//GPS定位结果
                sb.append("\nspeed:");
                sb.append(bdLocation.getSpeed());//单位：公里每小时
                sb.append("\nsatellite");
                sb.append(bdLocation.getSatelliteNumber());//卫星
                sb.append("\nheight:");
                sb.append(bdLocation.getAltitude());//单位：米
                sb.append("\ndirection:");
                sb.append(bdLocation.getDirection());//单位：度
                sb.append("\naddr:");
                sb.append(bdLocation.getAddrStr());
                sb.append("\ndescribe");
                sb.append("gps定位成功");
            }else if (bdLocation.getLocType() == BDLocation.TypeNetWorkLocation){//网络定位
                sb.append("\naddr:");
                sb.append(bdLocation.getAddrStr());
                //运行商信息
                sb.append("\noperationers:");
                sb.append(bdLocation.getOperators());
                sb.append("\ndescribe:");
                sb.append("网络定位成功");
            }else if (bdLocation.getLocType() == BDLocation.TypeOffLineLocation){//离线定位结果
                sb.append("\ndescribe:");
                sb.append("离线定位成功,离线定位结果也是有效的");
            }else if (bdLocation.getLocType() == BDLocation.TypeServerError){
                sb.append("\ndescribe:");
                sb.append("服务端网络定位失败,可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com,会有人追查原因");
            }else if (bdLocation.getLocType() == BDLocation.TypeNetWorkException){
                sb.append("\ndescribe:");
                sb.append("网络不通导致定位失败，请检查网络是否通畅");
            }else if (bdLocation.getLocType() == BDLocation.TypeCriteriaException){
                sb.append("\ndescribe:");
                sb.append("无法获取有效的定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成" +
                        "这种结果，可以试着关闭飞行模式");
            }
            sb.append("\nlocationdescribe:");
            sb.append(bdLocation.getLocationDescribe());//位置语义化信息
            List<Poi> list = bdLocation.getPoiList();//POI数据
            if (list != null){
                sb.append("\npoilist size:");
                sb.append(list.size());
                for (Poi p : list){
                    sb.append("\npoi=:");
                    sb.append(p.getId()+","+p.getName()+","+p.getRank());
                }
            }
            Log.d(TAG,sb.toString());
            point = null;
            option = null;
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy(),实现地图生命周期管理
        mMapView.onDestroy();
        //关闭定位
        mLocationClient.stop();
        bitmap.recycle();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //实现地图生命周期管理
        mMapView.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
