package com.cisco.ec2.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class Ec2Instance {
    private String name;
    private String instanceId;
    private String state;
    private String type;
    private String availabilityZone;
    private String privateIp;
    private String publicIp;
}

