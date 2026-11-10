package com.pxt.harmonysignal;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** 只读：打开即在一屏里列出纯血鸿蒙判定要用的信号，供截图。不申请权限、不写任何东西。 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setTextSize(12);
        tv.setTextColor(Color.BLACK);
        tv.setBackgroundColor(Color.WHITE);
        tv.setTextIsSelectable(true);
        int pad = (int) (12 * getResources().getDisplayMetrics().density);
        tv.setPadding(pad, pad * 3, pad, pad);
        tv.setText(collect());
        ScrollView sv = new ScrollView(this);
        sv.setBackgroundColor(Color.WHITE);
        sv.addView(tv);
        setContentView(sv);
    }

    private String collect() {
        String flavor = prop("ro.build.flavor");
        String lowerFlavor = flavor.toLowerCase();
        boolean huaweiNew = "HUAWEI".equalsIgnoreCase(Build.MANUFACTURER) && Build.VERSION.SDK_INT >= 33;
        boolean containsAnco = lowerFlavor.contains("anco");
        boolean segmentAnco = Arrays.asList(lowerFlavor.split("[_-]")).contains("anco");
        List<String> ancoProps = ancoProps();

        StringBuilder sb = new StringBuilder();
        sb.append("PXT鸿蒙信号 v3（只读）\n\n");
        sb.append("【判定】\n");
        sb.append("现行规则（含anco 或 华为且SDK≥33）= ").append(verdict(containsAnco || huaweiNew)).append('\n');
        sb.append("收紧规则（整段anco 或 华为且SDK≥33）= ").append(verdict(segmentAnco || huaweiNew)).append('\n');
        sb.append("只看华为且SDK≥33 = ").append(verdict(huaweiNew)).append('\n');
        sb.append("flavor含anco=").append(containsAnco).append("  整段anco=").append(segmentAnco).append('\n');
        sb.append("全部属性里带anco的条数=").append(ancoProps.size()).append('\n');

        sb.append("\n【Build】\n");
        line(sb, "MANUFACTURER", Build.MANUFACTURER);
        line(sb, "BRAND", Build.BRAND);
        line(sb, "MODEL", Build.MODEL);
        line(sb, "DEVICE", Build.DEVICE);
        line(sb, "PRODUCT", Build.PRODUCT);
        line(sb, "RELEASE", Build.VERSION.RELEASE);
        line(sb, "SDK_INT", String.valueOf(Build.VERSION.SDK_INT));
        line(sb, "FINGERPRINT", Build.FINGERPRINT);
        line(sb, "DISPLAY", Build.DISPLAY);

        sb.append("\n【系统属性】\n");
        for (String k : new String[]{"ro.build.flavor", "ro.huawei.build.display.id", "ro.build.version.emui",
            "ro.build.hw_emui_api_level", "hw_sc.build.platform.version", "ro.build.version.magic",
            "persist.sys.droi.tong", "ro.product.cpu.abilist", "persist.sys.anco.api.version",
            "ro.product.os.dist.anco.apiversion", "hw_sc.anco.enable", "ro.ark.anco.version"}) {
            line(sb, k, prop(k));
        }

        sb.append("\n【全部带anco的属性（").append(ancoProps.size()).append("条）】\n");
        for (String p : ancoProps) sb.append("  ").append(p).append('\n');
        return sb.toString();
    }

    private static String verdict(boolean harmony) {
        return harmony ? "纯血鸿蒙容器" : "普通设备";
    }

    private static void line(StringBuilder sb, String k, String v) {
        sb.append(k).append(" = ").append(v == null || v.isEmpty() ? "（空）" : v).append('\n');
    }

    private static String prop(String key) {
        try {
            Object v = Class.forName("android.os.SystemProperties").getMethod("get", String.class).invoke(null, key);
            return v == null ? "" : String.valueOf(v);
        } catch (Exception e) {
            return "ERR " + e.getClass().getSimpleName();
        }
    }

    private static List<String> ancoProps() {
        List<String> hits = new ArrayList<>();
        try {
            Process p = Runtime.getRuntime().exec("getprop");
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            for (String line; (line = r.readLine()) != null; ) {
                if (line.toLowerCase().contains("anco")) hits.add(line);
            }
            r.close();
        } catch (Exception e) {
            hits.add("getprop 失败：" + e);
        }
        return hits;
    }
}
