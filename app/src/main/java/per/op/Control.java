package per.op;

/**
 * Created by AlphaGo on 2017/12/23.
 */

public class Control {
    private int future;      //时间
    private String action;  //动作
    private String unit;   //时间单位
    /*与服务端的通信协议*/
    public Control(int future, String action, String unit) {
        this.future = future;
        this.action = action;
        this.unit = unit;
    }

    public String paraseAction(){
        int converFuture;
        String finalAction;
        if(action.equals("关机"))         //关机
            finalAction="s,";
        else if(action.equals("重启"))    //重启
            finalAction="r,";
        else if(action.equals("注销"))          //注销
            finalAction="l,";
        else finalAction="";          //别的动作，获取进程


        switch (unit){       //把时间单位统一转换成秒数
            case "秒":
                finalAction+=String.valueOf(future);
                break;
            case "分钟":
                converFuture=future*60;
                finalAction+=String.valueOf(converFuture);
                break;
            case "小时":
                converFuture=future*60*60;
                finalAction+=String.valueOf(converFuture);
                break;
            case "立即":
                finalAction+=String.valueOf(0);
                break;
            default:
                finalAction+="getProcess";
                break;

        }

        return finalAction;    //最终的对象

    }
}
