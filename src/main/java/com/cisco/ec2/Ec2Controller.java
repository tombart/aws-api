package com.cisco.ec2;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Reservation;
import com.cisco.ec2.model.Ec2Instance;
import com.cisco.ec2.model.GetEc2Response;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Optional;

import static com.cisco.ec2.Ec2InstanceMapper.toEc2Instances;

@Validated
@RestController
public class Ec2Controller {

    private static final Logger LOGGER = LoggerFactory.getLogger(Ec2Controller.class);
    private final Ec2ClientProvider ec2ClientProvider;

    public Ec2Controller(Ec2ClientProvider ec2ClientProvider) {
        this.ec2ClientProvider = ec2ClientProvider;
    }

    @GetMapping(value = "aws-api/v1/regions/{region}/ec2", produces = "application/json")
    public GetEc2Response getEc2Instances(@PathVariable("region") @NotBlank @Size(max = 20) String region,
                                          @ApiParam(value = "max results per page (5-500)", defaultValue = "10")
                                          @RequestParam(value = "size", required = false) @Min(5) @Max(500) Integer size,
                                          @RequestParam(value = "nextToken", required = false) @Size(max = 1024) String nextToken,
                                          @ApiParam(value = "sort results by value", defaultValue = "instanceId",
                                                  allowableValues = "name,instanceId,state,type,availabilityZone,privateIp,publicIp")
                                          @RequestParam(value = "sort", required = false) @Size(max = 256) String sort) {

        LOGGER.info("Executing describe instances for region: {} request", region);

        AmazonEC2 ec2 = ec2ClientProvider.getClient(region);

        DescribeInstancesResult response = ec2.describeInstances(new DescribeInstancesRequest().
                withMaxResults(size)
                .withNextToken(nextToken));

        return GetEc2Response.builder()
                .instances(mapToEc2Instance(response.getReservations(), sort))
                .nextToken(response.getNextToken())
                .build();
    }

    private List<Ec2Instance> mapToEc2Instance(List<Reservation> reservations, String sort) {
        return Optional.ofNullable(sort)
                .map(sortBy -> toEc2Instances(reservations, sortBy))
                .orElse(toEc2Instances(reservations));
    }
}
