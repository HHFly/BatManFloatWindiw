package ns.com.batmanfloatwindow.widget.textcounter.formatters;



import java.text.DecimalFormat;

import ns.com.batmanfloatwindow.widget.textcounter.Formatter;

/**
 * Created by prem on 10/28/14.
 */
public class DecimalFormatter implements Formatter {

    private final DecimalFormat format = new DecimalFormat("#.0");

    @Override
    public String format(String prefix, String suffix, float value) {
        return prefix + format.format(value) + suffix;
    }
}
