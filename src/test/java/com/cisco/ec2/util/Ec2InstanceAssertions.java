package com.cisco.ec2.util;

import com.amazonaws.services.ec2.model.Instance;
import com.cisco.ec2.model.Ec2Instance;

import static org.assertj.core.api.Assertions.assertThat;

public class Ec2InstanceAssertions {

    public static void assertEc2InstanceAttributes(Ec2Instance ec2Instance, Instance instance) {
        assertThat(ec2Instance.getName()).isEqualTo(instance.getTags().get(0).getValue());
        assertThat(ec2Instance.getPublicIp()).isEqualTo(instance.getPublicIpAddress());
        assertThat(ec2Instance.getPrivateIp()).isEqualTo(instance.getPrivateIpAddress());
        assertThat(ec2Instance.getType()).isEqualTo(instance.getInstanceType());
        assertThat(ec2Instance.getAvailabilityZone()).isEqualTo(instance.getPlacement().getAvailabilityZone());
        assertThat(ec2Instance.getInstanceId()).isEqualTo(instance.getInstanceId());
        assertThat(ec2Instance.getState()).isEqualTo(instance.getState().getName());
    }
}
