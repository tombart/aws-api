package com.cisco.ec2.util;

import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;

public class Ec2ModelGenerator {

    public static DescribeInstancesResult generateInstancesResult(Instance... instances) {
        return new DescribeInstancesResult()
                .withReservations(new Reservation().withInstances(instances));
    }
}
