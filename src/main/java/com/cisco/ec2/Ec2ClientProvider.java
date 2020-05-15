package com.cisco.ec2;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import org.springframework.stereotype.Component;

@Component
public class Ec2ClientProvider {

    public AmazonEC2 getClient(String region) {
        return AmazonEC2ClientBuilder.standard()
                .withRegion(region)
                .build();
    }
}
