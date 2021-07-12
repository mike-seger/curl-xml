package curl.xml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class App implements IOAware {
    public static void main(final String[] args) {
        new App().run(args);
    }

    public void run(String[] args) {
        final List<String> inputTransformations=new ArrayList<>();
        final List<String> outputTransformations=new ArrayList<>();
        final AtomicBoolean debug = new AtomicBoolean(false);
        final AtomicBoolean inputIsCsv = new AtomicBoolean(false);
        final AtomicBoolean isRemoteCall = new AtomicBoolean(false);
        final List<String> argList= Arrays.stream(args).filter(arg -> {
            arg = arg.trim();
            if(!isRemoteCall.get()) {
                isRemoteCall.set(arg.matches("https*://.*"));
            }
            if(arg.length()==0) {
                return false;
            } else if("-debug".equals(arg)) {
                debug.set(true);
                return false;
            } else if(arg.matches("-inTR=.*")) {
                inputTransformations.add(arg.substring(arg.indexOf('=')+1));
                return false;
            } else if(arg.matches("-outTR=.*")) {
                outputTransformations.add(arg.substring(arg.indexOf('=')+1));
                return false;
            } else if(arg.equals("-inCSV")) {
                inputIsCsv.set(true);
                return false;
            }
            return true;
        }).map(arg -> {
            arg = arg.trim();
            return arg;
        }).collect(Collectors.toList());

        String result = new CurlXml().call(
            argList, null,
            inputTransformations, outputTransformations,
                debug.get(), inputIsCsv.get(), isRemoteCall.get());
        System.out.println(result);
    }
}
