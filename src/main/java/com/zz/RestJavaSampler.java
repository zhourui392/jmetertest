package com.zz;

import com.zz.data.LoginResponseData;
import com.zz.util.HttpUtils;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhourui
 */
public class RestJavaSampler extends AbstractJavaSamplerClient {

    @Override
    public SampleResult runTest(JavaSamplerContext javaSamplerContext) {
        String url = "http://localhost:5555/api/auth/login";
        Map<String,Object> params = new HashMap<>(2);
        params.put("username","pad");
        params.put("password","123456");
        long start = System.currentTimeMillis();
        LoginResponseData data = HttpUtils.post(url,params, LoginResponseData.class);
        SampleResult results = SampleResult.createTestSample(start, System.currentTimeMillis());
        results.setSampleLabel("login request");
        if (data != null){
            results.setSuccessful(true);
        }
        //将数据打印到查看结果树当中
        results.setResponseData(data.toString(), null);
        results.setDataType(SampleResult.TEXT);

        return results;
    }

}
